package com.example.software2.dapp.AccidentDetect;

import static com.example.software2.dapp.DistanceCalculatorAlgorithm.calculateMinimumDistance;
import static com.example.software2.dapp.UserActivities.ui.home.HomeFragment.powerDb;
import static com.example.software2.dapp.UserActivities.ui.home.HomeFragment.stop;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.software2.dapp.AccidentDetect.Hosptialauthrity.AccidentList;
import com.example.software2.dapp.AmbulanceViewAccident.Accidents;
import com.example.software2.dapp.Coordinate;
import com.example.software2.dapp.EmergencyContact.DBEmergency;
import com.example.software2.dapp.EmergencyContact.EmerContact;
import com.example.software2.dapp.R;
import com.example.software2.dapp.SendSMSActivity;
import com.example.software2.dapp.UserActivities.ui.home.HomeFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SensorService extends Service implements SensorEventListener {

    ArrayList<String> longitudes = new ArrayList<>();
    ArrayList<ArrayList<String>> aList =
            new ArrayList<ArrayList<String>>();
    // TAG to identify notification
    DecibelMeter decibelMeter;
  //  public static File mOutputFile;
  //  public static File finaloutfile;
     String finaldecibel="0";
    boolean isstart=true;

    private PowerManager.WakeLock mWakeLock;
    int count;
    int finalcount;
    static int notificationonetime=0;

    public static String CHANNEL_1_ID = "channel1";
    public static String CHANNEL_2_ID = "channel2";
    private final IBinder mBinder = new LocalBinder();
     private FirebaseAuth firebaseAuth;
     private DatabaseReference databaseReference;
     private ProgressDialog progressDialog;
   public static CountDownTimer countDownTimer;
    static double latitude, longitude;
    private int mInterval = 6000; // 5 seconds by default, can be changed later
    private Handler mHandler;
    private Sensor accelerometer;
    private SensorManager mSensorManager;
    List<EmerContact> contact;
    ArrayList<String> add=new ArrayList<String>();
    DBEmergency db;

    private double accelerationX, accelerationY, accelerationZ;

    private int threshold = 35;// this value for shaking
    double soundthreshold=15; // This value is for sound decibel
    private GPSHandler mGPSHandler;

    // Notification Manager
    public static NotificationManager mNotificationManager;
    private static String location;
    public static double mx=0;
    public double sensor;
    static String finalresult;
    FirebaseUser userr;
    FirebaseAuth auth;
//    public static MediaRecorder mRecorder;
//     public static double powerDb;
//    static int amp;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public IBinder onBind(Intent intent) {
         mHandler = new Handler();


        startRepeatingTask();
         mGPSHandler = new GPSHandler(this);
         db = new DBEmergency(this);
         auth = FirebaseAuth.getInstance();
         userr = auth.getCurrentUser();

         isstart = true;
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannels();

        mGPSHandler = new GPSHandler(this);
        firebaseAuth = FirebaseAuth.getInstance();

        databaseReference = FirebaseDatabase.getInstance().getReference();
        if(notificationonetime==0){
            sendOnChannel2("start");
        }
        notificationonetime= 1;


        return mBinder;
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean isNotificationVisible() {
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        StatusBarNotification[] notifications = mNotificationManager.getActiveNotifications();
        for (StatusBarNotification notification : notifications) {
            if (notification.getId() == 2) {
                Toast.makeText(getApplicationContext(), "already present", Toast.LENGTH_SHORT).show();
                return true;
            }
        }

        return false;
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
        HomeFragment.sensorread.setText("");
        stopService(intent);

        return super.onUnbind(intent);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
       // startRepeatingTask();


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
     //   mRecorder = new MediaRecorder();

        decibelMeter = new DecibelMeter();
         Toast.makeText(getApplicationContext(), "onCreate is created ", Toast.LENGTH_LONG).show();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannels();
        //showAccidentNotification();
        //sendOnChannel2("start");
        firebaseAuth = FirebaseAuth.getInstance();

        mGPSHandler = new GPSHandler(this);

        databaseReference = FirebaseDatabase.getInstance().getReference();

    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showAccidentNotification() {

        Intent activityIntent = new Intent(this, SensorService.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, activityIntent, PendingIntent.FLAG_IMMUTABLE);

//        Intent broadcastIntent = new Intent(this, NotificationReceiver.class);
//        broadcastIntent.putExtra("toastMessage", message);
//        PendingIntent actionIntent = PendingIntent.getBroadcast(this,
//                0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "notify_001");
        mBuilder.setSmallIcon(R.drawable.bell);

        mBuilder.setContentTitle("Accident Detection System")
                .setContentText("Accident detection has been started.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setColor(Color.BLUE)
                .setContentIntent(contentIntent)
                .build();

        NotificationChannel channel = new NotificationChannel(CHANNEL_1_ID, "channel name", NotificationManager.IMPORTANCE_HIGH);
        channel.enableVibration(true);
        mNotificationManager.createNotificationChannel(channel);
        mBuilder.setChannelId(CHANNEL_1_ID);

        Notification notification = mBuilder.build();
         long[] pattern = {0, 100, 1000, 200, 2000};
//            vibrator.vibrate(pattern, -1);notification.

        startForeground(1, notification);
        startForegroundService(activityIntent);

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        notificationonetime = 0;
        stopRepeatingTask();

        //stopmedia();
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        HomeFragment.decibals.setText("");
        HomeFragment.sensorread.setText("");

      // mRecorder = null;


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

//                if (getdecibal()!=null&&Double.parseDouble(getdecibal())>=0) {
//                    if (Double.parseDouble(getdecibal())> soundthreshold) {
//                        Stopdecibal();
//                        finaldecibel = getdecibal();
//
//                    }
//
//                    Toast.makeText(getApplicationContext(), "decibel "+getdecibal(), Toast.LENGTH_SHORT).show();
//
//                }
//                if(getdecibal()!=null) {
//
//                }

                double dd = getSensor();
                HomeFragment.sensorread.setText(String.valueOf(dd));

                if(powerDb>0){
                    if(count==0){
                        if(userr!=null){
                            finaldecibel= String.valueOf(powerDb);
                            databaseReference.child("user").child(userr.getUid()).child("AccidentInfo").child("decibals").setValue(finaldecibel);
                            count++;
                        }

                    }

                    decibelMeter.stopRunning();
                    Stopdecibal();
                    stopRepeatingTask();
                    return;
                }


            }
            catch (Exception e){
                Log.d("hhhhh",e.getMessage());
                Toast.makeText(getApplicationContext(), "runnable "+e.getMessage(), Toast.LENGTH_LONG).show();
            }
            mHandler.postDelayed(status,600);
        }
    };

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {

                latitude =mGPSHandler.getLatitude();
                longitude = mGPSHandler.getLongitude();


                if(mGPSHandler.getCurrentAddress()!=null && isstart){
                    latitude =mGPSHandler.getLatitude();
                    longitude = mGPSHandler.getLongitude();
                    float speed = mGPSHandler.getspeed();
                    location =mGPSHandler.getCurrentAddress().replaceAll(",","");
                    Date d = new Date();
                    
                    final FirebaseUser user = firebaseAuth.getCurrentUser();
                    if(user!=null) {
                        contact = db.getContact(user.getEmail());
                    }
                    for (EmerContact cn : contact) {
                        add.add(cn._phone);
                    }
                    databaseReference = FirebaseDatabase.getInstance().getReference();
                    Map<String, Object> values = new HashMap<String, Object>();

                    values.put("datetime", String.valueOf(d));
                    values.put("location",location);
                    values.put("latitude",String.valueOf(latitude));
                    values.put("longitude",String.valueOf(longitude));
                    values.put("status","NULL");
                    values.put("sensorreading",getSensor());
                    values.put("speed",speed);
                    values.put("emergcontact",add.get(0));
                    values.put("hospitalassigned",finalresult);
                    values.put("decibals",finaldecibel);


                    if(location!=null) {
                        databaseReference.child("user").child(user.getUid()).child("AccidentInfo").setValue(values);
                       // Toast.makeText(getApplicationContext(), values.toString(), Toast.LENGTH_LONG).show();
                    }

                }
                //this function can change value of mInterval.
            } finally {
                FindNearestHospital();
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

        accelerationX = (Math.round(sensorEvent.values[0]*1000)/1000.0);
        accelerationY = (Math.round(sensorEvent.values[1]*1000)/1000.0);
        accelerationZ = (Math.round(sensorEvent.values[2]*1000)/1000.0);
        ArrayList<Double> max = new ArrayList<>();
        max.add(accelerationX);
        max.add(accelerationY);
        max.add(accelerationZ);
        if(max.isEmpty()){
            sensor = 1.2;// assigning some random value to not getting null point exception
        }
        else {
            sensor = Collections.max(max);
        }




        /*** Detect Accident ***/
        // if  getdecibal()>20 then detect accident

        if (accelerationX > threshold || accelerationY > threshold || accelerationZ > threshold) {

            //find max value between accelerationX, accelerationY and accelerationZ
            ArrayList<Double> maxx = new ArrayList<>();
            maxx.add(accelerationX);
            maxx.add(accelerationY);
            maxx.add(accelerationZ);
             mx = Collections.max(maxx);
            //mNotificationManager.cancelAll();
           // showUserNotification();
//            Intent mIntent = new Intent();
//            mIntent.setClass(getApplicationContext(), SendSMSActivity.class);

             mSensorManager.unregisterListener(SensorService.this);                            // Unregister sensor when not in use
            stop();
            stopSelf();
            sendOnChannel2("sendtouser");
            ServiceHandler.isBound = false;
            //sendOnChannel2("sendtoambulance");

            Intent intent = new Intent(getApplicationContext(), SendSMSActivity.class);
            intent.putExtra("accident","accident");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            countDownTimer = new CountDownTimer(15200, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    Toast.makeText(getApplicationContext(), "sending alert in "+millisUntilFinished/1000 +" seconds", Toast.LENGTH_SHORT).show();
                 }
                @Override
                public void onFinish() {
                    sendOnChannel2("sendtoambulance");
                    sendOnChannel2("sendtohospital");

                }

            }.start();

//            startActivity(mIntent);

        }
    }


    public double getSensor(){

        return sensor;
    }
//    public static String getdecibal() {
//        try {
//            if (mRecorder != null){
//                amp = mRecorder.getMaxAmplitude();
//            }
//            else{
//                amp=0;
//            }
//            powerDb = 20 * log10(amp / 2700.0);
//            return new DecimalFormat("##.##").format(powerDb).replace("-∞",String.valueOf(0));
//        }
//        catch (IllegalStateException e){
//           CustomToastActivity.showCustomToast(e.getMessage());
//        }
//      return "0";
//    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @SuppressLint("NotificationTrampoline")
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showUserNotification() {
        Log.d("SERVICE DEBUG", "Notification Shown");
        CharSequence text = "Started Data Collection";


        Intent activityIntent = new Intent(this, SensorService.class);
        @SuppressLint("UnspecifiedImmutableFlag")
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, activityIntent, 0);

        Intent broadcastIntent = new Intent(this, NotificationReceiver.class);
        broadcastIntent.putExtra("toastMessage", "Alarm cancelled");
        @SuppressLint("UnspecifiedImmutableFlag")
        PendingIntent actionIntent = PendingIntent.getBroadcast(this,
                0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);

         mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "notify_001");
//        //here we set all the properties for the notification
        RemoteViews contentView = new RemoteViews(this.getPackageName(), R.layout.notification_layout);
        @SuppressLint("RemoteViewLayout")
        RemoteViews contentView1 = new RemoteViews(this.getPackageName(), R.layout.notification_layout2);

        contentView.setImageViewResource(R.id.image, R.drawable.bell);
        contentView.setTextViewText(R.id.message,"Accident information will be sent in a moment. cancel if it is false alarm");
        contentView1.setImageViewResource(R.id.image,R.drawable.bell);
        contentView1.setTextViewText(R.id.message,"Accident information will be sent in a moment. cancel if it is false alarm");
       // contentView1.setOnClickPendingIntent(R.id.falsebutton,pendingNotificationIntent);
        //contentView.setTextViewText(R.id.date, "date");
          mBuilder.setSmallIcon(R.drawable.alarm);
          mBuilder.setPriority(Notification.PRIORITY_MAX);

          mBuilder.setContentIntent(contentIntent);
          mBuilder.setStyle(new NotificationCompat.DecoratedCustomViewStyle());
          mBuilder.addAction(R.mipmap.ic_launcher,"False Alarm",actionIntent);
         //mBuilder.setContent(contentView);
          mBuilder.setCustomContentView(contentView);
          mBuilder.setCustomBigContentView(contentView1);

             String channelId = "channel_id";
            NotificationChannel channel = new NotificationChannel(channelId, "channel name", NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(true);
            mNotificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId(channelId);

            Notification notification = mBuilder.build();
             long[] pattern = {0, 100, 1000, 200, 2000};

 //            vibrator.vibrate(pattern, -1);
            startForeground(1, notification);
            startForegroundService(activityIntent);



    }
    @SuppressLint("NotificationTrampoline")
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showAmbulanceNotification() {
        Log.d("SERVICE DEBUG", "Notification Shown");
        CharSequence text = "Started Data Collection";


        Intent activityIntent = new Intent(this, SensorService.class);
        @SuppressLint("UnspecifiedImmutableFlag")
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, activityIntent, 0);

        Intent broadcastIntent = new Intent(this, NotificationReceiver.class);
        broadcastIntent.putExtra("toastMessage", "Alarm cancelled");
        @SuppressLint("UnspecifiedImmutableFlag")
        PendingIntent actionIntent = PendingIntent.getBroadcast(this,
                0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "notify_001");
//        //here we set all the properties for the notification
        RemoteViews contentView = new RemoteViews(this.getPackageName(), R.layout.notification_layout);
        @SuppressLint("RemoteViewLayout")
        RemoteViews contentView1 = new RemoteViews(this.getPackageName(), R.layout.notification_layout2);

        contentView.setImageViewResource(R.id.image, R.drawable.bell);
        contentView.setTextViewText(R.id.message,"Accident information will be sent in a moment. cancel if it is false alarm");
        contentView1.setImageViewResource(R.id.image,R.drawable.bell);
        contentView1.setTextViewText(R.id.message,"Accident information will be sent in a moment. cancel if it is false alarm");
        // contentView1.setOnClickPendingIntent(R.id.falsebutton,pendingNotificationIntent);
        //contentView.setTextViewText(R.id.date, "date");
        mBuilder.setSmallIcon(R.drawable.alarm);
        mBuilder.setPriority(Notification.PRIORITY_MAX);

        mBuilder.setContentIntent(contentIntent);
        mBuilder.setStyle(new NotificationCompat.DecoratedCustomViewStyle());
        mBuilder.addAction(R.mipmap.ic_launcher,"False Alarm",actionIntent);
        //mBuilder.setContent(contentView);
        mBuilder.setCustomContentView(contentView);
         mBuilder.setCustomBigContentView(contentView1);

        String channelId = "channel_id";
        NotificationChannel channel = new NotificationChannel(channelId, "channel name", NotificationManager.IMPORTANCE_HIGH);
        channel.enableVibration(true);
        mNotificationManager.createNotificationChannel(channel);
        mBuilder.setChannelId(channelId);

        Notification notification = mBuilder.build();
        long[] pattern = {0, 100, 1000, 200, 2000};

//            vibrator.vibrate(pattern, -1);
        startForeground(1, notification);
        startForegroundService(activityIntent);



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
    public void sendOnChannel2(String notify) {

        if(notify.equals("start")) {
            Intent activityIntent = new Intent(this, SensorService.class);
            @SuppressLint("UnspecifiedImmutableFlag")
            PendingIntent contentIntent = PendingIntent.getActivity(this,
                    0, activityIntent, 0);
            Notification notification1 = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                    .setSmallIcon(R.drawable.alarm)
                    .setContentTitle("Accident Detection System")
                    .setContentText("Accident detection has been started.")
                    .setAutoCancel(false)
                    .setContentIntent(contentIntent)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setGroup("example_group")
                    .setOngoing(true)
                    .build();
             mNotificationManager.notify(2, notification1);

        }
        else if(notify.equals("sendtouser")) {

            Intent broadcastIntent = new Intent(this, NotificationReceiver.class);
             broadcastIntent.putExtra("toastMessage", "Alarm cancelled");
            @SuppressLint("UnspecifiedImmutableFlag")
            PendingIntent actionIntent = PendingIntent.getBroadcast(this,
                    0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            String text ="Accident information will be sent in a moment. cancel if it is false alarm";

            @SuppressLint("NotificationTrampoline") Notification notification2 = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                    .setSmallIcon(R.drawable.alarm)
                    .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                    .setOngoing(true)
                    .setAutoCancel(false)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                    .setContentTitle("Accident Detection System")
                    .setContentText(text)
                    .addAction(R.mipmap.ic_launcher, "False Alarm", actionIntent)
                     .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setGroup("example_group")
                    .build();
            mNotificationManager.notify(3,notification2);

        }
        else if(notify.equals("sendtoambulance")) {

            isstart=false;
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if(user!=null){
                Map<String, Object> values = new HashMap<String, Object>();
                values.put("Detected", "true");
                values.put("userid",user.getUid());
                values.put("hospital assigned",finalresult);
                databaseReference.child("Accidents").child(user.getUid()).setValue(values);

            }
//            RemoteViews contentView = new RemoteViews(this.getPackageName(), R.layout.notification_layout);
//            @SuppressLint("RemoteViewLayout")
//            RemoteViews contentView1 = new RemoteViews(this.getPackageName(), R.layout.notification_layout2);
            Intent broadcastIntent = new Intent(this, NotificationReceiver.class);

            broadcastIntent.putExtra("toastMessage", "Alarm cancelled");
            @SuppressLint("UnspecifiedImmutableFlag")
            PendingIntent actionIntent = PendingIntent.getBroadcast(this,
                    0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//            contentView.setImageViewResource(R.id.image, R.drawable.bell);
//            contentView.setTextViewText(R.id.message, "Accident detection for hospital");
//            contentView1.setImageViewResource(R.id.image, R.drawable.bell);
//            contentView1.setTextViewText(R.id.message, "Accident information will be sent in a moment. cancel if it is false alarm");

            Intent notificationIntent = new Intent(getApplicationContext(), Accidents.class);
            Intent notificationIntent1 = new Intent(getApplicationContext(), AccidentList.class);

            notificationIntent.putExtra("userid",user.getUid());
            notificationIntent.putExtra("assignedhospital",finalresult);

            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            @SuppressLint("UnspecifiedImmutableFlag") PendingIntent intent = PendingIntent.getActivity(getApplicationContext(), 0,
                    notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            notificationIntent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PendingIntent intent1 = PendingIntent.getActivity(getApplicationContext(), 0,
                    notificationIntent1, PendingIntent.FLAG_IMMUTABLE);
             Notification notification2 = new NotificationCompat.Builder(this, CHANNEL_2_ID)
                    .setSmallIcon(R.drawable.bell)
                    .setOngoing(true)
                    .setColor(Color.RED)
                     .setAutoCancel(false)
                     .setContentTitle("Accident Detection System for Ambulance")
                    .setContentIntent(intent)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setGroup("Ambulance")
                    .build();
              mNotificationManager.notify(5, notification2);

            Notification notification3 = new NotificationCompat.Builder(this, CHANNEL_2_ID)
                    .setSmallIcon(R.drawable.bell)
                    .setOngoing(true)
                    .setColor(Color.RED)
                    .setAutoCancel(false)
                    .setContentTitle("Accident Detection System for hospital")
                    .setContentIntent(intent1)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setGroup("Ambulance")
                    .build();
             mNotificationManager.notify(7, notification3);

            Notification summaryNotification1 = new NotificationCompat.Builder(this, CHANNEL_2_ID)
                    .setSmallIcon(R.drawable.bell)
                    .setStyle(new NotificationCompat.InboxStyle()
                    .setSummaryText("for other"))
                    .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                    .setOngoing(true)
                    .setAutoCancel(false)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText("Accident Detection System for Hospital"))
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setGroup("Ambulance")
                    .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)
                    .setGroupSummary(true)
                    .build();

             mNotificationManager.notify(6, summaryNotification1);

         }

        Notification summaryNotification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.alarm)
                .setStyle(new NotificationCompat.InboxStyle()
                        .addLine("Accident Detection System" + " " + "Accident detection has been started.")
                        .addLine( "Accident detection alert")
                         .setSummaryText("user@example.com"))
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setGroup("example_group")
                .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)
                .setGroupSummary(true)
                .build();

         mNotificationManager.notify(4, summaryNotification);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void Stopdecibal(){
        mSensorManager.unregisterListener(SensorService.this);                            // Unregister sensor when not in use
        stopSelf();
        sendOnChannel2("sendtouser");
        //sendOnChannel2("sendtoambulance");
        Intent intent = new Intent(getApplicationContext(),SendSMSActivity.class);
        intent.putExtra("accident","accident");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);


        countDownTimer = new CountDownTimer(15200, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Toast.makeText(getApplicationContext(), "sending alert in "+millisUntilFinished/1000 +" seconds", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFinish() {
                sendOnChannel2("sendtoambulance");
                sendOnChannel2("sendtohospital");



            }

        }.start();

    }

    //Dijkstra algorithm implementation to find nearest hospital using longitude and latitudes
    public void FindNearestHospital(){

        final FirebaseUser user = firebaseAuth.getCurrentUser();
        databaseReference.child("hospital").addValueEventListener(new ValueEventListener()
        {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                List<Coordinate> set1 = Collections.singletonList(new Coordinate(latitude, longitude, location));

                ArrayList<String> longitudes;
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    longitudes = new ArrayList<>();
                    longitudes.add(snapshot1.child("hospitalAddress").getValue().toString());
                    longitudes.add(snapshot1.child("longitude").getValue().toString());
                    longitudes.add(snapshot1.child("latitude").getValue().toString());
                    aList.add(finalcount, longitudes);
                    finalcount++;
                }
                Coordinate c;
                List<Coordinate> listt = new ArrayList<>();
                for (int i = 0; i < aList.size(); i++) {
                    c = new Coordinate(Double.parseDouble(aList.get(i).get(2)), Double.parseDouble(aList.get(i).get(1)), aList.get(i).get(0));
                    listt.add(c);
                }
                List<Coordinate> latitudelongitString = calculateMinimumDistance(set1, listt);
                String addresss= latitudelongitString.get(0).getAddress();
                double lat = latitudelongitString.get(0).getLatitude();
                double longi = latitudelongitString.get(0).getLongitude();

                if(user!=null&&addresss!=null&&lat!=0&&longi!=0) {
                    //progressDialog.dismiss();
                     finalresult = addresss+"\n latitude "+ lat+ "\n longitude "+ longi;
                    //Toast.makeText(getApplicationContext(), finalresult, Toast.LENGTH_SHORT).show();
                    databaseReference.child("user").child(user.getUid()).child("AccidentInfo").child("hospitalassigned").setValue(finalresult);
                }
                finalcount = 0;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



}
