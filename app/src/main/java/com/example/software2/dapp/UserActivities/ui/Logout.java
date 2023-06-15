package com.example.software2.dapp.UserActivities.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.software2.dapp.LoginSignup.LoginScreenActivity;
import com.example.software2.dapp.databinding.FragmentLogoutBinding;
import com.google.firebase.auth.FirebaseAuth;

public class Logout extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FragmentLogoutBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentLogoutBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        firebaseAuth = FirebaseAuth.getInstance();

        new AlertDialog.Builder(requireContext())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Closing Activity")
                .setMessage("Are you sure you want to close this activity?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (firebaseAuth.getCurrentUser() != null) {
                            FirebaseAuth.getInstance().signOut();
                            
                            requireActivity().finish();
                            startActivity(new Intent(requireContext(), LoginScreenActivity.class));
                        }
                    }
                })
                .setNegativeButton("No", null)
                .show();


        return root;
    }
}