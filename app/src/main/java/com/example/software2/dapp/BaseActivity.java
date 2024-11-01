package com.example.software2.dapp;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {


    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching data please wait...");
    }

    public void dismissDialog(){
        if(progressDialog!=null){
            progressDialog.dismiss();
        }
    }
    public void showDialog(){
        if(progressDialog!=null) {
            progressDialog.show();
        }
    }
}