package com.example.healthybuddy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
//import com.google.android.gms.location.places.Place;
//import com.google.android.libraries.places.compat.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;

import com.google.android.libraries.places.api.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import noman.googleplaces.NRPlaces;
import noman.googleplaces.PlaceType;
import noman.googleplaces.PlacesException;
import noman.googleplaces.PlacesListener;

public class GymActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        PlacesListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener{

        private GoogleMap mMap;
        private Marker currentMarker=null;

        private static final String TAG = "googlemap_example";
        private static final int GPS_ENABLE_REQUEST_CODE=2001;
        private static final int UPDATE_INTERVAL_MS=1000;
        private static final int FASTEST_UPDATE_INTERVAL_MS=500;

        // onRequestPermissionsResult?????? ????????? ????????????
        // ActivityCompat.requestPermissions??? ????????? ????????? ????????? ???????????? ?????? ?????????
        private static final int PERMISSIONS_REQUEST_CODE=100;
        boolean needRequest=false;


        // ?????? ???????????? ?????? ????????? ???????????? ??????
        String[] REQUIRED_PERMISSIONS =
                {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION}; // ?????? ?????????

        Location mCurrentLocation;
        LatLng currentPosition;
        LatLng aroundGym;

        private FusedLocationProviderClient mFusedLocationClient;
        private LocationRequest locationRequest;
        private Location location;

        private View mLayout; // Snackbar ????????? ????????? view ??????

        List<Marker> previous_marker=null;

        //private Button current;

        AutocompleteSupportFragment autocompleteFragment;

        // context ??????
        public static Context context;
        public String title, snippet;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            setContentView(R.layout.activity_gym);
            setTitle("Google Map");

            Intent inIntent = getIntent();

            Places.initialize(getApplicationContext(), "AIzaSyBa8_gRF_-iuEs_BcPQaBmQFYSfO3KnEzk");
            PlacesClient placesClient = Places.createClient(this);

            mLayout = findViewById(R.id.layout_gym);


            locationRequest = new LocationRequest()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(UPDATE_INTERVAL_MS)
                    .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

            LocationSettingsRequest.Builder builder =
                    new LocationSettingsRequest.Builder();
            builder.addLocationRequest(locationRequest);

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);


            // ????????? ??????
            previous_marker = new ArrayList<Marker>();

            Button button = (Button) findViewById(R.id.button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPlaceInformation(currentPosition);
                }
            });

            autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG));

            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    Log.i(TAG, "Place:" + place.getLatLng().latitude);

                    LatLng latLng = new LatLng(place.getLatLng().latitude,
                            place.getLatLng().longitude);
                    String markerSnippet = getCurrentAddress(latLng);

                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title(place.getName());
                    markerOptions.snippet(markerSnippet);
                    mMap.clear();
                    mMap.addMarker(markerOptions);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

                }

                @Override
                public void onError(@NonNull Status status) {
                    Log.i(TAG, "An error occurred: " + status);
                }
            });

        }

        private Location getLocationFromAddress(Context context, String address) {
            Geocoder geocoder = new Geocoder(context);
            List<Address> addresses;
            Location resLocation = new Location("");
            try {
                addresses = geocoder.getFromLocationName(address, 5);
                if((addresses == null) || (addresses.size() == 0)) {
                    return null;
                }
                Address addressLoc = addresses.get(0);

                resLocation.setLatitude(addressLoc.getLatitude());
                resLocation.setLongitude(addressLoc.getLongitude());

            } catch (Exception e) {
                e.printStackTrace();
            }
            return resLocation;
        }

        private void showCurrentLocation(Location location) {
            LatLng curPoint = new LatLng(location.getLatitude(), location.getLongitude());
            String msg = "Latitutde : " + curPoint.latitude
                    + "\nLongitude : " + curPoint.longitude;
            //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

            //?????? ??????, ????????? ????????? ??????
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(curPoint, 15));

            //?????? ??????
            Location targetLocation = new Location("");
            //targetLocation.setLatitude(37.4937);
            //targetLocation.setLongitude(127.0643);
            targetLocation.setLatitude(curPoint.latitude);
            targetLocation.setLongitude(curPoint.longitude);
            Log.d(TAG, msg);
            showMyMarker(targetLocation);
        }

        private void showMyMarker(Location location) {
            MarkerOptions markerOptions = new MarkerOptions();
            //if(markerOptions == null) {
            if(currentMarker!=null) currentMarker.remove();

                markerOptions.position(new LatLng(location.getLatitude(), location.getLongitude()));
                markerOptions.title("??? ??????");
                markerOptions.snippet(getCurrentAddress(new LatLng(location.getLatitude(), location.getLongitude())));
                markerOptions.draggable(true);
                mMap.addMarker(markerOptions);
            //}
        }

        // ????????????
        private void requestMyLocation() {
            LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            try {
                long minTime = 1000;    //?????? ??????
                float minDistance = 0;  //????????? ????????? ?????? ??????

                manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        showCurrentLocation(location);
                    }

                    @Override
                    public void onStatusChanged(String s, int i, Bundle bundle) {

                    }

                    @Override
                    public void onProviderEnabled(String s) {

                    }

                    @Override
                    public void onProviderDisabled(String s) {

                    }
                });
           } catch (SecurityException e) {
                e.printStackTrace();
            }
        }


        @Override
        public void onMapReady(final GoogleMap googleMap){
            Log.d(TAG, "onMapReady :");

            mMap = googleMap;


            // ????????? ????????? ?????? ??????????????? GPS ?????? ?????? ???????????? ???????????????
            // ????????? ?????? ????????? ????????? ??????
            //setDefaultLocation();

            // ????????? ????????? ??????
            // 1. ?????? ???????????? ????????? ????????? ??????
            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION);

            if(hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
                // 2. ?????? ???????????? ????????? ?????????
                startLocationUpdates();
            } else {
                // 2. ????????? ????????? ????????? ?????? ????????? ????????? ?????? ??????

                // 3-1. ???????????? ????????? ????????? ??? ??? ?????? ??????
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])){
                    Snackbar.make(mLayout,"??? ?????? ??????????????? ?????? ?????? ????????? ???????????????.",
                            Snackbar.LENGTH_INDEFINITE).setAction("??????", new View.OnClickListener(){
                                @Override
                                public void onClick(View view){
                                    ActivityCompat.requestPermissions(GymActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
                                }
                    }).show();
                } else {
                    // 4-1. ???????????? ????????? ????????? ??? ?????? ?????? ??????
                    ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
                }
            }

            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(@NonNull LatLng latLng) {
                    Log.d(TAG, "onMapClick :");
                }
            });

            // ?????? ????????? ?????? ????????? ??????
            mMap.setOnMarkerClickListener(this);
            mMap.setOnInfoWindowClickListener(this);
       
        }

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                List<Location> locationList = locationResult.getLocations();

                if(locationList.size()>0){
                    location = locationList.get(locationList.size()-1);

                    currentPosition = new LatLng(location.getLatitude(), location.getLongitude());

                    String markerTitle = getCurrentAddress(currentPosition);
                    String markerSnippet = "??????: "+
                            String.valueOf(location.getLatitude())+" ??????:" + String.valueOf(location.getLongitude());

                    Log.d(TAG, "onLocationResult : "+markerSnippet);

                    //?????? ????????? ?????? ???????????? ??????
                    //setCurrentLocation(location, markerTitle, markerSnippet);

                    mCurrentLocation = location;
                }
            }
        };


        private void startLocationUpdates(){
            if(!checkLocationServicesStatus()){
                showDialogForLocationServiceSetting();
            }else{
                int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
                int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

                if(hasFineLocationPermission != PackageManager.PERMISSION_GRANTED || hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED){
                    return;
                }

                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

                if(checkPermission())
                    mMap.setMyLocationEnabled(true);
            }
        }

        @Override
        protected  void onStart(){
            super.onStart();

            if(checkPermission()){
                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

                if(mMap!=null)
                    mMap.setMyLocationEnabled(true);
            }
        }

        @Override
        protected void onStop(){
            super.onStop();

            if(mFusedLocationClient != null){
                mFusedLocationClient.removeLocationUpdates(locationCallback);
            }
        }

        public String getCurrentAddress(LatLng latlng){
            // ???????????? Gps??? ????????? ??????
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());

            List<Address>addresses;

            try{
                addresses = geocoder.getFromLocation(
                        latlng.latitude,
                        latlng.longitude,
                        1);
            } catch(IOException ioException){
                // ???????????? ??????
                Toast.makeText(this, "???????????? ????????? ????????????", Toast.LENGTH_LONG).show();
                return "???????????? ????????? ????????????";
            } catch(IllegalArgumentException illegalArgumentException){
                Toast.makeText(this,"????????? GPS ??????", Toast.LENGTH_LONG).show();
                return "????????? GPS ??????";
            }

            if(addresses==null||addresses.size()==0){
                Toast.makeText(this,"?????? ?????????", Toast.LENGTH_LONG).show();
                return "?????? ?????????";
            } else {
                Address address = addresses.get(0);
                return address.getAddressLine(0).toString();
            }
        }

        public boolean checkLocationServicesStatus(){
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }

        boolean user = false;

        public void setCurrentLocation(Location location,String markerTitle, String markerSnippet){

            //boolean user = false;

            if(currentMarker!=null) currentMarker.remove();

            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(currentLatLng);
            markerOptions.title(markerTitle);
            markerOptions.snippet(markerSnippet);
            markerOptions.draggable(true);

            mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    user = false;

                    return false;
                }
            });

            currentMarker = mMap.addMarker(markerOptions);

            if(user == false){
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
                mMap.moveCamera(cameraUpdate);
            }


        }

        public void setDefaultLocation(){
            //????????? ??????, Seoul
            LatLng DEFAULT_LOCATION = new LatLng(37.56,126.97);
            String markerTitle="???????????? ????????? ??? ??????";
            String markerSnippet = "?????? ???????????? GPS ?????? ?????? ???????????????.";

            if(currentMarker!=null) currentMarker.remove();

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(DEFAULT_LOCATION);
            markerOptions.title(markerTitle);
            markerOptions.snippet(markerSnippet);
            markerOptions.draggable(true);

            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            currentMarker = mMap.addMarker(markerOptions);

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
            mMap.moveCamera(cameraUpdate);

        }

        // ????????? ????????? ????????? ?????? ?????????
        private boolean checkPermission(){
            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

            if(hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED){
                return true;
            }
            return false;
        }


        @Override
        public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grandResults) {

            super.onRequestPermissionsResult(permsRequestCode, permissions, grandResults);
            if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {
                boolean check_result = true;


                for (int result : grandResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        check_result = false;
                        break;
                    }
                }

                if (check_result) {
                    startLocationUpdates();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0]) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {
                        Snackbar.make(mLayout, "???????????? ?????????????????????. ?????? ?????? ???????????? ???????????? ??????????????????.",
                                Snackbar.LENGTH_INDEFINITE).setAction("??????", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                finish();
                            }
                        }).show();
                    } else {
                        Snackbar.make(mLayout, "???????????? ?????????????????????. ??????(??? ??????)?????? ???????????? ???????????? ?????????.",
                                Snackbar.LENGTH_INDEFINITE).setAction("??????", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                finish();
                            }
                        }).show();
                    }
                }
            }
        }


        // GPS ???????????? ?????? ?????????
        private void showDialogForLocationServiceSetting(){
            AlertDialog.Builder builder = new AlertDialog.Builder(GymActivity.this);
            builder.setTitle("?????? ????????? ????????????");
            builder.setMessage("?????? ???????????? ???????????? ?????? ???????????? ??????????????? \n"+"?????? ????????? ?????????????????????????");
            builder.setCancelable(true);
            builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent callGPSSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
                }
            });
            builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            builder.create().show();
        }

        @Override
        protected  void onActivityResult(int requestCode, int resultCode, Intent data){
            super.onActivityResult(requestCode, resultCode, data);

            switch (requestCode){
                case GPS_ENABLE_REQUEST_CODE:
                    if(checkLocationServicesStatus()){
                        if(checkLocationServicesStatus()){
                            needRequest=true;
                            return;
                        }
                    }
                    break;
            }
        }

        @Override
        public void onPlacesFailure(PlacesException e){

        }

        @Override
        public void onPlacesStart(){

        }

    @Override
    public void onPlacesSuccess(List<noman.googleplaces.Place> places) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for(noman.googleplaces.Place place : places){
                    LatLng latLng = new LatLng(place.getLatitude(),
                            place.getLongitude());
                    String markerSnippet = getCurrentAddress(latLng);

                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title(place.getName());
                    markerOptions.snippet(markerSnippet);
                    Marker item = mMap.addMarker(markerOptions);
                    previous_marker.add(item);
                }

                // ?????? ?????? ??????
                HashSet<Marker> hashSet = new HashSet<Marker>();
                hashSet.addAll(previous_marker);
                previous_marker.clear();
                previous_marker.addAll(hashSet);
            }
        });
    }


        @Override
        public void onPlacesFinished(){

        }

        public void showPlaceInformation(LatLng location){
            mMap.clear();

            if(previous_marker != null)
                previous_marker.clear();


                new NRPlaces.Builder()
                        .listener(GymActivity.this)
                        .key("AIzaSyBa8_gRF_-iuEs_BcPQaBmQFYSfO3KnEzk")
                        .latlng(location.latitude, location.longitude) // ?????? ??????
                        .radius(500) // 500?????? ????????? ??????
                        .type(PlaceType.GYM) // ?????????
                        .build()
                        .execute();

            Log.d("test", "??? ??????");


        }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        Log.d("test", marker.getTitle());
        //Toast.makeText(this, marker.getTitle() + "\n"+marker.getPosition(), Toast.LENGTH_SHORT).show();
        marker.showInfoWindow();
        return true;
    }


    @Override
    public void onInfoWindowClick(Marker marker) {
        title = marker.getTitle();
        snippet = marker.getSnippet();
        Intent outIntent = new Intent(getApplicationContext(), ProfileActivity.class);
        outIntent.putExtra("Gym",title + " "+snippet);
        setResult(RESULT_OK, outIntent);
        finish();
    }
}

