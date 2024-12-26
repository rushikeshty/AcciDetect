package com.example.software2.dapp.AccidentDetect.Hosptialauthrity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.software2.dapp.AccidentDetect.Utils.NotificationUtils;
import com.example.software2.dapp.AccidentDetect.viewmodel.AccidentListStatusViewmodel;
import com.example.software2.dapp.BaseActivity;
import com.example.software2.dapp.LoginSignup.LoginScreenActivity;
import com.example.software2.dapp.R;
import com.example.software2.dapp.AmbulanceViewAccident.Accidents;
import com.example.software2.dapp.UserActivities.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AccidentList extends BaseActivity {
    ListView mListview;
    SimpleAdapter adapter;
    private HashSet<Map<String, String>> data;
    public static String userEmail;
    String detected;
    String assignedHospital;
    DatabaseReference databaseReference;
    private Handler mHandler;
    int count = 0;
    static int finalCount;
    SwipeRefreshLayout swipeRefreshLayout;
    private AccidentListStatusViewmodel viewmodel;
    public String userid;
    HashSet<String> usersIds;
    private NotificationUtils notificationUtils;
    private ArrayList<Map<String, String>> MyDataList = null;
    private boolean alreadyInit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewmodel = new AccidentListStatusViewmodel();
        viewmodel.init();
        if (viewmodel.getCurrentUser() == null) {
            finishAffinity();
            startActivity(new Intent(AccidentList.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }
        mHandler = new Handler();
        FirebaseUser user = viewmodel.getCurrentUser();
        if (user != null) {
            userEmail = user.getEmail();
            status.run();
        }
        notificationUtils = new NotificationUtils(getApplicationContext());
        notificationUtils.createNotificationChannels();
        showDialog();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (userEmail.contains("hospital")) {
                actionBar.setTitle("Hospital Administrator");
            } else if (userEmail.contains("ambulance")) {
                actionBar.setTitle("Ambulance User");
            }
        }

    }

    public void init() {
        mListview = findViewById(R.id.listView);

        if (viewmodel.getCurrentUser() != null) {
            {
                userEmail = viewmodel.getCurrentUser().getEmail();
            }
        }
        data = new HashSet<>();
        initSwipeRefreshLayout();
    }

    public void initSwipeRefreshLayout() {
        swipeRefreshLayout = findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setColorSchemeColors(Color.BLUE);
        swipeRefreshLayout.setSize(SwipeRefreshLayout.LARGE);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            data.clear();
            GetAccident();
            final Handler h = new Handler(Looper.getMainLooper());
            h.postDelayed(() -> swipeRefreshLayout.setRefreshing(false), 3000);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        data = new HashSet<>();
        GetAccident();
    }

    @SuppressWarnings("unchecked")
    public void FetchData(String userid) {
        try {
            showDialog();
            databaseReference.child("user").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    ArrayList<String> values = new ArrayList<>();
                    databaseReference.child("user").child(Objects.requireNonNull(snapshot.getKey())).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            dismissDialog();
                            for (DataSnapshot child : snapshot.child("AccidentInfo").getChildren()) {
                                values.add(Objects.requireNonNull(child.getValue()).toString());
                            }
                            int position = 0;
                            List<String> list = new ArrayList<>(usersIds);
                            Collections.reverse(list);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                    position = list.indexOf(snapshot.getKey() + "true");
                                }
                            }
                            if (!values.isEmpty() && values.size() > 9) {
                                Map<String, String> accidentDetails = new HashMap<>();
                                accidentDetails.put("Location", values.get(5).replaceAll("\n", ""));
                                accidentDetails.put("status", values.get(9));
                                String finalDateTime = values.get(0);
                                if (count == 0) {
                                    accidentDetails.put("date", values.get(0));
                                    count++;
                                } else {
                                    accidentDetails.put("date", finalDateTime);
                                }
                                updateListItem(position, accidentDetails);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    Log.d("DataSnapshot", Objects.requireNonNull(snapshot.getKey()));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            databaseReference.child("user").child(userid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ArrayList<String> values = new ArrayList<>();
                    for (DataSnapshot child : snapshot.child("AccidentInfo").getChildren()) {
                        values.add(Objects.requireNonNull(child.getValue()).toString());
                    }
                    if (!values.isEmpty() && values.size() > 9) {
                        Map<String, String> accidentData = new HashMap<>();
                        accidentData.put("Location", values.get(5).replaceAll("\n", ""));
                        accidentData.put("status", values.get(9));
                        String finalDateTime = values.get(0);
                        if (count == 0) {
                            accidentData.put("date", values.get(0));
                            count++;
                        } else {
                            accidentData.put("date", finalDateTime);
                        }

                        accidentData.put("userid", snapshot.getKey());
                        data.add(accidentData);
                        final ListView lst = findViewById(R.id.listView);
                        MyDataList = new ArrayList<>(data);
                        String[] from = {"Location", "status", "date", "userid"};
                        int[] to = {R.id.location, R.id.status, R.id.timedate, R.id.userid};
                        adapter = new SimpleAdapter(getApplicationContext(), MyDataList, R.layout.single_accident_file, from, to);
                        lst.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        dismissDialog();

                        if (adapter.getCount() == 0) {
                            Toast.makeText(AccidentList.this, "There are no donors for the selected blood group", Toast.LENGTH_LONG).show();
                        }
                        lst.setOnItemClickListener((adapterView, view, i, l) -> {
                            HashMap<String, String> obj = (HashMap<String, String>) adapter.getItem(i);
                            String userid1 = obj.get("userid");
                            if (userEmail.contains("ambulance")) {
                                assert userid1 != null;
                                databaseReference.child("user").child(userid1).child("AccidentInfo").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot1) {
                                        assignedHospital = Objects.requireNonNull(snapshot1.child("hospitalassigned").getValue()).toString();
                                        Intent ambulance = new Intent(AccidentList.this, Accidents.class);
                                        ambulance.putExtra("assignedhospital", assignedHospital);
                                        ambulance.putExtra("userid", userid1);
                                        startActivity(ambulance);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            } else {

                                if (userid1 != null) {
                                    databaseReference.child("user").child(userid1).child("AccidentInfo").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot1) {
                                            ArrayList<String> values1 = new ArrayList<>(4);
                                            for (DataSnapshot child : snapshot1.getChildren()) {
                                                values1.add(Objects.requireNonNull(child.getValue()).toString());
                                            }

                                            Intent i = getIntent(values1);
                                            startActivity(i);
                                            Toast.makeText(getApplicationContext(), values1.toString(), Toast.LENGTH_SHORT).show();
                                        }

                                        @NonNull
                                        private Intent getIntent(ArrayList<String> values1) {
                                            Intent i = new Intent(AccidentList.this, ViewAccident.class);
                                            i.putExtra("speed", values1.get(8));
                                            i.putExtra("sensor", values1.get(7));
                                            i.putExtra("location", values1.get(5));
                                            i.putExtra("hospital", values1.get(3));
                                            i.putExtra("status", values1.get(9));
                                            i.putExtra("datetime", values1.get(0));
                                            i.putExtra("emergencycontact", values1.get(2));
                                            i.putExtra("userid", userid1);
                                            i.putExtra("decibel", values1.get(1));
                                            return i;
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }

                            }
                        });

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();

        }
    }

    public void updateListItem(int position, Map<String, String> accidentDetails) {
        if (position >= 0 && position <= MyDataList.size()) {
            MyDataList.set(position, accidentDetails);
            String[] from = {"Location", "status", "date", "userid"};
            int[] to = {R.id.location, R.id.status, R.id.timedate, R.id.userid};
            adapter = new SimpleAdapter(getApplicationContext(), MyDataList, R.layout.single_accident_file, from, to);
            mListview.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }


    public void GetAccident() {
        usersIds = new HashSet<>();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("Accidents").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    userid = Objects.requireNonNull(snapshot1.child("userid").getValue()).toString();
                    detected = Objects.requireNonNull(snapshot1.child("Detected").getValue()).toString();
                    if (detected.contains("true") && !userid.contains("com.google.firebase.auth.")) {
                        usersIds.add(userid + detected);
                    }
                }
                if (usersIds.isEmpty()) {
                    setContentView(R.layout.noaccidents);
                    if (alreadyInit) {
                        initSwipeRefreshLayout();
                        alreadyInit = true;
                    }

                    dismissDialog();
                } else {
                    setContentView(R.layout.activity_accident_list);
                    init();
                    String[] usersData = usersIds.toArray(new String[0]);
                    for (String usersDatum : usersData) {
                        FetchData(usersDatum.replace("true", ""));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dismissDialog();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mHandler.removeCallbacks(status);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(status);
    }

    @Override
    protected void onStop() {
        super.onStop();
        alreadyInit = false;
    }

    Runnable status = new Runnable() {
        @Override
        public void run() {
            try {
                databaseReference.child("Accidents").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            if (Objects.requireNonNull(snapshot1.child("Detected").getValue()).toString().contains("true") && userEmail.contains("ambulance")) {
                                if (finalCount < 1) {
                                    notificationUtils.sendOnChannel2("sendtoambulance");
                                    finalCount++;
                                }

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "runnable " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            mHandler.postDelayed(status, 1000);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:

                return true;
            case R.id.action_logout:
                new AlertDialog.Builder(AccidentList.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Closing Activity")
                        .setMessage("Are you sure you want to close this activity?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            if (viewmodel.getCurrentUser() != null) {
                                FirebaseAuth.getInstance().signOut();
                                finish();
                                startActivity(new Intent(getApplicationContext(), LoginScreenActivity.class));
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}