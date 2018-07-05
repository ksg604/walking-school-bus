//Welcome Screen activity to log in or sign up for Walking School Bus

package com.example.walkingschoolbus;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.WrapperListAdapter;


import com.example.walkingschoolbus.model.Session;
import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;

import retrofit2.Call;

public class WelcomeScreen extends AppCompatActivity {
    private User user;
    private WGServerProxy proxy;
    //private static Session tokenSession = Session.getInstance();

    private static final String TAG = "ServerTest";

    public String tempToken;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);

        Session.getStoredSession(this);
        Session tokenSession = Session.getInstance();
        String savedToken = tokenSession.getToken();
        if(savedToken != null){
            autoLogIn(savedToken);
            Log.i(TAG, "Existing token found. AutoLog in");
        } else{
            Log.i(TAG, "No Existing token found. No auto login ");
        }
        proxy = ProxyBuilder.getProxy(getString(R.string.api_key), null);

        setupSignInButton();
        setupDebugButton();
    }

//TODO Remove this before submission
    private void setupDebugButton() {
        Button btn = (Button) findViewById(R.id.buttonDebug);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = MainMenu.makeIntent(WelcomeScreen.this, tempToken);
                startActivity(intent);
            }
        });
        setupSignUpButton();
    }

    /**
     * Create Sign up button to push the creation of a new user
     */
    private void setupSignUpButton() {
        Button btn = findViewById(R.id.btnSignUp);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = SignUpActivity.makeIntent(WelcomeScreen.this); //maybe should change this make intent
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
                //EditText userName = (EditText) findViewById( R.id.nameInput );
                EditText userEmail = (EditText) findViewById( R.id.emailInput);
                EditText userPassword = (EditText) findViewById( R.id.passwordInput );

               user = User.getInstance();

               user.setEmail(userEmail.getText().toString());
               user.setPassword(userPassword.getText().toString());

                // Register for token received:
                ProxyBuilder.setOnTokenReceiveCallback( token -> onReceiveToken(token));

                // Make call
                Call<Void> caller = proxy.login(user);
                ProxyBuilder.callProxy(WelcomeScreen.this, caller, returnedNothing -> response(returnedNothing));
            }
        });
    }

    /**
     * Handle the token by generating a new Proxy which is encoded with it.
     * @param token
     */
    private void onReceiveToken(String token) {
        Session tokenSession = Session.getInstance();
        User user = User.getInstance();
        pullUser(token);
        // Replace the current proxy with one that uses the token!
        Log.w(TAG, "   --> NOW HAVE TOKEN: " + token);
        //tokenSession.setToken(token);
        String tokenSessionToken = tokenSession.getToken();

        //store newly logged in user info in session info
        //tokenSession.setEmail(user.getEmail());
        //tokenSession.setName(user.getName());
       // tokenSession.setid(user.getId());

        tokenSession.storeSession(this);
        Log.w(TAG, "   --> NOW HAVE TOKEN (output1): " + tokenSessionToken);
        proxy = ProxyBuilder.getProxy(getString(R.string.api_key), token);
        tempToken = token;
        Intent intent = MainMenu.makeIntent(WelcomeScreen.this, tempToken);


        Log.w(TAG, "   --> NOW HAVE TOKEN(output2): " + tempToken);
        startActivity(intent);

    }

    private void pullUser(String token) {
        proxy =ProxyBuilder.getProxy(getString(R.string.api_key),token);
        Call<User> caller = proxy.getUserByEmail(user.getEmail());
        ProxyBuilder.callProxy(WelcomeScreen.this,caller,returnedUser ->responseForUser(returnedUser, token));
        Log.i(TAG, "pull user");
    }

    private void responseForUser(User returnedUser, String token) {
        Log.i(TAG,"responseForUser");
        Session session = Session.getInstance();
        String email =returnedUser.getEmail();
        Long id = returnedUser.getId();
        String name = returnedUser.getName();

        //set singleton user
        user.setEmail(email);
        user.setId(id);
        user.setName(name);

        //set session data
        session.setSession(id,name,email,token);
        session.storeSession(this);
        Log.i(TAG,"responseForUser ||"+ email);
    }
//TODO: Confirm this. May need to test for HTTP error or abandon idea.
    /** Setup proxy with user token and move to main menu
     *
     * @param token
     */
    private void autoLogIn(String token){
        proxy = ProxyBuilder.getProxy(getString(R.string.api_key), token);
        Intent intent = MainMenu.makeIntent(WelcomeScreen.this, tempToken);
        startActivity(intent);

        Session session = Session.getInstance();
        Call<User> caller = proxy.getUserById(session.getid());
        ProxyBuilder.callProxy(WelcomeScreen.this, caller, returnedUser -> responseForAutoLogInUser(returnedUser));
        // Register for token received:
       // ProxyBuilder.setOnTokenReceiveCallback( token -> onReceiveToken(token));


        // Make call
       // Call<Void> caller = proxy.login(user);
        //ProxyBuilder.callProxy(WelcomeScreen.this, caller, returnedNothing -> response(returnedNothing));
        WelcomeScreen.this.finish();
    }
//TODO: Confirm logic with Steven that this makes sense
    /**
     * assumes bad get returns null OBJECT
     * @param returnedUser
     */
    private void responseForAutoLogInUser(User returnedUser){
        Session session= Session.getInstance();
        if(returnedUser.getEmail()!=null){
            Log.i(TAG, "fetching log in user successful");
        } else{
            //notify user of error, clear stack, delete saved session, restart welcome screen
            Log.i(TAG, "fetch log in user error");
            notifyUserViaLogAndToast("Please log in again");
            session.deleteToken();
            Intent intent = WelcomeScreen.makeIntent(this);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    /**
     * Login actually completes by calling this; nothing to do as it was all done
     * when we got the token.
     * @param returnedNothing
     */
    private void response(Void returnedNothing) {
        notifyUserViaLogAndToast("Server replied to login request (no content was expected).");
    }

    /**
     * Push a toast to user with result
     * @param message
     */
    private void notifyUserViaLogAndToast(String message) {
        Log.w(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**Make intent for welcome screen for use in log out
     *
     * @param context
     * @return
     */
    public static Intent makeIntent(Context context){
        Intent intent = new Intent( context, WelcomeScreen.class );
       //intent.putExtra("User Token", tokenToPass);
        return intent;
    }



}
