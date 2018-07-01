package com.example.walkingschoolbus;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Boolean mLocationPermissionsGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 0;
    private static final float DEFAULT_ZOOM = 14f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        getUserLocationPermission();

    }

    private void initMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    //TODO add dynamic location and delete this static ref to Sydney

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(mLocationPermissionsGranted){
            getUserLocation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
        }

        /*
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location myLocation = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
        
        //onLocationChanged(myLocation);

        //LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        //Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        double myLongitude = myLocation.getLongitude();
        double myLatitude = myLocation.getLatitude();
        LatLng myLatLng = new LatLng(myLatitude,myLongitude);




       // LatLng sydney = new LatLng(49,-123);
        mMap.addMarker(new MarkerOptions().position(myLatLng));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLatLng));
        */


    }


    private void getUserLocation(){
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            Task location = mFusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Location currentLocation = (Location) task.getResult();
                        moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),DEFAULT_ZOOM);
                    }
                }
            });
        }catch (SecurityException exception){
            Log.e("MapsActivity","Security Exception: "+exception.getMessage());
        }
    }

    /*
     *Moves the camera to a location.
     */
    private void moveCamera(LatLng latLng,float zoom){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
    }

    /*
     *Explicitly checks location permissions.
     */
    private void getUserLocationPermission(){
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION};

        //If both ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION permissions are granted,
        //device location permissions will be granted.
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            mLocationPermissionsGranted = true;
            initMap();
        }else{
        //Else, request the user to enable location services for the map.
        //LOCATION_PERMISSION_REQUEST_CODE will be passed to onRequestPermissionsResult method
        //to verify the results of user's selection.
            ActivityCompat.requestPermissions(this,permissions,LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    /*
     *Check request permission results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
       mLocationPermissionsGranted = false;
       switch(requestCode){
           case LOCATION_PERMISSION_REQUEST_CODE:
               if(grantResults.length > 0){
                   for(int i = 0; i < grantResults.length; i++){
                       if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                           mLocationPermissionsGranted = false;
                           return;
                       }
                   }
                   mLocationPermissionsGranted = true;
               }
       }
    }

    public static Intent makeIntent(Context context){
        Intent intent = new Intent(context, MapsActivity.class);
        return intent;
    }
}
