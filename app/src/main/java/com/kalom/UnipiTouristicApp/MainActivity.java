package com.kalom.UnipiTouristicApp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Date;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MainActivity extends AppCompatActivity {

    private EditText racicalText;
    private Button setDistanceButton;
    private Button showDetailsButton;
    private Button resetButton;
    private Button analiticsButton;
    private DatabaseReference myRef;
    private ProgressBar spinner;
    private FusedLocationProviderClient mFusedLocation;
    boolean outOfRange = true;
    private static boolean hasRun = false;
    private static boolean mLocationPermGranted = false;
    private static boolean canRedirect;
    private static final String TAG = "MainActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final int PERMGRANTED = PackageManager.PERMISSION_GRANTED;
    private static final long UPDATE_INTERVAL = 10 * 1000;  //10 SECOND
    private static final long FASTEST_INTERVAL = 2000; //2 SECOND
    private static final long SOME_DELAY = 1000; //5 SECOND
    private static ArrayList<PositionModel> pointOfInterestList = new ArrayList<>();
    private static ArrayList<PositionModel> saveModelForRedirect = new ArrayList<>();
    private static ArrayList<TimestampPosition> timestampList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        canRedirect = false;  //flag for redirect
        pointOfInterestList.clear(); //clear list of sequence "get" from FireBase
        racicalText = findViewById(R.id.editText);
        setDistanceButton = findViewById(R.id.rangeButton);
        showDetailsButton = findViewById(R.id.showActivityButton);
        analiticsButton = findViewById(R.id.analiticsButton);
        resetButton = findViewById(R.id.resetbtn);
        analiticsButton = findViewById(R.id.analiticsButton);
        showDetailsButton.setEnabled(canRedirect);
        spinner = findViewById(R.id.progressBar);
        spinner.setVisibility(View.GONE);
        mFusedLocation = getFusedLocationProviderClient(this);
        if (!hasRun) {
            migrateDataPOIs();
        }
        getPOIsList();
        setDistanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getLocationPermission() && mLocationPermGranted && isServicesOK()) {
                    String inputRadical = racicalText.getText().toString();
                    if (!inputRadical.equals("")) {

                        message("Range is saved!");
                        startLocationUpdates(inputRadical);
                    } else {
                        message("Enter a number in field!");
                    }
                }
            }
        });
        showDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectToShowActivity(saveModelForRedirect.get(saveModelForRedirect.size() - 1));
                spinner.setVisibility(View.VISIBLE);
            }
        });
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MainActivity.class));
            }
        });
        analiticsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTimeStamp();
            }
        });
    }

    private void getPOIsList() {
        myRef = FirebaseDatabase.getInstance().getReference("POIs");
        myRef.addChildEventListener(new ChildEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String prevChildKey) {
                getData(dataSnapshot);
            }

            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void getData(DataSnapshot dataSnapshot) {
        PositionModel position = dataSnapshot.getValue(PositionModel.class);
        pointOfInterestList.add(position);
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates(final String inputRadical) {
        @SuppressLint("RestrictedApi")
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);
        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        computeDistance(locationResult.getLastLocation(), inputRadical);
                    }
                },
                Looper.myLooper());
    }

    private void computeDistance(Location location, String inputRadical) {
        if (!outOfRange) {
            return;
        }
        for (PositionModel pointOfInterest : pointOfInterestList) {
            Location locationPointOfInterest = new Location("");
            locationPointOfInterest.setLatitude(pointOfInterest.getLatitude());
            locationPointOfInterest.setLongitude(pointOfInterest.getLongtitude());
            float distance = location.distanceTo(locationPointOfInterest);
            if (inputRadical.compareTo(Float.toString(distance)) > 0) {
                message("Your location is close to point of interest... " + pointOfInterest.getTitle());
                outOfRange = false;
                canRedirect = true;
                saveModelForRedirect.add(pointOfInterest);
                showDetailsButton.setEnabled(true);
                setTimestamp(pointOfInterest);
                return;
            }
        }
        outOfRange = true;
    }

    private void redirectToShowActivity(final PositionModel pointOfInterest) {
        //Put some delay
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                spinner.setVisibility(View.GONE);
                Intent i = new Intent(MainActivity.this, ShowActivity.class);
                i.putExtra("pointOfInterest", pointOfInterest);
                startActivity(i);
            }
        }, SOME_DELAY);
    }

    private boolean getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PERMGRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PERMGRANTED) {
                mLocationPermGranted = true;
                return true;
//                initMap();
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
                return false;
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermGranted = false;
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermGranted = true;
//                    initMap();
                }
            }
        }
    }

    private boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: checking google services version");
        //reset view in editText
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void message(String messageKey) {
        Toast.makeText(this, messageKey, Toast.LENGTH_SHORT).show();
    }

    private void migrateDataPOIs() {
        myRef = FirebaseDatabase.getInstance().getReference("POIs");
        DescriptionMigration desc = new DescriptionMigration();
        ArrayList<PositionModel> dataMigrated = new ArrayList<>();
        dataMigrated.add(new PositionModel("Acropolis", desc.getDesc_Acropolis(), "builtings", 37.968211, 23.720583));
        dataMigrated.add(new PositionModel("kalhmarmaro", desc.getDesc_Kalimarmaro(), "builtings", 37.969252, 23.740268));
        dataMigrated.add(new PositionModel("Stiles tou Dios", desc.getDesc_stiles(), "builtings", 37.968685, 23.731924));
        dataMigrated.add(new PositionModel("Arxaia Korinthos", desc.getDesc_ancienkorinthos(), "city", 37.904397, 22.877133));
        dataMigrated.add(new PositionModel("Nea Smirni alsos", desc.getDesc_alsosNs(), "park", 37.949240, 23.714658));
        myRef.setValue(dataMigrated);
        hasRun = true;

    }

    private void setTimestamp(PositionModel pointOfInterest) {
        myRef = FirebaseDatabase.getInstance().getReference("timestamp");
        Date timeStamp = new Date();
        TimestampPosition timePosition = new TimestampPosition(timeStamp, pointOfInterest);
        myRef.push().setValue(timePosition);

    }

    private void getTimeStamp() {
        myRef = FirebaseDatabase.getInstance().getReference("timestamp");
        myRef.addChildEventListener(new ChildEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String prevChildKey) {
                getTimeData(dataSnapshot);
            }

            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void getTimeData(DataSnapshot dataSnapshot) {
        TimestampPosition data = dataSnapshot.getValue(TimestampPosition.class);
        timestampList.add(data);
        int counterTimeStamp = timestampList.size();
//        for (TimestampPosition element : timestampList){
//            String[]categoryArray = new String[100];
//            for(int i = 0; i < counterTimeStamp - 1; i++){
//                categoryArray [i] = element.getPositionModel().getCateg();
//            }
//        }
    }

}
