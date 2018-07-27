/**
 * Shows a map with your kids on it and a selected group leader.
 *
 * Simple test app to show a Google Map.
 * - If using the emulator, Create an Emulator from the API 26 image.
 *   (API27's doesn't/didn't support maps; nor will 24 or before I believe).
 * - Accessing Google Maps requires an API key: You can request one for free (and should!)
 *   see /res/values/google_maps_api.xml
 * - More notes at the end of this file.
 */

package com.example.walkingschoolbus;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.location.LocationProvider;

import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.walkingschoolbus.model.GpsLocation;
import com.example.walkingschoolbus.model.Group;
import com.example.walkingschoolbus.model.Session;
import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
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

public class ParentsDashboardActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static GpsLocation gpsLocation;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Boolean mLocationPermissionsGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 0;
    private static final float DEFAULT_ZOOM = 14f;
    WGServerProxy proxy;
    private Session token = Session.getInstance();
    private static Handler handler = new Handler();
    private static Runnable runnableCode;

    private Marker kidLocationMarker;
    private User currentUser;

    private Map<Marker, Long> markerLongHashMapMap = new HashMap<Marker, Long>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        currentUser = Session.getInstance().getUser();

        String tokenValue= token.getToken();

        proxy = ProxyBuilder.getProxy(getString(R.string.api_key),tokenValue);
        getUserLocationPermission();
    }

    private void getAllKids(){
        mMap.clear();
        Call<List<User>> kidListCaller = proxy.getMonitorsUsers(currentUser.getId());
        ProxyBuilder.callProxy(ParentsDashboardActivity.this, kidListCaller, returnedKids -> response(returnedKids));
    }
    private void response(List<User> currentUserKids) {
        Intent getGroupFromOpenKid = getIntent();
        Long groupId = getGroupFromOpenKid.getExtras().getLong("G");
        Call<Group> groupCaller = proxy.getGroupById(groupId);
        ProxyBuilder.callProxy(ParentsDashboardActivity.this, groupCaller, returnedGroup -> responseForGroup(returnedGroup,currentUserKids));
    }

        private void responseForGroup (Group returnedGroup, List<User> passedKidList){
            User incompleteGroupLeader = returnedGroup.getLeader();

            Call<User> leaderCaller = proxy.getUserById(incompleteGroupLeader.getId());
            ProxyBuilder.callProxy(ParentsDashboardActivity.this, leaderCaller, completeLeader -> responseForLeader(completeLeader, passedKidList));

        }

        private void responseForLeader (User theLeader, List<User> passedKidList){
            Log.i("Tag 151", "Leader name is: "+theLeader.getName());
            String snippet;
            passedKidList.add(theLeader);
            for(User usersToPlot : passedKidList){

                if(usersToPlot == passedKidList.get(passedKidList.size()-1)){
                    snippet = "This user is the leader of this group." +"\n"
                            + "Leader Name: "+usersToPlot.getName() + "\n"
                            + "Last seen here: " + usersToPlot.getLastGpsLocation().getTimestamp() + "\n";


                }else {
                        snippet = "Kid Name: " + usersToPlot.getName() + "\n" +
                                "Last seen here: " + usersToPlot.getLastGpsLocation().getTimestamp() + "\n";
                    }

                    GpsLocation kidLocation = usersToPlot.getLastGpsLocation();
                    if (kidLocation != null && kidLocation.getLat() != null && kidLocation.getLng() != null && kidLocation.getTimestamp() != null) {
                        LatLng kidLatLng = new LatLng(kidLocation.getLat(), kidLocation.getLng());
                        kidLocationMarker = mMap.addMarker(new MarkerOptions()
                                .position(kidLatLng)
                                .title("Name: " + usersToPlot.getName()));

                        kidLocationMarker.setSnippet(snippet);

                        markerLongHashMapMap.put(kidLocationMarker, usersToPlot.getId());
                    }

                    //Source: https://stackoverflow.com/questions/13904651/android-google-maps-v2-how-to-add-marker-with-multiline-snippet
                    mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                        @Override
                        public View getInfoWindow(Marker marker) {
                            return null;
                        }

                        @Override
                        public View getInfoContents(Marker marker) {

                            LinearLayout info = new LinearLayout(ParentsDashboardActivity.this);
                            info.setOrientation(LinearLayout.VERTICAL);

                            TextView title = new TextView(ParentsDashboardActivity.this);
                            title.setTextColor(Color.BLACK);
                            title.setGravity(Gravity.CENTER);
                            title.setTypeface(null, Typeface.BOLD);
                            title.setText(marker.getTitle());

                            TextView snippet = new TextView(ParentsDashboardActivity.this);
                            snippet.setTextColor(Color.GRAY);
                            snippet.setText(marker.getSnippet());

                            info.addView(title);
                            info.addView(snippet);
                            return info;
                        }
                    });
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
        makeHandlerRun();
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
                marker.showInfoWindow();
                return false;
            }
        });
    }
    private void makeHandlerRun() {
        runnableCode = new Runnable(){
            public void run() {
                getAllKids();
                handler.postDelayed(this, 30000);
            }
        };
        handler.post(runnableCode);
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



    public static Intent makeIntent(Context context, Long groupIdToPass){
        Intent intent = new Intent(context, ParentsDashboardActivity.class);
        intent.putExtra("G",groupIdToPass);
        return intent;
    }
}



