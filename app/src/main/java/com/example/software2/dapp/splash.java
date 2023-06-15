package com.example.software2.dapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.software2.dapp.LoginSignup.HospitalAuthority;
import com.example.software2.dapp.LoginSignup.LoginScreenActivity;

import java.util.Timer;
import java.util.TimerTask;

public class splash extends AppCompatActivity {
    private Timer splashTimer;
    //When we create any splash activity we required splashTime.
    private boolean scheduled = false;
    // we have to start our mainactivity so we initially set it false after some delay we start our main activity.
    public static final int delay=4500;
    TextView splash;
    int countdelay=300;

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
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                splash.setText("W");
//            }
//        }, 300);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                splash.append("e");
//            }
//        }, 400);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                splash.append("l");
//            }
//        }, 500);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                splash.append("c");
//            }
//        }, 600);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                splash.append("o");
//            }
//        }, 700);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                splash.append("m");
//            }
//        }, 800);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                splash.append("e");
//            }
//        }, 900);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                splash.append(" T");
//            }
//        }, 1000);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                splash.append("o");
//            }
//        }, 1200);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                splash.append(" B");
//            }
//        }, 1300);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                splash.append("l");
//            }
//        }, 1400);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                splash.append("o");
//            }
//        }, 1500);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                splash.append("o");
//            }
//        }, 1600);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                splash.append("d");
//            }
//        }, 1700);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                splash.append(" B");
//            }
//        }, 1800);
//
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                splash.append("a");
//            }
//        }, 1900);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                splash.append("n");
//            }
//        }, 2000);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                splash.append("k");
//            }
//        }, 2100);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                splash.append(" M");
//            }
//        }, 2200);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                splash.append("a");
//            }
//        }, 2300);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                splash.append("n");
//            }
//        }, 2400);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                splash.append("a");
//            }
//        }, 2500);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                splash.append("g");
//            }
//        }, 2600);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                splash.append("e");
//            }
//        }, 2700);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                splash.append("m");
//            }
//        }, 2800);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                splash.append("e");
//            }
//        }, 2900);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                splash.append("n");
//            }
//        }, 3000);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                splash.append("t");
//            }
//        }, 3100);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                splash.append(" S");
//            }
//        }, 3200);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                splash.append("y");
//            }
//        }, 3300);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                splash.append("s");
//            }
//        }, 3400);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                splash.append("t");
//            }
//        }, 3500);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                splash.append("e");
//            }
//        }, 3600);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                splash.append("m");
//            }
//        }, 3700);

        // Context  handle  the environment of  your application which  is currently running in.

        //Create a splashTimer as a object to set the schedule
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
    }
