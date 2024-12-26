package com.example.software2.dapp;

import com.google.firebase.auth.FirebaseUser;

public interface ForWhichUser{
    void getUserId(FirebaseUser user, String finalResult);
}
