/**
 * Activity allows the user to view all members in a group and the group leader
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
import com.example.walkingschoolbus.model.Group;
import com.example.walkingschoolbus.model.Session;
import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

public class MemberActivity extends AppCompatActivity {

    private Session session = Session.getInstance();
    private String userToken;
    private static WGServerProxy proxy;
    private User user;
    private Group group;
    private List<String> leaderList = new ArrayList<>( );
    private List<String> memberList = new ArrayList<>( );
    public static final String TAG = "MemberActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_member );

        //get token from session
        userToken = session.getToken();
        // Build the server proxy
        proxy = ProxyBuilder.getProxy(getString( R.string.api_key),userToken);
        //get the group instance
        user = session.getUser();
        group = Group.getInstance();

        //set Walking With This Group Button
        setWalkWithThisGroupBtn();

        //Make call for a group info
        setUpLeader();
        setupMemberList();
        //Make call for a list of member
    }


    private void setUpLeader() {

        Call<Group> caller = proxy.getGroupById(group.getId());
        ProxyBuilder.callProxy(MemberActivity.this, caller, returnedGroup-> responseForGroup(returnedGroup));

    }

    private void responseForGroup(Group group) {
        Long leaderId = group.getLeader().getId();
        setGroupDescriptionTxt();
        Call<User> caller = proxy.getUserById( leaderId );
        ProxyBuilder.callProxy( MemberActivity.this, caller, returnedUser -> responseForLeader( returnedUser ));
    }

    private void responseForLeader(User leader){
        String leaderName = leader.getName();
        String leaderEmail = leader.getEmail();
        Long leaderId = leader.getId();

        String leaderInfo = getString( R.string.user_name_list )+ " "+ leaderName + "\n" +
                getString(R.string.user_email_list)+ " " + leaderEmail;
        leaderList.add(leaderInfo);

        SwipeMenuListView leaderListView = (SwipeMenuListView) findViewById(R.id.leaderInMember);

        ArrayAdapter adapterLeader = new ArrayAdapter( MemberActivity.this,
                R.layout.swipe_listview,leaderList );
        leaderListView.setAdapter( adapterLeader );
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
            }
        };

        // set creator
        leaderListView.setMenuCreator(creator);

        leaderListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        Intent intent = ViewUserSettingsActivity.makeIntent( MemberActivity.this, leaderId  );
                        startActivity(intent);
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });
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

    private void setupMemberList() {
        Call<List<User>> caller = proxy.getGroupMembers(group.getId());
        ProxyBuilder.callProxy(MemberActivity.this, caller, returnedUsers-> responseForMemList(returnedUsers));
    }

    private void responseForMemList(List<User> returnedUsers) {
        notifyUserViaLogAndToast("Got list of " + returnedUsers.size() + " users! See logcat.");
        Log.w(TAG, "Got all users of this group!!");

        SwipeMenuListView memberListView = (SwipeMenuListView) findViewById(R.id.memberListInMember);

        for (User member : returnedUsers) {

            Log.w( TAG, "    member: " + member.getId() );
            String userInfo = getString( R.string.user_name_list )+ " "+ member.getName() + "\n" +
                    getString(R.string.user_email_list)+ " " + member.getEmail();
            memberList.add( userInfo );
            ArrayAdapter adapter = new ArrayAdapter( MemberActivity.this, R.layout.swipe_listview, memberList );
            memberListView.setAdapter( adapter );
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
        memberListView.setMenuCreator(creator);

        memberListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {

                    case 0:
                        Long memberId = returnedUsers.get(position).getId();
                        Intent intent = ViewUserSettingsActivity.makeIntent( MemberActivity.this, memberId  );
                        startActivity(intent);
                        break;
                    case 1:
                        Call<Void> caller = proxy.removeGroupMember(group.getId(), returnedUsers.get(position).getId());
                        ProxyBuilder.callProxy(MemberActivity.this, caller, returnedNothing -> responseForRemove(returnedNothing));
                        memberListView.removeViewsInLayout(position,1);
                        finish();
                        ArrayAdapter adapter = new ArrayAdapter(MemberActivity.this, R.layout.swipe_listview, memberList);
                        memberListView.setAdapter(adapter);
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
        notifyUserViaLogAndToast("Delete successfully");
    }


    private void setWalkWithThisGroupBtn() {
        Button setWalkingWithThisGroup = (Button) findViewById( R.id.walkingWithThisGroupBtn );
        setWalkingWithThisGroup.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                session.setGroup(group);
                if(!session.isTracking()){
                    MainMenu.turnOnGpsUpdate();
                    session.setTracking( true );
                    notifyUserViaLogAndToast( "Now your GPS is updating " );
                }
            }
        } );
    }

    /**
     *Set group description
     */
    private void setGroupDescriptionTxt() {
        TextView groupDescription = (TextView) findViewById( R.id.groupDescripTxtInMember );
        groupDescription.setText( group.getGroupDescription() );

    }
}
