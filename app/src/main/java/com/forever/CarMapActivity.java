package com.forever;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class CarMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    private Button mLogOut, mProfile;
    private Boolean isLoggingOut = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_map);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mLogOut = findViewById(R.id.logoutButton);
        // mProfile = findViewById(R.id.profileButton);

        mLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLoggingOut = true;
                disconnectCarDriver();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(CarMapActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

       /* mProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CarMapActivity.this, UserProfileActivity.class);
                startActivity(intent);
                return;
            }
        });*/
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);//every second;
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //if version of Android OS later than Lollipop;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
            } else {
                checkLocationPermission();
            }
        }
    }

    LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {
                    mLastLocation = location;
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                    if (!getCarsAroundStarted)
                        getCarsAround();
                    if (!getTrucksAroundStarted)
                        getTrucksAround();

                    String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("CarsOnline");

                    GeoFire geoFire = new GeoFire(myRef);
                    geoFire.setLocation(user_id, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {

                        @Override
                        public void onComplete(String key, DatabaseError error) {
                        }
                    });
                }
            }
        }
    };

    private void checkLocationPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //Info dialog for user to grant permissions;
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                new AlertDialog.Builder(this)
                        .setTitle("No Permission")
                        .setMessage("Please, enable Geolocation in Settings")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                ActivityCompat.requestPermissions(CarMapActivity.this, new String[]
                                        {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(CarMapActivity.this, new String[]
                        {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }

                }else {
                    Toast.makeText(getApplicationContext(), "Please, enable Geolocation in Settings", Toast.LENGTH_LONG).show();
                }
            }
            break;
        }
    }

    private void connecCarDriver(){
        checkLocationPermission();
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);
    }

    private void disconnectCarDriver(){
        if(mFusedLocationClient != null){
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("CarsOnline");

            GeoFire geoFire = new GeoFire(ref);
            geoFire.removeLocation(userId, new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {

                }
            });
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        if(!isLoggingOut)
            disconnectCarDriver();
    }

    //get the active Trucks drivers in the provided radius;
    boolean getTrucksAroundStarted = false;
    List<Marker> markerListTrucks = new ArrayList<Marker>();
    private void getTrucksAround(){
        getTrucksAroundStarted = true;
        DatabaseReference trucksLocation = FirebaseDatabase.getInstance().getReference().child("TrucksOnline");
        GeoFire geoFire = new GeoFire(trucksLocation);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()), 10000);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            //the data been created in the database;
            public void onKeyEntered(String key, GeoLocation location) {
                for(Marker markerIterator : markerListTrucks){
                    if(markerIterator.getTag().equals(key)){
                        return;
                    }
                }

                LatLng userLocation = new LatLng(location.latitude, location.longitude);

                Marker mTruckMarker = mMap.addMarker(new MarkerOptions()
                        .position(userLocation).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_trucks_around)));
                mTruckMarker.setTag(key);

                markerListTrucks.add(mTruckMarker);
            }

            @Override
            //the user stops been available;
            public void onKeyExited(String key) {
                for(Marker markerIterator : markerListTrucks){
                    if(markerIterator.getTag().equals(key)){
                        markerIterator.remove();
                        markerListTrucks.remove(markerIterator);
                        return;
                    }
                }
            }

            @Override
            //if the user moved - the data changes in the database;
            public void onKeyMoved(String key, GeoLocation location) {
                for(Marker markerIterator : markerListTrucks){
                    if(markerIterator.getTag().equals(key)){
                        markerIterator.setPosition(new LatLng(location.latitude, location.longitude));

                    }
                }
            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    //get the active Cars drivers in the provided radius;
    boolean getCarsAroundStarted = false;
    List<Marker> markerListCars = new ArrayList<Marker>();
    private void getCarsAround(){
        getCarsAroundStarted = true;
        DatabaseReference carsLocation = FirebaseDatabase.getInstance().getReference().child("CarsOnline");
        GeoFire geoFire = new GeoFire(carsLocation);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()), 10000);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            //the data been created in the database;
            public void onKeyEntered(String key, GeoLocation location) {
                for(Marker markerIterator : markerListCars){
                    if(markerIterator.getTag().equals(key)){
                        return;
                    }
                }

                LatLng userLocation = new LatLng(location.latitude, location.longitude);

                Marker mCarMarker = mMap.addMarker(new MarkerOptions()
                        .position(userLocation).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_cars_around)));
                mCarMarker.setTag(key);



                markerListCars.add(mCarMarker);
            }

            @Override
            //the user stops been available;
            public void onKeyExited(String key) {
                for(Marker markerIterator : markerListCars){
                    if(markerIterator.getTag().equals(key)){
                        markerIterator.remove();
                        markerListCars.remove(markerIterator);
                        return;
                    }
                }
            }

            @Override
            //if the user moved - the data changes in the database;
            public void onKeyMoved(String key, GeoLocation location) {
                for(Marker markerIterator : markerListCars){
                    if(markerIterator.getTag().equals(key)){
                        markerIterator.setPosition(new LatLng(location.latitude, location.longitude));

                    }
                }
            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }
}
