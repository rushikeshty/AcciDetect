package com.example.software2.dapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("StaticFieldLeak")
public class CustomToastActivity {

    private static Toast mToast;

    private static TextView textViewToast;

    private static Activity mActivity;

    public static void CustomToast(Activity activity) {
        mActivity = activity;

        mToast = new Toast(mActivity);

        setLayoutAttributes();
    }

    private static void setLayoutAttributes() {
        // Setup Layout
        LayoutInflater mLayoutInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = mLayoutInflater.inflate(R.layout.custom_toast, mActivity.findViewById(R.id.toast));

        textViewToast = layout.findViewById(R.id.tv);
        Typeface fontToast = Typeface.createFromAsset(mActivity.getAssets(), "AvenirNextLTPro-Cn.otf");
        textViewToast.setTypeface(fontToast);
        mToast.setGravity(Gravity.BOTTOM, 0, 100);
        mToast.setDuration(Toast.LENGTH_LONG);
        mToast.setView(layout);
    }

    public static void showCustomToast(String text) {
        textViewToast.setText(text);
        mToast.show();
    }
}
