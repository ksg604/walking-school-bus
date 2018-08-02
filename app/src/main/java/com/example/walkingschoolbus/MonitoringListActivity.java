/**
 * Activity lists users whom logged in users monitor with options to delete
 */
package com.example.walkingschoolbus;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
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


public class MonitoringListActivity extends AppCompatActivity {

    private static final String TAG = "MonitoringListActivity";
    private Session session;
    private User user;
    private static WGServerProxy proxy;
    private ArrayList<String> monitoringUser = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring_list);

        Session.getStoredSession(this);
        session = Session.getInstance();
        user = session.getUser();
        String savedToken = session.getToken();
        setMonitoringTextView();

        proxy = ProxyBuilder.getProxy(getString(R.string.api_key),session.getToken());

        //setupAddKidsButton();
        // Make call
        Call<List<User>> caller = proxy.getMonitorsUsers(user.getId());
        ProxyBuilder.callProxy(MonitoringListActivity.this, caller, returnedUsers -> response(returnedUsers));
    }

    private void response(List<User> returnedUsers) {
        SwipeMenuListView monitoringList = (SwipeMenuListView) findViewById(R.id.myKidsList);

        for (User user : returnedUsers) {
            Log.w(TAG, "    User: " + user.toString());
            String userInfo = getString(R.string.monitoring_user_name) + " "  + user.getName() +"\n"+
                    getString(R.string.monitoring_user_email)+ " " + user.getEmail();

            monitoringUser.add(userInfo);
            ArrayAdapter adapter = new ArrayAdapter(MonitoringListActivity.this, R.layout.swipe_listview, monitoringUser);
            monitoringList.setAdapter(adapter);
        }
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem( getApplicationContext());
                // set item background
                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9, 0xCE)));
                // set item width
                openItem.setWidth(180);
                // set item title
                openItem.setTitle(getString(R.string.delete_swipe));
                // set item title fontsize
                openItem.setTitleSize(18);
                // set item title font color
                openItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(openItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem( getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(220, 20,
                        60)));

                // set item width
                deleteItem.setWidth(180);
                // set item title
                deleteItem.setTitle(getString(R.string.add_to_group_swipe));
                // set item title fontsize
                deleteItem.setTitleSize(18);
                // set item title font color
                deleteItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(deleteItem);

                // create "group" list item
                SwipeMenuItem goGroup = new SwipeMenuItem( getApplicationContext());
                // set item background
                goGroup.setBackground(new ColorDrawable(Color.rgb(120, 120,
                        20)));

                // set item width
                goGroup.setWidth(180);
                // set item title
                goGroup.setTitle(getString(R.string.gorup_swipe));
                // set item title fontsize
                goGroup.setTitleSize(18);
                // set item title font color
                goGroup.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(goGroup);
            }
        };

        // set creator
        monitoringList.setMenuCreator(creator);

        monitoringList.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        // Make call
                        Call<Void> caller = proxy.removeFromMonitorsUsers(user.getId(), returnedUsers.get(position).getId());
                        ProxyBuilder.callProxy(MonitoringListActivity.this, caller, returnedNothing -> response(returnedNothing));
                        monitoringList.removeViewsInLayout(position,1);
                    break;

                    case 1:

                        Intent intentForAdd = MonitorActivity.makeIntent(MonitoringListActivity.this,
                                returnedUsers.get(position).getEmail());
                        startActivity(intentForAdd);
                        break;

                    case 2:
                        Intent intentForRemove = RemoveMonitoringUserFromGroup.makeIntentWithEmailToPass(MonitoringListActivity.this,
                                returnedUsers.get(position).getEmail());
                        startActivity(intentForRemove);
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });
    }

    private void setupAddKidsButton() {

        Button button = (Button) findViewById(R.id.addMyKidsBtn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentAdd = AddNewParentsActivity.makeIntent( MonitoringListActivity.this );
                startActivity(intentAdd);
            }
        });
    }

    private void response(Void returnedNothing) {
        notifyUserViaLogAndToast(MonitoringListActivity.this.getString(R.string.notify_not_monitor));
    }


    private void setMonitoringTextView() {
        TextView monitoringList = (TextView) findViewById( R.id.monitoringListText );
        String monitoring = getString(R.string.monitoring_title);
        monitoringList.setText( monitoring );
    }

    public static Intent makeIntent(Context context) {
        Intent intent = new Intent(context, MonitoringListActivity.class);
        return intent;
    }

    private void notifyUserViaLogAndToast(String message) {
        Log.w(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
