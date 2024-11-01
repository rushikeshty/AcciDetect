package com.example.software2.dapp.EmergencyContact;

import static com.example.software2.dapp.UserActivities.ui.AddContact.AddContactFragment.swipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.software2.dapp.UserActivities.ui.AddContact.AddContactFragment;
import com.example.software2.dapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class Add_EmergencyActivity extends DialogFragment {
    EditText editText1,editText10;
    TextView btn1;
    Toast toast;
    TextView toast_text;
    ContactListAdapter contactListAdapter;
    Typeface toast_font;
    public LayoutInflater inflater;
    View layout;
    DBEmergency db;
    FirebaseAuth firebaseAuth;
    String email="";
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
         View view = inflater.inflate(R.layout.activity_contact_add,container,false);
         firebaseAuth = FirebaseAuth.getInstance();

        final FirebaseUser user = firebaseAuth.getCurrentUser();
        email=user.getEmail();

        toast_font = Typeface.createFromAsset(requireContext().getAssets(), "AvenirNextLTPro-Cn.otf");
        inflater = (LayoutInflater)requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layout = inflater.inflate(R.layout.custom_toast, (ViewGroup) view.findViewById(R.id.toast));
        toast_text = (TextView) layout.findViewById(R.id.tv);
        toast = new Toast(requireContext().getApplicationContext());
        editText1 = (EditText)view.findViewById(R.id.add_name);
        editText10=(EditText)view.findViewById(R.id.add_phone);
        editText10.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        btn1=(TextView)view.findViewById(R.id.text_add);
        db=new DBEmergency(requireContext());
        Typeface custom_font = Typeface.createFromAsset(requireContext().getAssets(), "AvenirNextLTPro-UltLtCn.otf");
        btn1.setTypeface(custom_font, Typeface.BOLD);
        //Toast variables initialisation
        toast_text.setTypeface(toast_font);
        toast.setGravity(Gravity.BOTTOM, 0, 100);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);

        btn1.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View v) {
                if(editText1.getText().toString().length()==0)
                {
                    toast_text.setText("Enter Name");
                    toast.show();
                }
                else if(editText10.toString().length()==0)
                {
                    toast_text.setText("Enter phone number");
                    toast.show();
                }
                else {
                    swipeRefreshLayout.setRefreshing(true);

                    //swipeRefreshLayout.setRefreshing(true);
                    String text = db.addContact(new EmerContact(editText1.getText().toString(),editText10.getText().toString(),email));
                    toast_text.setText(text);
                    toast.show();
                      dismiss();
                    dismissNow();
                    final Handler h=new Handler(Looper.getMainLooper());
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            AddContactFragment.Isrefresh();

                        }
                    },3000);


                    //                    Intent intent = new Intent(requireContext(), AddContactFragment.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    startActivity(intent);
                }
            }
        });
        return view;
    }



}
