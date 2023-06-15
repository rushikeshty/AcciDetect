package com.example.software2.dapp.LoginSignup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.software2.dapp.AccidentDetect.HospitalAssigned;
import com.example.software2.dapp.AccidentDetect.Hosptialauthrity.AccidentList;
import com.example.software2.dapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class AmbulanceDriver extends AppCompatActivity {

    EditText editTextemail, editTextpassword;
     private View layout;
    private LayoutInflater inflater;
     private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ambulance_driver);
        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        layout = inflater.inflate(R.layout.custom_toast, (ViewGroup) this.findViewById(R.id.toast));
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        editTextemail = findViewById(R.id.editTextEmail);
        editTextpassword =findViewById(R.id.editTextPassword);
         editTextemail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (editTextemail.getText().toString().contains("@")) {
                     Toast.makeText(getApplicationContext(), "username should not contain @ ", Toast.LENGTH_LONG).show();
                    editTextemail.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editTextemail.getText().toString().contains("@")) {
                     Toast.makeText(getApplicationContext(), "username should not contain @ ", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    public void register(View view) {

        if (editTextemail.getText() != null && editTextpassword.getText() != null) {
            final String email = editTextemail.getText().toString().trim() + "@ambulance.com";
            final String password = editTextpassword.getText().toString().trim();




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
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            //progressDialog.dismiss();
                            if (task.isSuccessful()) {
                                // Login with details
                                 firebaseAuth.signInWithEmailAndPassword(email, password);
                                  progressDialog.dismiss();
                                 Toast.makeText(getApplicationContext(), "Welcome!!", Toast.LENGTH_SHORT).show();
                                finish();
                                startActivity(new Intent(AmbulanceDriver.this, AccidentList.class));


                            } else {
                                Toast.makeText(getApplicationContext(), "check your internet connection or try another username", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                             }
                        }
                    });
        }
    }
}