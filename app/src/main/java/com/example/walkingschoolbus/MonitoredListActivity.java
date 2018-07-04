package com.example.walkingschoolbus;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

public class MonitoredListActivity extends AppCompatActivity {


    private static final String TAG = "MonitoredListActivity";
    public static final String USER_TOKEN = "User Token";
    private String userToken4;
    //private User user;
    private static WGServerProxy proxy;

    ArrayList<String> monitoredUser = new ArrayList<>();
    //SwipeMenuListView monitoringList = (SwipeMenuListView) findViewById(R.id.monitoringList);
    long temp_id = 932;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // User user = User.getInstance();
        //long temp_id = 932;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitored_list);

        userToken4 = extractDataFromIntent();
        // Build the server proxy
        proxy = ProxyBuilder.getProxy(getString(R.string.api_key),userToken4);


        // Make call
        Call<List<User>> caller = proxy.getMonitoredByUsers(temp_id);
        ProxyBuilder.callProxy(MonitoredListActivity.this, caller, returnedUsers -> response(returnedUsers));


    }

    private String extractDataFromIntent() {
        Intent intent = getIntent();
        return intent.getStringExtra(USER_TOKEN);
    }

    private void response(List<User> returnedUsers) {
        notifyUserViaLogAndToast("Got list of " + returnedUsers.size() + " users! See logcat.");
        Log.w(TAG, "All Users:");

        SwipeMenuListView monitoredList = (SwipeMenuListView) findViewById(R.id.monitoredList);
        //List<String> monitoringUser = new ArrayList<>();
        //List<Integer> child_ID = new ArrayList<Integer>();
        for (User user : returnedUsers) {
            Log.w(TAG, "    User: " + user.toString());
            String userInfo = "User Email: "+ user.getEmail() + "   " + "User Name: " + user.getName()
                    + "User ID: "+ user.getId();

            monitoredUser.add(userInfo);
            ArrayAdapter adapter = new ArrayAdapter(MonitoredListActivity.this, R.layout.da_items, monitoredUser);
            monitoredList.setAdapter(adapter);

        }

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                        0xCE)));
                // set item width
                deleteItem.setWidth(180);
                // set item title
                deleteItem.setTitle("DELETE");
                // set item title fontsize
                deleteItem.setTitleSize(18);
                // set item title font color
                deleteItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(deleteItem);

            }
        };

// set creator
        monitoredList.setMenuCreator(creator);

        monitoredList.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        // Make call
                        Call<Void> caller = proxy.removeFromMonitoredByUsers(temp_id, returnedUsers.get(position).getId());
                        ProxyBuilder.callProxy(MonitoredListActivity.this, caller, returnedNothing -> response(returnedNothing));
                        monitoredList.removeViewsInLayout(position,1);
                        // finish();
                        //ArrayAdapter adapter = new ArrayAdapter(MonitoringListActivity.this, R.layout.da_items, monitoringUser);
                        //monitoringList.setAdapter(adapter);


                }


                // false : close the menu; true : not close the menu
                return false;
            }
        });


    }


    private void response(Void returnedNothing) {
        notifyUserViaLogAndToast(" You will not be monitored by this user anymore.");
    }





    public static Intent makeIntent(Context context, String tokenToPass) {
        Intent intent = new Intent(context, MonitoredListActivity.class);
        intent.putExtra(USER_TOKEN, tokenToPass);
        return intent;
    }



    private void notifyUserViaLogAndToast(String message) {
        Log.w(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }


}
