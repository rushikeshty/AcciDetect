package com.example.software2.dapp.AccidentDetect;

import static com.example.software2.dapp.DistanceCalculator.calculateMinimumDistance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.software2.dapp.AccidentDetect.Hosptialauthrity.AccidentList;
import com.example.software2.dapp.Coordinate;
import com.example.software2.dapp.CustomToastActivity;
import com.example.software2.dapp.DistanceCalculator;
import com.example.software2.dapp.LoginSignup.LoginScreenActivity;
import com.example.software2.dapp.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HospitalAssigned extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    public  GPSHandler mGPSHandler;
    String yes=null;
    ArrayList<String> keys = new ArrayList<>();
    public static List<Coordinate> set2;
    static String address="";
    ProgressDialog progressDialog;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public static String CHANNEL_1_ID = "channel1";
    public static String CHANNEL_2_ID = "channel2";
    public static NotificationManager mNotificationManager;
    static int finalcount;
     GPSTracker tracker;
 //  ArrayList<String> latitudes = new ArrayList<>();
 private int mInterval = 6000;
 Button button,logout,update;
    int count=0;
    // 6 seconds by default, can be changed later
    private Handler mHandler;
    String hospital="";

    static double latitude, longitude= 0;
 EditText lat,lon,adress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_assigned);
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
         createNotificationChannels();
        hospital = getIntent().getStringExtra("hospital");
        tracker = new GPSTracker(this);
        button = findViewById(R.id.viewaccident);
        logout = findViewById(R.id.logout);
        lat = findViewById(R.id.latitude);
        lon = findViewById(R.id.lontitude);
        adress = findViewById(R.id.address);

        if(!tracker.canGetLocation()){
            tracker.showSettingsAlert();
        }
        Toast.makeText(getApplicationContext(), "Swipe down to refresh", Toast.LENGTH_SHORT).show();


        update = findViewById(R.id.updatebtn);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user!=null){
                Map<String, Object> values = new HashMap<String, Object>();
                values.put("hospitalAddress",adress.getText().toString());
                values.put("latitude",latitude);
                values.put("longitude",longitude);
                databaseReference.child("hospital").child("hospital "+user.getUid()).setValue(values);
                count=0;
                    Toast.makeText(getApplicationContext(), "Address Updated succesfully.", Toast.LENGTH_SHORT).show();
            }}
        });
         mHandler = new Handler();
        mGPSHandler = new GPSHandler(this);
        latitude = mGPSHandler.getLatitude();
        longitude = mGPSHandler.getLongitude();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("fetching data...");
        progressDialog.show();

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new androidx.appcompat.app.AlertDialog.Builder(HospitalAssigned.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Closing Activity")
                        .setMessage("Are you sure you want to close this activity?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (firebaseAuth.getCurrentUser() != null) {
                                    FirebaseAuth.getInstance().signOut();
                                    finish();
                                    startActivity(new Intent(getApplicationContext(), LoginScreenActivity.class));
                                }
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();

            }
        });

        startRepeatingTask();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                Intent i = new Intent(HospitalAssigned.this, AccidentList.class);
                startActivity(i);
                progressDialog.dismiss();
            }
        });
        Checkstatus();
        adress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                count=1;
                //user can edit the address
            }
        });

    }

    public void Checkstatus(){
        progressDialog.dismiss();
        latitude = mGPSHandler.getLatitude();
        longitude = mGPSHandler.getLongitude();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        databaseReference.child("hospital").child("hospital "+ user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String address = snapshot.child("hospitalAddress").getValue().toString();
                Map<String, Object> values = new HashMap<String, Object>();
                values.put("hospitalAddress",address);
                values.put("latitude",latitude);
                values.put("longitude",longitude);
                databaseReference.child("hospital").child("hospital "+user.getUid()).setValue(values);
                //textView.setText("latitude :-" + latitude+ " \nlongitude:- "+ longitude+"\n Hospital Assigned :- "+address);
                lat.setText("latitude "+ latitude);
                lon.setText("longitude "+ longitude);
                adress.setText("Your address "+address);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


     }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {

                if(count==0){
                    Checkstatus();
                }

                    databaseReference.child("Accidents").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot snapshot1:snapshot.getChildren()){
                                if(snapshot1.child("Detected").getValue().toString().contains("true")){
                                    if(finalcount<1){
                                        Toast.makeText(getApplicationContext(), "zzzzzzz", Toast.LENGTH_LONG).show();
                                        sendOnChannel2("sendtohospital");
                                        finalcount++;
                                    }
                                   // textView.setText("latitude :-" + latitude+ " \nlongitude:- "+ longitude+"\n Hospital Assigned :- "+ snapshot1.child("hospital assigned").getValue().toString());

                                }

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            }finally{

                    // textView.setText(keys.toString());

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
        finalcount =0;
    }

    public static class GPSTracker extends Service implements LocationListener {

        private final Context mContext;

        // flag for GPS status
        boolean isGPSEnabled = false;

        // flag for network status
        boolean isNetworkEnabled = false;

        // flag for GPS status
        boolean canGetLocation = false;


        public Location location; // location

        double latitude; // latitude
        double longitude; // longitude


        // The minimum distance to change Updates in meters
        private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

        // The minimum time between updates in milliseconds
        private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

        // Declaring a Location Manager
        protected LocationManager locationManager;

        public GPSTracker(Context context) {
            this.mContext = context;
            getLocation();
        }

        @SuppressLint("MissingPermission")
        public Location getLocation() {
            try {
                locationManager = (LocationManager) mContext
                        .getSystemService(LOCATION_SERVICE);

                // getting GPS status
                isGPSEnabled = locationManager
                        .isProviderEnabled(LocationManager.GPS_PROVIDER);

                // getting network status
                isNetworkEnabled = locationManager
                        .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if (!isGPSEnabled && !isNetworkEnabled) {
                    // no network provider is enabled
                } else {
                    this.canGetLocation = true;
                    if (isNetworkEnabled) {
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("Network", "Network");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                    // if GPS Enabled get lat/long using GPS Services
                    if (isGPSEnabled) {
                        if (location == null) {
                            locationManager.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    MIN_TIME_BW_UPDATES,
                                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                            Log.d("GPS Enabled", "GPS Enabled");
                            if (locationManager != null) {
                                location = locationManager
                                        .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                if (location != null) {
                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                }
                            }
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return location;
        }


        /**
         * Function to get latitude
         */
        public double getLatitude() {
            if (location != null) {
                latitude = location.getLatitude();
            }

            // return latitude
            return latitude;
        }

        /**
         * Function to get longitude
         */
        public double getLongitude() {
            if (location != null) {
                longitude = location.getLongitude();
            }

            // return longitude
            return longitude;
        }


        public boolean canGetLocation() {
            return this.canGetLocation;
        }

        /**
         * Function to show settings alert dialog
         * On pressing Settings button will lauch Settings Options
         */
        public void showSettingsAlert() {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

            // Setting Dialog Title
            alertDialog.setTitle("GPS is settings");

            // Setting Dialog Message
            alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

            // On pressing Settings button
            alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    mContext.startActivity(intent);
                }
            });

            // on pressing cancel button
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            // Showing Alert Message
            alertDialog.show();
        }

        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public IBinder onBind(Intent arg0) {
            return null;
        }

    }
    public void sendOnChannel2(String notify) {
        if(notify.equals("sendtohospital")) {

            Intent notificationIntent1 = new Intent(getApplicationContext(), AccidentList.class);

            notificationIntent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PendingIntent intent1 = PendingIntent.getActivity(getApplicationContext(), 0,
                    notificationIntent1, 0);

            Notification notification3 = new NotificationCompat.Builder(this, CHANNEL_2_ID)
                    .setSmallIcon(R.drawable.bell)
                    .setOngoing(true)
                    .setColor(Color.RED)
                    .setAutoCancel(false)
                    .setContentTitle("Accident Detection System for hospital")
                    .setContentIntent(intent1)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setGroup("hospital")
                    .build();
            SystemClock.sleep(500);
            mNotificationManager.notify(7, notification3);


        }


    }

    private void createNotificationChannels()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,
                    "Channel 1",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription("This is Channel 1");

            NotificationChannel channel2 = new NotificationChannel(
                    CHANNEL_2_ID,
                    "Channel 2",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel2.setDescription("This is Channel 2");

            mNotificationManager.createNotificationChannel(channel1);
            mNotificationManager.createNotificationChannel(channel2);
        }
    }


}
