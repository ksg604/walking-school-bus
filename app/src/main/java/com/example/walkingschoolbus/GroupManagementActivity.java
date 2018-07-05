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
import android.widget.ListView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.walkingschoolbus.model.Group;
import com.example.walkingschoolbus.model.Session;
import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

public class GroupManagementActivity extends AppCompatActivity {


    public static final String USER_TOKEN = "User Token";
    private String userToken;
    private static WGServerProxy proxy;
    List<String> stringGroupList = new ArrayList< >( );
    List<Group> groupList = new ArrayList<>();
    List<Long> groupIdList = new ArrayList<>();
    private Session tokenSession = Session.getInstance();
    private User user;
    private static final String TAG = "GroupManagementActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_management);

        //get token from session
        userToken = tokenSession.getToken();
        // Build the server proxy
        proxy = ProxyBuilder.getProxy(getString( R.string.api_key),userToken);

        user = User.getInstance();
        //Make call
        Call<User> caller = proxy.getUserByEmail(user.getEmail());
        ProxyBuilder.callProxy(GroupManagementActivity.this, caller, returnedUser -> responseForUser(returnedUser));

        // proxy = ProxyBuilder.getProxy( getString( R.string.api_key ));
        setupCreateGroupButton();



    }

    /*
     * get user to obtain memeberOfGroups
     * call the list of groups and then return the groups that user is in.
     */
    private void responseForUser(User returnedUser) {

        groupList = returnedUser.getMemberOfGroups();
        for( Group group : groupList ){
            groupIdList.add(group.getId());
        }
        // Make call
        Call<List<Group>> caller = proxy.getGroups();
        ProxyBuilder.callProxy(GroupManagementActivity.this, caller, returnedGroupList -> responseForGroup(returnedGroupList));
    }


    /**
     * set up the button to create group
     */
    private void setupCreateGroupButton() {
        Button createGroupButton = findViewById( R.id.btnAddGroup );
        createGroupButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = PlacePickerActivity.makeIntent(GroupManagementActivity.this);
                startActivity(intent);
            }
        } );
    }

   /**get response List<Group> objects to save data into stringGroupList
    * A user stores a group when the user belongs to that group
    *
    */
    private void responseForGroup(List<Group> returnedGroups) {
        notifyUserViaLogAndToast("Got list of " + returnedGroups.size() + " users! See logcat.");
        Log.w(TAG, "All Users:");

        SwipeMenuListView groupListView = (SwipeMenuListView) findViewById(R.id.groupList);

        for (Group group : returnedGroups) {

            if (groupIdList.contains( group.getId() )){
                Log.w( TAG, "    Group: " + group.getId() );


                String groupInfo = "group Description: " + group.getGroupDescription();
                stringGroupList.add( groupInfo );

            }
            ArrayAdapter adapter = new ArrayAdapter( GroupManagementActivity.this, R.layout.da_items, stringGroupList );

            groupListView.setAdapter( adapter );

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
                openItem.setTitle("Open");
                // set item title fontsize
                openItem.setTitleSize(18);
                // set item title font color
                openItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(openItem);


                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem( getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable( Color.rgb(220, 20, 60)));
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
        groupListView.setMenuCreator(creator);

        groupListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                    switch (index) {
                        /*
                         open group I want to see
                         */
                        case 0:

                        case 1:
                            /*// Make call
                             *Call<Void> caller = proxy.removeFromMonitoredByUsers(temp_id, returnedUsers.get(position).getId());
                             *ProxyBuilder.callProxy(MonitoredListActivity.this, caller, returnedNothing -> response(returnedNothing));
                             *monitoredList.removeViewsInLayout(position,1);
                             * finish();
                             *ArrayAdapter adapter = new ArrayAdapter(MonitoringListActivity.this, R.layout.da_items, monitoringUser);
                             *monitoringList.setAdapter(adapter);
                             */



                    }


                    // false : close the menu; true : not close the menu
                    return false;
                }
        });

    }



    private void response(Void returnedNothing) {
        notifyUserViaLogAndToast(" You will not be monitored by this user anymore.");
    }

    private void notifyUserViaLogAndToast(String message) {
        Log.w(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }


    /**
     * Create intent for this activity
     * @param context
     * @return
     */
    public static Intent makeIntent(Context context, String tokenToPass){
        Intent intent = new Intent(context,GroupManagementActivity.class);
        intent.putExtra(USER_TOKEN, tokenToPass);
        return intent;
    }


}
