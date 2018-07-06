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


    private String userToken;
    private static WGServerProxy proxy;
    List<String> stringMemberGroupList = new ArrayList< >( );
    List<String> stringLeaderGroupList = new ArrayList< >( );
    List<Group> groupLeaderList = new ArrayList<>();
    List<Group> modifiedGroupLeaderList = new ArrayList<>(  );
    List<Long> groupIdLeaderList = new ArrayList<>();
    private static final int requestNum = 1234;

    List<Group> groupMemberList = new ArrayList<>();
    List<Group> modifiedGroupMemberList = new ArrayList<>( );
    List<Long> groupIdMemberList = new ArrayList<>();
    private Session tokenSession = Session.getInstance();
    private  Group group = Group.getInstance();
    private  User user;
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
                startActivityForResult( intent, requestNum);




            }
        } );
    }

   /**get response List<Group> objects to save data into stringGroupList
    * to see group list on swipeMenuListView
    *
    */
    private void responseForGroup(List<Group> returnedGroups) {
        notifyUserViaLogAndToast("Got list of " + returnedGroups.size() + " users! See logcat.");
        Log.w(TAG, "All Users:");

        SwipeMenuListView groupAsLeaderListView = (SwipeMenuListView) findViewById( R.id.groupAsLeaderList);
        SwipeMenuListView groupAsMemberListView = (SwipeMenuListView) findViewById(R.id.groupAsMemberList);



        for (Group group : returnedGroups) {

            if (groupIdMemberList.contains( group.getId() )){
                Log.w( TAG, "    Group: " + group.getId() );

                modifiedGroupMemberList.add(group);
                String groupInfo = "Group : " + group.getGroupDescription();
                stringMemberGroupList.add( groupInfo );

            }else if( groupIdLeaderList.contains(group.getId())){
                Log.w( TAG, "    Group: " + group.getId() );

                modifiedGroupLeaderList.add(group);
                String groupInfo = "Group : " + group.getGroupDescription();
                stringLeaderGroupList.add( groupInfo );


            }

            ArrayAdapter adapterLeader = new ArrayAdapter( GroupManagementActivity.this, R.layout.da_items, stringLeaderGroupList );

            groupAsLeaderListView.setAdapter( adapterLeader );

            ArrayAdapter adapterMember = new ArrayAdapter( GroupManagementActivity.this, R.layout.da_items, stringMemberGroupList );

            groupAsMemberListView.setAdapter( adapterMember );





        }


        SwipeMenuCreator creatorForLeader = new SwipeMenuCreator() {

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
                deleteItem.setTitle("Delete");
                // set item title fontsize
                deleteItem.setTitleSize(18);
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

                        Long groupId = modifiedGroupLeaderList.get(position).getId();

                        group.setId(groupId);
                        Intent intent = LeaderActivity.makeIntent( GroupManagementActivity.this );
                        startActivity( intent );
                        break;


                    case 1:

                         //make Call
                         Call<Void> caller = proxy.deleteGroup( modifiedGroupLeaderList.get(position).getId());
                         ProxyBuilder.callProxy(GroupManagementActivity.this, caller, returnedNothing -> response(returnedNothing));
                         groupAsLeaderListView.removeViewsInLayout(position,1);
                         finish();

                         ArrayAdapter adapter = new ArrayAdapter(GroupManagementActivity.this, R.layout.da_items, stringLeaderGroupList);
                         groupAsLeaderListView.setAdapter(adapter);
                         startActivity(getIntent());


                         break;

                }


                // false : close the menu; true : not close the menu
                return false;
            }
        });

        SwipeMenuCreator creatorForMember = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {


                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem( getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable( Color.rgb(220, 20, 60)));
                // set item width
                deleteItem.setWidth(180);
                // set item title
                deleteItem.setTitle("Delete");
                // set item title fontsize
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
                            // Make call
                             Call<Void> caller = proxy.removeGroupMember(modifiedGroupMemberList.get(position).getId(), user.getId());
                             ProxyBuilder.callProxy(GroupManagementActivity.this, caller, returnedNothing -> response(returnedNothing));
                             groupAsMemberListView.removeViewsInLayout(position,1);
                             finish();
                             ArrayAdapter adapter = new ArrayAdapter(GroupManagementActivity.this, R.layout.da_items, stringMemberGroupList);
                             groupAsMemberListView.setAdapter(adapter);
                             startActivity(getIntent());
                             break;

                    }
                    // false : close the menu; true : not close the menu
                    return false;
                }
        });

    }



    private void response(Void returnedNothing) {
        notifyUserViaLogAndToast("Delete successfully");
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        switch(requestCode){
            case requestNum:
                if (resultCode == Activity.RESULT_OK) {
                    finish();
                    startActivity(getIntent());
                }
                break;
        }

    }


}
