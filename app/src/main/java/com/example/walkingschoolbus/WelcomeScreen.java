package com.example.walkingschoolbus;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;

import retrofit2.Call;

public class WelcomeScreen extends AppCompatActivity {
    private User user;
    private WGServerProxy proxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);

        proxy = ProxyBuilder.getProxy(getString(R.string.api_key), null);
        setupLoginButton();

    }

    // Login to get a Token
    // ------------------------------------------------------------------------------------------
    // When server sends us a token, have the proxy store it for future use.
    // The token identifies us as a logged in user without having to revalidate passwords all the time.
    private void setupLoginButton() {
        Button btn = findViewById(R.id.loginButton);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Build new user
                EditText userName = (EditText) findViewById( R.id.userNameInput );
                EditText userEmail = (EditText) findViewById( R.id.emailInput);
                EditText userPassword = (EditText) findViewById( R.id.passwordInput );

                user = User.getInstance();
                user.setName(userName.toString());
                user.setEmail(userEmail.toString());
                user.setPassword(userPassword.toString());

                // Register for token received:
                ProxyBuilder.setOnTokenReceiveCallback( token -> onReceiveToken(token));

                // Make call
                Call<Void> caller = proxy.login(user);
                ProxyBuilder.callProxy(WelcomeScreen.this, caller, returnedNothing -> response(returnedNothing));
            }
        });
    }

    // Handle the token by generating a new Proxy which is encoded with it.
    private void onReceiveToken(String token) {
        // Replace the current proxy with one that uses the token!
        //Log.w(TAG, "   --> NOW HAVE TOKEN: " + token);
        proxy = ProxyBuilder.getProxy(getString(R.string.api_key), token);
    }

    // Login actually completes by calling this; nothing to do as it was all done
    // when we got the token.
    private void response(Void returnedNothing) {
        //notifyUserViaLogAndToast("Server replied to login request (no content was expected).");
        Intent intent = MainMenu.makeIntent(WelcomeScreen.this);
        startActivity(intent);

    }



}
