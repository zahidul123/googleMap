package com.example.trademajestics;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Show_AllLocation extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    DataBaseHelper dataBaseHelper;
    List<String>allSavedLocation;
    Marker mMarker;
   Marker mCurrentLocationMarker;
    private FusedLocationProviderClient fusedLocationClient;
     CameraPosition cameraPosition;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show__all_location);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        dataBaseHelper=new DataBaseHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        allSavedLocation=dataBaseHelper.getAllContacts();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
         cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(23.7505129, 90.3950225)).zoom(7).build();

        mMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));
        mMap.setMaxZoomPreference(15f);
        mMap.setMinZoomPreference(11f);
        /*mMap.addMarker(new MarkerOptions().position(new LatLng(23.7505129, 90.3950225)).title("Bangladesh"));
        mMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));*/
       // getcurrentLocation();
        if (allSavedLocation.size()>0){
            mMap.clear();
            for (int i=0;i<allSavedLocation.size();i++){
                showAllMarker(i);
            }

        }
    }

    private void showAllMarker(int i) {


        MarkerOptions markerOptions = new MarkerOptions();

        Geocoder geocoder = new Geocoder(getBaseContext());
        List<Address> addressList = null;
        try {

            addressList = geocoder.getFromLocationName(allSavedLocation.get(i), 1);
            Address address = addressList.get(0);
            LatLng place = new LatLng(address.getLatitude(), address.getLongitude());
            markerOptions.position(place);
            markerOptions.title(address.getAddressLine(0));
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            mCurrentLocationMarker = mMap.addMarker(markerOptions);

        /*    mMarker = mMap.addMarker(new MarkerOptions()
                    .position(place)
                    .title()
                    .snippet(address.getAddressLine(0))
            );
          markerOptions=mMap.addMarker()*/

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getcurrentLocation() {
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

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {

                                LatLng sydney = new LatLng(location.getLatitude(), location.getLongitude());
                                Geocoder geocoder = new Geocoder(Show_AllLocation.this, Locale.getDefault());
                                try {
                                    List<Address> addresses= geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                    mMap.addMarker(new MarkerOptions().position(sydney).title(addresses.toString()));
                                    mMap.animateCamera(CameraUpdateFactory
                                            .newCameraPosition(cameraPosition));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

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
                        resolvable.startResolutionForResult(Show_AllLocation.this,
                                102);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }
}