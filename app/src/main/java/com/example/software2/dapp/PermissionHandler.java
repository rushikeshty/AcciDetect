package com.example.software2.dapp;
import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionHandler {

    public Activity mActivity;

    public PermissionHandler(Activity activity) {
        mActivity = activity;
    }

    public boolean requestPermissions(int code, @NonNull List<String> permissions, List<String> rationale) {
        final int MY_PERMISSION_REQUEST_CODE = code;

        List<String> permissionsNeeded = new ArrayList<>();
        final List<String> permissionList = new ArrayList<>();

        for (int ii = 0; ii < permissions.size(); ++ii) {
            if (!addPermission(permissionList, permissions.get(ii))) permissionsNeeded.add(rationale.get(ii));
        }

        if (permissionList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                // Need Rationale
                String message = "You need to grant access to " + permissionsNeeded.get(0);
                for (int ii = 1; ii < permissionsNeeded.size(); ii++) {
                    message = message + ", " + permissionsNeeded.get(ii);
                }

                showRationaleMessage("App needs access to Location " + message, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(mActivity, permissionList.toArray(new String[permissionList.size()]), MY_PERMISSION_REQUEST_CODE);
                    }
                });
                return false;
            }
            ActivityCompat.requestPermissions(mActivity, permissionList.toArray(new String[permissionList.size()]), MY_PERMISSION_REQUEST_CODE);
            return false;
        }

        return true;
    }

    // Function to show dialog of the rationale for the permission
    private void showRationaleMessage(String message, DialogInterface.OnClickListener okListtener) {
        new AlertDialog.Builder(mActivity)
                .setMessage(message)
                .setPositiveButton("OK", okListtener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private boolean addPermission(List<String> permissionList, String permission) {
        if (ContextCompat.checkSelfPermission(mActivity, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(permission);
            if (!ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permission))
                return false;
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public boolean handleRequestResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Map<String, Integer> perms = new HashMap<>();

        perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
        perms.put(Manifest.permission.READ_PHONE_STATE, PackageManager.PERMISSION_GRANTED);
        perms.put(Manifest.permission.SEND_SMS, PackageManager.PERMISSION_GRANTED);
        perms.put(Manifest.permission.RECORD_AUDIO,PackageManager.PERMISSION_GRANTED);
 //        permissionTag.add("Capture audio output");

        for (int ii = 0; ii < permissions.length; ii++) {
            perms.put(permissions[ii], grantResults[ii]);
        }

        // Check for permissions granted
        if (perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && perms.get(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                && perms.get(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
                 && perms.get(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED


        )
        {
            // All permissions granted
            return true;
        } else {
            Toast.makeText(mActivity, "You Suck.", Toast.LENGTH_SHORT).show();
            Toast.makeText(mActivity, "Just Kidding. LOL...", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
