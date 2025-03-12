package com.example.mycontactlist;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PackageManagerCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ContactMapActivity extends AppCompatActivity implements OnMapReadyCallback {

//    LocationManager locationManager;
    LocationListener gpsListener;

    final int PERMISSION_REQUEST_CODE = 101;

    private SensorEventListener mySensorEventListener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}

        float[] accelerometerValues;
        float[] magneticValues;


        @Override
        public void onSensorChanged(SensorEvent event) {
            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                accelerometerValues = event.values;
            if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                magneticValues = event.values;
            if(accelerometerValues != null && magneticValues != null) {
                float R[] = new float[9];
                float I[] = new float[9];

                boolean success = SensorManager.getRotationMatrix(R, I, accelerometerValues, magneticValues);

                if(success) {
                    float orientation[] = new float[3];
                    SensorManager.getOrientation(R, orientation);

                    float azimut = (float) Math.toDegrees(orientation[0]);
                    if(azimut < 0.0f){
                        azimut += 360.0f;
                    }
                    String direction;

                    if(azimut > 315 || azimut < 45){
                        direction = "N";

                    }

                    else if (azimut >= 225 && azimut < 315) {
                        direction = "W";
                    }

                    else if (azimut >= 135 && azimut < 225) {
                        direction = "S";
                    }

                    else {
                        direction = "E";
                    }
                    textDirection.setText(direction);

                }

            }
        }

    };


    GoogleMap gMap;
    SensorManager sensorManager;
    Sensor accelerometer;
    Sensor magnetometer;
    TextView textDirection;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;

    ArrayList<Contact> contacts = new ArrayList<>();
    Contact currentContact = null;

    LocationListener networkListener;

    Location currentBestLocation;

    final int PERMISSION_REQUEST_LOCATION = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contact_map);
        Bundle extras = getIntent().getExtras();
        try {
            ContactDataSource ds = new ContactDataSource(ContactMapActivity.this);
            ds.open();
            if(extras != null){
                currentContact = ds.getSpecificContact(extras.getInt("contactID"));

            }

            else {
                contacts = ds.getContacts("contactname","ASC" );
            }
            ds.close();
        }
        catch (Exception e) {
            Toast.makeText(this, "Contact(s) could not be retrieved.", Toast.LENGTH_LONG).show();
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(this, "Error loading map", Toast.LENGTH_SHORT).show();
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_contact_map), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        locationButton();
        settingButton();
        ListButton();
        createLocationCallback();
        createLocationRequest();
        initMapTypeButton();



        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        if(accelerometer != null && magnetometer != null){
            sensorManager.registerListener(mySensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(mySensorEventListener, magnetometer, SensorManager.SENSOR_DELAY_FASTEST);
        }

        else {
            Toast.makeText(this, "Sensors not found", Toast.LENGTH_LONG).show();
        }

        textDirection = (TextView) findViewById(R.id.textHeading);


    }


    private void createLocationRequest() {
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setMinUpdateIntervalMillis(5000) // Equivalent to setFastestInterval()
                .build();

    }

    private void createLocationCallback(){
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if(locationResult == null){
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    Toast.makeText(getBaseContext(), "Lat: " + location.getLatitude() + "Long: "
                            + location.getLongitude() + "Accuarcy: " + location.getAccuracy(), Toast.LENGTH_LONG).show();

                }
            }
        };
    }

    private void startLocationUpdates() {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(getBaseContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getBaseContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback,null);
        gMap.setMyLocationEnabled(true);


    }

    private void stopLocationUpdates() {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(getBaseContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getBaseContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onPause(){
        super.onPause();
        stopLocationUpdates();

    }

    private void locationButton() {
        ImageButton ibList = findViewById(R.id.imageButtonLo);
        ibList.setOnClickListener(v -> {
            Intent intent = new Intent(ContactMapActivity.this, ContactMapActivity.class);
            intent.setFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }

    private void settingButton() {
        ImageButton ibList = findViewById(R.id.imageButtonSetting);
        ibList.setOnClickListener(v -> {
            Intent intent = new Intent(ContactMapActivity.this, ContactSettingsActivity.class);
            intent.setFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }



    private void ListButton() {
        ImageButton ibList = findViewById(R.id.imageButtonList);
        ibList.setOnClickListener(v -> {
            Intent intent = new Intent(ContactMapActivity.this, ContactListActivity.class);
            intent.setFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode) {
            case PERMISSION_REQUEST_LOCATION: {
                if(grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates();
                }

                else {
                    Toast.makeText(ContactMapActivity.this, "My ContactList will not locate your contacts.",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d("GoogleMaps", "onMapReady() called");
        gMap = googleMap;
        gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        Point size = new Point();
        WindowManager windowManager = getWindowManager();
        windowManager.getDefaultDisplay().getSize(size);
        int measuredWidth = size.x;
        int measuredHeight = size.y;
        if (contacts.size() > 0) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (int i = 0; i < contacts.size(); i++) {
                currentContact = contacts.get(i);

                Geocoder geocoder = new Geocoder(this);
                List<Address> addresses = null;
                String address = currentContact.getStreetAddress() + ", " +
                        currentContact.getCity() + ", " + currentContact.getState() + " "
                        + currentContact.getZipCode();

                try {
                    addresses = geocoder.getFromLocationName(address, 1);
                } catch (Exception e) {
                    Log.d("MapActivity", "Error", e);
                }
                LatLng point = new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
                builder.include(point);
                gMap.addMarker(new MarkerOptions().position(point).title(currentContact.getContactName()));
            }
            gMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), measuredWidth, measuredHeight, 30));
        } else {
            if (currentContact != null) {
                Geocoder geocoder = new Geocoder(this);
                List<Address> addresses = null;
                String address = currentContact.getStreetAddress() + ", " +
                        currentContact.getCity() + ", " + currentContact.getState() + " "
                        + currentContact.getZipCode();
                try {
                    addresses = geocoder.getFromLocationName(address, 1);
                } catch (Exception e) {
                    Log.d("MapActivity", "Error", e);
                }
                LatLng point = new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
                gMap.addMarker(new MarkerOptions().position(point).title(currentContact.getContactName()).snippet(address));
                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 16));
            } else {
                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("No Data");
                alertDialog.setMessage("No data is available for the mapping function");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                        (dialog, which) -> dialog.dismiss());
                alertDialog.show();
            }

        }

        try {
            if (Build.VERSION.SDK_INT >= 23) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        Snackbar.make(findViewById(R.id.main), "Location permission is needed to display location", Snackbar.LENGTH_INDEFINITE)
                                .setAction("OK", v1 -> ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE)).show();
                    } else {
                        startLocationUpdates();
                    }
                } else {
                    startLocationUpdates();
                }
            } else {
                startLocationUpdates();
            }
        } catch (Exception e) {
            Log.d("MapActivity", "Error", e);
            Toast.makeText(this, "Error. Location not available", Toast.LENGTH_LONG).show();
        }
    }

    private void initMapTypeButton() {
        RadioGroup rgMapType = findViewById(R.id.radioGroupMapType);
        rgMapType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rbNormal = findViewById(R.id.radioButtonNormal);
                if(rbNormal.isChecked()) {
                    gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }
                else {
                    gMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                }
            }
        });
    }


//    @Override
//    public void onMapReady(@NonNull GoogleMap googleMap) {
//        gMap = googleMap;
//        Log.d("MapsDebug", "Map is ready!");
//        gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//
//        Point size = new Point();
//        WindowManager w = getWindowManager();
//        w.getDefaultDisplay().getSize(size);
//
//        int measuredWidth = size.x;
//        int measuredHeight = size.y;
//        if(contacts.size() > 0) {
//            LatLngBounds.Builder builder = new LatLngBounds.Builder();
//            for (int i = 0; i < contacts.size(); i++) {
//                currentContact = contacts.get(i);
//
//                Geocoder geocoder = new Geocoder(this);
//                List<Address> addresses = null;
//
//
//                String address = currentContact.getStreetAddress() + ", " +
//                        currentContact.getCity() + ", " + currentContact.getState() + " "
//                        + currentContact.getZipCode();
//                try {
//                    addresses = geocoder.getFromLocationName(address, 1);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                if (addresses != null && !addresses.isEmpty()){
//                    LatLng point = new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
//                    builder.include(point);
//                    gMap.addMarker(new MarkerOptions().position(point).title(currentContact.getContactName()).snippet(address));
//                    Log.d("MapsDebug", "Map got address!");
//                } else {
//                    Log.e("MapsDebug", "Geocoder failed for: " + address);
//                }
//
//            }
//
//            gMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), measuredWidth,
//                    measuredHeight,450));
//
//        }
//        else {
//            if(currentContact != null) {
//                Geocoder geocoder = new Geocoder(this);
//                List<Address> addresses = null;
//
//                String address = currentContact.getStreetAddress() + ", " +
//                        currentContact.getCity() + ", " + currentContact.getState() + " "
//                        + currentContact.getZipCode();
//
//                try {
//                    addresses = geocoder.getFromLocationName(address, 1);
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                if (addresses != null && !addresses.isEmpty()) {
//                    LatLng point = new LatLng(addresses.get(0).getLatitude(),addresses.get(0).getLongitude());
//
//                    gMap.addMarker(new MarkerOptions().position(point).title(currentContact.getContactName()).snippet(address));
//                    gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 16));
//
//                }
//
//                else {
//                    Log.e("MapsDebug", "Geocoder failed for: " + address);
//                }
//
//
//            }
//            else {
//                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
//                alertDialog.setTitle("No Data");
//                alertDialog.setMessage("No data is available for the mapping function");
//                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
//                        (dialog, which) -> dialog.dismiss());
//                alertDialog.show();
//            }
//
//        }
//
//
//
//        try {
//            if (Build.VERSION.SDK_INT >= 23) {
//                if (ContextCompat.checkSelfPermission(ContactMapActivity.this,
//                        Manifest.permission.ACCESS_FINE_LOCATION) !=
//                        PackageManager.PERMISSION_GRANTED) {
//                    if (ActivityCompat.shouldShowRequestPermissionRationale(
//                            ContactMapActivity.this,
//                            Manifest.permission.ACCESS_FINE_LOCATION)) {
//
//                        Snackbar.make(findViewById(R.id.activity_contact_map),
//                                "MyContactList requires this permission to locate " +
//                                        "your contacts", Snackbar.LENGTH_INDEFINITE).setAction("OK",
//                                new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//                                        ActivityCompat.requestPermissions(
//                                                ContactMapActivity.this,
//                                                new String[]{
//                                                        Manifest.permission.ACCESS_FINE_LOCATION},
//                                                PERMISSION_REQUEST_LOCATION);
//                                    }
//                                }).show();
//
//                    } else {
//                        ActivityCompat.requestPermissions(ContactMapActivity.this, new String[]{
//                                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
//                    }
//                } else {
//                    startLocationUpdates();
//                }
//
//            } else {
//                startLocationUpdates();
//            }
//        } catch (Exception e) {
//            Toast.makeText(getBaseContext(), "Error requesting permission", Toast.LENGTH_LONG).show();
//
//
//        }
//
//
//    }


    //    private void initGetLocationButton(){
//        Button location = findViewById(R.id.buttonGC);
//        location.setOnClickListener( v -> {
//
//
//            try {
//                if (Build.VERSION.SDK_INT >= 23) {
//                    if (ContextCompat.checkSelfPermission(ContactMapActivity.this,
//                            Manifest.permission.ACCESS_FINE_LOCATION) !=
//                            PackageManager.PERMISSION_GRANTED) {
//                        if (ActivityCompat.shouldShowRequestPermissionRationale(
//                                ContactMapActivity.this,
//                                Manifest.permission.ACCESS_FINE_LOCATION)) {
//
//                            Snackbar.make(findViewById(R.id.activity_contact_map),
//                                    "MyContactList requires this permission to locate " +
//                                            "your contacts", Snackbar.LENGTH_INDEFINITE).setAction("OK",
//                                    new View.OnClickListener() {
//                                        @Override
//                                        public void onClick(View v) {
//                                            ActivityCompat.requestPermissions(
//                                                    ContactMapActivity.this,
//                                                    new String[]{
//                                                            Manifest.permission.ACCESS_FINE_LOCATION},
//                                                    PERMISSION_REQUEST_LOCATION);
//                                        }
//                                    }).show();
//
//                        } else {
//                            ActivityCompat.requestPermissions(ContactMapActivity.this, new String[]{
//                                    Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
//                            }
//                    } else {
//                       startLocationUpdates();
//                    }
//
//                } else {
//                    startLocationUpdates();
//                }
//            } catch (Exception e) {
//                Toast.makeText(getBaseContext(), "Error requesting permission", Toast.LENGTH_LONG).show();
//
//
//            }
//
//
//        });
//    }


//    private boolean isBetterLocation(Location location) {
//        boolean isBetter = false;
//        if (currentBestLocation == null) {
//            isBetter = true;
//        } else if (location.getAccuracy() <= currentBestLocation.getAccuracy()) {
//            isBetter = true;
//
//        } else if (location.getTime() - currentBestLocation.getTime() > 5*60*1000) {
//
//            isBetter = true;
//        }
//
//        return isBetter;
//
//    }
}
