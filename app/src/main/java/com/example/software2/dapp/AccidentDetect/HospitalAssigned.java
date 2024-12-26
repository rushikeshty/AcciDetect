package com.example.software2.dapp.AccidentDetect;

import androidx.annotation.NonNull;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.software2.dapp.AccidentDetect.Hosptialauthrity.AccidentList;
import com.example.software2.dapp.AccidentDetect.viewmodel.AccidentListStatusViewmodel;
import com.example.software2.dapp.AmbulanceViewAccident.Accidents;
import com.example.software2.dapp.BaseActivity;
import com.example.software2.dapp.LoginSignup.LoginScreenActivity;
import com.example.software2.dapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HospitalAssigned extends BaseActivity {
    public GPSHandler mGPSHandler;
    static int finalCount;
    Accidents.GPSTracker tracker;
    Button button, logout, update;
    int count = 0;
    // 6 seconds by default, can be changed later
    private Handler mHandler;
    String hospital = "";
    static double latitude, longitude = 0;
    EditText lat, lon, address;
    private AccidentListStatusViewmodel viewmodel;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_assigned);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Hospital Authority");
        }
        viewmodel = new AccidentListStatusViewmodel();
        viewmodel.init();
        initUI();
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    String token = task.getResult();
                    Log.d("FCM", "Token: " + token);
                    if (user != null && !Objects.requireNonNull(user.getEmail()).contains("user")) {
                        viewmodel.getDatabaseReference().child("hospital").child(user.getUid()).child("token").setValue(token);
                    } else {
                        viewmodel.getDatabaseReference().child("user").child(viewmodel.getCurrentUser().getUid()).child("UserToken").setValue(token);
                    }

                });
        user = viewmodel.getCurrentUser();

        mHandler = new Handler();
        hospital = getIntent().getStringExtra("hospital");
        tracker = new Accidents.GPSTracker(this);
        if (tracker.canGetLocation()) {
            tracker.showSettingsAlert();
        }
        attachListeners();
        mHandler = new Handler();
        mGPSHandler = new GPSHandler(this);
        startRepeatingTask();
        showDialog();
        checkStatus();
    }

    public void initUI() {
        button = findViewById(R.id.viewaccident);
        logout = findViewById(R.id.logout);
        lat = findViewById(R.id.latitude);
        lon = findViewById(R.id.lontitude);
        address = findViewById(R.id.address);
        update = findViewById(R.id.updatebtn);
    }

    public void attachListeners() {
        update.setOnClickListener(view -> {
            if (user != null) {
                Map<String, Object> values = new HashMap<>();
                values.put("hospitalAddress", address.getText().toString());
                values.put("latitude", latitude);
                values.put("longitude", longitude);
                viewmodel.getDatabaseReference().child("hospital").child("hospital " + user.getUid()).setValue(values);
                count = 0;
                Toast.makeText(getApplicationContext(), "Address Updated succesfully.", Toast.LENGTH_SHORT).show();
            }
        });
        logout.setOnClickListener(view -> new androidx.appcompat.app.AlertDialog.Builder(HospitalAssigned.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Closing Activity")
                .setMessage("Are you sure you want to close this activity?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (user != null) {
                        FirebaseAuth.getInstance().signOut();
                        finish();
                        startActivity(new Intent(getApplicationContext(), LoginScreenActivity.class));
                    }
                })
                .setNegativeButton("No", null)
                .show());
        button.setOnClickListener(view -> {
            showDialog();
            Intent i = new Intent(HospitalAssigned.this, AccidentList.class);
            startActivity(i);
            dismissDialog();
        });
        address.setOnClickListener(view -> count = 1);
    }

    public void checkStatus() {
        latitude = mGPSHandler.getLatitude();
        longitude = mGPSHandler.getLongitude();
        viewmodel.getDatabaseReference().child("hospital").child("hospital " + user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String address = Objects.requireNonNull(snapshot.child("hospitalAddress").getValue()).toString();
                Map<String, Object> values = new HashMap<>();
                values.put("hospitalAddress", address);
                values.put("latitude", latitude);
                values.put("longitude", longitude);
                viewmodel.getDatabaseReference().child("hospital").child("hospital " + user.getUid()).setValue(values);
                lat.setText("latitude " + latitude);
                lon.setText("longitude " + longitude);
                HospitalAssigned.this.address.setText("Your address " + address);
                dismissDialog();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dismissDialog();
            }
        });


    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                if (count == 0) {
                    checkStatus();
                }
                viewmodel.getDatabaseReference().child("Accidents").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            if (Objects.requireNonNull(snapshot1.child("Detected").getValue()).toString().contains("true")) {
                                if (finalCount < 1) {
                                    notificationUtils.sendOnChannel2("sendtohospital");
                                    finalCount++;
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            } finally {
                int mInterval = 6000;
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
        finalCount = 0;
    }

}
