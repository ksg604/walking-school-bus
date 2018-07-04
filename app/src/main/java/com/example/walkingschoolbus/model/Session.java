// Session Class will hold user log in token
// Welcome page will check for this token and auto log in if it is found
// delete token will act as part of the "log out function"

package com.example.walkingschoolbus.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

public class Session {
    private String token;
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

    public void setToken(String string){
        this.token = string;
    }

    public String getToken(){
        return token;
    }

    public void deleteToken(){
        this.token = null;
    }

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

    public static void getStoredSession(Context context){
        SharedPreferences prefs = context.getSharedPreferences(SHAREDPREF_SESSION, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(SHAREDPREF_SESSION, null);
        instance = gson.fromJson(json, Session.class);
        Log.i(TAG,"in get Session" );
    }



}

