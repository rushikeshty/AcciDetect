package com.example.software2.dapp.UserActivities.ui.AddContact;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.software2.dapp.EmergencyContact.Add_EmergencyActivity;
import com.example.software2.dapp.EmergencyContact.ContactListAdapter;
import com.example.software2.dapp.EmergencyContact.DBEmergency;
import com.example.software2.dapp.EmergencyContact.EmerContact;
import com.example.software2.dapp.R;
import com.example.software2.dapp.databinding.AddContactBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class AddContactFragment extends Fragment {
    private AddContactBinding binding;

    @SuppressLint("StaticFieldLeak")
    public static SwipeRefreshLayout swipeRefreshLayout;

    static String email = "";
    Toast toast;
    TextView toast_text;
    Typeface toast_font;
    View layout2;
    Button btn_add;
    public static RecyclerView dataList;
    public static List<EmerContact> contact;
    public static ArrayList<EmerContact> add = new ArrayList<>();
    @SuppressLint("StaticFieldLeak")
    public static DBEmergency db;
    @SuppressLint("StaticFieldLeak")
    public static ContactListAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = AddContactBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        swipeRefreshLayout = root.findViewById(R.id.swiperefresh);

        Typeface.createFromAsset(requireContext().getAssets(), "AvenirNextLTPro-MediumCn.otf");

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();


        final FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            email = user.getEmail();
        }
        toast_font = Typeface.createFromAsset(requireContext().getAssets(), "AvenirNextLTPro-Cn.otf");
        inflater = (LayoutInflater) requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layout2 = inflater.inflate(R.layout.custom_toast, root.findViewById(R.id.toast));
        toast_text = layout2.findViewById(R.id.tv);
        toast = new Toast(requireContext().getApplicationContext());
        btn_add = root.findViewById(R.id.btn_add);
        dataList = root.findViewById(R.id.listView);
        db = new DBEmergency(requireContext());

        dataList.setLayoutManager(new LinearLayoutManager(requireContext()));
        dataList.setItemAnimator(new DefaultItemAnimator());

        toast_text.setTypeface(toast_font);
        toast.setGravity(Gravity.BOTTOM, 0, 100);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout2);

        Typeface.createFromAsset(requireContext().getAssets(), "AvenirNextLTPro-MediumCn.otf");
        contact = db.getContact(email);
        add.addAll(contact);

        adapter = new ContactListAdapter(add, R.layout.emercontact_list_item, requireContext(), email);
        dataList.setAdapter(adapter);


        swipeRefreshLayout.setOnRefreshListener(() -> {
            contact.clear();
            contact = db.getContact(email);
            add.clear();

            add.addAll(contact);

            adapter = new ContactListAdapter(add, R.layout.emercontact_list_item, requireContext(), email);
            dataList.setAdapter(adapter);
            swipeRefreshLayout.setRefreshing(false);

        });

        btn_add.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View v) {
                Add_EmergencyActivity contact = new Add_EmergencyActivity();
                contact.show(requireActivity().getSupportFragmentManager(), "add contact");
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        add.clear();
        binding = null;
    }

    public static void IsRefresh() {
        if (swipeRefreshLayout.isRefreshing()) {
            contact.clear();
            contact = db.getContact(email);
            add.clear();
            add.addAll(contact);
            adapter = new ContactListAdapter(add, R.layout.emercontact_list_item, swipeRefreshLayout.getContext(), email);
            dataList.setAdapter(adapter);
            swipeRefreshLayout.setRefreshing(false);

        }
    }


}