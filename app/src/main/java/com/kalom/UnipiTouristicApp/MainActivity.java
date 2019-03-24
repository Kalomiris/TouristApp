package com.kalom.UnipiTouristicApp;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
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
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements LocationListener {

    TextView racicalText;
    Button setDistanceButton;
    DatabaseReference myRef;
    LocationManager mLocationManager;
    private FusedLocationProviderClient mFusedLocation;
    //    private GoogleMap mMap;
    private static ArrayList<PositionModel> pointOfInterestList = new ArrayList<>();
    private static boolean hasRun = false;
    private static final String TAG = "MainActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final int PERMGRANTED = PackageManager.PERMISSION_GRANTED;
    private boolean mLocationPermGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        racicalText = findViewById(R.id.textViewRadical);
        setDistanceButton = findViewById(R.id.button);
        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
//        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        getLocationPermission();

        if (!hasRun) {
            migrateDataPOIs();
        }
        getPOIsList();
        setDistanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLocationPermGranted && isServicesOK()) {
                    getDeviceLocation();
                    if ((ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                        return;
                    }
                    message("Radical is saved!");
                }
            }
        });
    }

    private boolean isTooClose(Location location) {
        for (PositionModel pointOfInterest : pointOfInterestList)
            if (racicalText != null) {
                Location locationPointOfInterest = new Location("");
                locationPointOfInterest.setLatitude(pointOfInterest.getLatitude());
                locationPointOfInterest.setLongitude(pointOfInterest.getLongtitude());
                float distance = location.distanceTo(locationPointOfInterest);
                if (racicalText.getText().toString().compareTo(Float.toString(distance)) < 0) {
                    message("Your location is close to point of interest..." + "in " + pointOfInterest.getTitle());
                    return true;
                }
            }
        message("Set up your device, error...");
        return false;
    }

    private void getPOIsList() {
        myRef = FirebaseDatabase.getInstance().getReference("POIs");
        myRef.addChildEventListener(new ChildEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String prevChildKey) {
                showData(dataSnapshot);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}


//                addValueEventListener(new ValueEventListener() {
//            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                showData(dataSnapshot);
//                pointOfInterestList.clear();
//                for (DataSnapshot POIs : dataSnapshot.getChildren()) {
//                    PositionModel pointOfInterest = POIs.getValue(PositionModel.class);
//                    pointOfInterestList.add(pointOfInterest);
//                }
//            }

//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
//            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void showData(DataSnapshot dataSnapshot) {
        PositionModel position = dataSnapshot.getValue(PositionModel.class);
        pointOfInterestList.add(position);
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");
        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLocationPermGranted) {
                final Task location = mFusedLocation.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();
                            if (isTooClose(currentLocation)) {
                            }
//                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
//                                    DEFAULT_ZOOM);
                        } else {
                            Log.d(TAG, "onComplete: current location is null");
//                            Toast.makeText(MapActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PERMGRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PERMGRANTED) {
                mLocationPermGranted = true;
//                initMap();
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    public boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: checking google services version");

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermGranted = false;
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermGranted = true;
                    //initialize our map
//                    initMap();
                }
            }
        }
    }

    private void message(String messageKey) {
        Toast.makeText(this, messageKey, Toast.LENGTH_LONG).show();
    }

    private void migrateDataPOIs() {
        myRef = FirebaseDatabase.getInstance().getReference("POIs");
        ArrayList<PositionModel> dataMigrated = new ArrayList<>();
        dataMigrated.add(new PositionModel("Acropolis", "Archeological place high level", "builtings", 37.968211, 23.720583));
        dataMigrated.add(new PositionModel("kalhmarmaro", "Archeological place high level", "builtings", 37.969252, 23.740268));
        dataMigrated.add(new PositionModel("Stiles tou Dios", "Archeological place high level", "builtings", 37.968685, 23.731924));
        dataMigrated.add(new PositionModel("Arxaia Korinthos", "Archeological place high level", "city", 37.904397, 22.877133));
        myRef.setValue(dataMigrated);
        hasRun = true;

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
