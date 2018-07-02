package com.example.walkingschoolbus;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.security.PrivateKey;
import java.util.List;
import java.util.Map;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;


/**
 * Simple test app to show a Google Map.
 * - If using the emulator, Create an Emulator from the API 26 image.
 *   (API27's doesn't/didn't support maps; nor will 24 or before I believe).
 * - Accessing Google Maps requires an API key: You can request one for free (and should!)
 *   see /res/values/google_maps_api.xml
 * - More notes at the end of this file.
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient client;
    private Location mylocation;

    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }


        //check network provider is good
        if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    double latitude =  location.getLatitude();
                    double longitude = location.getLongitude();

                    LatLng latlng =  new LatLng(latitude, longitude);
                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    //try {
                        //List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);

                        mMap.addMarker(new MarkerOptions().position(latlng));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 10f));
                    //}catch (IOException e) {
                      //  e.printStackTrace();
                    //}

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
        }
        else if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    double latitude =  location.getLatitude();
                    double longitude = location.getLongitude();
                    LatLng latlng =  new LatLng(latitude, longitude);
                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    //try {
                    //List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);

                    mMap.addMarker(new MarkerOptions().position(latlng));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 5f));

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

        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        Log.d("STEP1", "Code is good here in step1");


        mMap = googleMap;

        // LatLng sydney = new LatLng(-47,128);
         // mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
         // mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

    }



    public static Intent makeIntent(Context context){
        Intent intent = new Intent(context, MapsActivity.class);
        return intent;
    }
}


/*
    Google API
    - RUN ON ANDROID API 26! (27 does not show maps, 24 does not include support).

        - Install "Google APIs Intel x86 Atom System Image"
        Tools -> Android -> SDK Manager
        Check "Show Package Details" (bottom right)
        Expand selected SDK level, check "Google APIs Intel x86 Atom System Image", install

        - Create AVD (Android Virtual Device; i.e., an emulator) targeting new "Google APIs Intel x86 Atom System Image"

        - In AVD, install "Fake GPS Location" app to fake locations
        AVD: Set as mock location app:
        - Phone Settings -> About
        - Keep taping build number to unlock developer options
        - Settings -> Developer Options -> Select mock location app
        Run app, click start; leave running.


        - Add dependencies:
        File -> Project Structure -> app (on left); Dependencies tab
        Add:
        com.google.android.gms:play-services-location:11.8.0
        com.google.android.gms:play-services-maps:11.8.0

        - App
        - Verify Play services in onResume()
        - Add location permissions



        ///////////////////////////////////////////////////////////////
        Maps
        ///////////////////////////////////////////////////////////////
        - Run on emulator at API 26
        - Get Google Maps API key:
        developers.google.com/maps/documentation/android/start
        * Don't create a new project, just add:
        Right click on package, New -> Activity -> Gallerey: select Google Maps Activity


        ERROR: Maps Android API: Google Maps Android API v2 only supports devices with OpenGL ES 2.0 and above
        - Check OpenGL Version via a hardware info app. If OpenGL ES 1.0, Run on API 26 (not 27!)

*/