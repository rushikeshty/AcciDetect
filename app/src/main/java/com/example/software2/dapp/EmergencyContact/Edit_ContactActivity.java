package com.example.software2.dapp.EmergencyContact;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.software2.dapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Edit_ContactActivity extends Dialog {
    EditText editText1,editText2;
    String name,phone;
    int cont;
    TextView btn1;
    Toast toast;
    TextView toast_text;
    Typeface toast_font;
    LayoutInflater inflater;
    View layout;
    DBEmergency db;
    private FirebaseAuth firebaseAuth;
    String email="";
    Bundle bundle;

    public Edit_ContactActivity(@NonNull Context context,Bundle b) {
        super(context);
        this.bundle = b;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_edit);

        firebaseAuth = FirebaseAuth.getInstance();

        final FirebaseUser user = firebaseAuth.getCurrentUser();
        email=user.getEmail();


        name=bundle.getString("Name");
        phone=bundle.getString("Phone");
        cont=bundle.getInt("ContactIndex")+1;

        toast_font = Typeface.createFromAsset(getContext().getAssets(), "AvenirNextLTPro-Cn.otf");
        inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layout = inflater.inflate(R.layout.custom_toast, (ViewGroup) this.findViewById(R.id.toast));
        toast_text = (TextView) layout.findViewById(R.id.tv);
        toast = new Toast(getContext().getApplicationContext());
        editText1 = (EditText)findViewById(R.id.edit_name);
        editText2 = (EditText)findViewById(R.id.edit_phone);
        editText2.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        btn1=(TextView)findViewById(R.id.text_edit);
        db=new DBEmergency(getContext());



        Typeface custom_font = Typeface.createFromAsset(getContext().getAssets(), "AvenirNextLTPro-UltLtCn.otf");
        editText1.setTypeface(custom_font);
        editText2.setTypeface(custom_font);
        btn1.setTypeface(custom_font, Typeface.BOLD);

        editText1.setText(name);
        editText2.setText(phone);

        //Toast variables initialisation
        toast_text.setTypeface(toast_font);
        toast.setGravity(Gravity.BOTTOM, 0, 100);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);




        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editText1.getText().toString().length()==0 || editText2.getText().toString().length()==0)
                {
                    toast_text.setText("Enter Details");
                    toast.show();
                }
                else if(editText1.getText().toString().contains("=")
                        || editText2.getText().toString().contains("=")
                        || editText1.getText().toString().contains("+")
                        || editText2.getText().toString().contains("+")
                        )
                {
                    toast_text.setText("Kindly do not put any special characters like =.,");
                    toast.show();
                }
                else{
                    String s="'Name="+editText1.getText().toString()+" Phone="+editText2.getText().toString()+"'";
                    String text=db.updatecontact("cont" + cont, s,email);
                    toast_text.setText(text);
                    toast.show();
                    dismiss();;

                }

            }
        });
    }

}
