// Session Class will hold user log in token, and basic information
// Welcome page will check for this token and auto log in if it is found
// delete token will act as part of the "log out function"

package com.example.walkingschoolbus.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

public class Session {
    private String token;
    private String name;
    private String email;
    private Long id;

    private static final String TAG ="Session";
    private static Session instance;
    private static final String SHAREDPREF_SESSION = "user session token";

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

    private void setToken(String string){
        this.token = string;
    }

    public String getToken(){
        return token;
    }

    /**
     * set session name
     * @param string the logged in users name
     */
    private void setName(String string){this.name = string;}

    /**
     * @return name of sessions user
     */
    public String getName(){return name;}

    /**
     * @param string the logged in users email
     */
    private void setEmail(String string){this.email = string;}

    /**
     * @return the logged in user's email
     */
    public String getEmail(){return email;}

    /**
     * @param number the logged in users id
     */
    private void setid(Long number){this.id = number;}

    /**
     * @return the logged in users id
     */
    public Long getid(){return id;}

    /**
     * delete all info from this logged in session
     */
    public void deleteToken(){
        this.token = null;
        this.id = null;
        this.name = null;
        this.email = null;
    }
    public void setSession(Long setID, String setName, String setEmail, String setToken){
        this.token = setToken;
        this.id = setID;
        this.email = setEmail;
        this.name = setName;
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
        Log.i(TAG,"in store Session" );
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
        Log.i(TAG,"in get Session" );
    }



}

