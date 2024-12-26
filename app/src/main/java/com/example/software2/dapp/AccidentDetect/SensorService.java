package com.example.software2.dapp.AccidentDetect;

import static com.example.software2.dapp.DistanceCalculatorAlgorithm.calculateMinimumDistance;
import static com.example.software2.dapp.UserActivities.ui.home.HomeFragment.powerDb;
import static com.example.software2.dapp.UserActivities.ui.home.HomeFragment.stop;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.software2.dapp.AccidentDetect.Utils.NotificationUtils;
import com.example.software2.dapp.Coordinate;
import com.example.software2.dapp.EmergencyContact.DBEmergency;
import com.example.software2.dapp.EmergencyContact.EmerContact;
import com.example.software2.dapp.ForWhichUser;
import com.example.software2.dapp.MyFirebaseMessagingService;
import com.example.software2.dapp.SendNotification.ServiceToken;
import com.example.software2.dapp.SendSMSActivity;
import com.example.software2.dapp.UserActivities.ui.home.HomeFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class SensorService extends Service implements SensorEventListener {

    ArrayList<ArrayList<String>> aList =
            new ArrayList<>();
    String finalDecibel = "0";
    boolean isStart = true;
    private NotificationUtils notificationUtils;
    int count;
    int finalCount;
    static int notificationOneTime = 0;

    public static String CHANNEL_1_ID = "channel1";
    public static String CHANNEL_2_ID = "channel2";
    private final IBinder mBinder = new LocalBinder();
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    public static CountDownTimer countDownTimer;
    static double latitude, longitude;
    private Handler mHandler;
    private Sensor accelerometer;
    private SensorManager mSensorManager;
    List<EmerContact> contact;
    ArrayList<String> add = new ArrayList<>();

    DBEmergency db;

    private GPSHandler mGPSHandler;

    // Notification Manager
    public static NotificationManager mNotificationManager;
    private static String location;
    public static double mx = 0;
    public double sensor;
    static String finalResult = "hospital";
    FirebaseUser firebaseUser;
    FirebaseAuth auth;
    HashSet<String> hospitalUsersTokens = new HashSet<>();
    private String tokenValue;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public IBinder onBind(Intent intent) {
        mHandler = new Handler();

        startRepeatingTask();
        notificationUtils = new NotificationUtils(this);
        db = new DBEmergency(this);
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        ForWhichUser forWhichUser = new MyFirebaseMessagingService();

        isStart = true;
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        tokenValue = ServiceToken.getAccessToken();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannels();

        mGPSHandler = new GPSHandler(this);
        firebaseAuth = FirebaseAuth.getInstance();

        forWhichUser.getUserId(firebaseUser, finalResult);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        if (notificationOneTime == 0) {
            notificationUtils.sendNotificationToAll("start", firebaseUser, databaseReference, finalResult);
        }
        notificationOneTime = 1;

        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        mNotificationManager.cancelAll();
        stopRepeatingTask();
        Toast.makeText(getApplicationContext(), "sensor service destroy", Toast.LENGTH_SHORT).show();
        mSensorManager.unregisterListener(this);
        // Unregister sensor when not in use
        stopSelf();
        HomeFragment.decibals.setText("");
        HomeFragment.sensorReading.setText("");
        stopService(intent);

        return super.onUnbind(intent);
    }

    public class LocalBinder extends Binder {
        public SensorService getService() {
            return SensorService.this;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(getApplicationContext(), "onCreate is created ", Toast.LENGTH_LONG).show();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannels();

        firebaseAuth = FirebaseAuth.getInstance();

        mGPSHandler = new GPSHandler(this);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("hospital").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    if (snapshot1.getValue() != null && !snapshot1.getValue().toString().contains("hospital")) {
                        hospitalUsersTokens.add(Objects.requireNonNull(snapshot1.child("token").getValue()).toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        notificationOneTime = 0;
        stopRepeatingTask();
        HomeFragment.decibals.setText("");
        HomeFragment.sensorReading.setText("");

        Toast.makeText(getApplicationContext(), "sensor service destroy", Toast.LENGTH_SHORT).show();
        mSensorManager.unregisterListener(this);                            // Unregister sensor when not in use
        stopSelf();

    }

    Runnable status = new Runnable() {
        @SuppressLint("SetTextI18n")
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void run() {
            try {
                double dd = getSensor();
                HomeFragment.sensorReading.setText(String.valueOf(dd));

                if (powerDb > 0) {
                    if (count == 0) {
                        if (firebaseUser != null) {
                            finalDecibel = String.valueOf(powerDb);
                            databaseReference.child("user").child(firebaseUser.getUid()).child("AccidentInfo").child("decibals").setValue(finalDecibel);
                            count++;
                        }

                    }

                    stopDecibels();
                    stopRepeatingTask();
                    return;
                }


            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "runnable " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            mHandler.postDelayed(status, 600);
        }
    };

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {

                latitude = mGPSHandler.getLatitude();
                longitude = mGPSHandler.getLongitude();


                if (mGPSHandler.getCurrentAddress() != null && isStart) {
                    latitude = mGPSHandler.getLatitude();
                    longitude = mGPSHandler.getLongitude();
                    float speed = mGPSHandler.getSpeed();
                    location = mGPSHandler.getCurrentAddress().replaceAll(",", "");
                    Date d = new Date();

                    final FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        contact = db.getContact(user.getEmail());
                    }
                    for (EmerContact cn : contact) {
                        add.add(cn._phone);
                    }
                    databaseReference = FirebaseDatabase.getInstance().getReference();
                    Map<String, Object> values = new HashMap<>();

                    values.put("datetime", String.valueOf(d));
                    values.put("location", location);
                    values.put("latitude", String.valueOf(latitude));
                    values.put("longitude", String.valueOf(longitude));
                    values.put("status", "NULL");
                    values.put("sensorreading", getSensor());
                    values.put("speed", speed);
                    values.put("emergcontact", add.get(0));
                    values.put("hospitalassigned", finalResult);
                    values.put("decibals", finalDecibel);

                    if (location != null && user != null) {
                        databaseReference.child("user").child(user.getUid()).child("AccidentInfo").setValue(values);
                    }

                }
                //this function can change value of mInterval.
            } finally {
                FindNearestHospital();
                // 5 seconds by default, can be changed later
                int mInterval = 6000;
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
        status.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
        mHandler.removeCallbacks(status);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) throws NumberFormatException {
        double accelerationX = (Math.round(sensorEvent.values[0] * 1000) / 1000.0);
        double accelerationY = (Math.round(sensorEvent.values[1] * 1000) / 1000.0);
        double accelerationZ = (Math.round(sensorEvent.values[2] * 1000) / 1000.0);
        ArrayList<Double> max = new ArrayList<>();
        max.add(accelerationX);
        max.add(accelerationY);
        max.add(accelerationZ);
        sensor = Collections.max(max);


        /*** Detect Accident ***/
        // if  getDecibel()>20 then detect accident

        // this value for shaking
        int threshold = 15;
        if (accelerationX > threshold || accelerationY > threshold || accelerationZ > threshold) {

            //find max value between accelerationX, accelerationY and accelerationZ
            ArrayList<Double> maxValue = new ArrayList<>();
            maxValue.add(accelerationX);
            maxValue.add(accelerationY);
            maxValue.add(accelerationZ);
            mx = Collections.max(maxValue);

            mSensorManager.unregisterListener(SensorService.this);                            // Unregister sensor when not in use
            stop();
            stopSelf();
            stopRepeatingTask();
            notificationUtils.sendNotificationToAll("sendtouser", firebaseUser, databaseReference, finalResult);
            ServiceHandler.isBound = false;

            Intent intent = new Intent(getApplicationContext(), SendSMSActivity.class);
            intent.putExtra("accident", "accident");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            countDownTimer = new CountDownTimer(15200, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    Toast.makeText(getApplicationContext(), "sending alert in " + millisUntilFinished / 1000 + " seconds", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFinish() {
                    for (String token : hospitalUsersTokens) {
                        try {
                            notificationUtils.sendNotification(token, tokenValue);
                            break;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    notificationUtils.sendNotificationToAll("sendtoambulance", firebaseUser, databaseReference, finalResult);
                    notificationUtils.sendNotificationToAll("sendtohospital", firebaseUser, databaseReference, finalResult);

                }

            }.start();
        }
    }


    public double getSensor() {
        return sensor;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void createNotificationChannels() {
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void stopDecibels() {
        mSensorManager.unregisterListener(SensorService.this);                            // Unregister sensor when not in use
        stopSelf();
        notificationUtils.sendNotificationToAll("sendtouser", firebaseUser, databaseReference, finalResult);
        Intent intent = new Intent(getApplicationContext(), SendSMSActivity.class);
        intent.putExtra("accident", "accident");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        countDownTimer = new CountDownTimer(15200, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Toast.makeText(getApplicationContext(), "sending alert in " + millisUntilFinished / 1000 + " seconds", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFinish() {
                for (String token : hospitalUsersTokens) {
                    try {
                        notificationUtils.sendNotification(token, tokenValue);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                notificationUtils.sendNotificationToAll("sendtoambulance", firebaseUser, databaseReference, finalResult);
                notificationUtils.sendNotificationToAll("sendtohospital", firebaseUser, databaseReference, finalResult);
            }

        }.start();
    }

    //Dijkstra algorithm implementation to find nearest hospital using longitude and latitudes
    public void FindNearestHospital() {
        final FirebaseUser user = firebaseAuth.getCurrentUser();
        databaseReference.child("hospital").addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                List<Coordinate> set1 = Collections.singletonList(new Coordinate(latitude, longitude, location));

                ArrayList<String> longitudes;
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    if (snapshot1.getKey() != null && snapshot1.getKey().contains("hospital")) {
                        longitudes = new ArrayList<>();
                        longitudes.add(Objects.requireNonNull(snapshot1.child("hospitalAddress").getValue()).toString());
                        longitudes.add(Objects.requireNonNull(snapshot1.child("longitude").getValue()).toString());
                        longitudes.add(Objects.requireNonNull(snapshot1.child("latitude").getValue()).toString());
                        aList.add(finalCount, longitudes);
                        finalCount++;
                    }
                }
                Coordinate c;
                List<Coordinate> listOfCoordinates = new ArrayList<>();
                for (int i = 0; i < aList.size(); i++) {
                    c = new Coordinate(Double.parseDouble(aList.get(i).get(2)), Double.parseDouble(aList.get(i).get(1)), aList.get(i).get(0));
                    listOfCoordinates.add(c);
                }
                List<Coordinate> latitudeLongitudeValue = calculateMinimumDistance(set1, listOfCoordinates);
                if (latitudeLongitudeValue.get(0) != null) {

                    String addresses = latitudeLongitudeValue.get(0).getAddress();
                    double lat = latitudeLongitudeValue.get(0).getLatitude();
                    double longitude = latitudeLongitudeValue.get(0).getLongitude();

                    if (user != null && addresses != null && lat != 0 && longitude != 0) {
                        finalResult = addresses + "\n latitude " + lat + "\n longitude " + longitude;
                        Log.d("Final Result --->", finalResult);
                        databaseReference.child("user").child(user.getUid()).child("AccidentInfo").child("hospitalassigned").setValue(finalResult);
                    }
                    finalCount = 0;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
