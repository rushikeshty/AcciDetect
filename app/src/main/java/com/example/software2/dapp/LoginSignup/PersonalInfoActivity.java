package com.example.software2.dapp.LoginSignup;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
 import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.software2.dapp.R;

public class PersonalInfoActivity extends AppCompatActivity {

    private EditText editTextFirstName, editTextLastName, editTextPhoneNumber;
    private Button btnPersonal;
    private Toast toast;
    private TextView toast_text;
    private Typeface toast_font;
    private LayoutInflater inflater;
    private View layout;
    private TextView textViewTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);

        //Custom Toast
        toast_font = Typeface.createFromAsset(getAssets(), "AvenirNextLTPro-Cn.otf");
        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layout = inflater.inflate(R.layout.custom_toast, (ViewGroup) this.findViewById(R.id.toast));
        toast_text = (TextView) layout.findViewById(R.id.tv);
        toast = new Toast(this.getApplicationContext());
        toast_text.setTypeface(toast_font);
        toast.setGravity(Gravity.BOTTOM, 0, 100);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);

        editTextFirstName = (EditText) findViewById(R.id.editTextFirstName);
        editTextLastName = (EditText) findViewById(R.id.editTextLastName);
        editTextPhoneNumber = (EditText) findViewById(R.id.editTextPhoneNumber);
        btnPersonal = (Button) findViewById(R.id.btnPersonal);
        textViewTitle = (TextView) findViewById(R.id.textViewTitle);
        editTextPhoneNumber.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

    }

    public void goToHome(View view)
    {
        String firstName = editTextFirstName.getText().toString().trim();
        String lastName = editTextLastName.getText().toString().trim();
         String phoneNumber = editTextPhoneNumber.getText().toString().trim();

        if (TextUtils.isEmpty(firstName)) {
            Toast.makeText(this, "Please enter your first name", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (TextUtils.isEmpty(lastName)) {
            Toast.makeText(this, "Please enter your last name", Toast.LENGTH_SHORT).show();
            return;
        }

        else if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Go To Home", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, SignUpActivity.class);
        Bundle mBundle = new Bundle();
        mBundle.putString("firstName", firstName);
        mBundle.putString("lastName", lastName);
         mBundle.putString("phoneNumber", phoneNumber);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtras(mBundle);
        finish();
        startActivity(intent);
    }
}
