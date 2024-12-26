package com.example.software2.dapp.LoginSignup;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.software2.dapp.AccidentDetect.Hosptialauthrity.AccidentList;
import com.example.software2.dapp.BaseActivity;
import com.example.software2.dapp.R;
import com.google.firebase.auth.FirebaseAuth;

public class AmbulanceDriver extends BaseActivity {

    EditText editTextEmail, editTextPassword;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ambulance_driver);

        firebaseAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (editTextEmail.getText().toString().contains("@")) {
                    Toast.makeText(getApplicationContext(), "username should not contain @ ", Toast.LENGTH_LONG).show();
                    editTextEmail.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editTextEmail.getText().toString().contains("@")) {
                    Toast.makeText(getApplicationContext(), "username should not contain @ ", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    public void register(View view) {

        if (editTextEmail.getText() != null && editTextPassword.getText() != null) {
            final String email = editTextEmail.getText().toString().trim() + "@ambulance.com";
            final String password = editTextPassword.getText().toString().trim();

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(getApplicationContext(), "Invalid username,Try again", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 8) {
                Toast.makeText(getApplicationContext(), "Password must be of 8 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(getApplicationContext(), "No Username Entered", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(getApplicationContext(), "No Password Entered", Toast.LENGTH_SHORT).show();
                return;
            }

            progressDialog.setMessage("Registering...");
            progressDialog.show();

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            firebaseAuth.signInWithEmailAndPassword(email, password);
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Welcome!!", Toast.LENGTH_SHORT).show();
                            finish();
                            startActivity(new Intent(AmbulanceDriver.this, AccidentList.class));

                        } else {
                            Toast.makeText(getApplicationContext(), "check your internet connection or try another username", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    });
        }
    }
}