package com.example.software2.dapp.AccidentDetect.Hosptialauthrity;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.software2.dapp.MyAccount;
import com.example.software2.dapp.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ViewAccident extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    private static final String TAG = ViewAccident.class.toString();
    EditText Accelerometer, sensorReading, userLocation, hospitalAddress, status, datetime, emergencyContact;
    public Spinner spinner;
    TextView decibels;
    static String item;
    Button updateBtn, userDetails;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_accident);
        Accelerometer = findViewById(R.id.Accelerometer);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        sensorReading = findViewById(R.id.sensorread);
        userLocation = findViewById(R.id.locationuser);
        hospitalAddress = findViewById(R.id.hospitaladdress);
        status = findViewById(R.id.status);

        @SuppressLint("ResourceType") AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(getApplicationContext(),
                R.anim.property_animator);
        set.setTarget(status);
        set.start();

        updateBtn = findViewById(R.id.directionbtn);
        userDetails = findViewById(R.id.userdetails);
        datetime = findViewById(R.id.datetime);
        emergencyContact = findViewById(R.id.emergencycontact);
        Intent i = getIntent();
        String speed = i.getStringExtra("speed");
        Accelerometer.setText(speed);
        String sensor = i.getStringExtra("sensor");
        sensorReading.setText(sensor);
        String location = i.getStringExtra("location");
        userLocation.setText(location);
        String hospital = i.getStringExtra("hospital");
        hospitalAddress.setText(hospital);
        String status1 = i.getStringExtra("status");
        status.setText(status1);
        String datetime1 = i.getStringExtra("datetime");
        datetime.setText(datetime1);
        String emerg = i.getStringExtra("emergencycontact");
        emergencyContact.setText(emerg);
        String decibel = i.getStringExtra("decibel");
        decibels = findViewById(R.id.decibelss2);
        decibels.setText(decibel);

        String userid = i.getStringExtra("userid");

        spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);

        // Spinner Drop down elements
        ArrayAdapter<String> dataAdapter = getStringArrayAdapter();
        spinner.setAdapter(dataAdapter);
        updateBtn.setOnClickListener(view -> {
            if (item.contains("select")) {
                Toast.makeText(getApplicationContext(), "Please select the status", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Details updated successfully.", Toast.LENGTH_SHORT).show();
                if (userid != null) {
                    databaseReference.child("user").child(userid).child("AccidentInfo").child("status").setValue(item);
                }
            }
        });
        userDetails.setOnClickListener(view -> {
            MyAccount myAccount = new MyAccount(ViewAccident.this, userid);
            myAccount.show();
        });
    }

    @NonNull
    private ArrayAdapter<String> getStringArrayAdapter() {
        List<String> categories = new ArrayList<>();
        categories.add("select user status");
        categories.add("AMBULANCE ALLOTTED");
        categories.add("USER PICKED");
        categories.add("USER DROPPED AT HOSPITAL");
        categories.add("USER ADMITTED AT HOSPITAL");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return dataAdapter;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
         item = parent.getItemAtPosition(position).toString();
        Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
    }
    public void onNothingSelected(AdapterView<?> arg0) {
        Log.i(TAG,"Nothing selected ");
    }

}