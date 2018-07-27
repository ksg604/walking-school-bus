/**
 * Activity allows one to remove a child from a group.
 */

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
import com.example.walkingschoolbus.model.Group;
import com.example.walkingschoolbus.model.Session;
import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

public class RemoveMonitoringUserFromGroup extends AppCompatActivity {

    private Session session;
    private static WGServerProxy proxy;
    private static Long userId;
    private static final String TAG = "Monitor";
    private List<String> stringMemberGroupList = new ArrayList< >( );
    private List<Group> groupListOfThisMember = new ArrayList<>();
    private List<Long> groupIdMemberList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_monitoring_user_from_group);

        session = Session.getInstance();
        // Build the server proxy
        proxy = ProxyBuilder.getProxy(getString(R.string.api_key),session.getToken());
        //get user info from previous activity
        Intent neededIntent = getIntent();
        String tempString = neededIntent.getStringExtra("Email");

        Call<User> caller =proxy.getUserByEmail(tempString);

        ProxyBuilder.callProxy(RemoveMonitoringUserFromGroup.this, caller,
                returnedInputUser -> responseForRemove(returnedInputUser));
    }

    private void responseForRemove(User returnedInputUser) {
        groupListOfThisMember = returnedInputUser.getMemberOfGroups();
        userId = returnedInputUser.getId();

        for (Group group : groupListOfThisMember) {
            groupIdMemberList.add(group.getId());
        }
        Call<List<Group>> caller = proxy.getGroups();
        ProxyBuilder.callProxy(RemoveMonitoringUserFromGroup.this,
                caller, returnedGroupList -> responseForGroup(returnedGroupList));
    }

    private void responseForGroup(List<Group> returnedGroups) {
        // modifiedGroupMemberList.add(group);
        SwipeMenuListView userGroups = (SwipeMenuListView) findViewById(R.id.listOfGroups);

        for(Group group : returnedGroups) {
            if(groupIdMemberList.contains(group.getId())){
                String groupInfo = getString( R.string.group_list) + " " + group.getGroupDescription();
                stringMemberGroupList.add(groupInfo);
            }
        }

        ArrayAdapter adapterLeader = new ArrayAdapter( RemoveMonitoringUserFromGroup.this,
                R.layout.swipe_listview, stringMemberGroupList );

        userGroups.setAdapter( adapterLeader );

        SwipeMenuCreator creatorForLeader = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem( getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable( Color.rgb(220, 20, 60)));
                // set item width
                deleteItem.setWidth(180);
                // set item title
                deleteItem.setTitle(getString(R.string.delete_swipe));
                // set item title fontsize
                deleteItem.setTitleSize(18);
                // set item title font color
                deleteItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };
        // set creatorForLeader
        userGroups.setMenuCreator(creatorForLeader);

        userGroups.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        Call<Void> caller = proxy.removeGroupMember(groupListOfThisMember.get(position).getId(), userId);
                        ProxyBuilder.callProxy(RemoveMonitoringUserFromGroup.this, caller, returnedNothing -> response(returnedNothing));
                        userGroups.removeViewsInLayout(position,1);
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });
    }

    private void response(Void returnedNothing) {
        notifyUserViaLogAndToast( getString( R.string.delete_message));
    }

    private void notifyUserViaLogAndToast(String message) {
        Log.w(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void responseMessage(List<User> returnedUser) { }

    public static Intent makeIntentWithEmailToPass(Context context, String userEmailToPass){
        Intent intent = new Intent(context, RemoveMonitoringUserFromGroup.class);
        intent.putExtra("Email",userEmailToPass);
        return intent;
    }

    public static Intent makeIntent(Context context){
        Intent intent = new Intent(context, RemoveMonitoringUserFromGroup.class);
        return intent;
    }
}
