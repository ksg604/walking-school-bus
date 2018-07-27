/**
 * Lists all users and leaders in a group
 * Navigated to from a child's page
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
import android.widget.TextView;

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

public class OpenKidGroupActivity extends AppCompatActivity {

    private Session session;
    private static WGServerProxy proxy;
    private static final String TAG = "OpenKidGroupActivity";
    private List<String> groupUserListInfo = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_kid_group);

        session = Session.getInstance();
        proxy = ProxyBuilder.getProxy(getString(R.string.api_key), session.getToken());
        getFromOpenKid();
    }

    private void getFromOpenKid(){
        Intent getFromOpenKidsIntent = getIntent();
        Long groupId = getFromOpenKidsIntent.getExtras().getLong("I");
        Log.i("Tag 92","Group id is: "+groupId);

        Call<Group> caller = proxy.getGroupById(groupId);
        ProxyBuilder.callProxy(OpenKidGroupActivity.this, caller, returnedGroup -> response(returnedGroup));

    }

    private void response(Group theReturnedGroup){
        if(theReturnedGroup != null) {
            Log.i("Tag 93", "Server callback success");
        }else{
            Log.i("Tag 93","Server callback failure");
        }

        //Set layout textviews
        TextView groupName = findViewById(R.id.groupNameOpenGroupKid);
        groupName.setText(getString(R.string.group_name)+ " "+theReturnedGroup.getGroupDescription());
        TextView members = findViewById(R.id.groupMembersOpenKid);
        members.setText(getString(R.string.kid_group_members));
        //Get the group members
        Call<List<User>> groupUserCaller = proxy.getGroupMembers(theReturnedGroup.getId());
        ProxyBuilder.callProxy(OpenKidGroupActivity.this, groupUserCaller, returnedUsers -> response2(returnedUsers));
    }

    private void response2(List<User> theReturnedUsers){
        SwipeMenuListView groupUserList = (SwipeMenuListView) findViewById(R.id.groupUserList);

        for(User userInGroup : theReturnedUsers){
            Log.w(TAG, "    User: " + userInGroup.toString());

            String userInfo = getString(R.string.mykids_user_name) + " " + userInGroup.getName() + "\n" +
                    getString(R.string.mykids_user_email) + " " + userInGroup.getEmail();
            Log.i("Tag88","Initial user id: "+ userInGroup.getId());
            groupUserListInfo.add(userInfo);
            ArrayAdapter adapter = new ArrayAdapter(OpenKidGroupActivity.this, R.layout.swipe_listview, groupUserListInfo);
            groupUserList.setAdapter(adapter);

            SwipeMenuCreator creator = new SwipeMenuCreator() {
                @Override
                public void create(SwipeMenu menu) {
                    // create "open" item
                    SwipeMenuItem monitoredByItem = new SwipeMenuItem(getApplicationContext());
                    // set item background
                    monitoredByItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9, 0xCE)));
                    // set item width
                    monitoredByItem.setWidth(180);
                    // set item title
                    monitoredByItem.setTitle(getString(R.string.details));
                    // set item title fontsize
                    monitoredByItem.setTitleSize(12);
                    // set item title font color
                    monitoredByItem.setTitleColor(Color.WHITE);
                    // add to menu
                    menu.addMenuItem(monitoredByItem);
                }
            };

            groupUserList.setMenuCreator(creator);
            groupUserList.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                    switch(index){
                        case 0:
                            Intent intentToViewMonitoredBy = ViewUserSettingsActivity.makeIntent(
                                    OpenKidGroupActivity.this,
                                    theReturnedUsers.get(position).getId());
                            Log.w("Tag 89","user id: "+userInGroup.getId());
                            startActivity(intentToViewMonitoredBy);
                            break;
                    }
                    return false;
                }
            });
        }
    }

    public static Intent makeIntent(Context context, Long groupIdToPass) {
        Intent intent = new Intent(context, OpenKidGroupActivity.class);
        intent.putExtra("I",groupIdToPass);
        return intent;
    }
}
