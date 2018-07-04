package com.example.walkingschoolbus;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Query;

public class UserSettingActivity extends AppCompatActivity {

    public static final String USER_TOKEN = "User Token";
    private String userToken2;
    private WGServerProxy proxy;
    private static final String TAG = "UserSetting";
    private String userEmail;
    long temp_id = 932;
    private int addToMonitoringFlag;
    private int addToMonitoredFlag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_setting);
        userToken2 = extractDataFromIntent();

        // Build the server proxy
        proxy = ProxyBuilder.getProxy(getString(R.string.api_key),userToken2);


        setupMonitoringListButton();
        setupMonitoredListButton();
        setupAddToMonitoringButton();
        setupAddToMonitoredButton();

    }


    private void setupAddToMonitoringButton() {
        Button button = (Button) findViewById(R.id.btnAddToMonitoringList);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToMonitoringFlag = 1;
                TextView user_Email = (TextView) findViewById(R.id.emailAddress);
                userEmail = user_Email.getText().toString();
                Call<User> callerForGettingUser = proxy.getUserByEmail(userEmail);
                Log.w("Test the pushed email:", userEmail);
                ProxyBuilder.callProxy(UserSettingActivity.this, callerForGettingUser,
                        returnedUsers -> response(returnedUsers));

            }
        });

    }
    private User response(User returnedUsers) {
        notifyUserViaLogAndToast("Server replied with user: " + returnedUsers.getEmail());
        if(userEmail.equals(returnedUsers.getEmail()) ) {

            // Make call
            if(addToMonitoringFlag ==1) {
                Call<List<User>> caller = proxy.addToMonitorsUsers(temp_id, returnedUsers);
                ProxyBuilder.callProxy(UserSettingActivity.this, caller, returnedUser -> response(returnedUser));
            }
            if(addToMonitoredFlag == 1) {
                Call<List<User>> caller = proxy.addToMonitoredByUsers(temp_id, returnedUsers);
                ProxyBuilder.callProxy(UserSettingActivity.this, caller, returnedUser -> response(returnedUser));

            }
            //       returnedUsers -> response(returnedUsers));
        }
        return returnedUsers;
    }

    private void response(List<User> returnedUser) {
        notifyUserViaLogAndToast("User added successfully.");
    }


    private void setupAddToMonitoredButton() {

        Button button = (Button) findViewById(R.id.btnAddMonitoredList);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToMonitoredFlag = 1;
                TextView user_Email = (TextView) findViewById(R.id.emailAddress);
                userEmail = user_Email.getText().toString();
                Call<User> callerForGettingUser = proxy.getUserByEmail(userEmail);
                Log.w("Test the pushed email:", userEmail);
                ProxyBuilder.callProxy(UserSettingActivity.this, callerForGettingUser,
                        returnedMonitoredUsers -> response(returnedMonitoredUsers));

            }
        });

    }





    private void setupMonitoredListButton() {
        Button btnMonitoring = (Button) findViewById(R.id.btnMonitoredList);
        btnMonitoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = MonitoredListActivity.makeIntent(UserSettingActivity.this, userToken2);
                Log.w("UserSettingTest", "   --> NOW HAVE TOKEN(output4): " + userToken2);
                startActivity(intent);
            }
        });
    }

    private String extractDataFromIntent() {
        Intent intent = getIntent();
        return intent.getStringExtra(USER_TOKEN);
    }

    private void setupMonitoringListButton() {
        Button btnMonitoring = (Button) findViewById(R.id.btnMonitoringList);
        btnMonitoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = MonitoringListActivity.makeIntent(UserSettingActivity.this, userToken2);
                Log.w("UserSettingTest", "   --> NOW HAVE TOKEN(output4): " + userToken2);
                startActivity(intent);
            }
        });
    }


    public static Intent makeIntent(Context context, String tokenToPass){
        Intent intent = new Intent(context, UserSettingActivity.class);
        intent.putExtra(USER_TOKEN, tokenToPass);
        return intent;
    }

    private void notifyUserViaLogAndToast(String message) {
        Log.w(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

}
