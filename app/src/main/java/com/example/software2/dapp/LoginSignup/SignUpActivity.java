package com.example.software2.dapp.LoginSignup;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.software2.dapp.UserActivities.MainActivity;
import com.example.software2.dapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword,bloodgroup;
    private Button buttonRegister;
    private Toast toast;
    private TextView toast_text;
    private Typeface toast_font;
    private LayoutInflater inflater;
    private View layout;
    private TextView textViewTitle;

    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private String firstName, lastName, phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registeractivity);

        Bundle mBundle = getIntent().getExtras();
        firstName = mBundle.getString("firstName");
        lastName = mBundle.getString("lastName");
        phoneNumber = mBundle.getString("phoneNumber");

        //policyNumber = mBundle.getString("policyNumber");

        //Custom Toast
        toast_font = Typeface.createFromAsset(getAssets(), "AvenirNextLTPro-Cn.otf");
        inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layout = inflater.inflate(R.layout.custom_toast, (ViewGroup) this.findViewById(R.id.toast));
        toast_text = (TextView) layout.findViewById(R.id.tv);
        toast = new Toast(this.getApplicationContext());
        toast_text.setTypeface(toast_font);
        toast.setGravity(Gravity.BOTTOM, 0, 100);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);

        //Initialisation of all the components
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        buttonRegister = (Button) findViewById(R.id.btnRegister);
        textViewTitle = (TextView) findViewById(R.id.textViewTitle);
        bloodgroup = findViewById(R.id.bloodgroup);
        //Changing font of all layout components
         editTextEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editTextEmail.getText().toString().contains("@")){
                    toast_text.setText("username should not contain @ ");
                    editTextEmail.setText("");
                }
            }
        });
        progressDialog = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public void register(View view) {
        if (editTextEmail.getText() != null && editTextPassword.getText() != null) {

            final String email = editTextEmail.getText().toString().trim() + "@user.com";
            final String password = editTextPassword.getText().toString().trim();

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                toast_text.setText("Invalid Username,Try again");
                toast.show();
                return;
            }

            if (password.length() < 8) {
                toast_text.setText("Password must be of 8 characters");
                toast.show();
                return;
            }

            if (TextUtils.isEmpty(email)) {
                toast_text.setText("No Username Entered");
                toast.show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                toast_text.setText("No Password Entered");
                toast.show();
                return;
            }

            if (TextUtils.isEmpty(bloodgroup.getText().toString())) {
                toast_text.setText("Please enter blood group");
                toast.show();
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
                                UserInformation userInformation = new UserInformation(firstName, lastName, phoneNumber, bloodgroup.getText().toString());
                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                databaseReference.child("user").child(user.getUid()).child("personal info").setValue(userInformation);
                                progressDialog.dismiss();
                                toast_text.setText("Welcome!!");
                                toast.show();
                                startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                                finish();

                            } else {
                                progressDialog.dismiss();
                                toast_text.setText("check your internet connection or try another username");
                                toast.show();
                            }
                        }
                    });
        }
    }
}
