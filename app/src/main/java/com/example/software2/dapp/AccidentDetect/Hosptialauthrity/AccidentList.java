package com.example.software2.dapp.AccidentDetect.Hosptialauthrity;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.software2.dapp.LoginSignup.LoginScreenActivity;
import com.example.software2.dapp.R;
import com.example.software2.dapp.AmbulanceViewAccident.Accidents;
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
import java.util.Map;

import pl.droidsonroids.gif.GifImageView;

public class AccidentList extends AppCompatActivity {
    ListView mListview;
    public static String CHANNEL_1_ID = "channel1";
    public static String CHANNEL_2_ID = "channel2";
    public static NotificationManager mNotificationManager;

    // ArrayList<Model> dataholder = new ArrayList<>();
    //Array list to add reminders and display in recyclerview
    SimpleAdapter adapter;
    private ArrayList<Map<String, String>> data;
    static String whichuser;
     String detected;
    String assignedhospital;
    DatabaseReference databaseReference;
    private int mInterval = 6000; // 5 seconds by default, can be changed later
    private Handler mHandler;

    int count=0;
    static int finalcount;

    SwipeRefreshLayout swipeRefreshLayout;
    ProgressDialog progressDialog;
    GifImageView gifImageView;
      FirebaseAuth firebaseAuth;
    public String userid;

     ArrayList<String> usersid;
     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching Data...");
        progressDialog.show();
        firebaseAuth = FirebaseAuth.getInstance();
         databaseReference = FirebaseDatabase.getInstance().getReference();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user!=null){
                whichuser = user.getEmail();
            }
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannels();
         databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
         status.run();
        GetAccident();



    }
    public void Init(){
        swipeRefreshLayout = findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setColorSchemeColors(Color.BLUE);
        swipeRefreshLayout.setSize(40);

        //gifImageView = findViewById(R.id.warninggif);


        mListview = (ListView) findViewById(R.id.listView);
        firebaseAuth = FirebaseAuth.getInstance();
         databaseReference = FirebaseDatabase.getInstance().getReference();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user!=null){{
            whichuser=user.getEmail();
        }}




        data = new ArrayList<Map<String, String>>();


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                data.clear();
                adapter.notifyDataSetChanged();
                GetAccident();
                final Handler h = new Handler(Looper.getMainLooper());
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                },3000);



            }
        });
    }
    public void FetchData(String userid){

        try {

            databaseReference.child("user").child(userid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    progressDialog.dismiss();
                    ArrayList<String> values = new ArrayList<String>();
                    for (DataSnapshot child : snapshot.child("AccidentInfo").getChildren()) {

                        values.add(child.getValue().toString());
                    }
                    Toast.makeText(getApplicationContext(), values.toString(), Toast.LENGTH_SHORT).show();
                    if (values.size() > 0) {

                        Map<String, String> dtname = new HashMap<String, String>();
                        dtname.put("Location", values.get(5).replaceAll("\n", ""));
                        dtname.put("status",  values.get(9));
                        String finaldatetime = values.get(0);
                        if (count == 0) {
                            dtname.put("date", values.get(0));
                            count++;
                        } else {
                            dtname.put("date", finaldatetime);
                        }

                        dtname.put("userid", snapshot.getKey());
                        data.add(dtname);
                        final ListView lst = (ListView) findViewById(R.id.listView);
                        List<Map<String, String>> MyDataList = null;
                        MyDataList = data;
                        String[] from = {"Location", "status", "date", "userid"};
                        int[] to = {R.id.location, R.id.status, R.id.timedate, R.id.userid};
                        adapter = new SimpleAdapter(getApplicationContext(), MyDataList, R.layout.single_accident_file, from, to);
                        lst.setAdapter(adapter);


                        if (adapter.getCount() == 0) {
                            Toast.makeText(AccidentList.this, "There are no donors for the selected blood group", Toast.LENGTH_LONG).show();
                        }
                        lst.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                if (whichuser.contains("ambulance")) {
                                    progressDialog.show();
                                    HashMap<String, String> obj = (HashMap<String, String>) adapter.getItem(i);
                                    String userid = (String) obj.get("userid");
                                    assert userid != null;
                                    databaseReference.child("user").child(userid).child("AccidentInfo").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                 assignedhospital = snapshot.child("hospitalassigned").getValue().toString();

                                            Intent ambulance = new Intent(AccidentList.this,Accidents.class);
                                            ambulance.putExtra("assignedhospital",assignedhospital);
                                            ambulance.putExtra("userid",userid);
                                            startActivity(ambulance);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                }
                                else {
                                    progressDialog.show();
                                    HashMap<String, String> obj = (HashMap<String, String>) adapter.getItem(i);
                                    String userid = (String) obj.get("userid");
                                    assert userid != null;

                                    databaseReference.child("user").child(userid).child("AccidentInfo").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                                            progressDialog.dismiss();
                                            ArrayList<String> values = new ArrayList<String>(4);
                                            for (DataSnapshot child : snapshot.getChildren()) {
                                                values.add(child.getValue().toString());
                                            }

                                            Intent i = new Intent(AccidentList.this, ViewAccident.class);
                                            i.putExtra("speed", values.get(8));
                                            i.putExtra("sensor", values.get(7));
                                            i.putExtra("location", values.get(5));
                                            i.putExtra("hospital", values.get(3));
                                            i.putExtra("status", values.get(9));
                                            i.putExtra("datetime", values.get(0));
                                            i.putExtra("emergencycontact", values.get(2));
                                            i.putExtra("userid", userid);
                                            i.putExtra("decibel",values.get(1));
                                            startActivity(i);
                                            Toast.makeText(getApplicationContext(), values.toString(), Toast.LENGTH_SHORT).show();

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
        }

        catch (Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();

        }
    }


    public void GetAccident(){
        usersid = new ArrayList<>();
         databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("Accidents").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.dismiss();
                for(DataSnapshot snapshot1 :snapshot.getChildren()){
                    userid = snapshot1.child("userid").getValue().toString();
                    detected = snapshot1.child("Detected").getValue().toString();
                    if(detected.contains("true")) {
                        usersid.add(userid + "" + detected);
                    }
//                   for(DataSnapshot snapshot11:snapshot1.getChildren()){
//                       userid = snapshot11.getValue().toString();
//                   }


                }
                if(usersid.size()==0){

                    setContentView(R.layout.noaccidents);
                }
                else {
                    setContentView(R.layout.activity_accident_list);
                    Init();
                    for(int i=0;i< usersid.size();i++){
                        FetchData(usersid.get(i).replace("true",""));
                    }

                }




            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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

    Runnable status = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void run() {
            try{
                databaseReference.child("Accidents").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snapshot1:snapshot.getChildren()){
                            if(snapshot1.child("Detected").getValue().toString().contains("true")&&whichuser.contains("ambulance")){
                                if(finalcount<1){
                                    sendOnChannel2("sendtoambulance");
                                    finalcount++;
                                }

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
            catch (Exception e){
                Toast.makeText(getApplicationContext(), "runnable "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            mHandler.postDelayed(status,1000);
        }
    };
    public void sendOnChannel2(String notify) {
         if(notify.equals("sendtoambulance")) {

            Intent notificationIntent1 = new Intent(getApplicationContext(), AccidentList.class);

            notificationIntent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PendingIntent intent1 = PendingIntent.getActivity(getApplicationContext(), 0,
                    notificationIntent1, 0);
            Notification notification2 = new NotificationCompat.Builder(this, CHANNEL_2_ID)
                    .setSmallIcon(R.drawable.bell)
                    .setOngoing(true)
                    .setColor(Color.RED)
                    .setAutoCancel(false)
                    .setContentTitle("Accident Detection System for Ambulance")
                    .setContentIntent(intent1)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .build();
            SystemClock.sleep(2000);
            mNotificationManager.notify(5, notification2);

        }


    }

    private void createNotificationChannels()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,
                    "Channel 1",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription("This is Channel 1");

            NotificationChannel channel2 = new NotificationChannel(
                    CHANNEL_2_ID,
                    "Channel 2",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel2.setDescription("This is Channel 2");

            mNotificationManager.createNotificationChannel(channel1);
            mNotificationManager.createNotificationChannel(channel2);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);


        return true;
    }
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
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (firebaseAuth.getCurrentUser() != null) {
                                    FirebaseAuth.getInstance().signOut();

                                     finish();
                                    startActivity(new Intent(getApplicationContext(), LoginScreenActivity.class));
                                }
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