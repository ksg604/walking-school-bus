package com.example.walkingschoolbus;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.walkingschoolbus.model.GpsLocation;
import com.example.walkingschoolbus.model.Group;
import com.example.walkingschoolbus.model.Session;
import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;
import com.google.android.gms.location.FusedLocationProviderClient;


import java.util.List;

import retrofit2.Call;

/**
 * Main menu screen to give users highest level option after log in
 */
public class MainMenu extends AppCompatActivity {

    public static final String USER_TOKEN = "User token";
    private static final String TAG = "MainMenu";
    private GpsLocation lastGpsLocation = new GpsLocation();
    Session session = Session.getInstance();
    User user = User.getInstance();
    String token = session.getToken();
    private String userToken;
    private static WGServerProxy proxy;
    private Boolean mLocationPermissionsGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 0;
    private FusedLocationProviderClient mFusedLocationProviderClient;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main_menu );

        getUserLocationPermission();

        proxy = ProxyBuilder.getProxy( getString( R.string.api_key ), token );

        setupButtonSettings();
        setupLayoutGroups();
        setupLayoutMaps();
        setupLayoutMessages();
        setupLayoutMyParents();
        setupLogOutButton();
        setupOnTrackingBtn();


        setTextViewMessage();
    }

    private void setupOnTrackingBtn() {
        Switch OnTracking = (Switch) findViewById( R.id.trackingSwitch );
        OnTracking.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isOn) {
                Handler handler = new Handler();
                Runnable runnable = new Runnable() {
                    public void run() {
                        updateLastGpsLocation();
                        handler.postDelayed( this, 30000 );
                    }

                };
                if (!isOn) {
                    // Start the initial runnable task by posting through the handler
                    handler.post(runnable);
                }else {
                    //Stop updating location
                    handler.removeCallbacksAndMessages(runnable);
                }
            }




        } );


    }


    private void responseForGps(GpsLocation returnedGps) {
        user.setLastGpsLocation( returnedGps );

    }

    private void setupButtonSettings() {
        ImageView btn = (ImageView) findViewById( R.id.btnUserDetails );
        btn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long sessionID = session.getid();
                Intent intent = ViewUserSettingsActivity.makeIntent( MainMenu.this, sessionID );
                startActivity( intent );
            }
        } );
    }

    /**
     * setup logout button to finish this app.
     */
    private void setupLogOutButton() {
        Button btn = findViewById( R.id.btnLogOut );
        btn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                session.deleteToken();
                session.storeSession( MainMenu.this );
                Intent intent = WelcomeScreen.makeIntent( MainMenu.this );
                startActivity( intent );
                finish();
            }
        } );

    }


    /**
     * setup linear layout to redirect to settings page on click
     */
    private void setupLayoutMessages() {
        LinearLayout setting = (LinearLayout) findViewById( R.id.linearLayoutMessages );
        setting.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        } );
    }

    /**
     * setup linear layout to redirect to group management page on click
     */
    private void setupLayoutGroups() {
        LinearLayout group = (LinearLayout) findViewById( R.id.linearLayoutGroup );
        group.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = GroupManagementActivity.makeIntent( MainMenu.this );
                startActivity( intent );
                Log.w( "Main Menu", "Group Activity Launched" );
            }
        } );
    }


    /**
     * setup linear layout to redirect to my parents page on click
     */
    private void setupLayoutMyParents() {
        LinearLayout setting = (LinearLayout) findViewById( R.id.linearLayoutMyParents );
        setting.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = MyParentsActivity.makeIntent( MainMenu.this );
                Log.w( "Maintest", "   --> NOW HAVE TOKEN(output3): " + token );
                startActivity( intent );
            }
        } );
    }


    /**
     * setup linear layout to redirect to my kids page on click
     */
    private void setupLayoutMyKids() {
        LinearLayout setting = (LinearLayout) findViewById( R.id.linearLayoutMyKids );
        setting.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = MonitoringListActivity.makeIntent( MainMenu.this );
                Log.w( "Maintest", "   --> NOW HAVE TOKEN(output3): " + token );
                startActivity( intent );
            }
        } );
    }


    /**
     *Show welcome message to the user loggged in
     */
    private void setTextViewMessage() {
        String welcomeMessage;
        TextView welcome = (TextView) findViewById( R.id.mainMenuWelcomeMessage );
        if (session.getName() != null) {
            welcomeMessage = getString( R.string.hello ) + " " + session.getName();
        } else {
            welcomeMessage = getString( R.string.hello );
        }
        welcome.setText( welcomeMessage );

    }


    /**
     * setup linear layout to redirect to map activity
     */
    private void setupLayoutMaps() {
        LinearLayout maps = (LinearLayout) findViewById( R.id.linearLayoutMaps );
        maps.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = MapsActivity.makeIntent( MainMenu.this );
                startActivity( intent );
                Log.i( "Sprint1", "Map activity Launched" );
            }
        } );
    }

    public static Intent makeIntent(Context context) {
        return new Intent( context, MainMenu.class );
    }


    private void updateLastGpsLocation() {
        LocationManager locationManager = (LocationManager) this.getSystemService( Context.LOCATION_SERVICE );

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                lastGpsLocation.setLng( location.getLongitude() );
                lastGpsLocation.setLat( location.getLatitude() );
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        // Register the listener with the Location Manager to receive location update
        try {
            if (ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
             getUserLocationPermission();
                return;
            }else{
                locationManager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                Location location = locationManager.getLastKnownLocation( LocationManager.GPS_PROVIDER );
                lastGpsLocation.setLng( location.getLongitude() );
                lastGpsLocation.setLat( location.getLatitude() );


                Call<GpsLocation> caller = proxy.setLastGpsLocation(user.getId(),lastGpsLocation );
                ProxyBuilder.callProxy(MainMenu.this, caller, returnedGpsLocation -> responseForGps(returnedGpsLocation));

            }

        }catch(SecurityException exception){
            exception.printStackTrace();
        }
    }



    /**
     * Requests permission from the user to allow location services for the map.
     */
    private void getUserLocationPermission(){
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION};

        //If both ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION permissions are granted,
        //device location permissions will be granted.
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            mLocationPermissionsGranted = true;

        }else{
            //Else, request the user to enable location services for the map.
            //LOCATION_PERMISSION_REQUEST_CODE will be passed to onRequestPermissionsResult method
            //to verify the results of user's selection.
            ActivityCompat.requestPermissions(this,permissions,LOCATION_PERMISSION_REQUEST_CODE);
        }


    }

     /*
      * @param requestCode The code passed by getUserLocationMethod providing the results of whether user denied or allowed location services.
      * @param permissions The array of requested permissions.
      * @param grantResults Grant results for the requested permissions.  Either PERMISSION_GRANTED or PERMISSION_DENIED.
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

}
