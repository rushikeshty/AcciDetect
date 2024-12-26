package com.example.software2.dapp.AccidentDetect.Utils;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.software2.dapp.AccidentDetect.Hosptialauthrity.AccidentList;
import com.example.software2.dapp.AccidentDetect.NotificationReceiver;
import com.example.software2.dapp.AccidentDetect.SensorService;
import com.example.software2.dapp.AmbulanceViewAccident.Accidents;
import com.example.software2.dapp.R;
import com.example.software2.dapp.SendNotification.FCMRequest;
import com.example.software2.dapp.SendNotification.NotificationAPI;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationUtils {
    public static String CHANNEL_1_ID = "channel1";
    public static String CHANNEL_2_ID = "channel2";
    public static NotificationManager mNotificationManager;
    private final Context context;
    boolean isStarted = true;

    public NotificationUtils(Context context) {
        this.context = context;
    }

    public void createNotificationChannels() {
        initNotificationManager();
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

    public void sendOnChannel2(String notify) {
        if (notify.equals("sendtoambulance")) {

            Intent notificationIntent1 = new Intent(context, AccidentList.class);

            notificationIntent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PendingIntent intent1;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                intent1 = PendingIntent.getActivity(context, 0,
                        notificationIntent1, PendingIntent.FLAG_MUTABLE);
            } else {
                intent1 = PendingIntent.getActivity(context,
                        0, notificationIntent1, PendingIntent.FLAG_IMMUTABLE);
            }

            Notification notification2 = new NotificationCompat.Builder(context, CHANNEL_2_ID)
                    .setSmallIcon(R.drawable.bell)
                    .setOngoing(true)
                    .setColor(Color.RED)
                    .setAutoCancel(false)
                    .setContentTitle("Accident Detection System for Ambulance")
                    .setContentIntent(intent1)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .build();
            SystemClock.sleep(2000);
            mNotificationManager.notify(5, notification2);

        }


    }

    public void sendNotification(String serviceToken, String tokenValue) throws IOException {
        Map<String, String> data = new HashMap<>();
        data.put("body", "hhh");
        data.put("title", "Accident Detection System for Ambulance");
        Log.d("FCM TOKEN ", serviceToken);
        FCMRequest.Message message = new FCMRequest.Message();

        FCMRequest request = new FCMRequest(message);
        String token = "Bearer " + tokenValue;
        Log.d("FCM TOKEN ", token);

        NotificationAPI.INSTANCE.sendNotification().sendNotification(token, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("FCM", "Notification sent successfully!");
                } else {
                    Log.e("FCM", "Failed to send notification: " + response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e("FCM", "Error sending notification", t);
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void sendNotificationToAll(String notify, FirebaseUser user, DatabaseReference databaseReference, String finalResult) {
        initNotificationManager();
        switch (notify) {
            case "start": {
                Intent activityIntent = new Intent(context, SensorService.class);
                PendingIntent contentIntent;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    contentIntent = PendingIntent.getActivity(context,
                            0, activityIntent, PendingIntent.FLAG_MUTABLE);
                } else {
                    contentIntent = PendingIntent.getActivity(context,
                            0, activityIntent, PendingIntent.FLAG_IMMUTABLE);
                }

                Notification notification1 = new NotificationCompat.Builder(context, CHANNEL_1_ID)
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

                break;
            }
            case "sendtouser": {

                Intent broadcastIntent = new Intent(context, NotificationReceiver.class);
                broadcastIntent.putExtra("toastMessage", "Alarm cancelled");

                PendingIntent contentIntent;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    contentIntent = PendingIntent.getBroadcast(context,
                            0, broadcastIntent, PendingIntent.FLAG_MUTABLE);
                } else {
                    contentIntent = PendingIntent.getBroadcast(context,
                            0, broadcastIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
                }
                String text = "Accident information will be sent in a moment. cancel if it is false alarm";

                @SuppressLint("NotificationTrampoline") Notification notification2 = new NotificationCompat.Builder(context, CHANNEL_1_ID)
                        .setSmallIcon(R.drawable.alarm)
                        .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                        .setOngoing(true)
                        .setAutoCancel(false)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                        .setContentTitle("Accident Detection System")
                        .setContentText(text)
                        .addAction(R.mipmap.ic_launcher, "False Alarm", contentIntent)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setGroup("example_group")
                        .build();
                mNotificationManager.notify(3, notification2);

                break;
            }
            case "sendtoambulance": {

                isStarted = false;
                if (user != null) {
                    Map<String, Object> values = new HashMap<>();
                    values.put("Detected", "true");
                    values.put("userid", user.getUid());
                    values.put("hospital assigned", finalResult);
                    databaseReference.child("Accidents").child(user.getUid()).setValue(values);
                }

                break;
            }
        }

        Notification summaryNotification = new NotificationCompat.Builder(context, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.alarm)
                .setStyle(new NotificationCompat.InboxStyle()
                        .addLine("Accident Detection System" + " " + "Accident detection has been started.")
                        .addLine("Accident detection alert")
                        .setSummaryText("user@example.com"))
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setGroup("example_group")
                .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)
                .setGroupSummary(true)
                .build();

        mNotificationManager.notify(4, summaryNotification);

    }

    public void sendAmbulanceNotification(FirebaseUser user, String finalResult, String title) {
        Intent broadcastIntent = new Intent(context, NotificationReceiver.class);

        broadcastIntent.putExtra("toastMessage", "Alarm cancelled");

        Intent notificationIntent = new Intent(context, Accidents.class);
        Intent notificationIntent1 = new Intent(context, AccidentList.class);

        notificationIntent.putExtra("userid", user.getUid());
        notificationIntent.putExtra("assignedhospital", finalResult);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            intent = PendingIntent.getActivity(context, 0,
                    notificationIntent, PendingIntent.FLAG_MUTABLE);
        } else {
            intent = PendingIntent.getActivity(context,
                    0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        }

        notificationIntent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent contentIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            contentIntent = PendingIntent.getActivity(context,
                    0, notificationIntent1, PendingIntent.FLAG_MUTABLE);
        } else {
            contentIntent = PendingIntent.getActivity(context,
                    0, notificationIntent1, PendingIntent.FLAG_IMMUTABLE);
        }
        Notification notification2 = new NotificationCompat.Builder(context, CHANNEL_2_ID)
                .setSmallIcon(R.drawable.bell)
                .setOngoing(true)
                .setColor(Color.RED)
                .setAutoCancel(false)
                .setContentTitle(title)
                .setContentIntent(intent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setGroup("Ambulance")
                .build();
        mNotificationManager.notify(5, notification2);

        Notification notification3 = new NotificationCompat.Builder(context, CHANNEL_2_ID)
                .setSmallIcon(R.drawable.bell)
                .setOngoing(true)
                .setColor(Color.RED)
                .setAutoCancel(false)
                .setContentTitle("Accident Detection System for hospital")
                .setContentIntent(contentIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setGroup("Ambulance")
                .build();
        mNotificationManager.notify(7, notification3);

        Notification summaryNotification1 = new NotificationCompat.Builder(context, CHANNEL_2_ID)
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

    public void initNotificationManager() {
        mNotificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
    }
}
