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
    private static Session tokenSession = new Session();

    private static final String TAG = "ServerTest";

    public String tempToken;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);
        //String tempToken;
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

    // Login to get a Token
    // ------------------------------------------------------------------------------------------
    // When server sends us a token, have the proxy store it for future use.
    // The token identifies us as a logged in user without having to revalidate passwords all the time.
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




              // Toast.makeText(WelcomeScreen.this, message1, Toast.LENGTH_LONG).show();
                //user.setEmail(userEmail.getText().toString());

                // Register for token received:
                ProxyBuilder.setOnTokenReceiveCallback( token -> onReceiveToken(token));

                // Make call
                Call<Void> caller = proxy.login(user);
                ProxyBuilder.callProxy(WelcomeScreen.this, caller, returnedNothing -> response(returnedNothing));

               // Log.i("Welcome test1:",tempToken);


            }
        });
    }

    // Handle the token by generating a new Proxy which is encoded with it.
    private void onReceiveToken(String token) {
        // Replace the current proxy with one that uses the token!
        Log.w(TAG, "   --> NOW HAVE TOKEN: " + token);
        tokenSession.setToken(token);
        Log.w(TAG, "   --> NOW HAVE TOKEN (output1): " + token);
        proxy = ProxyBuilder.getProxy(getString(R.string.api_key), token);
        tempToken = token;
        Intent intent = MainMenu.makeIntent(WelcomeScreen.this, tempToken);


        Log.w(TAG, "   --> NOW HAVE TOKEN(output2): " + tempToken);
        startActivity(intent);

    }

    // Login actually completes by calling this; nothing to do as it was all done
    // when we got the token.
    private void response(Void returnedNothing) {
        notifyUserViaLogAndToast("Server replied to login request (no content was expected).");
    }

    private void notifyUserViaLogAndToast(String message) {
        Log.w(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }





    public static Intent makeIntent(Context context){
        Intent intent = new Intent( context, WelcomeScreen.class );
       //intent.putExtra("User Token", tokenToPass);
        return intent;
    }



}
