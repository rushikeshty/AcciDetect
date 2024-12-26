package com.example.software2.dapp;

import static com.example.software2.dapp.AccidentDetect.SensorService.countDownTimer;
import static com.example.software2.dapp.AccidentDetect.SensorService.mNotificationManager;
import static com.example.software2.dapp.UserActivities.ui.home.HomeFragment.powerDb;
import static com.example.software2.dapp.UserActivities.ui.home.HomeFragment.stop;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;

import com.example.software2.dapp.AccidentDetect.GPSHandler;
import com.example.software2.dapp.AccidentDetect.SensorService;
import com.example.software2.dapp.UserActivities.MainActivity;
import com.example.software2.dapp.EmergencyContact.DBEmergency;
import com.example.software2.dapp.EmergencyContact.EmerContact;
import com.example.software2.dapp.LoginSignup.LoginScreenActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class SendSMSActivity extends BaseActivity {

    private GPSHandler mGPSHandler;
    private SmsManager mSmsManager;
    private DBEmergency mDatabase;
    public static CountDownTimer timer;
    public static MediaPlayer mediaPlayerAlarm;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String username = "";
    private DatabaseReference databaseReference;

    private TextView textViewTimer;
    private TextView textViewEmergencyMessage;

    private String[] phoneNumberArray;
    private final ArrayList<EmerContact> add = new ArrayList<>();

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_sms);

        CustomToastActivity.CustomToast(this);

        Intent i = getIntent();
        String acc = i.getStringExtra("accident");
        if (acc != null) {
            if (acc.equals("accident")) {
                setVisible(false);
                final Dialog dialog = new Dialog(SendSMSActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.activity_dialog_message);
                dialog.show();
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);

                lp.copyFrom(Objects.requireNonNull(dialog.getWindow()).getAttributes());
                dialog.getWindow().setAttributes(lp);
                final VideoView videoview = dialog.findViewById(R.id.videoview);
                Uri uri = Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.accident);
                videoview.setVideoURI(uri);
                videoview.start();

                videoview.setOnCompletionListener(mediaPlayer -> runOnUiThread(() -> {
                    setVisible(true);
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), "completed", Toast.LENGTH_SHORT).show();
                }));

            }
        }

        mGPSHandler = new GPSHandler(this);
        mDatabase = new DBEmergency(this);
        mSmsManager = SmsManager.getDefault();
        mediaPlayerAlarm = MediaPlayer.create(this, R.raw.rising_swoops);

        setupFirebase();
        setupUI();
        setupTimer();

    }

    public void cancelAlarm(View view) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            databaseReference.child("Accidents").child(user.getUid()).child("Detected").setValue("false");
        }
        mNotificationManager.cancelAll();
        CustomToastActivity.showCustomToast("Alarm is cancelled");
        mediaPlayerAlarm.pause();
        timer.cancel();
        countDownTimer.cancel();
        stop();
        powerDb = 0;
        finish();
        Intent i = new Intent(SendSMSActivity.this, SensorService.class);
        stopService(i);

        startActivity(new Intent(this, MainActivity.class));
    }

    public void sendButtonPress(View view) {
        sendSMSMessage();
    }

    public void finish() {
        super.finish();
    }

    private void setupUI() {
        String email = firebaseUser.getEmail();
        List<EmerContact> contact;

        textViewTimer = findViewById(R.id.timer);
        textViewEmergencyMessage = findViewById(R.id.emergencymessage);
        TextView textViewPhoneNumber = findViewById(R.id.phone1);
        TextView textViewName = findViewById(R.id.name1);

        contact = mDatabase.getContact(email);
        add.addAll(contact);

        phoneNumberArray = new String[3];
        String[] nameArray = new String[3];
        StringBuilder names = new StringBuilder();
        StringBuilder phoneNumbers = new StringBuilder();
        for (int i = 0; i < add.size(); i++) {
            EmerContact emerContact = add.get(i);
            nameArray[i] = emerContact._name;
            phoneNumberArray[i] = emerContact._phone;
            names.append(nameArray[i]).append('\n');
            phoneNumbers.append(phoneNumberArray[i]).append('\n');
        }

        textViewName.setText(names.toString());
        textViewPhoneNumber.setText(phoneNumbers.toString());

        final Animation animShake = AnimationUtils.loadAnimation(this, R.anim.shake);
        ImageView alarm = findViewById(R.id.imageView2);
        ImageView imageView10 = findViewById(R.id.imageView10);
        alarm.startAnimation(animShake);
        imageView10.startAnimation(animShake);
    }

    private void setupFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        databaseReference = FirebaseDatabase.getInstance().getReference();

        if (firebaseUser == null) {
            finish();
            startActivity(new Intent(this, LoginScreenActivity.class));
        }
        getUsername();
    }

    private void getUsername() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        showDialog();
        databaseReference.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> values = new ArrayList<>(4);
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    values.add(Objects.requireNonNull(child.getValue()).toString());
                }

                if (!values.isEmpty()) {
                    username = values.get(0) + " " + values.get(1);
                }

                dismissDialog();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SendSMSActivity.this, "Could not retrieve data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupTimer() {
        timer = new CountDownTimer(13000, 1000) {

            @SuppressLint("SetTextI18n")
            public void onTick(long millisUntilFinished) {
                textViewTimer.setText("seconds remaining: " + millisUntilFinished / 1000);
                //here you can have your logic to set text to edittext
                mediaPlayerAlarm.start();
                // send.setEnabled(true);
            }

            @SuppressLint("SetTextI18n")
            public void onFinish() {
                //You can comment out below method if you want to send the message to emergency contacct
                // sendSMSMessage();
                textViewEmergencyMessage.setText("");
                textViewTimer.setText("Message sent");
                Toast.makeText(getApplicationContext(), "message sent", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(() -> startActivity(new Intent(getApplicationContext(), MainActivity.class)), 2000);
            }
        };

        timer.start();
    }

    /* Function to send a alert text message */
    private void sendSMSMessage() {
        // Stop the media and alarm
        mediaPlayerAlarm.pause();
        countDownTimer.cancel();
        timer.cancel();

        // Need to divide the message for the SMS handler to handle a long message
        String message = constructMessage();
        textViewEmergencyMessage.setText(message);
        ArrayList<String> dividedMessage = mSmsManager.divideMessage(message);

        // Need to send message to all emergency contacts
        for (int i = 0; i < add.size(); i++) {
            try {
                mSmsManager.sendMultipartTextMessage(phoneNumberArray[i].replaceAll("[-() ]", "") // Need to remove special characters
                        , null, dividedMessage, null, null);
            } catch (Exception e) {
                Log.d("Error:-", Objects.requireNonNull(e.getMessage()));
            }
        }

    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            doubleBackToExitPressedOnce = false;
            startActivity(new Intent(SendSMSActivity.this, MainActivity.class));
        }, 2000);
    }


    /* Function to put together the text message */
    private String constructMessage() {
        String location = mGPSHandler.getCurrentAddress();
        StringBuilder message = new StringBuilder("Alert! It appears  that the " + username
                + " may have been in a car accident. " + username
                + " has chosen you as their emergency contact. " + username
                + "'s current location is " + location
                + " .  ");

        final TextView textMessage = findViewById(R.id.textmessage);
        textMessage.setText(message.toString());
        Toast.makeText(getApplicationContext(), message.toString(), Toast.LENGTH_SHORT).show();
        return message.toString();
    }

}
