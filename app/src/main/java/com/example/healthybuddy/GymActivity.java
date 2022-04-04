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

        // onRequestPermissionsResult에서 수신된 결과에서
        // ActivityCompat.requestPermissions를 사용한 퍼미션 요청을 구별하기 위해 사용됨
        private static final int PERMISSIONS_REQUEST_CODE=100;
        boolean needRequest=false;


        // 앱을 실행하기 위해 필요한 퍼미션을 정의
        String[] REQUIRED_PERMISSIONS =
                {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION}; // 외부 저장소

        Location mCurrentLocation;
        LatLng currentPosition;
        LatLng aroundGym;

        private FusedLocationProviderClient mFusedLocationClient;
        private LocationRequest locationRequest;
        private Location location;

        private View mLayout; // Snackbar 사용을 위해서 view 필요

        List<Marker> previous_marker=null;

        //private Button current;

        AutocompleteSupportFragment autocompleteFragment;

        // context 변수
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


            // 헬스장 표시
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

            //화면 확대, 숫자가 클수록 확대
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(curPoint, 15));

            //마커 찍기
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
                markerOptions.title("내 위치");
                markerOptions.snippet(getCurrentAddress(new LatLng(location.getLatitude(), location.getLongitude())));
                markerOptions.draggable(true);
                mMap.addMarker(markerOptions);
            //}
        }

        // 위치확인
        private void requestMyLocation() {
            LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            try {
                long minTime = 1000;    //갱신 시간
                float minDistance = 0;  //갱신에 필요한 최소 거리

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


            // 런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기전에
            // 지도의 초기 위치를 서울로 이동
            //setDefaultLocation();

            // 런타임 퍼미션 처리
            // 1. 위치 퍼미션을 가지고 있는지 체크
            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION);

            if(hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
                // 2. 이미 퍼시면을 가지고 있으면
                startLocationUpdates();
            } else {
                // 2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청 필요

                // 3-1. 사용자가 퍼미션 거부를 한 적 있는 경우
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])){
                    Snackbar.make(mLayout,"이 앱을 실행하려면 위치 접근 권한이 필요합니다.",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener(){
                                @Override
                                public void onClick(View view){
                                    ActivityCompat.requestPermissions(GymActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
                                }
                    }).show();
                } else {
                    // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우
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

            // 마커 클릭에 대한 이벤트 처리
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
                    String markerSnippet = "위도: "+
                            String.valueOf(location.getLatitude())+" 경도:" + String.valueOf(location.getLongitude());

                    Log.d(TAG, "onLocationResult : "+markerSnippet);

                    //현재 위치에 마커 생성하고 이동
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
            // 지오코더 Gps를 주소로 변환
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());

            List<Address>addresses;

            try{
                addresses = geocoder.getFromLocation(
                        latlng.latitude,
                        latlng.longitude,
                        1);
            } catch(IOException ioException){
                // 네트워크 문제
                Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
                return "지오코더 서비스 사용불가";
            } catch(IllegalArgumentException illegalArgumentException){
                Toast.makeText(this,"잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
                return "잘못된 GPS 좌표";
            }

            if(addresses==null||addresses.size()==0){
                Toast.makeText(this,"주소 미발견", Toast.LENGTH_LONG).show();
                return "주소 미발견";
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
            //디폴트 위치, Seoul
            LatLng DEFAULT_LOCATION = new LatLng(37.56,126.97);
            String markerTitle="위치정보 가져올 수 없음";
            String markerSnippet = "위치 퍼미션과 GPS 활성 여부 확인하세요.";

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

        // 런타임 퍼미션 처리를 위한 메소드
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
                        Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.",
                                Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                finish();
                            }
                        }).show();
                    } else {
                        Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다.",
                                Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                finish();
                            }
                        }).show();
                    }
                }
            }
        }


        // GPS 활성화를 위한 메소드
        private void showDialogForLocationServiceSetting(){
            AlertDialog.Builder builder = new AlertDialog.Builder(GymActivity.this);
            builder.setTitle("위치 서비스 비활성화");
            builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다 \n"+"위치 설정을 수정하시겠습니까?");
            builder.setCancelable(true);
            builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent callGPSSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
                }
            });
            builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
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

                // 중복 마커 제거
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
                        .latlng(location.latitude, location.longitude) // 현재 위치
                        .radius(500) // 500미터 내에서 검색
                        .type(PlaceType.GYM) // 헬스장
                        .build()
                        .execute();

            Log.d("test", "왜 안돼");


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

