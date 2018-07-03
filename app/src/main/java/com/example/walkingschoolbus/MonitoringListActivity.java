package com.example.walkingschoolbus;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

public class MonitoringListActivity extends AppCompatActivity {


    private static final String TAG = "MonitoringListActivity";
    public static final String USER_TOKEN = "User Token";
    private String userToken3;
    //private User user;
    private static WGServerProxy proxy;
    ArrayList<String> monitoringUser = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
       // User user = User.getInstance();
        long temp_id = 932;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring_list);

        userToken3 = extractDataFromIntent();
        // Build the server proxy
        proxy = ProxyBuilder.getProxy(getString(R.string.api_key),userToken3);


        SwipeMenuListView monitoringList = (SwipeMenuListView) findViewById(R.id.monitoringList);
        List<User> monitoringUser = new ArrayList<>();


        // Make call
        Call<List<User>> caller = proxy.getMonitorsUsers(temp_id);
        ProxyBuilder.callProxy(MonitoringListActivity.this, caller, returnedUsers -> response(returnedUsers));



        ArrayAdapter adapter = new ArrayAdapter(MonitoringListActivity.this, R.layout.da_items, monitoringUser );
        monitoringList.setAdapter(adapter);

    }

    private String extractDataFromIntent() {
        Intent intent = getIntent();
        return intent.getStringExtra(USER_TOKEN);
    }

    private void response(List<User> returnedUsers) {
        notifyUserViaLogAndToast("Got list of " + returnedUsers.size() + " users! See logcat.");
        Log.w(TAG, "All Users:");
        for (User user : returnedUsers) {
            Log.w(TAG, "    User: " + user.toString());
            monitoringUser.add("User Email: "+ user.getEmail() + "   " + "User Name: " + user.getName());
        }
    }




    public static Intent makeIntent(Context context, String tokenToPass) {
        Intent intent = new Intent(context, MonitoringListActivity.class);
        intent.putExtra(USER_TOKEN, tokenToPass);
        return intent;
    }



    private void notifyUserViaLogAndToast(String message) {
        Log.w(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }


}
