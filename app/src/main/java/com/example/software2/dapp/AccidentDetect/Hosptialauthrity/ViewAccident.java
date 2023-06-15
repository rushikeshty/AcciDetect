package com.example.software2.dapp.AccidentDetect.Hosptialauthrity;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.software2.dapp.MyAccount;
import com.example.software2.dapp.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ViewAccident extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    EditText Accelerometer,sesorread,locationuser,hospitaladdress,status,datetime,emergencycontact;
    public Spinner spinner;
    TextView textView;
    TextView decibelss2;
    static String item;
    Button updatebtn,userdetails;
     private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_accident);
        Accelerometer = findViewById(R.id.Accelerometer);
          databaseReference = FirebaseDatabase.getInstance().getReference();

        sesorread = findViewById(R.id.sensorread);
        locationuser = findViewById(R.id.locationuser);
        hospitaladdress = findViewById(R.id.hospitaladdress);
        status = findViewById(R.id.status);
        @SuppressLint("ResourceType")
        AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(getApplicationContext(),
                R.anim.property_animator);
        set.setTarget(status);
        set.start();

        updatebtn = findViewById(R.id.directionbtn);
        userdetails = findViewById(R.id.userdetails);
        //healthcondition = findViewById(R.id.healthcondition);
        datetime = findViewById(R.id.datetime);
        emergencycontact = findViewById(R.id.emergencycontact);
         Intent i = getIntent();
        String speed = i.getStringExtra("speed");
        Accelerometer.setText(speed);
        String sensor = i.getStringExtra("sensor");
        sesorread.setText(sensor);
        String location = i.getStringExtra("location");
        locationuser.setText(location);
        String hospital = i.getStringExtra("hospital");
        hospitaladdress.setText(hospital);
        String status1 = i.getStringExtra("status");
        status.setText(status1);
        String datetime1 =i.getStringExtra("datetime");
        datetime.setText(datetime1);
        String emerg=i.getStringExtra("emergencycontact");
        emergencycontact.setText(emerg);
        String decibel = i.getStringExtra("decibel");
        decibelss2 = findViewById(R.id.decibelss2);
        decibelss2.setText(decibel);

        String userid = i.getStringExtra("userid");

        spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);

        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();
        categories.add("select user status");
        categories.add("AMBULANCE ALLOTTED");
        categories.add("USER PICKED");
        categories.add("USER DROPPED AT HOSPITAL");
        categories.add("USER ADMITTED AT HOSPITAL");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        updatebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(item.contains("select")){
                    Toast.makeText(getApplicationContext(), "Please select the status", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Details updated successfully.", Toast.LENGTH_SHORT).show();
                    databaseReference.child("user").child(userid).child("AccidentInfo").child("status").setValue(item);


                }
            }
        });
        userdetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyAccount myAccount = new MyAccount(ViewAccident.this,userid);
                myAccount.show();
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
         item = parent.getItemAtPosition(position).toString();
         // Showing selected spinner item
        Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
    }
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generted method stub

    }

}