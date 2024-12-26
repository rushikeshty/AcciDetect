package com.example.software2.dapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.software2.dapp.LoginSignup.LoginScreenActivity;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class splash extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    // we have to start our MainActivity so we initially set it false after some delay we start our main activity.
    public static final int delay = 4000;
    TextView splash;
    int countDelay = 400;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        splash = findViewById(R.id.splash);

        Objects.requireNonNull(getSupportActionBar()).hide();

        String accident = "Welcome To Accident Detection System";

        for (int i = 0; i < accident.length(); i++) {
            int finalI = i;
            new Handler().postDelayed(() -> splash.append(accident.charAt(finalI) + ""), countDelay);
            countDelay += 100;
        }

        Timer splashTimer = new Timer();

        // This method will be executed once the timer is over
        splashTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                finish();
                Intent intent = new Intent(splash.this, LoginScreenActivity.class);
                startActivity(intent);

            }
        }, delay);
        //When we create any splash activity we required splashTime.
        //we have to set schedule as a true to start our Login.
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
