package com.example.software2.dapp.AmbulanceViewAccident;

import static com.example.software2.dapp.AccidentDetect.Hosptialauthrity.AccidentList.userEmail;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
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
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentContainerView;

import com.example.software2.dapp.AccidentDetect.GPSHandler;
import com.example.software2.dapp.AccidentDetect.Hosptialauthrity.AccidentList;
import com.example.software2.dapp.AccidentDetect.viewmodel.AccidentListStatusViewmodel;
import com.example.software2.dapp.BaseActivity;
import com.example.software2.dapp.Coordinate;
import com.example.software2.dapp.MyAccount;
import com.example.software2.dapp.R;
import com.example.software2.dapp.UserActivities.MainActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Accidents extends BaseActivity implements GoogleMap.OnCameraIdleListener, OnMapReadyCallback, DirectionFinderListener, AdapterView.OnItemSelectedListener {

    private static final int REQUEST_LOCATION = 1;
    Button directionbtn, updatebtn, viewuser, hospitalassig;
    TextView datetime, address;
    int count = 0;
    private int mInterval = 6000;
    // 6 seconds by default, can be changed later
    private Handler mHandler;
    String longitude, latitude;
    static String userid;
    double ambulancelatitude, ambulancelongitude;
    FragmentContainerView fragmentContainerView;
    private @ColorInt int mPulseEffectColor;
    private int[] mPulseEffectColorElements;
    private ValueAnimator mPulseEffectAnimator;
    private Circle mPulseCircle;
    ArrayList<String> values = new ArrayList<>();
    GPSTracker tracker;
    private GPSHandler mGPSHandler;
    private static String location;
    private FusedLocationProviderClient fusedLocationClient;//One of the location APIs in google play services
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;//Request Code is used to check which permission called this function. // This request code is provided when the user is prompt for permission.
    private LocationAddressResultReceiver addressResultReceiver;//receives the address results
    @SuppressLint("StaticFieldLeak")
    private android.location.Location currentLocation;
    private LocationCallback locationCallback;
    private SupportMapFragment supportMapFragment;
    public Spinner spinner;
    public static Uri gmmIntentUri;
    static String assignedhospital;
    TextView locationmymap, textttt;
    private String ambulanceOrigin;
    String status = "";
    private List<Coordinate> set2;
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
        directionbtn = findViewById(R.id.directionbutton);
        // Intent intent = this.getIntent();
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

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        mGPSHandler = new GPSHandler(this);

        viewuser.setOnClickListener(view -> {
            MyAccount myAccount = new MyAccount(Accidents.this, userid);
            myAccount.show();
        });

        tracker = new GPSTracker(this);
        if (!tracker.canGetLocation()) {
            dismissDialog();
            tracker.showSettingsAlert();
        } else {
            ambulancelatitude = tracker.getLatitude();
            ambulancelongitude = tracker.getLongitude();

        }
        getdata();
        startRepeatingTask();

        // Spinner Drop down elements
        updateSpinner();
        directionbtn.setOnClickListener(v -> {
            dismissDialog();
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        });

        updatebtn.setOnClickListener(v -> {
            if (status.contains("select")) {
                Toast.makeText(getApplicationContext(), "Please select the status", Toast.LENGTH_SHORT).show();

            } else {
                viewmodel.getDatabaseReference().child("user").child(userid).child("AccidentInfo").child("status").setValue(status);
                Toast.makeText(getApplicationContext(), "Updated successfully.", Toast.LENGTH_SHORT).show();
            }

        });
        hospitalassig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(Accidents.this);
                builder1.setMessage("Assigned Hospital :- " + assignedhospital);
                builder1.setPositiveButton("ok", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                });
                builder1.setCancelable(true);
                AlertDialog alert11 = builder1.create();
                alert11.show();

            }
        });
    }
    public void initUI(){
        updatebtn = findViewById(R.id.updatebtn);
        datetime = findViewById(R.id.datetime);
        fragmentContainerView = findViewById(R.id.myMap);
        textttt = findViewById(R.id.textttt);
        address = findViewById(R.id.address);
        locationmymap = findViewById(R.id.locationmymap);
        hospitalassig = findViewById(R.id.hospitalassigned);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        viewuser = findViewById(R.id.viewuserdetails);
        spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
    }

    public void updateSpinner(){
        List<String> categories = new ArrayList<>();
        if(!status.isEmpty()){
            categories.add(status);
        }
        else {
            categories.add("SELECT USER STATUS:-");
        }
        categories.add("AMBULANCE ALLOTTED");
        categories.add("USER PICKED");
        categories.add("USER DROPPED AT HOSPITAL");
        categories.add("USER ADMITTED AT HOSPITAL");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }
    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle extras = intent.getExtras();
        assignedhospital = extras.getString("assignedhospital");
        userid = extras.getString("userid");
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
        // TODO Auto-generated method stub
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("MissingPermission")
    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new
                            String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(10000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    @SuppressWarnings("MissingPermission")
    private void getAddress() {
        if (!Geocoder.isPresent()) {
            Toast.makeText(Accidents.this, "Can't find current address, ",
                    Toast.LENGTH_SHORT).show();
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

    private void getdata() {
        //final FirebaseUser user = firebaseAuth.getCurrentUser();
        viewmodel.getDatabaseReference().child("user").child(userid).child("AccidentInfo").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dismissDialog();
                for (DataSnapshot child : snapshot.getChildren()) {
                    values.add(child.getValue().toString());
                }
                if (count == 0) {
                    //here we update time and date only one time
                    datetime.setText(values.get(0));
                    count++;
                }
                address.setText(values.get(5).replaceAll("\n", ""));
                locationmymap.setText(values.get(5).replaceAll("\n", ""));
                latitude = values.get(4);
                longitude = values.get(6);
                status = values.get(9);

                if(!status.isEmpty() && !isSelected){
                    updateSpinner();
                }
                //Toast.makeText(getApplicationContext(), values.toString(), Toast.LENGTH_SHORT).show();
                supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.myMap);
                if (supportMapFragment != null) {
                    supportMapFragment.getMapAsync(Accidents.this);
                }
                ambulancelatitude = tracker.getLatitude();
                ambulancelongitude = tracker.getLongitude();

                sendCallMap();
                values.clear();
                dismissDialog();
                //    HosiptalAssigned();

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
        mPulseEffectColorElements = new int[]{
                Color.red(mPulseEffectColor),
                Color.green(mPulseEffectColor),
                Color.blue(mPulseEffectColor)
        };

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
        if (mPulseCircle != null)
            mPulseCircle.remove();

        if (mPulseEffectAnimator != null) {
            mPulseEffectAnimator.removeAllUpdateListeners();
            mPulseEffectAnimator.removeAllListeners();
            mPulseEffectAnimator.end();
        }

        mPulseCircle = googleMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(0).strokeWidth(0)
                .fillColor(mPulseEffectColor));
        mPulseEffectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (mPulseCircle == null)
                    return;

                int alpha = (int) ((1 - valueAnimator.getAnimatedFraction()) * 128);
                mPulseCircle.setFillColor(Color.argb(alpha,
                        mPulseEffectColorElements[0], mPulseEffectColorElements[1], mPulseEffectColorElements[2]));
                mPulseCircle.setRadius((float) valueAnimator.getAnimatedValue());

            }
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

    @Override
    public void onDirectionFinderStart() {

    }

    @Override
    public void onDirectionFinderSuccess(List<Route> route) {

    }

    private void sendCallMap() {

        if (latitude != null && longitude != null) {

            double lati = Double.parseDouble(latitude);
            double longi = Double.parseDouble(longitude);
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            //Geocoding refers to transforming street address or any address
            List<Address> addresses = null;
            List<Address> addressOfambulance = null;
            try {
                addressOfambulance = geocoder.getFromLocation(ambulancelatitude, ambulancelongitude, 1);

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            if (addressOfambulance == null || addressOfambulance.size() == 0) {
                Toast.makeText(getApplicationContext(), "no address", Toast.LENGTH_SHORT).show();

            } else {
                Address addressofamb = addressOfambulance.get(0);
                ambulanceOrigin = ambulancelatitude + "," + ambulancelongitude;
                //Toast.makeText(getApplicationContext(), ambulanceOrigin, Toast.LENGTH_LONG).show();

            }
            try {
                addresses = geocoder.getFromLocation(lati, longi, 1);
            } catch (Exception ioException) {
                Toast.makeText(getApplicationContext(), ioException.getMessage(), Toast.LENGTH_SHORT).show();
            }

            if (addresses == null || addresses.size() == 0) {
                Toast.makeText(getApplicationContext(), "no address", Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(), String.valueOf(addresses), Toast.LENGTH_SHORT).show();
            } else {
                // Address address = lati+","+longi;

                // String origin = "26.854980, 75.830206";
                String destination = lati + "," + longi;

                //   String origin = ambulancelatitude + " " + ambulancelongitude;
                if (destination != null) {

                    try {

                        String Urll = new DirectionFinder(this, ambulanceOrigin, destination).createUrll();

                        gmmIntentUri = Uri.parse("google.navigation:q=" + Urll + "&mode=c");
                        SpannableString content = new SpannableString(gmmIntentUri.toString());
                        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
//                        url.setText(content);

                        fragmentContainerView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                mapIntent.setPackage("com.google.android.apps.maps");
                                startActivity(mapIntent);

                            }
                        });


                    } catch (UnsupportedEncodingException e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

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
        private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

        // Declaring a Location Manager
        protected LocationManager locationManager;

        public GPSTracker(Context context) {
            this.mContext = context;
            getLocation();
        }

        @SuppressLint("MissingPermission")
        public Location getLocation() {
            try {
                locationManager = (LocationManager) mContext
                        .getSystemService(LOCATION_SERVICE);

                // getting GPS status
                isGPSEnabled = locationManager
                        .isProviderEnabled(LocationManager.GPS_PROVIDER);

                // getting network status
                isNetworkEnabled = locationManager
                        .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if (!isGPSEnabled && !isNetworkEnabled) {
                    // no network provider is enabled
                } else {
                    this.canGetLocation = true;
                    if (isNetworkEnabled) {
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("Network", "Network");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                    // if GPS Enabled get lat/long using GPS Services
                    if (isGPSEnabled) {
                        if (location == null) {
                            locationManager.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    MIN_TIME_BW_UPDATES,
                                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                            Log.d("GPS Enabled", "GPS Enabled");
                            if (locationManager != null) {
                                location = locationManager
                                        .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                if (location != null) {
                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                }
                            }
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
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

            // return latitude
            return latitude;
        }

        /**
         * Function to get longitude
         */
        public double getLongitude() {
            if (location != null) {
                longitude = location.getLongitude();
            }

            // return longitude
            return longitude;
        }


        public boolean canGetLocation() {
            return this.canGetLocation;
        }

        /**
         * Function to show settings alert dialog
         * On pressing Settings button will lauch Settings Options
         */
        public void showSettingsAlert() {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

            // Setting Dialog Title
            alertDialog.setTitle("GPS is settings");

            // Setting Dialog Message
            alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

            // On pressing Settings button
            alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    mContext.startActivity(intent);
                }
            });

            // on pressing cancel button
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            // Showing Alert Message
            alertDialog.show();
        }

        @Override
        public void onLocationChanged(Location location) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
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
            // getdata();
            if (resultCode == 0) {
                getAddress();
            }
            if (resultCode == 1) {
                Toast.makeText(Accidents.this, "Address not found, ", Toast.LENGTH_SHORT).show();
            }
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
                getdata();
            } finally {
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
