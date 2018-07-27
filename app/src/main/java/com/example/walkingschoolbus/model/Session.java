// Session Class will hold user log in token, and basic information
// Welcome page will check for this token and auto log in if it is found
// delete token will act as part of the "log out function"

package com.example.walkingschoolbus.model;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.example.walkingschoolbus.MainMenu;
import com.example.walkingschoolbus.R;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;
import com.google.gson.Gson;


import retrofit2.Call;

/**
 * This class holds basic info from the loged in user such as name, id, token, and email
 * - Used to auto log in and log out
 * - Singleton used to pass information around the activities.
 */
public class Session {
    private String token;
    private User user;
    private Group group;
    private String numberOfMessages;
    private boolean tracking;
    private static final String TAG ="Session";
    private static Session instance;
    private static final String SHAREDPREF_SESSION = "user session token";
    private static int numOfUnreadMessage;
    private static int numOfUnreadPermissions;

    private boolean mLocationPermissionsGranted;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 0;
    private GpsLocation lastGpsLocation;

    /** Signleton support of session class
     *
     * @return singleton of Session
     */
    public static Session getInstance(){
        if(instance == null){
            instance = new Session();
        }
        return instance;
    }
    private Session(){}

    public String getToken(){
        return token;
    }

    public User getUser(){
        if(user ==null) {
            this.user = new User();
        }
        return user;
        }

    public void setUser(User updatedUser){this.user = new User(updatedUser);}

    public String getName(){
        String name;
        try{
            name = user.getName();
        }catch(NullPointerException e){
            name = " ";
            e.printStackTrace();
        }

        return name;}

    public String getEmail(){return user.getEmail();}

    public static int getNumOfUnreadMessage() {
        return numOfUnreadMessage;
    }

    public static int getNumOfUnreadPermissions() {
        return numOfUnreadPermissions;
    }

    public static void setNumOfUnreadPermissions(int numOfUnreadPermissions) {
        Session.numOfUnreadPermissions = numOfUnreadPermissions;
    }

    public static void setNumOfUnreadMessage(int numOfUnreadMessage) {
        Session.numOfUnreadMessage = numOfUnreadMessage;
    }

    public Long getid(){return user.getId();}

    public boolean isTracking() {
        return tracking;
    }

    public void setTracking(boolean tracking) {
        this.tracking = tracking;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public void deleteTokenAndVariables(){
        this.token = null;
        this.user = null;
        this.group = null;
        this.tracking=false;
        }

    public void setSession(User user, String setToken){
        this.token = setToken;
        this.user = user;
    }

    /**
     * Save this object
     * @param context
     */
    public void storeSession(Context context){
        SharedPreferences prefs = context.getSharedPreferences(SHAREDPREF_SESSION,Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(this.instance);
        prefsEditor.putString(SHAREDPREF_SESSION,json);
        //System.out.print(json);
        prefsEditor.apply();
        try {
            Log.i(TAG, "session stored: " + this.user.getName());
        } catch(NullPointerException e){
            Log.i(TAG, "session stored with null values: ");
            e.printStackTrace();
        }
    }

    /**
     * recovered saved object instance
     * @param context
     */
    public static void getStoredSession(Context context){
        SharedPreferences prefs = context.getSharedPreferences(SHAREDPREF_SESSION, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(SHAREDPREF_SESSION, null);
        instance = gson.fromJson(json, Session.class);
        Log.i(TAG,"Session grabbed");
    }

    public String getNumberOfMessages() {
        return numberOfMessages;
    }

    public void setNumberOfMessages(String numberOfMessages) {
        this.numberOfMessages = numberOfMessages;
    }
}

