package com.example.software2.dapp.AccidentDetect;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class GPSHandler {
    private static final long MIN_TIME = 0;
    private static final float MIN_DISTANCE = 0;
    private static final int MAX_RESULTS = 1;
    public static float speed;
    private Geocoder mGeocoder;
    private String currentAddress;
    private List<String> hospitalAddresses;

    static double latitude, longitude;

    public String getCurrentAddress() {
        return currentAddress;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }


    public List<String> getHospitalAddress() {
        return hospitalAddresses;
    }

    public GPSHandler(Context context) {
        LocationManager mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationListener mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                Log.d("debug", "location changed");
                findCurrentAddress(location);
                findHospitalAddress(location);
                speed = location.getSpeed();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }

            @Override
            public void onProviderEnabled(@NonNull String s) {
            }

            @Override
            public void onProviderDisabled(@NonNull String s) {
            }
        };
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, mLocationListener);
        mGeocoder = new Geocoder(context);

        hospitalAddresses = new ArrayList<>();
    }

    public float getSpeed() {
        return speed;
    }

    private void findCurrentAddress(Location location) {
        List<Address> addresses = null;
        try {
            addresses = mGeocoder.getFromLocation(location.getLatitude(), location.getLongitude(), MAX_RESULTS);
            latitude = location.getLatitude();
            longitude = location.getLongitude();


        } catch (IOException ignored) {
        }

        currentAddress = "";
        if (addresses != null && !addresses.isEmpty()) {
            currentAddress = addresses.get(0).getAddressLine(0);
        }
    }

    private void findHospitalAddress(Location location) {
        final String locationParams = location.getLatitude() + "," + location.getLongitude();
        new Thread(() -> {
            InputStream inputStream;
            HttpURLConnection urlConnection;
            try {
                URL url = new URL("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + locationParams + "&radius=5000&types=hospital&key=AIzaSyDplDZF-U-Aj_XmVO6lmcwwzCSiyLZsT3Q");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                inputStream = urlConnection.getInputStream();
                JSONArray results = getJsonArray(inputStream);

                hospitalAddresses.clear();
                for (int ii = 0; ii < results.length(); ++ii) {
                    JSONObject jsonObjectEachResult = results.getJSONObject(ii);

                    String name = jsonObjectEachResult.optString("name");
                    String vicinity = jsonObjectEachResult.optString("vicinity");

                    hospitalAddresses.add(name + " at " + vicinity);
                }
            } catch (Exception ignored) {
            }
        }).start();
    }

    @NonNull
    private static JSONArray getJsonArray(InputStream inputStream) throws IOException, JSONException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuffer = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) stringBuffer.append(line);
        JSONObject jsonObject = new JSONObject((stringBuffer.toString()));
        return jsonObject.getJSONArray("results");
    }

}
