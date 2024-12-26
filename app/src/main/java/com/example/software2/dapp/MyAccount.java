package com.example.software2.dapp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class MyAccount extends Dialog {

    private ProgressDialog progressDialog;
    private TextView editFirstName, editLastName, editPhoneNumber, bloodType;
    String userid;
    Toast toast;
    TextView toast_text, textTitle;
    Typeface toast_font;
    LayoutInflater inflater;
    View layout2;
    Context context;
    ImageView closeButton;

    public MyAccount(@NonNull Context context, String userid) {
        super(context);
        this.context = context;
        this.userid = userid;
    }


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myaccount);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        toast_font = Typeface.createFromAsset(context.getAssets(), "AvenirNextLTPro-Cn.otf");
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layout2 = inflater.inflate(R.layout.custom_toast, this.findViewById(R.id.toast));
        toast_text = layout2.findViewById(R.id.tv);
        toast = new Toast(context.getApplicationContext());

        toast_text.setTypeface(toast_font);
        toast.setGravity(Gravity.BOTTOM, 0, 100);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout2);

        editFirstName = findViewById(R.id.editFirstName);
        editLastName = findViewById(R.id.editLastName);
        editPhoneNumber = findViewById(R.id.editPhoneNumber);
        closeButton = findViewById(R.id.closeButton);
        Button btnSave = findViewById(R.id.btnSave);
        textTitle = findViewById(R.id.textTitle);
        LinearProgressIndicator linearProgressIndicator = findViewById(R.id.progress_horizontal);
        linearProgressIndicator.show();
        editPhoneNumber.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        Typeface custom_font = Typeface.createFromAsset(context.getAssets(), "AvenirNextLTPro-UltLtCn.otf");
        btnSave.setTypeface(custom_font, Typeface.BOLD);
        bloodType = findViewById(R.id.bloodtype);
        closeButton.setOnClickListener(view -> this.dismiss());

        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Fetching Data...");
        progressDialog.show();
        databaseReference.child("user").child(userid).child("personal info").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> values = new ArrayList<>(4);
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    values.add(Objects.requireNonNull(child.getValue()).toString());
                }

                if (!values.isEmpty()) {
                    editFirstName.setEnabled(false);
                    editLastName.setEnabled(false);
                    editPhoneNumber.setEnabled(false);
                    bloodType.setEnabled(false);
                    editFirstName.setText(values.get(1));
                    editLastName.setText(values.get(2));
                    editPhoneNumber.setText(values.get(3));
                    bloodType.setText(values.get(0));
                }
                linearProgressIndicator.setVisibility(View.GONE);
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, "Could not retrieve data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(@NonNull Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

}
