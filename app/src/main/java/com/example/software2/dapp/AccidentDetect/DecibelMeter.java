package com.example.software2.dapp.AccidentDetect;

import android.util.Log;

public class DecibelMeter extends Thread {
    private volatile boolean running = true;

    public void stopRunning() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
         }
    }
}

