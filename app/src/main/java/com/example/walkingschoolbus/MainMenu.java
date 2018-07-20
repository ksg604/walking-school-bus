/**
 * Main menu screen to give users highest level option after log in
 */
package com.example.walkingschoolbus;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.example.walkingschoolbus.model.GpsLocation;
import com.example.walkingschoolbus.model.Group;
import com.example.walkingschoolbus.model.Message;
import com.example.walkingschoolbus.model.Session;
import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;
import com.google.android.gms.location.FusedLocationProviderClient;


import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import retrofit2.Call;

import static com.example.walkingschoolbus.model.Session.getNumOfUnreadMessage;

public class MainMenu extends AppCompatActivity {

    public static final String USER_TOKEN = "User token";
    private static final String TAG = "MainMenu";
    private GpsLocation lastGpsLocation = new GpsLocation();
    Session session = Session.getInstance();
    User user = session.getUser();
    private GpsLocation schoolLocation = new GpsLocation();
    String token = session.getToken();
    private Group group = session.getGroup();
    private String userToken;
    private static WGServerProxy proxy;
    private Boolean mLocationPermissionsGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 0;
    private static final int REQUEST_CODE = 2016;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static Handler handlerForGps = new Handler();
    private static Handler handlerForMessages = new Handler();
    private static Runnable runnableForGps;
    private static Runnable runnableForMessages;
    private static int zeroDistance = 0;

    private final static String unread = "unread";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main_menu );

        getUserLocationPermission();

        proxy = ProxyBuilder.getProxy( getString( R.string.api_key ), token );

        setupButtonSettings();
        setupLayoutGroups();
        setupLayoutMessages();
        setupLayoutMyParents();
        setupLayoutMyKids();
        setupLogOutButton();
        setupOnTrackingBtn();

        setupEmergencyButton();
        setupBroadcastsButton();
        setTextViewMessage();
        setupMessageNumber();
        setWalkingWithMessage();

        makeHandlerRunForGps();
        makeHandlerRunForMessages();



    }


    private void setupMessageNumber() {

        Call<List<Message>> callerForUnreadMessage = proxy.getMessageNotRead(user.getId(), unread);
        ProxyBuilder.callProxy(MainMenu.this, callerForUnreadMessage,
                returnedMessageList -> responseForUnreadMessage(returnedMessageList));

    }
    private void responseForUnreadMessage(List<Message> returnedMessageList) {
       // session.setNumOfUnreadMessage(returnedMessageList.size());
        int counter = 0;
       for(Message message : returnedMessageList){
           if(!(message.getFromUser().getId().equals(user.getId()))) {
               counter++;
           }
       }
        //String temp = Integer.toString(returnedMessageList.size());
        String temp = Integer.toString(counter);
        TextView messages = (TextView) findViewById( R.id.messagesMainMenu );
        String numMessages = temp + " " +getString(R.string.menu_message);
        session.setNumberOfMessages( numMessages );
        messages.setText( numMessages );

    }

    private void setupOnTrackingBtn() {
        Switch onTracking = (Switch) findViewById( R.id.trackingSwitch );

        onTracking.setChecked( session.isTracking());
        onTracking.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isOn) {
                if (isOn == true) {
                    turnOnGpsUpdate();
                    session.setTracking( true );

                }else{
                   turnOffGpsUpdate();
                   session.setTracking (false);
                }
            }
        } );
    }

    private void setupButtonSettings() {
        ImageView btn = (ImageView) findViewById( R.id.btnUserDetails );
        btn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    long sessionID = session.getid();
                    Intent intent = ViewUserSettingsActivity.makeIntent( MainMenu.this, sessionID);
                    startActivity( intent );
                } catch(NullPointerException e){
                    Log.e(TAG, "exception", e);
                    Intent intent = WelcomeScreen.makeIntent(MainMenu.this);
                    startActivity(intent);
                    finish();
                }
            }
        } );
    }

    private void setupBroadcastsButton() {
        LinearLayout broadcasts = (LinearLayout) findViewById(R.id.linearLayoutBroadcasts);
        broadcasts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = emergencyMessageActivity.makeIntent(MainMenu.this, false);
                startActivity(intent);
            }
        });
    }

    private void setupEmergencyButton() {
        Button btn = findViewById(R.id.btnEmergency);
        btn.setText( R.string.emergency);


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = emergencyMessageActivity.makeIntent(MainMenu.this,true);
                startActivity(intent);

            }
        });
    }

    /**
     * setup logout button to finish this app.
     */
    private void setupLogOutButton() {
        Button btn = findViewById( R.id.btnLogOut );
        btn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                session.deleteTokenAndVariables();
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
                Intent intent = MessageActivity.makeIntent(MainMenu.this);
                startActivity(intent);

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
                Intent intent = MyKidsActivity.makeIntent(MainMenu.this);
                Log.w("Maintest", "   --> NOW HAVE TOKEN(output3): " + token);
                startActivity(intent);
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

    private void setWalkingWithMessage() {
        String walkingMessage;
        TextView walking = findViewById(R.id.txtViewWalkingMessage);
        if(session.getGroup() != null){
            walkingMessage = getString(R.string.mm_walkingwith) + session.getGroup().getGroupDescription();
        }else{
            walkingMessage = getString(R.string.mm_not_walking);
        }
        walking.setText(walkingMessage);
    }



    public static Intent makeIntent(Context context) {
        return new Intent( context, MainMenu.class );
    }

    public static Intent makeIntent(Context context, int number){
        Intent intent = new Intent(context, MainMenu.class);
        intent.putExtra("Unread Message Number", number);
        return intent;
    }

    private void makeHandlerRunForGps() {
        runnableForGps = new Runnable() {
            public void run() {
                updateLastGpsLocation();
                handlerForGps.postDelayed( this, 30000 );
            }
        };
    }

    private void makeHandlerRunForMessages(){
        runnableForMessages = new Runnable(){
            public void run() {
                setupMessageNumber();
                handlerForMessages.postDelayed( this,60000 );
            }
        };
        handlerForMessages.post( runnableForMessages);
    }





    private void updateLastGpsLocation() {
        LocationManager locationManager = (LocationManager) this.getSystemService( Context.LOCATION_SERVICE );

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {  }
            public void onStatusChanged(String provider, int status, Bundle extras) {   }
            public void onProviderEnabled(String provider) {    }
            public void onProviderDisabled(String provider) {   }
        };

        // Register the listener with the Location Manager to receive location update
        try {
            if (ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
                getUserLocationPermission();
                return;
            }else{
                locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location location = locationManager.getLastKnownLocation( LocationManager.GPS_PROVIDER );
                if(location != null) {
                    Log.i(TAG, "location set");
                    lastGpsLocation.setLng(location.getLongitude());
                    lastGpsLocation.setLat(location.getLatitude());
                    lastGpsLocation.setTimestamp(getTimeStamp());
                    user.setLastGpsLocation(lastGpsLocation);
                    if(user == null){Log.i(TAG, "user null");}
                    if(lastGpsLocation!=null) {
                        Call<GpsLocation> caller = proxy.setLastGpsLocation(user.getId(), user.getLastGpsLocation());
                        ProxyBuilder.callProxy(MainMenu.this, caller, returnedGpsLocation -> responseForGps(returnedGpsLocation));
                    }
                }else {
                    Log.i(TAG,"location null");
                }
            }

        }catch(SecurityException exception){
            exception.printStackTrace();
        }
    }

    private void responseForGps(GpsLocation returnedGps) {

        user.setLastGpsLocation( returnedGps );
        group = session.getGroup();
        setWalkingWithMessage();
        zeroDistance = countZeroDistance(zeroDistance);
        if (zeroDistance == 20){
            turnOffGpsUpdate();
        } else{
            Switch onTracking = (Switch) findViewById( R.id.trackingSwitch );

            onTracking.setChecked(session.isTracking());
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

    private String getTimeStamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String timeStamp  = dateFormat.format(new Date());
        return timeStamp;
    }

    /**
     * Turn on tracking by handler post
     *
     */
    public static void turnOnGpsUpdate(){
        handlerForGps.post( runnableForGps );
    }

    /**
     *Turn off tracking by removeMessage
     */
    public static void turnOffGpsUpdate (){
        handlerForGps.removeMessages(0);
    }

    private int countZeroDistance(int count){
        if(group != null) {
            if (group.getRouteLngArray().size() == 2 && group.getRouteLatArray().size() == 2) {

                schoolLocation.setLat(group.getRouteLatArray().get(1));
                schoolLocation.setLng(group.getRouteLngArray().get(1));

                if ((Math.abs(schoolLocation.getLat() - lastGpsLocation.getLat()) < 0.1)
                        && (Math.abs(schoolLocation.getLng() - lastGpsLocation.getLng()) < 0.1)) {
                    count += 1;
                } else {
                    count = 0;
                }
            }
            Log.i(TAG, "returning count");
            return count;
        } else{
            Log.i(TAG,"group is null");
            return 0;
        }
    }
}
