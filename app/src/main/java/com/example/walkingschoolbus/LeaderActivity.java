/**
 * LeaderActivity class to provide user with options related to groups he or her manages
 * Such as adding or removing members.
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
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.walkingschoolbus.model.GpsLocation;
import com.example.walkingschoolbus.model.Group;
import com.example.walkingschoolbus.model.Session;
import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;


public class LeaderActivity extends AppCompatActivity {

    private Session tokenSession = Session.getInstance();
    private List<User> userList = new ArrayList<>();
    private List<String> stringUserList = new ArrayList<>( );
    private Group group;
    private User user;
    private GpsLocation userLastGpsLocation = new GpsLocation();
    private GpsLocation schoolGpsLocation = new GpsLocation();
    private String userToken;
    private static WGServerProxy proxy;
    private static final String TAG = "LeaderActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_leader );

        //get token from session
        userToken = tokenSession.getToken();
        // Build the server proxy
        proxy = ProxyBuilder.getProxy(getString( R.string.api_key),userToken);
        //get the group instance
        user = tokenSession.getUser();
        group = Group.getInstance();
        getGroupInfo();
        //set Walking With This Group Button
        setWalkWithThisGroupBtn();

        //Make call for a list of member
        Call<List<User>> caller = proxy.getGroupMembers(group.getId());
        ProxyBuilder.callProxy(LeaderActivity.this, caller, returnedUsers-> responseForList(returnedUsers));
    }
    /**
     * get response from the server to get a group member list as a leader
     * @param returnedUsers the list of members of the group I want to get
     *
     */
    private void responseForList(List<User> returnedUsers) {
        Log.w(TAG, "Got all users of this group!!");

        SwipeMenuListView userListView = (SwipeMenuListView) findViewById(R.id.userList);

        for (User member : returnedUsers) {

            Log.w( TAG, "    member: " + member.getId() );

            String userInfo = getString( R.string.user_name_list )+ " "+ member.getName() + "\n" +
                    getString(R.string.user_email_list)+ " " + member.getEmail();
            stringUserList.add( userInfo );

            ArrayAdapter adapter = new ArrayAdapter( LeaderActivity.this, R.layout.swipe_listview, stringUserList );

            userListView.setAdapter( adapter );
        }

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {

                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem( getApplicationContext());
                // set item background
                openItem.setBackground(new ColorDrawable( Color.rgb(0xC9, 0xC9, 0xCE)));
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
                deleteItem.setTitle(getString( R.string.delete_swipe));
                // set item title font size
                deleteItem.setTitleSize(18);
                // set item title font color
                deleteItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };

        // set creator
        userListView.setMenuCreator(creator);
        userListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {

                    case 0:
                        Long memberId = returnedUsers.get(position).getId();
                        Intent intent = ViewUserSettingsActivity.makeIntent( LeaderActivity.this, memberId  );
                        startActivity(intent);
                        break;

                    case 1:

                        Call<Void> caller = proxy.removeGroupMember(group.getId(), returnedUsers.get(position).getId());
                        ProxyBuilder.callProxy(LeaderActivity.this, caller, returnedNothing -> responseForRemove(returnedNothing));
                        userListView.removeViewsInLayout(position,1);
                        finish();
                        ArrayAdapter adapter = new ArrayAdapter(LeaderActivity.this, R.layout.swipe_listview, stringUserList);
                        userListView.setAdapter(adapter);
                        startActivity( getIntent() );
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });
    }

    /**
     * After delete the group, show user that group is deleted
     */
    private void responseForRemove(Void returnedNothing) {
        notifyUserViaLogAndToast(getString(R.string.delete_message));
    }

    /**
     *Set group description
     */
    private void setGroupDescriptionTxt() {
        TextView groupDescription = (TextView) findViewById( R.id.groupDescripTxt );
        groupDescription.setText( group.getGroupDescription() );

    }



    private void notifyUserViaLogAndToast(String message) {
        Log.w(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**Make intent for main menu activity
     *
     * @param context
     * @return
     */
    public static Intent makeIntent(Context context){
        Intent intent = new Intent( context, LeaderActivity.class );
        return intent;
    }


    private void getGroupInfo() {
        Call<Group> caller = proxy.getGroupById(group.getId());
        ProxyBuilder.callProxy(LeaderActivity.this, caller, returnedGroup-> responseForGroup(returnedGroup));
    }

    private void responseForGroup(Group returnedGroup) {
        group.setGroupDescription( returnedGroup.getGroupDescription() );
        group.setRouteLatArray( returnedGroup.getRouteLatArray() );
        group.setRouteLngArray( returnedGroup.getRouteLngArray() );
        group.setLeader( returnedGroup.getLeader() );
        group.setMemberUsers( returnedGroup.getMemberUsers() );
        schoolGpsLocation.setLat( group.getRouteLatArray().get(1));
        schoolGpsLocation.setLng( group.getRouteLatArray().get(1));
        setGroupDescriptionTxt();
    }

    private void setWalkWithThisGroupBtn() {
        Button setWalkingWithThisGroup = (Button) findViewById( R.id.walkingWithThisGroupBtn );
        setWalkingWithThisGroup.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tokenSession.setGroup(group);
                if(!tokenSession.isTracking()){
                    MainMenu.turnOnGpsUpdate();
                    tokenSession.setTracking( true );
                    notifyUserViaLogAndToast( getString(R.string.gps_message) );

                }
            }
        } );
    }
}
