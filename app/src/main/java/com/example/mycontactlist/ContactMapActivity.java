package com.example.mycontactlist;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PackageManagerCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.List;

public class ContactMapActivity extends AppCompatActivity {

    LocationManager locationManager;
    LocationListener gpsListener;

    final int PERMISSION_REQUEST_LOCATION = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contact_map);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_contact_map), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        locationButton();
        settingButton();
        ListButton();
        initGetLocationButton();
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


    private void initGetLocationButton(){
        Button location = findViewById(R.id.buttonGC);
        location.setOnClickListener( v -> {


            try {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (ContextCompat.checkSelfPermission(ContactMapActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                ContactMapActivity.this,
                                Manifest.permission.ACCESS_FINE_LOCATION)) {

                            Snackbar.make(findViewById(R.id.activity_contact_map),
                                    "MyContactList requires this permission to locate " +
                                            "your contacts", Snackbar.LENGTH_INDEFINITE).setAction("OK",
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            ActivityCompat.requestPermissions(
                                                    ContactMapActivity.this,
                                                    new String[]{
                                                            Manifest.permission.ACCESS_FINE_LOCATION},
                                                    PERMISSION_REQUEST_LOCATION);
                                        }
                                    }).show();

                        } else {
                            ActivityCompat.requestPermissions(ContactMapActivity.this, new String[]{
                                    Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
                        }
                    } else {
                        startLocationUpdates();
                    }

                } else {
                    startLocationUpdates();
                }
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), "Error requesting permission", Toast.LENGTH_LONG).show();

            }




//            EditText editAddress = findViewById(R.id.editTextStreetAddress);
//            EditText editCity = findViewById(R.id.editTextCity);
//            EditText editState = findViewById(R.id.editTextState);
//            EditText editZip = findViewById(R.id.editTextZip);
//
//            String address = editAddress.getText().toString() + ", " +
//                    editCity.getText().toString() + ", " +
//                    editState.getText().toString() + ", " +
//                    editZip.getText().toString();
//
//            List<Address> addresses = null;
//
//            Geocoder geo = new Geocoder(ContactMapActivity.this);
//
//            try {
//                locationManager = (LocationManager) getBaseContext().getSystemService(Context.LOCATION_SERVICE);
//
//
//                gpsListener = new LocationListener() {
//                    @Override
//                    public void onLocationChanged(@NonNull Location location) {
//                        TextView txtLatitude = (TextView) findViewById(R.id.textViewLatitude);
//                        TextView txtLongitude = (TextView) findViewById(R.id.textViewLongitude);
//                        TextView txtAccuracy = (TextView) findViewById(R.id.textViewAccuarcy);
//
//                        txtLatitude.setText(String.valueOf(location.getLatitude()));
//                        txtLongitude.setText(String.valueOf(location.getLongitude()));
//                        txtAccuracy.setText(String.valueOf(location.getAccuracy()));
//                    }
//
//                    public void onStatusChanged(String provider, int status, Bundle extra) {
//                    }
//
//                    public void onProviderEnabled(String provider) {
//                    }
//
//                    public void onProviderDisabled(String provider) {
//                    }
//                };
//
//                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsListener);
//
//                addresses = geo.getFromLocationName(address, 1);
//            }
//            catch (IOException e){
//                // ;
//            } catch (Exception e) {
//                Toast.makeText(getBaseContext(), "Error location not available", Toast.LENGTH_LONG).show();
//            }
//
//            try {
//            locationManager.removeUpdates(gpsListener);
//        }
//
//        catch (Exception e) {
//            e.printStackTrace();
//
//        }



        });
    }

    @Override
    public void onPause(){
        super.onPause();

        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(getBaseContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getBaseContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;

        }

        EditText editAddress = findViewById(R.id.editTextStreetAddress);
        EditText editCity = findViewById(R.id.editTextCity);
        EditText editState = findViewById(R.id.editTextState);
        EditText editZip = findViewById(R.id.editTextZip);

        String address = editAddress.getText().toString() + ", " +
                editCity.getText().toString() + ", " +
                editState.getText().toString() + ", " +
                editZip.getText().toString();


        List<Address> addresses = null;

        Geocoder geo = new Geocoder(ContactMapActivity.this);

        try {
            locationManager = (LocationManager) getBaseContext().getSystemService(Context.LOCATION_SERVICE);
            gpsListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    TextView txtLatitude = (TextView) findViewById(R.id.textViewLatitude);
                    TextView txtLongitude = (TextView) findViewById(R.id.textViewLongitude);
                    TextView txtAccuracy = (TextView) findViewById(R.id.textViewAccuarcy);

                    txtLatitude.setText(String.valueOf(location.getLatitude()));
                    txtLongitude.setText(String.valueOf(location.getLongitude()));
                    txtAccuracy.setText(String.valueOf(location.getAccuracy()));
                }

                public void onStatusChanged(String provider, int status, Bundle extra) {
                }

                public void onProviderEnabled(String provider) {
                }

                public void onProviderDisabled(String provider) {
                }
            };

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsListener);

            addresses = geo.getFromLocationName(address, 1);
        }
        catch (SecurityException e){

            Toast.makeText(getBaseContext(), "Error location not available", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
             locationManager.removeUpdates(gpsListener);
            }

        catch (Exception e) {
             e.printStackTrace();

        }




    }

    private void startLocationUpdates(){
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(getBaseContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getBaseContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return ;
        }
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
}
