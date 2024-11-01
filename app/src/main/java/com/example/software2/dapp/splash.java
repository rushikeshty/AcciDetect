package com.example.software2.dapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ListAdapter;

import com.example.software2.dapp.LoginSignup.HospitalAuthority;
import com.example.software2.dapp.LoginSignup.LoginScreenActivity;

import java.util.Timer;
import java.util.TimerTask;

public class splash extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Timer splashTimer;
    //When we create any splash activity we required splashTime.
    private boolean scheduled = false;
    // we have to start our mainactivity so we initially set it false after some delay we start our main activity.
    public static final int delay = 4000;
    TextView splash;
    int countdelay = 400;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        splash = findViewById(R.id.splash);

        getSupportActionBar().hide();
        //Here we hide the action bar.ActionBar is the element present at the top of the activity screen.

        String accident = "Welcome To Accident Detection System";

        for(int i=0;i<accident.length();i++){
            int finalI = i;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    splash.append(accident.charAt(finalI)+"");

                }
            },countdelay);
            countdelay+=100;
        }

        splashTimer = new Timer();

        // This method will be executed once the timer is over
        splashTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                finish();
                Intent intent = new Intent(splash.this, LoginScreenActivity.class);
                startActivity(intent);

            }
        }, delay);
        scheduled = true;
        //we have to set schedule as a true to start our Login.
    }

    void method(){


    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
