package com.example.trademajestics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.trademajestics.databinding.ActivityMainBinding;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding mainBinding;
    private FusedLocationProviderClient fusedLocationClient;
    DataBaseHelper mdataBaseHelper;
    Location mLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mdataBaseHelper=new DataBaseHelper(this);
        checkLocationPermission();

        getAltitude();
    }

    private void getAltitude() {

        mainBinding.save.setOnClickListener(v -> {
                    Geocoder geocoder = new Geocoder(getBaseContext());
                    List<Address> addressList = null;
                    try {
                        addressList = geocoder.getFromLocationName(mainBinding.etAddress.getText().toString(), 1);
                        Address address = addressList.get(0);
                        float[] results = new float[1];
                        if (address!=null){
                            Location.distanceBetween(mLocation.getLatitude(), mLocation.getLongitude(),
                                    address.getLatitude(), address.getLongitude(), results);
                            mainBinding.etAltitude.setText(String.valueOf(results[0]));
                            long result= mdataBaseHelper.addContact(mainBinding.etAddress.getText().toString());
                            if (result>0){
                                mainBinding.etAddress.setText("");
                                List<String>mylist=mdataBaseHelper.getAllContacts();
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
        }
        );
        

        mainBinding.nextPage.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this,Show_AllLocation.class));
        });
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getBaseContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    101);

        } else {
            getCurrentLocation();
        }

    }

    private void getCurrentLocation() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            enableGpsProgramatically();
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                mLocation=location;
                                mainBinding.etLatitude.setText(String.valueOf(location.getLatitude()));
                                mainBinding.etLongitude.setText(String.valueOf(location.getLongitude()));
                                mainBinding.etAltitude.setText("0");

                            }
                        }
                    });
        }
    }

    void enableGpsProgramatically() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {

            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this,
                                102);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 102) {
            if (resultCode == Activity.RESULT_OK) {
                getCurrentLocation();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
                enableGpsProgramatically();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode==101){
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
                Toast.makeText(getApplicationContext(),
                        "Location permission has now been granted.",
                        Toast.LENGTH_LONG).show();

            } else {

                Toast.makeText(getApplicationContext(),
                        "Location permission was not granted.",
                        Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode==102){
            if (grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, permissions[0].toString()+"is granted", Toast.LENGTH_SHORT).show();
            }
        }
    }
}