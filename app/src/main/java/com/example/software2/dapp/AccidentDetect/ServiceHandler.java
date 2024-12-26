package com.example.software2.dapp.AccidentDetect;

import static com.example.software2.dapp.AccidentDetect.SensorService.notificationOneTime;
import static com.example.software2.dapp.UserActivities.ui.home.HomeFragment.stop;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

public class ServiceHandler {

    private SensorService mLocalService;
    public static boolean isBound = false;

    private final Context mContext;

    public ServiceHandler(Context context) {
        this.mContext = context;
        mLocalService = new SensorService();
    }

    public ServiceConnection myConnection = new ServiceConnection() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            SensorService.LocalBinder mLocalBinder = (SensorService.LocalBinder) iBinder;
            mLocalService = mLocalBinder.getService();
            Intent i = new Intent(mContext, SensorService.class);
            mLocalService.onBind(i);
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            notificationOneTime = 0;
            isBound = false;
            Intent i = new Intent(mContext, SensorService.class);
            mLocalService.onUnbind(i);
            mLocalService.stopService(i);
            mLocalService.onDestroy();
            mLocalService = null;
            stop();
            Toast.makeText(mContext.getApplicationContext(), "service disconnected", Toast.LENGTH_SHORT).show();

        }

    };

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void doBindService() {
        isBound = true;
        Intent intent = new Intent(mContext, SensorService.class);
        mContext.bindService(intent, myConnection, Context.BIND_AUTO_CREATE);

    }

    public void doUnbindService() {
        isBound = false;
        mContext.unbindService(myConnection);

        mContext.stopService(new Intent(mContext, SensorService.class));
        mLocalService.onDestroy();
    }

    public static boolean isBound() {
        return isBound;
    }

}
