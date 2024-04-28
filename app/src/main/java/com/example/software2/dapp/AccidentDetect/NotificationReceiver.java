package com.example.software2.dapp.AccidentDetect;

import static com.example.software2.dapp.AccidentDetect.SensorService.countDownTimer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.software2.dapp.UserActivities.MainActivity;
import com.example.software2.dapp.SendSMSActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class NotificationReceiver extends BroadcastReceiver {
    private ServiceHandler mServiceHandler;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    @Override
    public void onReceive(Context context, @NonNull Intent intent) {
        context.stopService(intent);
         countDownTimer.cancel();

        SensorService.mNotificationManager.cancelAll();
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("Detected", "false");
        values.put("userid",String.valueOf(user));

         if(user!=null){
             databaseReference.child("Accidents").child(user.getUid()).setValue(values);
         }

        Toast.makeText(context, "start again tracing", Toast.LENGTH_SHORT).show();
        SendSMSActivity.mediaPlayeralarm.stop();
        SendSMSActivity.timer.cancel();

        Intent i = new Intent(context, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        //i.setAction("starttrack");
        context.startActivity(i);

    }
 }
