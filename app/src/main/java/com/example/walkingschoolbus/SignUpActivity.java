package com.example.walkingschoolbus;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;

import retrofit2.Call;

public class SignUpActivity extends AppCompatActivity {

    private static WGServerProxy proxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        proxy = ProxyBuilder.getProxy(getString(R.string.api_key), null);

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
                // Build new user (with random email to avoid conflicts)
                User user = User.getInstance();
                int random = (int) (Math.random() * 100000);
                user.setEmail("testuser"+random+"@test.com");
                user.setName("I. B. Rocking");
                user.setPassword("1");
               // user.setCurrentPoints(100);
               // user.setTotalPointsEarned(2500);
               // user.setRewards(new EarnedRewards());

                // Make call

                Call<User> caller = proxy.createNewUser(user);
                ProxyBuilder.callProxy(SignUpActivity.this, caller, returnedUser -> response(returnedUser));
            }
        });
    }
    private void response(User user) {
        //notifyUserViaLogAndToast("Server replied with user: " + user.toString());
        long userId = user.getId();
        String userEmail = user.getEmail();


    }



}
