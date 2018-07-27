//This is a google maps activity to allow users to add their kid to existing Walking Groups

package com.example.walkingschoolbus;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.location.LocationProvider;

import android.os.Build;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.walkingschoolbus.model.GpsLocation;
import com.example.walkingschoolbus.model.Group;
import com.example.walkingschoolbus.model.Session;
import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;


import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import retrofit2.Call;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;


/**
 * Simple test app to show a Google Map.
 * - If using the emulator, Create an Emulator from the API 26 image.
 *   (API27's doesn't/didn't support maps; nor will 24 or before I believe).
 * - Accessing Google Maps requires an API key: You can request one for free (and should!)
 *   see /res/values/google_maps_api.xml
 * - More notes at the end of this file.
 */

public class AddKidToGroupActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Boolean mLocationPermissionsGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 0;
    private static final float DEFAULT_ZOOM = 14f;
    WGServerProxy proxy;
    private Session token = Session.getInstance();
    private User kidUser;
    private Marker groupFinalLocationMarker;
    private Map<Marker, Long> markerLongHashMapMap = new HashMap<Marker, Long>();
    private static final int REQUEST_CODE = 3333;
    private static final String TAG = "AddKidToGroupActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        String tokenValue= token.getToken();

        proxy = ProxyBuilder.getProxy(getString(R.string.api_key),tokenValue);
        getUserLocationPermission();

    }

    /**
     * Retrieve kid information from previous activity.
     */
    private void getKidDetails(){
        Intent intent = getIntent();
        String kidEmail = intent.getExtras().getString("kidEmail");

        Call<User> kidCaller = proxy.getUserByEmail(kidEmail);
        ProxyBuilder.callProxy(AddKidToGroupActivity.this, kidCaller, returnedKid -> responseKid(returnedKid));
    }
    private void responseKid(User theReturnedKid){
        kidUser = theReturnedKid;
        if(kidUser != null && kidUser.getEmail() != null){
            Log.i("Debug42","Successfully retrieved kidUser");
        }
    }

    /**
     * This method will send a request to the server to get all the groups and then
     * the callback response will display all the group's locations and meeting places on the map.
     *
     * This method is different from displayUserGroups in that it will display all groups from the server
     * rather than just the groups that the user is a member of.
     *
     * Only call one of the two methods upon initiating the map!
     */
    private void displayAllGroups(){
        Call<List<Group>> groupListCaller = proxy.getGroups();
        ProxyBuilder.callProxy(AddKidToGroupActivity.this, groupListCaller, returnedGroups -> response(returnedGroups));
    }
    /*
     * Server response is to return server groups and then display all of their current locations and
     * meeting locations on the map.
     */

    private void response(List<Group> returnedGroupList) {

        for (Group group : returnedGroupList) {
            if(group.getRouteLatArray().size() == 2 && group.getRouteLngArray().size() == 2){
                LatLng groupFinalLocation = new LatLng( group.getRouteLatArray().get( 0 ), group.getRouteLngArray().get( 0 ) );
                groupFinalLocationMarker = mMap.addMarker(new MarkerOptions()
                        .position( groupFinalLocation )
                        .title( "Group: " + group.getGroupDescription() ));
                Log.i("Debug tag 0.8","group initial id is: "+group.getId());
                markerLongHashMapMap.put(groupFinalLocationMarker,group.getId());

            }
        }
    }

    private void initMap(){
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

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
        Toast.makeText(AddKidToGroupActivity.this,getString(R.string.opening_message),Toast.LENGTH_LONG)
                .show();
        getKidDetails();
        displayAllGroups();

        //If user enables the app to access their location, get user location.
        if(mLocationPermissionsGranted){
            getUserLocation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
        }
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                Long groupId = markerLongHashMapMap.get(marker);

                Intent intent = OnMarkerClickKidActivity.makeIntent(getApplicationContext());
                intent.putExtra("id",groupId);
                Log.i("Debug30","kidUser email is "+kidUser.getEmail());
                intent.putExtra("theKidEmail", kidUser.getEmail());

                startActivityForResult(intent, REQUEST_CODE);
                //setContentView(R.layout.activity_on_marker_click);
                return false;
            }
        });

    }

    /**
     * Retrieves the phones location and marks it on the map.
     * The map will automatically centre on to the phone location upon initiation of the map.
     * There is a button on the top right corner of the map that will centre on to the phones's location if clicked.
     */
    //Source: https://www.youtube.com/watch?v=fPFr0So1LmI&list=PLgCYzUzKIBE-vInwQhGSdnbyJ62nixHCt&index=5
    private void getUserLocation(){
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            Task location = mFusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()) {
                        Location currentLocation = (Location) task.getResult();
                        if (currentLocation == null) {
                            Location sfu = new Location("");
                            sfu.setLatitude(49.27);
                            sfu.setLongitude(-122.98);
                            moveCamera(new LatLng(sfu.getLatitude(), sfu.getLongitude()), DEFAULT_ZOOM);
                        } else{
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);

                        }
                    }
                }
            });
        }catch (SecurityException exception){
            Log.e("MapsActivity","Security Exception: "+exception.getMessage());
        }
    }

    /**
     * Centers the screen to the specified position.
     * @param latLng Position coordinates of the location
     * @param zoom Zooms on the location by a specified amount.
     */
    private void moveCamera(LatLng latLng,float zoom){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
    }

    /**
     * Requests permission from the user to allow location services for the map.
     */
    //Source: https://www.youtube.com/watch?v=Vt6H9TOmsuo&index=4&list=PLgCYzUzKIBE-vInwQhGSdnbyJ62nixHCt
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

    /**
     *
     * @param requestCode The code passed by getUserLocationMethod providing the results of whether user denied or allowed location services.
     * @param permissions The array of requested permissions.
     * @param grantResults Grant results for the requested permissions.  Either PERMISSION_GRANTED or PERMISSION_DENIED.
     */ //Source: https://www.youtube.com/watch?v=Vt6H9TOmsuo&index=4&list=PLgCYzUzKIBE-vInwQhGSdnbyJ62nixHCt
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
        Intent intent = new Intent(context, AddKidToGroupActivity.class);
        return intent;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.i(TAG,"request code: "+requestCode + "resultcode: "+resultCode);
        switch(requestCode){
            case REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    finish();
                    Log.i("tag50","entered");
                    startActivity(getIntent());
                }
                break;
        }
    }

}



