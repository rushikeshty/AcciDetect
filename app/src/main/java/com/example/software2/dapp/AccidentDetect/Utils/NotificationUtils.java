package com.example.software2.dapp.AccidentDetect.Utils;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.SystemClock;

import androidx.core.app.NotificationCompat;

import com.example.software2.dapp.AccidentDetect.Hosptialauthrity.AccidentList;
import com.example.software2.dapp.R;

public class NotificationUtils {
    public static String CHANNEL_1_ID = "channel1";
    public static String CHANNEL_2_ID = "channel2";
    public static NotificationManager mNotificationManager;
    private Context context;

    public NotificationUtils(Context context){
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
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
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

    public void initNotificationManager(){
        mNotificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
    }
}
