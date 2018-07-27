//Welcome Screen activity to log in or sign up for Walking School Bus

package com.example.walkingschoolbus;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.example.walkingschoolbus.model.Session;
import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;

import retrofit2.Call;

/**
 * Welcome screen allows user to log in using provided credentials or access the sign in page
 */
public class WelcomeScreen extends AppCompatActivity {

    private WGServerProxy proxy;
    private boolean autoLogInFlag = false;
    private static Session session;
    private User user;
    private static User loginUser;

    private static final String TAG = "ServerTest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);

        Session.getStoredSession(this);
        session = Session.getInstance();
        user = session.getUser();
        String savedToken = session.getToken();
        if(savedToken != null){
            autoLogInFlag =true;
            pullUser(savedToken, session.getEmail());
            Log.i(TAG, "Existing token found. AutoLog in");
        } else{
            Log.i(TAG, "No Existing token found. No auto login ");
        }
        proxy = ProxyBuilder.getProxy(getString(R.string.api_key), null);
        setupSignUpButton();
        setupSignInButton();
    }

    /**
     * Create Sign up button to push the creation of a new user
     */
    private void setupSignUpButton() {
        Button btn = findViewById(R.id.btnSignUp);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Launch activity to grab information and create new user
                Intent intent = SignUpActivity.makeIntent(WelcomeScreen.this);
                startActivity(intent);
                Log.i("Sprint1","Sign up activity launched from welcome screen");
            }
        });
    }
    /**
     * Login to get a Token
     *------------------------------------------------------------------------------------------
     * When server sends us a token, have the proxy store it for future use.
     * The token identifies us as a logged in user without having to revalidate passwords all the time.
     */
    private void setupSignInButton() {
        Button btn = findViewById(R.id.btnSignIn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Build new user
                EditText userEmail = (EditText) findViewById( R.id.emailInput);
                EditText userPassword = (EditText) findViewById( R.id.passwordInput );
                //create user for the log-in call
                loginUser = new User(userEmail.getText().toString(),
                        userPassword.getText().toString());

                // Register for token received:
                ProxyBuilder.setOnTokenReceiveCallback( token -> onReceiveToken(token));
                // Make call
                Call<Void> caller = proxy.login(loginUser);
                ProxyBuilder.callProxy(WelcomeScreen.this, caller, returnedNothing -> response(returnedNothing));
            }
        });
    }

    /**
     * Handle the token by generating a new Proxy which is encoded with it.
     * @param token
     */
    private void onReceiveToken(String token) {
        //pull user object using info entered into phone
        pullUser(token, loginUser.getEmail());
        // Replace the current proxy with one that uses the token!
        Log.w(TAG, "   --> NOW HAVE TOKEN: " + token);
    }

    /**
     * Pull user data to set into the user instance
     * @param token
     * @param email
     */
    private void pullUser(String token, String email) {
        proxy =ProxyBuilder.getProxy(getString(R.string.api_key),token);
        Call<User> caller = proxy.getUserByEmail(email);
        ProxyBuilder.callProxy(WelcomeScreen.this,caller,returnedUser ->
                responseForUser(returnedUser, token));
        Log.i(TAG, "pull user");
    }

    /**
     * get response from the user
     */
    private void responseForUser(User returnedUser, String token) {
        Log.i(TAG,"responseForUser method called");

        //set variables for session object
        String email =returnedUser.getEmail();

        //set singleton user to point to user pulled from server
        session.setUser(returnedUser);
        Log.i(TAG, "set user to: "+ returnedUser.getName());

        //set and save session data
        session.setSession(returnedUser,token);
        session.storeSession(this);
        Log.i(TAG,"responseForUser ||"+ email);

        moveToMainMenu();
    }

    /**
     * Login actually completes by calling this; nothing to do as it was all done
     * when we got the token.
     * @param returnedNothing
     */
    private void response(Void returnedNothing) {
        notifyUserViaLogAndToast(WelcomeScreen.this.getString(R.string.login_success));
    }

    /**
     * Push a toast to user with result
     * @param message
     */
    private void notifyUserViaLogAndToast(String message) {
        Log.w(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * make intent to get to welcome screen
     * @param context
     */
    public static Intent makeIntent(Context context){
        Intent intent = new Intent( context, WelcomeScreen.class );
        return intent;
    }

    /**
     * move to main menu screen using intent
     *
     */
    private void moveToMainMenu(){
        Intent intent = MainMenu.makeIntent(WelcomeScreen.this);
        startActivity(intent);
        WelcomeScreen.this.finish();
    }
}
