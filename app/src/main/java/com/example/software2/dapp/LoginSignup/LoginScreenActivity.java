package com.example.software2.dapp.LoginSignup;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.software2.dapp.AccidentDetect.HospitalAssigned;
import com.example.software2.dapp.AccidentDetect.Hosptialauthrity.AccidentList;
import com.example.software2.dapp.UserActivities.MainActivity;
import com.example.software2.dapp.EmergencyContact.DBEmergency;
import com.example.software2.dapp.PermissionHandler;
import com.example.software2.dapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class LoginScreenActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    Button btnLogin;
    Toast toast;
    TextView toast_text;
    Typeface toast_font;
    LayoutInflater inflater;
    View layout;
    private TextView textViewRegister;
    private ProgressDialog progressDialog;
    public FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    DBEmergency db;
    private final int MY_PERMISSION_REQUEST_CODE = 1;
    private PermissionHandler mPermissionHandler;

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginactivity);

        //Custom Toast
        mPermissionHandler = new PermissionHandler(LoginScreenActivity.this);
        List<String> permissionName = new ArrayList<>();
        List<String> permissionTag = new ArrayList<>();
        permissionName.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissionName.add(Manifest.permission.READ_PHONE_STATE);
        permissionName.add(Manifest.permission.SEND_SMS);
        permissionName.add(Manifest.permission.RECORD_AUDIO);

        permissionTag.add("Access Location");
        permissionTag.add("Read Phone State");
        permissionTag.add("Send SMS");
        permissionTag.add("Record audio");


        if (!mPermissionHandler.requestPermissions(MY_PERMISSION_REQUEST_CODE, permissionName, permissionTag)
                || !locationServicesStatusCheck()) {
            Toast.makeText(getApplicationContext(), "permission granted", Toast.LENGTH_SHORT).show();
        }


        toast_font = Typeface.createFromAsset(getAssets(), "AvenirNextLTPro-Cn.otf");
        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layout = inflater.inflate(R.layout.custom_toast, (ViewGroup) this.findViewById(R.id.toast));
        toast_text = (TextView) layout.findViewById(R.id.tv);
        toast = new Toast(this.getApplicationContext());
        toast_text.setTypeface(toast_font);
        toast.setGravity(Gravity.BOTTOM, 0, 100);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);

        //Initialisation of all the components
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        btnLogin = findViewById(R.id.btnLogin);
        textViewRegister = findViewById(R.id.textViewRegister);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        @SuppressLint("ResourceType") AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(getApplicationContext(),
                R.anim.anmation2);
        set.setTarget(textViewRegister);
        set.start();

        //Changing font of all layout components
        //        textViewRegister.setTypeface(custom_font);
        progressDialog = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        btnLogin.setOnClickListener(view -> {
            String user1 = editTextEmail.getText().toString().trim() + "@user.com";
            String hospital = editTextEmail.getText().toString().trim() + "@hospital.com";
            String ambulance = editTextEmail.getText().toString().trim() + "@ambulance.com";
            String password = editTextPassword.getText().toString().trim();

            if (TextUtils.isEmpty(user1)) {
                toast_text.setText("No username Entered");
                toast.show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                toast_text.setText("No Password Entered");
                toast.show();
                return;
            }

            progressDialog.setMessage("Logging In...");
            progressDialog.show();

            firebaseAuth.signInWithEmailAndPassword(user1, password)
                    .addOnCompleteListener(LoginScreenActivity.this, new OnCompleteListener<AuthResult>() {

                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressDialog.dismiss();
                            if (task.isSuccessful()) {
                                // Start Dashboard Activity
                                toast_text.setText("Logged In!");
                                toast.show();
                                finish();
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            } else if (!task.isSuccessful()) {
                                firebaseAuth.signInWithEmailAndPassword(hospital, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            finish();
                                            startActivity(new Intent(getApplicationContext(), HospitalAssigned.class));
                                        } else if (!task.isSuccessful()) {
                                            firebaseAuth.signInWithEmailAndPassword(ambulance, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                    if (task.isSuccessful()) {
                                                        finish();
                                                        startActivity(new Intent(getApplicationContext(), AccidentList.class));
                                                    } else {
                                                        toast_text.setText("Incorrect Credentials or No Network.");
                                                        toast.show();
                                                    }

                                                }
                                            });
                                        }

                                    }
                                });
                            }

                        }
                    });

        });


        if (user != null && user.getEmail().contains("hospital")) {
            // Start Dashboard Activity
            finish();
            startActivity(new Intent(this, HospitalAssigned.class));

        } else if (user != null && user.getEmail().contains("user")) {
            // Start Dashboard Activity
            finish();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));

        } else if (user != null && user.getEmail().contains("ambulance")) {
            finish();
            startActivity(new Intent(getApplicationContext(), AccidentList.class));
        }

        editTextEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editTextEmail.getText().toString().contains("@")) {
                    toast_text.setText("username should not contain @ ");
                    editTextEmail.setText("");
                    toast.show();
                }
            }
        });

    }


    public void goToRegister(View view) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(LoginScreenActivity.this);

        // set the custom icon to the alert dialog
        alertDialog.setIcon(R.drawable.alarm);

        // title of the alert dialog
        alertDialog.setTitle("Register as a");

        // list of the items to be displayed to the user in the
        // form of list so that user can select the item from
        final String[] listItems = new String[]{"As a User", "As a Hospital authority", "As a Ambulance driver"};
        final int[] checkedItem = {-1};
        alertDialog.setSingleChoiceItems(listItems, checkedItem[0], (dialog, which) -> {
            // update the selected item which is selected by the user so that it should be selected
            // when user opens the dialog next time and pass the instance to setSingleChoiceItems method
            checkedItem[0] = which;
            switch (listItems[which]) {
                case "As a User":
                    startActivity(new Intent(this, PersonalInfoActivity.class));
                    break;
                case "As a Hospital authority":
                    startActivity(new Intent(this, HospitalAuthority.class));
                    break;
                case "As a Ambulance driver":
                    startActivity(new Intent(this, AmbulanceDriver.class));
                    ;
                    break;

            }
            dialog.dismiss();
        });

        // set the negative button if the user is not interested to select or change already selected item
        alertDialog.setNegativeButton("Cancel", (dialog, which) -> {

        });

        // create and build the AlertDialog instance with the AlertDialog builder instance
        AlertDialog customAlertDialog = alertDialog.create();

        // show the alert dialog when the button is clicked
        customAlertDialog.show();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (mPermissionHandler.handleRequestResult(requestCode, permissions, grantResults)) {
                        Toast.makeText(getApplicationContext(), "Permission granted", Toast.LENGTH_SHORT).show();

                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean locationServicesStatusCheck() {
        final LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) return true;

        AlertDialog.Builder builder = new AlertDialog.Builder(LoginScreenActivity.this);
        builder.setTitle("Enable GPS")
                .setMessage("This function needs your GPS, do you want to enable it now?")
                .setIcon(R.drawable.ic_launcher_background)
                .setCancelable(false)
                .setPositiveButton("Yes", (dialogInterface, i) -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)));
        AlertDialog dialog = builder.create();
        dialog.show();


        return false;
    }

}







