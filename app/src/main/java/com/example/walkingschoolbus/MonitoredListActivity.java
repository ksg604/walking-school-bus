package com.example.walkingschoolbus;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.walkingschoolbus.model.Session;
import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

/**
 * MonitoredList activity provides user with list of persons who monitors me with options to remove.
 */
public class MonitoredListActivity extends AppCompatActivity {


    private static final String TAG = "MonitoredListActivity";


    private User user;
    private static WGServerProxy proxy;
    private Session session;

    private ArrayList<String> monitoredUser = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        user = User.getInstance();
        session = Session.getInstance();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitored_list);

        // Build the server proxy
        proxy = ProxyBuilder.getProxy(getString(R.string.api_key),session.getToken());
        setMonitoredTextView();


        // Make call
        Call<List<User>> caller = proxy.getMonitoredByUsers(user.getId());
        ProxyBuilder.callProxy(MonitoredListActivity.this, caller, returnedUsers -> response(returnedUsers));


    }

    private void response(List<User> returnedUsers) {

        SwipeMenuListView monitoredList = (SwipeMenuListView) findViewById(R.id.monitoredList);

        for (User user : returnedUsers) {
            Log.w(TAG, "    User: " + user.toString());
            String userInfo =  "User Name: " + user.getName()+"\nUser Email: " + user.getEmail();

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
                deleteItem.setBackground(new ColorDrawable(Color.rgb(220, 20, 60)));
                // set item width
                deleteItem.setWidth(180);
                // set item title
                deleteItem.setTitle(MonitoredListActivity.this.getString(R.string.delete_swipe));
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
                        Call<Void> caller = proxy.removeFromMonitoredByUsers(user.getId(), returnedUsers.get(position).getId());
                        ProxyBuilder.callProxy(MonitoredListActivity.this, caller, returnedNothing -> response(returnedNothing));
                        monitoredList.removeViewsInLayout(position,1);

                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });


    }

    private void response(Void returnedNothing) {
        notifyUserViaLogAndToast(MonitoredListActivity.this.getString(R.string.notify_delete));
    }


    private void setMonitoredTextView() {
        TextView monitoredList = (TextView) findViewById( R.id.monitoredListText );
        String monitored = getString(R.string.monitored_title);
        monitoredList.setText( monitored );

    }


    public static Intent makeIntent(Context context) {
        Intent intent = new Intent(context, MonitoredListActivity.class);
        //intent.putExtra(USER_TOKEN, tokenToPass);
        return intent;
    }

    private void notifyUserViaLogAndToast(String message) {
        Log.w(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
