package com.example.software2.dapp.UserActivities.ui.home;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.software2.dapp.AccidentDetect.ServiceHandler;
import com.example.software2.dapp.CustomToastActivity;
import com.example.software2.dapp.EmergencyContact.DBEmergency;
import com.example.software2.dapp.EmergencyContact.EmerContact;
import com.example.software2.dapp.LoginSignup.LoginScreenActivity;
import com.example.software2.dapp.PermissionHandler;
import com.example.software2.dapp.R;
import com.example.software2.dapp.databinding.FragmentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import pl.droidsonroids.gif.GifImageView;

public class HomeFragment extends Fragment {
    private final int MY_PERMISSION_REQUEST_CODE = 1;
    private PermissionHandler mPermissionHandler;
    GifImageView gifImageView;
    private FirebaseAuth firebaseAuth;
    private TextView accidentstatus;
    private FirebaseUser firebaseUser;
    private Handler mHandler;
     TextView sensortext, decibaltext;
    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;
    double soundThreshold=25;//
     static int amp;
     TextView boolen;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch aSwitch;
    private ServiceHandler mServiceHandler;
    public static double powerDb;
    public static boolean isTracking;
    private Button buttonToggleTracking;
    private TextToSpeech textToSpeech;
    private DBEmergency mDatabase;
    public static TextView sensorread, decibals;
    private Typeface custom_font;
    RelativeLayout layout;
    Button hospitalassigned;

    private FragmentHomeBinding binding;
    static String assignedhospital;
    int count = 0;

    private static final int SAMPLE_RATE = 44100;
    private final int MY_PERMISSIONS_RECORD_AUDIO = 1;
     private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);

    private AudioRecord audioRecord;
    public static boolean isRecording = false;
    Button btn_record;
    private double currentDecibel = 0;
    private boolean isShown =false;


    @RequiresApi(api = Build.VERSION_CODES.O)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
// Check if the storage permission has been granted


        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        mHandler = new Handler();
        layout = root.findViewById(R.id.relativeLayout);
        boolen = root.findViewById(R.id.bollean);
        hospitalassigned = (Button) root.findViewById(R.id.hospitalassigned);
        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Fetching data...");
        custom_font = Typeface.createFromAsset(requireContext().getAssets(), "AvenirNextLTPro-MediumCn.otf");
        @SuppressLint("ResourceType") AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(),
                R.anim.homefragmentanimation);
        set.setTarget(layout);
        set.start();
        mPermissionHandler = new PermissionHandler(requireActivity());
        firebaseAuth = FirebaseAuth.getInstance();
        mServiceHandler = new ServiceHandler(getContext());
        if (ServiceHandler.isBound()) {
            isTracking = true;

        } else {
            isTracking = false;
        }

        buttonToggleTracking = root.findViewById(R.id.buttonToggleTracking);
        buttonToggleTracking.setVisibility(View.GONE);
        sensorread = root.findViewById(R.id.sensorread);
        sensortext = root.findViewById(R.id.sensortext);
        decibaltext = root.findViewById(R.id.decibaltext);
        gifImageView = root.findViewById(R.id.gifimage);
        gifImageView.setVisibility(View.GONE);
        decibals = root.findViewById(R.id.decibal);
        sensortext.setVisibility(View.INVISIBLE);
        decibaltext.setVisibility(View.INVISIBLE);
        sensorread.setVisibility(View.INVISIBLE);
        decibals.setVisibility(View.INVISIBLE);

        aSwitch = root.findViewById(R.id.switch1);
        accidentstatus = root.findViewById(R.id.accidentstatus);
        @SuppressLint("ResourceType") AnimatorSet set1 = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(),
                R.anim.property_animator);
        set1.setTarget(accidentstatus);
        set1.start();
        accidentstatus.setVisibility(View.INVISIBLE);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    sensortext.setVisibility(View.VISIBLE);
                    decibaltext.setVisibility(View.VISIBLE);
                    sensorread.setVisibility(View.VISIBLE);
                    decibals.setVisibility(View.VISIBLE);
                } else {
                    sensortext.setVisibility(View.INVISIBLE);
                    decibaltext.setVisibility(View.INVISIBLE);
                    sensorread.setVisibility(View.INVISIBLE);
                    decibals.setVisibility(View.INVISIBLE);
                }
            }
        });
        CustomToastActivity.CustomToastActivity(requireActivity());
        mDatabase = new DBEmergency(requireContext());
        FirebaseUser user = firebaseAuth.getCurrentUser();
        setupFirebase();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        hospitalassigned.setOnClickListener(view -> {
            progressDialog.show();
            isShown = false;
            if (user != null) {
                databaseReference.child("user").child(user.getUid()).child("AccidentInfo").child("hospitalassigned").addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("UseCompatLoadingForDrawables")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progressDialog.dismiss();
                        assignedhospital = snapshot.getValue().toString();
                        if (!assignedhospital.isEmpty() && !isShown) {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(requireContext());
                            builder1.setMessage("Assigned Hospital :- " + assignedhospital);
                            builder1.setIcon(requireActivity().getDrawable(R.drawable.bell));
                            builder1.setCancelable(true);
                            builder1.show();
                            isShown = true;
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

            }
        });

        aSwitch = root.findViewById(R.id.switch1);
//
        status.run();

        if (getArguments() != null) {
            String acciden = getArguments().toString();
            if (acciden.equals("accident")) {
                gifImageView.setVisibility(View.GONE);
                requireActivity().setVisible(false);
                final Dialog dialog = new Dialog(requireContext());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.activity_dialog_message);
                dialog.show();
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);

                lp.copyFrom(dialog.getWindow().getAttributes());
                dialog.getWindow().setAttributes(lp);
                final VideoView videoview = dialog.findViewById(R.id.videoview);
                Uri uri = Uri.parse("android.resource://" + requireContext().getPackageName() + "/" + R.raw.accident);
                videoview.setVideoURI(uri);
                videoview.start();

                videoview.setOnCompletionListener(mediaPlayer -> {
                    // alertDialog.cancel();
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            requireActivity().setVisible(true);
                            dialog.dismiss();
                            Toast.makeText(requireContext(), "completed", Toast.LENGTH_SHORT).show();
                        }
                    });

                });

            }
        }

        accidentstatus.setOnClickListener(view -> {
            AlertDialog.Builder dialog = new AlertDialog.Builder(requireContext());
            dialog.setIcon(android.R.drawable.ic_dialog_alert);
            dialog.setMessage("Are you sure want to exit from Accident?");
            dialog.setPositiveButton("Yes", (dialog1, which) -> {
                buttonToggleTracking.setVisibility(View.VISIBLE);
                hospitalassigned.setVisibility(View.INVISIBLE);
                accidentstatus.setVisibility(View.INVISIBLE);
                if (user != null) {
                    databaseReference.child("Accidents").child(user.getUid()).child("Detected").setValue("false");
                }
            });
            dialog.setNegativeButton("cancel",((dialog1, which) -> {
                dialog1.dismiss();
            }));
            dialog.show();
        });

        buttonToggleTracking.setOnClickListener(view -> {
            if (buttonToggleTracking.getText().toString().contains("start")) {
                gifImageView.setVisibility(View.INVISIBLE);

            }
            if (isTracking) {
                toggleTracking();
            } else {
                // Ask for Permissions
                // Add permissions to the permissionName List
                List<String> permissionName = new ArrayList<>();
                List<String> permissionTag = new ArrayList<>();
                permissionName.add(Manifest.permission.ACCESS_FINE_LOCATION);
                permissionName.add(Manifest.permission.READ_PHONE_STATE);
                permissionName.add(Manifest.permission.SEND_SMS);
                permissionName.add(Manifest.permission.RECORD_AUDIO);
                permissionTag.add("Access Location");
                permissionTag.add("Read Phone State");
                permissionTag.add("Send SMS");
                permissionTag.add("Record audio");
                if (!mPermissionHandler.requestPermissions(MY_PERMISSION_REQUEST_CODE, permissionName, permissionTag)
                        || !locationServicesStatusCheck() || !hasContact())
                    return;
                gifImageView.setVisibility(View.VISIBLE);

                toggleTracking();
            }
        });
        //prepareListDataSignin();
        custom_font = Typeface.createFromAsset(requireContext().getAssets(), "AvenirNextLTPro-MediumCn.otf");
        // Listview Group expanded listener
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHandler.removeCallbacks(status);
        binding = null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint({"ObjectAnimatorBinding", "Recycle"})
    private void toggleTracking() {
        //Intent intent = new Intent(DashboardActivity.this, TrackingActivity.class);
        //startActivity(intent);

        if (isTracking) {
            mServiceHandler.doUnbindService();
            buttonToggleTracking.setText("Start Tracking");
            decibals.setText("0");
            sensorread.setText("0");
            gifImageView.setVisibility(View.INVISIBLE);
            isTracking = false;
            stop();

        } else {
            mServiceHandler.doBindService();

            start();
            @SuppressLint("ResourceType")
            AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(),
                    R.anim.property_animator);
            set.setTarget(aSwitch);
            set.start();
            buttonToggleTracking.setText("Stop Tracking");
            isTracking = true;
            gifImageView.setVisibility(View.VISIBLE);
            boolen.setText(isTracking + "  service is bound " + ServiceHandler.isBound());

        }
    }

    private void setupFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            requireActivity().finish();
            startActivity(new Intent(requireContext(), LoginScreenActivity.class));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (mPermissionHandler.handleRequestResult(requestCode, permissions, grantResults)) {
                    toggleTracking();

                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean locationServicesStatusCheck() {
        final LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) return true;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Enable GPS")
                .setMessage("This function needs your GPS, do you want to enable it now?")
                .setIcon(R.drawable.ic_launcher_background)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();


        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onResume() {
        super.onResume();
        count =0 ;
        boolen.setText(isTracking + "  service is bound " + ServiceHandler.isBound());

        if (ServiceHandler.isBound()) {
            //stopmedia();
            try {
              start();

            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            mServiceHandler.doBindService();


            @SuppressLint("ResourceType")
            AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(),
                    R.anim.property_animator);
            set.setTarget(aSwitch);
            set.start();
            buttonToggleTracking.setText("Stop Tracking");
            isTracking = true;
            Toast.makeText(getContext(), String.valueOf(isTracking), Toast.LENGTH_SHORT).show();
            gifImageView.setVisibility(View.VISIBLE);
            boolen.setText(isTracking + "  service is bound " + ServiceHandler.isBound());
        }

    }

    @Override
    public void onStart() {
        super.onStart();

    }


    private boolean hasContact() {
        String email = firebaseUser.getEmail();
        List<EmerContact> contact = mDatabase.getContact(email);
        if (contact.isEmpty()) {
            CustomToastActivity.showCustomToast("Please add at least 1 Emergency Contact. ");
            return false;
        } else {
            return true;
        }
    }

    public void start() {
        if (isRecording) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{android.Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_RECORD_AUDIO);
            Toast.makeText(getContext(), "granted", Toast.LENGTH_SHORT).show();
            return;
        }

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE);

        audioRecord.startRecording();
        isRecording = true;

        Thread thread = new Thread(() -> {
            short[] buffer = new short[BUFFER_SIZE];

            while (isRecording) {
                int readSize = audioRecord.read(buffer, 0, buffer.length);
                if (readSize < 0) {
                    break;
                }

                double sum = 0;
                for (int i = 0; i < readSize; i++) {
                    sum += buffer[i] * buffer[i];
                }
                double rms = Math.sqrt(sum / readSize);
                currentDecibel = 20 * Math.log10(rms / 1700.0);

                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        requireActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                @SuppressLint("DefaultLocale")
                                String formattedNum = String.format("%.3f", currentDecibel);
                                decibals.setText(formattedNum);
                                if(currentDecibel>soundThreshold){
                                    //set the powerdb value then see the sensor class if powerdb has some value then trigger alarm
                                    powerDb = Double.parseDouble(formattedNum);
                                }
                            }
                        });



                    }
                });

            }

            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
            isRecording = false;
            currentDecibel = 0;
        });

        thread.start();
    }

    public static void stop() {
        isRecording = false;
    }

    //    @RequiresApi(api = Build.VERSION_CODES.P)

    Runnable status = new Runnable() {
        @Override
        public void run() {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                databaseReference.child("Accidents").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.child("Detected").getValue() != null && snapshot.child("Detected").getValue().toString().contains("true")) {
                            if (count == 0) {
                                accidentstatus.setText("You are currently in accident status. Tap to stop");
                                buttonToggleTracking.setVisibility(View.INVISIBLE);
                                count = 1;
                            }
                            accidentstatus.setVisibility(View.VISIBLE);
                            hospitalassigned.setVisibility(View.VISIBLE);
                        } else {
                            buttonToggleTracking.setVisibility(View.VISIBLE);
                            hospitalassigned.setVisibility(View.INVISIBLE);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
            mHandler.postDelayed(status, 600);
        }
    };

}

