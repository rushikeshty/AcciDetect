package com.example.software2.dapp.AmbulanceViewAccident;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentContainerView;

import com.example.software2.dapp.AccidentDetect.viewmodel.AccidentListStatusViewmodel;
import com.example.software2.dapp.BaseActivity;
import com.example.software2.dapp.MyAccount;
import com.example.software2.dapp.R;
import com.example.software2.dapp.UserActivities.MainActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class Accidents extends BaseActivity implements GoogleMap.OnCameraIdleListener, OnMapReadyCallback, AdapterView.OnItemSelectedListener {

    private static final int REQUEST_LOCATION = 1;
    private static final String TAG = Accidents.class.getName();
    Button directionBtn, updateBtn, viewUser, assignedHospital;
    TextView datetime, address;
    int count = 0;
    private Handler mHandler;
    String longitude, latitude;
    static String userid;
    double ambulanceLatitude, ambulanceLongitude;
    FragmentContainerView fragmentContainerView;
    private @ColorInt int mPulseEffectColor;
    private int[] mPulseEffectColorElements;
    private ValueAnimator mPulseEffectAnimator;
    private Circle mPulseCircle;
    ArrayList<String> values = new ArrayList<>();
    GPSTracker tracker;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;//Request Code is used to check which permission called this function. // This request code is provided when the user is prompt for permission.
    private LocationAddressResultReceiver addressResultReceiver;//receives the address results
    @SuppressLint("StaticFieldLeak")
    private Location currentLocation;
    private LocationCallback locationCallback;
    private SupportMapFragment supportMapFragment;
    public Spinner spinner;
    public static Uri gmmIntentUri;
    static String assignedHospitalName;
    TextView locationOnMap;
    String status = "";
    private boolean isSelected = false;
    private AccidentListStatusViewmodel viewmodel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accidents);

        viewmodel = new AccidentListStatusViewmodel();
        viewmodel.init();
        if (viewmodel.getCurrentUser() == null) {
            finishAffinity();
            startActivity(new Intent(Accidents.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }
        directionBtn = findViewById(R.id.directionbutton);
        onNewIntent(getIntent());
        initUI();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Ambulance User");

        }

        addressResultReceiver = new LocationAddressResultReceiver(new Handler());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                currentLocation = locationResult.getLocations().get(0);
                getAddress();
            }
        };
        startLocationUpdates();//call this function to check location permission

        mHandler = new Handler();

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        viewUser.setOnClickListener(view -> {
            MyAccount myAccount = new MyAccount(Accidents.this, userid);
            myAccount.show();
        });

        tracker = new GPSTracker(this);
        if (tracker.canGetLocation()) {
            dismissDialog();
            tracker.showSettingsAlert();
        } else {
            ambulanceLatitude = tracker.getLatitude();
            ambulanceLongitude = tracker.getLongitude();

        }
        getData();
        startRepeatingTask();
        updateSpinner();

        directionBtn.setOnClickListener(v -> {
            dismissDialog();
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        });

        updateBtn.setOnClickListener(v -> {
            if (status.contains("select")) {
                Toast.makeText(getApplicationContext(), "Please select the status", Toast.LENGTH_SHORT).show();

            } else {
                viewmodel.getDatabaseReference().child("user").child(userid).child("AccidentInfo").child("status").setValue(status);
                Toast.makeText(getApplicationContext(), "Updated successfully.", Toast.LENGTH_SHORT).show();
            }

        });
        assignedHospital.setOnClickListener(view -> {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(Accidents.this);
            builder1.setMessage("Assigned Hospital :- " + assignedHospitalName);
            builder1.setPositiveButton("ok", (dialogInterface, i) -> dialogInterface.dismiss());
            builder1.setCancelable(true);
            AlertDialog alert11 = builder1.create();
            alert11.show();
        });
    }

    public void initUI() {
        updateBtn = findViewById(R.id.updatebtn);
        datetime = findViewById(R.id.datetime);
        fragmentContainerView = findViewById(R.id.myMap);
        address = findViewById(R.id.address);
        locationOnMap = findViewById(R.id.locationmymap);
        assignedHospital = findViewById(R.id.hospitalassigned);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        viewUser = findViewById(R.id.viewuserdetails);
        spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
    }

    public void updateSpinner() {
        ArrayList<String> categories = new ArrayList<>();
        if (!status.isEmpty()) {
            categories.add(status);
        } else {
            categories.add("SELECT USER STATUS:-");
        }
        categories.add("AMBULANCE ALLOTTED");
        categories.add("USER PICKED");
        categories.add("USER DROPPED AT HOSPITAL");
        categories.add("USER ADMITTED AT HOSPITAL");

        int idx = categories.lastIndexOf(status);
        if (idx > -1) {
            categories.remove(idx);
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>(categories));

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }

    @Override
    public void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        Bundle extras = intent.getExtras();
        if (extras != null) {
            assignedHospitalName = extras.getString("assignedhospital");
            userid = extras.getString("userid");
        }
    }

    @Override
    public void onItemSelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        status = parent.getItemAtPosition(position).toString();
        isSelected = true;

        // Showing selected spinner item
        Toast.makeText(getApplicationContext(), "Selected: " + status, Toast.LENGTH_SHORT).show();
    }

    public void onNothingSelected(AdapterView<?> arg0) {
        Log.d(Accidents.TAG, arg0.toString());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).setMinUpdateIntervalMillis(10000).build();
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    @SuppressWarnings("MissingPermission")
    private void getAddress() {
        if (!Geocoder.isPresent()) {
            Toast.makeText(Accidents.this, "Can't find current address, ", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, GetAllData.class);
        intent.putExtra("add_receiver", addressResultReceiver);
        intent.putExtra("add_location", currentLocation);
        startService(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Location permission not granted, " + "restart the app if you want the feature", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getData() {
        viewmodel.getDatabaseReference().child("user").child(userid).child("AccidentInfo").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dismissDialog();
                for (DataSnapshot child : snapshot.getChildren()) {
                    if (child.getValue() != null) {
                        values.add(child.getValue().toString());
                    }
                }
                if (count == 0) {
                    //here we update time and date only one time
                    datetime.setText(values.get(0));
                    count++;
                }
                address.setText(values.get(5).replaceAll("\n", ""));
                locationOnMap.setText(values.get(5).replaceAll("\n", ""));
                latitude = values.get(4);
                longitude = values.get(6);
                status = values.get(9);

                if (!status.isEmpty() && !isSelected) {
                    updateSpinner();
                }
                //Toast.makeText(getApplicationContext(), values.toString(), Toast.LENGTH_SHORT).show();
                supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.myMap);
                if (supportMapFragment != null) {
                    supportMapFragment.getMapAsync(Accidents.this);
                }
                ambulanceLatitude = tracker.getLatitude();
                ambulanceLongitude = tracker.getLongitude();

                sendCallMap();
                values.clear();
                dismissDialog();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dismissDialog();
                Toast.makeText(Accidents.this, "Fail to get data.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void initPulseEffect() {
        mPulseEffectColor = ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_dark);
        mPulseEffectColorElements = new int[]{Color.red(mPulseEffectColor), Color.green(mPulseEffectColor), Color.blue(mPulseEffectColor)};

        mPulseEffectAnimator = ValueAnimator.ofFloat(0, calculatePulseRadius());
        mPulseEffectAnimator.setStartDelay(3000);
        mPulseEffectAnimator.setDuration(400);
        mPulseEffectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
    }

    private static float calculatePulseRadius() {
        return (float) Math.pow(2, 16 - (float) 12) * 160;
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        LatLng latLng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("I am here!");
        googleMap.addMarker(markerOptions);
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
        initPulseEffect();
        //   googleMap.addMarker(markerOptions);
        if (mPulseCircle != null) mPulseCircle.remove();

        if (mPulseEffectAnimator != null) {
            mPulseEffectAnimator.removeAllUpdateListeners();
            mPulseEffectAnimator.removeAllListeners();
            mPulseEffectAnimator.end();
        }

        mPulseCircle = googleMap.addCircle(new CircleOptions().center(latLng).radius(0).strokeWidth(0).fillColor(mPulseEffectColor));
        mPulseEffectAnimator.addUpdateListener(valueAnimator -> {
            if (mPulseCircle == null) return;

            int alpha = (int) ((1 - valueAnimator.getAnimatedFraction()) * 128);
            mPulseCircle.setFillColor(Color.argb(alpha, mPulseEffectColorElements[0], mPulseEffectColorElements[1], mPulseEffectColorElements[2]));
            mPulseCircle.setRadius((float) valueAnimator.getAnimatedValue());

        });
        mPulseEffectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mPulseEffectAnimator.setStartDelay(500);
                mPulseEffectAnimator.start();

            }
        });
        mPulseEffectAnimator.start();
    }

    private void sendCallMap() {

        if (latitude != null && longitude != null) {
            double lat = Double.parseDouble(latitude);
            double longs = Double.parseDouble(longitude);
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            //Geocoding refers to transforming street address or any address
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(lat, longs, 1);
            } catch (Exception ioException) {
                Toast.makeText(getApplicationContext(), ioException.getMessage(), Toast.LENGTH_SHORT).show();
            }

            if (addresses == null || addresses.isEmpty()) {
                Toast.makeText(getApplicationContext(), "no address", Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(), String.valueOf(addresses), Toast.LENGTH_SHORT).show();
            } else {

                String destination = lat + "," + longs;
                try {
                    String url = new DirectionFinder(destination).createUrl();

                    gmmIntentUri = Uri.parse("google.navigation:q=" + url + "&mode=c");
                    SpannableString content = new SpannableString(gmmIntentUri.toString());
                    content.setSpan(new UnderlineSpan(), 0, content.length(), 0);

                    fragmentContainerView.setOnClickListener(view -> {
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        startActivity(mapIntent);

                    });

                } catch (UnsupportedEncodingException e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    @Override
    public void onCameraIdle() {

    }


    public static class GPSTracker extends Service implements LocationListener {

        private final Context mContext;

        // flag for GPS status
        boolean isGPSEnabled = false;

        // flag for network status
        boolean isNetworkEnabled = false;

        // flag for GPS status
        boolean canGetLocation = false;

        public Location location; // location

        double latitude; // latitude
        double longitude; // longitude


        // The minimum distance to change Updates in meters
        private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

        // The minimum time between updates in milliseconds
        private static final long MIN_TIME_BW_UPDATES = 1000 * 60; // 1 minute

        // Declaring a Location Manager
        protected LocationManager locationManager;

        public GPSTracker(Context context) {
            this.mContext = context;
            getLocation();
        }

        @SuppressLint("MissingPermission")
        public Location getLocation() {
            try {
                locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

                // getting GPS status
                isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

                // getting network status
                isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if (!isGPSEnabled && !isNetworkEnabled) {
                    Log.d(TAG, "No Network Provided");
                    // no network provider is enabled
                } else {
                    this.canGetLocation = true;
                    if (isNetworkEnabled) {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("Network", "Network");
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                    // if GPS Enabled get lat/long using GPS Services
                    if (isGPSEnabled) {
                        if (location == null) {
                            if (locationManager != null) {
                                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                            }
                            Log.d("GPS Enabled", "GPS Enabled");
                            if (locationManager != null) {
                                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                if (location != null) {
                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                }
                            }
                        }
                    }
                }

            } catch (Exception e) {
                Log.d("Exception", Objects.requireNonNull(e.getMessage()));
            }

            return location;
        }


        /**
         * Function to get latitude
         */
        public double getLatitude() {
            if (location != null) {
                latitude = location.getLatitude();
            }
            return latitude;
        }

        /**
         * Function to get longitude
         */
        public double getLongitude() {
            if (location != null) {
                longitude = location.getLongitude();
            }
            return longitude;
        }


        public boolean canGetLocation() {
            return !this.canGetLocation;
        }

        /**
         * Function to show settings alert dialog
         * On pressing Settings button will launch Settings Options
         */
        public void showSettingsAlert() {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
            alertDialog.setTitle("GPS is settings");
            alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
            alertDialog.setPositiveButton("Settings", (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            });
            alertDialog.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            alertDialog.show();
        }

        @Override
        public void onLocationChanged(@NonNull Location location) {
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public IBinder onBind(Intent arg0) {
            return null;
        }

    }

    private class LocationAddressResultReceiver extends ResultReceiver {
        LocationAddressResultReceiver(Handler handler) {
            super(handler);

        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == 0) {
                getAddress();
            }
            if (resultCode == 1) {
                Toast.makeText(Accidents.this, "Address not found, ", Toast.LENGTH_SHORT).show();
            }
            String location;
            if (resultCode == 2) {
                //save current location of user
                location = resultData.getString("address_result");
                viewmodel.getDatabaseReference().child("user").child(viewmodel.getCurrentUser().getUid()).child("AccidentInfo").child("location").setValue(location);
            }

            location = resultData.getString("address_result");
            viewmodel.getDatabaseReference().child("user").child(viewmodel.getCurrentUser().getUid()).child("AccidentInfo").child("location").setValue(location);
            address.setText(location);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        startLocationUpdates();

    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                getData();
            } finally {
                int mInterval = 6000;
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isSelected = false;
    }
}