package com.example.walkingschoolbus;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;

import retrofit2.Call;

public class SignUpActivity extends AppCompatActivity {
    private static User user;
    private static WGServerProxy proxy;
    private String TAG = "Signup";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        proxy = ProxyBuilder.getProxy(getString(R.string.api_key), null);
        setupNewUserButton();

    }

    /**
     * Create intent for sign-up activity
     * @param context
     * @return
     */
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
                // Build new user
                user = User.getInstance();

                EditText name = (EditText) findViewById( R.id.edtTxtName );
                EditText email = (EditText) findViewById( R.id.edtTxtEmail );
                EditText password1 = (EditText) findViewById( R.id.edtTxtPassword1 );
                EditText password2 = (EditText) findViewById( R.id.edtTxtPassword2 );

                user.setName(name.getText().toString());
                user.setEmail(email.getText().toString());
                if( password1.getText().toString().equals( password2.getText().toString() )){
                    user.setPassword(password1.getText().toString());
                    Intent intent = WelcomeScreen.makeIntent( SignUpActivity.this );
                    startActivity( intent );


                }else{
                    Toast.makeText(SignUpActivity.this,"invalid password",Toast.LENGTH_LONG).show();
                }


               // user.setCurrentPoints(100);
               // user.setTotalPointsEarned(2500);
               // user.setRewards(new EarnedRewards());

                // Make call
                Call<User> caller = proxy.createUser(user);
                ProxyBuilder.callProxy(SignUpActivity.this, caller, returnedUser -> response(returnedUser));
            }
        });
    }
    private void response(User user) {

        notifyUserViaLogAndToast("Server replied with user: " + user.toString());
        //long userId = user.getId();
        //String userEmail = user.getEmail();


    }

    private void notifyUserViaLogAndToast(String message) {
        Log.w(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }




}
