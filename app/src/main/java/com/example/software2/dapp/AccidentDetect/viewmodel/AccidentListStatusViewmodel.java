package com.example.software2.dapp.AccidentDetect.viewmodel;

import android.app.NotificationManager;

import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AccidentListStatusViewmodel extends ViewModel {
    DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;

    public void init(){
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }
    public DatabaseReference getDatabaseReference() {
        return databaseReference;
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }
}