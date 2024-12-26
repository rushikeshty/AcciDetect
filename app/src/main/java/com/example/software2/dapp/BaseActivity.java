package com.example.software2.dapp;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.software2.dapp.AccidentDetect.Utils.NotificationUtils;

public class BaseActivity extends AppCompatActivity {
    protected ProgressDialog progressDialog;
    public NotificationUtils notificationUtils;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading please wait...");
        notificationUtils = new NotificationUtils(getApplicationContext());
        notificationUtils.createNotificationChannels();
    }

    public void dismissDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    public void showDialog() {
        if (progressDialog != null) {
            progressDialog.show();
        }
    }
}