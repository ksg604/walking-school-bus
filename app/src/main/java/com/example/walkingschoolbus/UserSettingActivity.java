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

import com.example.walkingschoolbus.model.Session;
import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Query;

/**
 * User settings activity allows user to change add/remove users they monitor from groups
 */
public class UserSettingActivity extends AppCompatActivity {


    private WGServerProxy proxy;
    private static final String TAG = "UserSetting";
    private String userEmail;
    private User user;
    private Session session;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_setting);

        session = Session.getInstance();
        user = User.getInstance();

        //Log.w("TESTTEST:::",user.getId().toString());

        // Build the server proxy
        proxy = ProxyBuilder.getProxy(getString(R.string.api_key),session.getToken());

        setupMonitoringListButton();
        setupMonitoredListButton();
        setupAddToMonitoringButton();
        setupAddToMonitoredButton();
    }


    private void setupMonitoredListButton() {
        Button btnMonitoring = (Button) findViewById(R.id.btnMonitoredList);
        btnMonitoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = MonitoredListActivity.makeIntent(UserSettingActivity.this);
                Log.w("UserSettingTest", "   --> NOW HAVE TOKEN(output4): " + session.getToken());
                startActivity(intent);
            }
        });
    }


    private void setupMonitoringListButton() {
        Button btnMonitoring = (Button) findViewById(R.id.btnMonitoringList);
        btnMonitoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = MonitoringListActivity.makeIntent(UserSettingActivity.this);
                Log.w("UserSettingTest", "   --> NOW HAVE TOKEN(output4): " + session.getToken());
                startActivity(intent);
            }
        });
    }


    private void setupAddToMonitoringButton() {
        Button button = (Button) findViewById(R.id.btnAddToMonitoringList);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TextView user_Email = (TextView) findViewById(R.id.emailAddress);
                userEmail = user_Email.getText().toString();
                Call<User> callerForGettingUser = proxy.getUserByEmail(userEmail);
                Log.w("Test the pushed email:", userEmail);
                ProxyBuilder.callProxy(UserSettingActivity.this, callerForGettingUser,
                        returnedUsers -> responseForMonitors(returnedUsers));

            }
        });

    }

    private void setupAddToMonitoredButton() {

        Button button = (Button) findViewById(R.id.btnAddMonitoredList);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TextView user_Email = (TextView) findViewById(R.id.emailAddress);
                userEmail = user_Email.getText().toString();
                Call<User> callerForGettingUser = proxy.getUserByEmail(userEmail);
                Log.w("Test the pushed email:", userEmail);
                ProxyBuilder.callProxy(UserSettingActivity.this, callerForGettingUser,
                        returnedMonitoredUsers -> responseForMonitored(returnedMonitoredUsers));

            }
        });

    }

    private User responseForMonitors(User returnedUsers) {
        notifyUserViaLogAndToast("Server replied with user: " + returnedUsers.getEmail());
        if(userEmail.equals(returnedUsers.getEmail()) ) {
            Call<List<User>> caller = proxy.addToMonitorsUsers(user.getId(), returnedUsers);
            ProxyBuilder.callProxy(UserSettingActivity.this, caller, returnedUser -> response(returnedUser));
        }
        return returnedUsers;
    }

    private User responseForMonitored(User returnedUsers) {
        notifyUserViaLogAndToast("Server replied with user: " + returnedUsers.getEmail());
        if(userEmail.equals(returnedUsers.getEmail()) ) {
            Call<List<User>> caller = proxy.addToMonitoredByUsers(user.getId(), returnedUsers);
            ProxyBuilder.callProxy(UserSettingActivity.this, caller, returnedUser -> response(returnedUser));
        }
        return returnedUsers;
    }


    private void response(List<User> returnedUser) {
        notifyUserViaLogAndToast("User added successfully.");
    }

    private void notifyUserViaLogAndToast(String message) {
        Log.w(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public static Intent makeIntent(Context context){
        Intent intent = new Intent(context, UserSettingActivity.class);
        return intent;
    }

}
