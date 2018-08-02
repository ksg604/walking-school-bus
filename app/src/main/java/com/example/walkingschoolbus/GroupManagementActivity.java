/**
 * Class to list all groups related to logged in user with option to delete or move to place picker
 * activity to create a new group.
 */

package com.example.walkingschoolbus;

import android.app.Activity;
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

    private List<String> stringMemberGroupList = new ArrayList< >( );
    private List<String> stringLeaderGroupList = new ArrayList< >( );
    private List<Group> groupLeaderList = new ArrayList<>();
    private List<Group> modifiedGroupLeaderList = new ArrayList<>(  );
    private List<Long> groupIdLeaderList = new ArrayList<>();
    private List<Group> groupMemberList = new ArrayList<>();
    private List<Group> modifiedGroupMemberList = new ArrayList<>( );
    private List<Long> groupIdMemberList = new ArrayList<>();
    private Session session = Session.getInstance();
    private Group group = Group.getInstance();
    private User user;
    private String userToken;
    private static WGServerProxy proxy;
    private static final String TAG = "GroupManagementActivity";
    private static final int REQUEST_CODE = 1004;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_management);

        //get token from session
        userToken = session.getToken();
        // Build the server proxy
        proxy = ProxyBuilder.getProxy(getString( R.string.api_key),userToken,true);

        user = session.getUser();

        //Make call
        Call<User> caller = proxy.getUserByEmail(user.getEmail());
        ProxyBuilder.callProxy(GroupManagementActivity.this, caller,
                returnedUser -> responseForUser(returnedUser));

        setupCreateGroupButton();
        setupJoinGroupButton();
    }

    /*
     * get user to obtain memeberOfGroups
     * call the list of groups and then return the groups that user is in.
     */
    private void responseForUser(User returnedUser) {

        groupLeaderList = returnedUser.getLeadsGroups();
        for(Group group : groupLeaderList ){
            groupIdLeaderList.add(group.getId());
        }

        groupMemberList = returnedUser.getMemberOfGroups();
        for( Group group : groupMemberList ){
            groupIdMemberList.add(group.getId());
        }

        user.setId(returnedUser.getId());

        // Make call
        Call<List<Group>> caller = proxy.getGroups();
        ProxyBuilder.callProxy(GroupManagementActivity.this, caller,
                returnedGroupList -> responseForGroup(returnedGroupList));
    }

   /**get response List<Group> objects to save data into stringGroupList
    * to see group list on swipeMenuListView
    *
    */
    private void responseForGroup(List<Group> returnedGroups) {
        SwipeMenuListView groupAsLeaderListView = (SwipeMenuListView) findViewById( R.id.messagesGot);
        SwipeMenuListView groupAsMemberListView = (SwipeMenuListView) findViewById(R.id.groupAsMemberList);

        for (Group group : returnedGroups) {

            if (groupIdMemberList.contains( group.getId() )){
                Log.w( TAG, getString( R.string.group_list) + " " + group.getId() );

                modifiedGroupMemberList.add(group);
                String groupInfo = getString( R.string.group_list) + " " + group.getGroupDescription()+"\n";
                stringMemberGroupList.add( groupInfo );

            }else if( groupIdLeaderList.contains(group.getId())){
                Log.w( TAG, getString( R.string.group_list) + " " + group.getId() );

                modifiedGroupLeaderList.add(group);
                String groupInfo = getString( R.string.group_list) + " " + group.getGroupDescription()+"\n";
                stringLeaderGroupList.add( groupInfo );
            }
        }

        ArrayAdapter adapterLeader = new ArrayAdapter( GroupManagementActivity.this,
                R.layout.swipe_listview, stringLeaderGroupList );
        groupAsLeaderListView.setAdapter( adapterLeader );
        ArrayAdapter adapterMember = new ArrayAdapter( GroupManagementActivity.this,
                R.layout.swipe_listview, stringMemberGroupList );
        groupAsMemberListView.setAdapter( adapterMember );
        SwipeMenuCreator creatorForLeader = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {

                // create "open" item
                SwipeMenuItem sendMessage = new SwipeMenuItem( getApplicationContext());
                // set item background
                sendMessage.setBackground(new ColorDrawable(Color.rgb(80, 56, 184)));
                // set item width
                sendMessage.setWidth(240);
                // set item title
                sendMessage.setTitle(getString( R.string.message_swipe ));
                // set item title font size
                sendMessage.setTitleSize(15);
                // set item title font color
                sendMessage.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(sendMessage);

                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem( getApplicationContext());
                // set item background
                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9, 0xCE)));
                // set item width
                openItem.setWidth(240);
                // set item title
                openItem.setTitle(getString( R.string.open_swipe ));
                // set item title font size
                openItem.setTitleSize(15);
                // set item title font color
                openItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(openItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem( getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable( Color.rgb(220, 20, 60)));
                // set item width
                deleteItem.setWidth(240);
                // set item title
                deleteItem.setTitle(getString(R.string.delete_swipe));
                // set item title font size
                deleteItem.setTitleSize(15);
                // set item title font color
                deleteItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(deleteItem);

            }
        };

        // set creatorForLeader
        groupAsLeaderListView.setMenuCreator(creatorForLeader);

        /*
         make swipeMenuListView to show groups I am a member of
         I can remove group from this this list
         */
        groupAsLeaderListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {

                    case 0:
                        Long tempID = modifiedGroupLeaderList.get(position).getId();
                        Intent intent2 = SendMessageActivity.makeIntent(GroupManagementActivity.this, tempID);
                        startActivity(intent2);
                        break;

                    case 1:
                        Long groupId = modifiedGroupLeaderList.get(position).getId();
                        group.setId(groupId);
                        Intent intent = LeaderActivity.makeIntent( GroupManagementActivity.this );
                        startActivity( intent );
                        break;

                    case 2:
                         //make Call
                         Call<Void> caller = proxy.deleteGroup( modifiedGroupLeaderList.get(position).getId());
                         ProxyBuilder.callProxy(GroupManagementActivity.this, caller, returnedNothing -> response(returnedNothing));
                         groupAsLeaderListView.removeViewsInLayout(position,1);
                         adapterLeader.notifyDataSetChanged();
                         break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });

        SwipeMenuCreator creatorForMember = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {

                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem( getApplicationContext());
                // set item background
                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9, 0xCE)));
                // set item width
                openItem.setWidth(180);
                // set item title
                openItem.setTitle(getString( R.string.open_swipe ));
                // set item title font size
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
                deleteItem.setTitle(R.string.leave_swipe);
                // set item title font size
                deleteItem.setTitleSize(18);
                // set item title font color
                deleteItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };

        // set creatorForMember
        groupAsMemberListView.setMenuCreator(creatorForMember);

        /*
         make swipeMenuListView to show groups I am a member of
         I can remove group from this this list
         */
        groupAsMemberListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                    switch (index) {

                        case 0:
                            Long groupId = modifiedGroupMemberList.get(position).getId();
                            group.setId(groupId);
                            Intent intent = LeaderActivity.makeIntent( GroupManagementActivity.this );
                            startActivity( intent );
                            break;

                        case 1:
                            // Make call
                            Call<Void> caller = proxy.removeGroupMember(modifiedGroupMemberList.get(position).getId(), user.getId());
                            ProxyBuilder.callProxy(GroupManagementActivity.this, caller, returnedNothing -> response(returnedNothing));
                            groupAsMemberListView.removeViewsInLayout(position,1);
                            adapterMember.notifyDataSetChanged();

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

    /**
     * set up the button to create group
     */
    private void setupCreateGroupButton() {
        Button createGroupButton = findViewById( R.id.btnCreateGroupInGroupManage );
        createGroupButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = PlacePickerActivity.makeIntent(GroupManagementActivity.this);
                startActivityForResult( intent, REQUEST_CODE);
            }
        } );
    }

    /**
     * setup the button to join a existing group
     */
    private void setupJoinGroupButton() {
        Button joinGroupButton = (Button) findViewById( R.id.btnJoinGroup );
        joinGroupButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = MapsActivity.makeIntent( GroupManagementActivity.this );
                startActivity( intent);

            }
        } );
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
    public static Intent makeIntent(Context context){
        Intent intent = new Intent(context,GroupManagementActivity.class);
        return intent;
    }

    private Intent makeIntentBack(Context context, int resultcode) {
        Intent intent = new Intent(context, GroupManagementActivity.class );
        setResult(resultcode, intent );
        return intent;
    }

    /**
     * put the result from PlacePickerActivity on the listview to update
     * @param requestCode arbitrary code number in this activity to get result from the other Activity
     * @param resultCode the result code from the other Activity
     * @param intent intent for going to another activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        switch(requestCode){
            case REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                   finish();
                   startActivity(getIntent());
                }
                break;
        }
    }

    @Override
    public void onRestart(){
        super.onRestart();
        finish();
        startActivity( getIntent() );
    }
}
