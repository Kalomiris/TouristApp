package com.kalom.UnipiTouristicApp;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements LocationListener {

    TextView racicalText;
    Button setDistanceButton;
    DatabaseReference myRef;
    LocationManager mLocationManager;
    Location location;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    //    private GoogleMap mMap;
    private static ArrayList<PositionModel> pointOfInterestList = new ArrayList<>();
    private static boolean hasRun = false;
    private static final String TAG = "MainActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final int PERMGRANTED = PackageManager.PERMISSION_GRANTED;
    private boolean mLocationPermissionsGranted = false;
    ArrayList<String> dataBasePointsOfInterest = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        racicalText = findViewById(R.id.textViewRadical);
        setDistanceButton = findViewById(R.id.button);
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        getPOIsList();
        getLocationPermission();

        if (!hasRun) {
            migrateDataPOIs();
            hasRun = true;
        }
        setDistanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLocationPermissionsGranted && isServicesOK()) {
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
        for (PositionModel poisLocation : pointOfInterestList)
            if (racicalText != null) {
                float distance = location.distanceTo(poisLocation.getGeoloc());
                if (racicalText.getText().toString().compareTo(Float.toString(distance)) < 0) {
                    message("Your location is close to point of interest..." + "in " + poisLocation.getTitle());
                    return true;
                }
            }
        message("Set up your device, error...");
        return false;
    }

    private void getPOIsList() {
        myRef = FirebaseDatabase.getInstance().getReference("POIs");
        myRef.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                showData(dataSnapshot);
//                pointOfInterestList.clear();
//                for (DataSnapshot POIs : dataSnapshot.getChildren()) {
//                    PositionModel pointOfInterest = POIs.getValue(PositionModel.class);
//                    pointOfInterestList.add(pointOfInterest);
//                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void showData(DataSnapshot dataSnapshot) {
        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            PositionModel pointOfInterest = new PositionModel();
            pointOfInterest.setTitle(Objects.requireNonNull(ds.getValue(PositionModel.class)).getTitle()); //Set title
            pointOfInterest.setCateg(Objects.requireNonNull(ds.getValue(PositionModel.class).getCateg())); //Set Gategory
            pointOfInterest.setDesc(Objects.requireNonNull(ds.getValue(PositionModel.class).getDesc())); //Set Description
            pointOfInterest.setGeoloc(Objects.requireNonNull(ds.getValue(PositionModel.class).getGeoloc())); //Set Location Object
        }
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLocationPermissionsGranted) {
                final Task location = mFusedLocationProviderClient.getLastLocation();
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
                mLocationPermissionsGranted = true;
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
        mLocationPermissionsGranted = false;
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
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
        String id = myRef.push().getKey();
        Location geoloc1 = new Location("");
        geoloc1.setLatitude(37.968211);
        geoloc1.setLongitude(23.720583);
        PositionModel position1 = new PositionModel("Acropolis", "Archeological place high level", "builtings", geoloc1);

        Location geoloc2 = new Location("");
        geoloc2.setLatitude(37.969252);
        geoloc2.setLongitude(23.740268);
        PositionModel position2 = new PositionModel("kalhmarmaro", "Archeological place high level", "builtings", geoloc2);

        Location geoloc3 = new Location("");
        geoloc3.setLatitude(37.968685);
        geoloc3.setLongitude(23.731924);
        PositionModel position3 = new PositionModel("Stiles tou Dios", "Archeological place high level", "builtings", geoloc3);

        ArrayList<PositionModel> dataMigrated = new ArrayList<>();
        dataMigrated.add(position1);
        dataMigrated.add(position2);
        dataMigrated.add(position3);
        myRef.child(id).setValue(dataMigrated);
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
