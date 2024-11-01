package com.example.software2.dapp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.software2.dapp.LoginSignup.UserInformation;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyAccount extends Dialog {

    // private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;
    private CharSequence mTitle;
    private TextView editFirstName, editLastName, editPhoneNumber, bloodtype;
    private Button btnSave;
    int[] login_icons = new int[]{
            R.drawable.carpool_32,
            R.drawable.myaccount,
            R.drawable.policy1,
            R.drawable.logout,
    };
    private Typeface custom_font;
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
        custom_font = Typeface.createFromAsset(context.getAssets(), "AvenirNextLTPro-MediumCn.otf");

        databaseReference = FirebaseDatabase.getInstance().getReference();

        toast_font = Typeface.createFromAsset(context.getAssets(), "AvenirNextLTPro-Cn.otf");
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layout2 = inflater.inflate(R.layout.custom_toast, (ViewGroup) this.findViewById(R.id.toast));
        toast_text = (TextView) layout2.findViewById(R.id.tv);
        toast = new Toast(context.getApplicationContext());

        //Toast variables initialisation
        toast_text.setTypeface(toast_font);
        toast.setGravity(Gravity.BOTTOM, 0, 100);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout2);

        editFirstName = findViewById(R.id.editFirstName);
        editLastName = findViewById(R.id.editLastName);
        editPhoneNumber = findViewById(R.id.editPhoneNumber);
        closeButton = findViewById(R.id.closeButton);
        btnSave = (Button) findViewById(R.id.btnSave);
        textTitle = (TextView) findViewById(R.id.textTitle);
        LinearProgressIndicator linearProgressIndicator = findViewById(R.id.progress_horizontal);
        linearProgressIndicator.show();
        editPhoneNumber.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        //Changing font of all layout components
        Typeface custom_font = Typeface.createFromAsset(context.getAssets(), "AvenirNextLTPro-UltLtCn.otf");
        btnSave.setTypeface(custom_font, Typeface.BOLD);
        bloodtype = findViewById(R.id.bloodtype);
        closeButton.setOnClickListener(view -> {
            this.dismiss();
        });

        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Fetching Data...");
        progressDialog.show();
        databaseReference.child("user").child(userid).child("personal info").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> values = new ArrayList<String>(4);
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    values.add(child.getValue().toString());
                }

                if (!values.isEmpty()) {
                    editFirstName.setEnabled(false);
                    editLastName.setEnabled(false);
                    editPhoneNumber.setEnabled(false);
                    bloodtype.setEnabled(false);
                    editFirstName.setText(values.get(1));
                    editLastName.setText(values.get(2));
                    editPhoneNumber.setText(values.get(3));
                    bloodtype.setText(values.get(0));
                    //   policyNumber = values.get(3);
                }
                linearProgressIndicator.setVisibility(View.GONE);
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, "Could not retrieve data.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void goToSave(View view) {
        String firstName = editFirstName.getText().toString().trim();
        String lastName = editLastName.getText().toString().trim();
        String phoneNumber = editPhoneNumber.getText().toString().trim();

        if (TextUtils.isEmpty(firstName)) {
            Toast.makeText(context, "Please enter your first name", Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(lastName)) {
            Toast.makeText(context, "Please enter your last name", Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(context, "Please enter your phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        UserInformation userInformation = new UserInformation(firstName, lastName, phoneNumber, "user");


        progressDialog.setMessage("Saving...");
        progressDialog.show();

        databaseReference.child(userid).setValue(userInformation);

        progressDialog.dismiss();
        Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

}
