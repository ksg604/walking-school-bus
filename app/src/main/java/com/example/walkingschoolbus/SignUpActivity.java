/**
 * Signup Activity allows users to create a new account
 */
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

import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;

import retrofit2.Call;

public class SignUpActivity extends AppCompatActivity {

    private static WGServerProxy proxy;
    private User user;
    private static final String TAG = "SignupActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        proxy = ProxyBuilder.getProxy(getString(R.string.api_key),null);
        setupNewUserButton();
        setSignUpTextView();
    }

    public static Intent makeIntent(Context context){
        return new Intent(context,SignUpActivity.class);
    }

    // Create a new user on the server
    // ------------------------------------------------------------------------------------------
    private void setupNewUserButton() {
        Button btn = findViewById(R.id.btnCreateUser);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Build new user (with random email to avoid conflicts)
                user = new User();

                EditText name = (EditText) findViewById( R.id.edtTxtName );
                EditText email = (EditText) findViewById( R.id.edtTxtEmail );
                EditText password = (EditText) findViewById( R.id.edtTxtPassword2 );
                user.setName(name.getText().toString());
                user.setEmail(email.getText().toString());
                user.setPassword(password.getText().toString());

                // Make call
                Call<User> caller = proxy.createUser(user);
                ProxyBuilder.callProxy(SignUpActivity.this, caller, returnedUser -> response(returnedUser));

                //switch
                Intent intent = WelcomeScreen.makeIntent(SignUpActivity.this);
                startActivity(intent);
            }
        });
    }

    /*
     *Set signup text view to show title
     */
    private void setSignUpTextView() {
        TextView signup = (TextView) findViewById( R.id.signUpText );
        String signUp = getString(R.string.sign_up);
        signup.setText( signUp );
    }

    private void response(User user) {  }

    private void notifyUserViaLogAndToast(String message) {
        Log.w(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
