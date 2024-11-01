package com.example.software2.dapp.LoginSignup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.software2.dapp.AccidentDetect.GPSHandler;
import com.example.software2.dapp.AccidentDetect.HospitalAssigned;
import com.example.software2.dapp.BaseActivity;
import com.example.software2.dapp.R;
import com.example.software2.dapp.AmbulanceViewAccident.Accidents;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class HospitalAuthority extends BaseActivity {
    TextView locationdetails;
    EditText editTextEmail, editTextPassword, Hospitaladdress;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private GPSHandler mGPSHandler;
    static double latitude, longitude;
    private static String location;
    EditText hospitaladdress;
    private int mInterval = 3000; // 5 seconds by default, can be changed later
    private Handler mHandler;
    private Toast toast;
    private TextView toast_text;
    private Typeface toast_font;
    private LayoutInflater inflater;
    Runnable mStatusChecker;
    private View layout;

    public Accidents.GPSTracker tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_authority);
        locationdetails = findViewById(R.id.locationdetails);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        hospitaladdress = findViewById(R.id.hospital);
        firebaseAuth = FirebaseAuth.getInstance();

        tracker = new Accidents.GPSTracker(this);
        if (!tracker.canGetLocation()) {
            tracker.showSettingsAlert();
        } else {
            latitude = tracker.getLatitude();
            longitude = tracker.getLongitude();
        }

        hospitaladdress = findViewById(R.id.hospital);
        mGPSHandler = new GPSHandler(this);
        toast_font = Typeface.createFromAsset(getAssets(), "AvenirNextLTPro-Cn.otf");
        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layout = inflater.inflate(R.layout.custom_toast, (ViewGroup) this.findViewById(R.id.toast));
        toast_text = (TextView) layout.findViewById(R.id.tv);
        toast = new Toast(this.getApplicationContext());
        toast_text.setTypeface(toast_font);
        toast.setGravity(Gravity.BOTTOM, 0, 100);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        editTextEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editTextEmail.getText().toString().contains("@")) {
                    toast_text.setText("username should not contain @ ");
                    editTextEmail.setText("");
                }
            }
        });

        mHandler = new Handler();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        mStatusChecker = new Runnable() {
            @Override
            public void run() {
                try {
                    if (mGPSHandler.getCurrentAddress() != null) {
                        latitude = mGPSHandler.getLatitude();
                        longitude = mGPSHandler.getLongitude();
                        location = mGPSHandler.getCurrentAddress().replaceAll(",", "");
                        locationdetails.setText("latitude= " + latitude + " longitude =" + longitude);
                        Toast.makeText(getApplicationContext(), "latitude=" + latitude + " longitude" + longitude, Toast.LENGTH_SHORT).show();
                    }
                } finally {
                    mHandler.postDelayed(mStatusChecker, mInterval);
                }
            }
        };


    }


    public void register(View view) {
        if (editTextEmail.getText() != null && editTextPassword.getText() != null) {
            mHandler.removeCallbacks(mStatusChecker);
            final String email = editTextEmail.getText().toString().trim() + "@hospital.com";
            final String password = editTextPassword.getText().toString().trim();
            final String hospital = hospitaladdress.getText().toString().trim();


            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                toast_text.setText("Invalid username,Try again");
                toast.show();
                return;
            }

            if (password.length() < 8) {
                toast_text.setText("Password must be of 8 characters");
                toast.show();
                return;
            }

            if (TextUtils.isEmpty(email)) {
                toast_text.setText("No Username Entered");
                toast.show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                toast_text.setText("No Password Entered");
                toast.show();
                return;
            }
            if (TextUtils.isEmpty(hospital)) {
                toast_text.setText("No hospital address Entered");
                toast.show();
                return;
            }
            showDialog();

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Login with details

                                firebaseAuth.signInWithEmailAndPassword(email, password);
                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                Map<String, Object> values = new HashMap<String, Object>();
                                values.put("hospitalAddress", hospital);
                                values.put("latitude", latitude);
                                values.put("longitude", longitude);
                                //   FirebaseUser user = firebaseAuth.getCurrentUser();

                                databaseReference.child("hospital").child("hospital " + user.getUid()).setValue(values);
                                dismissDialog();
                                toast_text.setText("Welcome!!");
                                toast.show();
                                startActivity(new Intent(HospitalAuthority.this, HospitalAssigned.class).putExtra("hospital", hospital));
                                finish();
                            } else {
                                dismissDialog();
                                toast_text.setText("check your internet connection or try another username");
                                toast.show();
                            }
                        }
                    });
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        mStatusChecker.run();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mStatusChecker.run();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mStatusChecker);
    }
}