package com.example.software2.dapp.UserActivities.ui.MyAccount;

import android.app.ProgressDialog;
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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.software2.dapp.LoginSignup.LoginScreenActivity;
import com.example.software2.dapp.LoginSignup.UserInformation;
import com.example.software2.dapp.R;
import com.example.software2.dapp.databinding.MyAccountBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MyAccountFragment extends Fragment {

     private MyAccountBinding binding;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;
    private String policyNumber;
     private EditText editFirstName, editLastName, editPhoneNumber,bloodtype;
    private Button btnSave;
     HashMap<String, List<String>> listDataChild;
     private Typeface custom_font;
    Toast toast;
    TextView toast_text, textTitle;
    Typeface toast_font;
    LayoutInflater inflater;
    View layout2;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = MyAccountBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        custom_font = Typeface.createFromAsset(requireContext().getAssets(), "AvenirNextLTPro-MediumCn.otf");

        firebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        if (user == null) {
            requireActivity().finish();
            startActivity(new Intent(requireContext(), LoginScreenActivity.class));
        }
        toast_font = Typeface.createFromAsset(requireContext().getAssets(), "AvenirNextLTPro-Cn.otf");
        inflater = (LayoutInflater) requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layout2 = inflater.inflate(R.layout.custom_toast, (ViewGroup)root.findViewById(R.id.toast));
        toast_text = (TextView) layout2.findViewById(R.id.tv);
        toast = new Toast(requireContext().getApplicationContext());

        //Toast variables initialisation
        toast_text.setTypeface(toast_font);
        toast.setGravity(Gravity.BOTTOM, 0, 100);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout2);
        bloodtype =(EditText) root.findViewById(R.id.bloodtype);

        editFirstName = (EditText) root.findViewById(R.id.editFirstName);
        editLastName = (EditText) root.findViewById(R.id.editLastName);
        editPhoneNumber = (EditText) root.findViewById(R.id.editPhoneNumber);
        btnSave = (Button) root.findViewById(R.id.btnSave);
        textTitle = (TextView) root.findViewById(R.id.textTitle);
        editPhoneNumber.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        //Changing font of all layout components
        Typeface custom_font = Typeface.createFromAsset(requireContext().getAssets(), "AvenirNextLTPro-UltLtCn.otf");
//        editFirstName.setTypeface(custom_font);
//        editLastName.setTypeface(custom_font);
//        editPhoneNumber.setTypeface(custom_font);
        //btnSave.setTypeface(custom_font, Typeface.BOLD);
        //textTitle.setTypeface(custom_font, Typeface.BOLD);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String firstName = editFirstName.getText().toString().trim();
                String lastName = editLastName.getText().toString().trim();
                String phoneNumber = editPhoneNumber.getText().toString().trim();
                String bloodtyp = bloodtype.getText().toString();

                if (TextUtils.isEmpty(firstName)) {
                    Toast.makeText(requireContext(), "Please enter your first name", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(lastName)) {
                    Toast.makeText(requireContext(), "Please enter your last name", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(phoneNumber)) {
                    Toast.makeText(requireContext(), "Please enter your phone number", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (TextUtils.isEmpty(bloodtyp)) {
                    Toast.makeText(requireContext(), "Please enter your Blood group", Toast.LENGTH_SHORT).show();
                    return;
                }

                UserInformation userInformation = new UserInformation(firstName, lastName, phoneNumber, bloodtyp);

                FirebaseUser user = firebaseAuth.getCurrentUser();

                progressDialog.setMessage("Saving...");
                progressDialog.show();
                if (user != null) {
                    databaseReference.child("user").child(user.getUid()).child("personal info").setValue(userInformation);
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show();

                }
            }
        });
        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Fetching Data...");
        progressDialog.show();
        databaseReference.child("user").child(user.getUid()).child("personal info").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> values = new ArrayList<String>(4);
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    values.add(child.getValue().toString());
                }

                if (!values.isEmpty()) {
                    editFirstName.setText(values.get(1));
                    editLastName.setText(values.get(2));
                    editPhoneNumber.setText(values.get(3));
                    bloodtype.setText(values.get(0));
                }

                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(requireContext(), "Could not retrieve data.", Toast.LENGTH_SHORT).show();
            }
        });


        final TextView textView = binding.textSlideshow;
        textView.setText("This is slideshow");
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }




}