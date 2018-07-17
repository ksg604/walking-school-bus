package com.example.walkingschoolbus;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.walkingschoolbus.model.Group;
import com.example.walkingschoolbus.model.Session;
import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;

public class MessageActivity extends AppCompatActivity {

    private List<String> stringMemberGroupList = new ArrayList< >( );
    private List<String> stringLeaderGroupList = new ArrayList< >( );
    private List<Group> groupLeaderList = new ArrayList<>();
    private List<Group> modifiedGroupLeaderList = new ArrayList<>(  );
    private List<Long> groupIdLeaderList = new ArrayList<>();

    private List<Group> groupMemberList = new ArrayList<>();
    private List<Group> modifiedGroupMemberList = new ArrayList<>( );
    private List<Long> groupIdMemberList = new ArrayList<>();



    private ExpandableListView listView;
    private ExpandableListAdapter listAdapter;
    private List<String> listDataHeader;
    private HashMap<String,List<String>> listHash;

    private Session session;
    private User user;
    private static WGServerProxy proxy;

    private ArrayList<Long> monitoringUser = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        listDataHeader = new ArrayList<>();
        listHash = new HashMap<>();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message2);


        //user = User.getInstance();

        Session.getStoredSession(this);
        session = Session.getInstance();
        user = User.getInstance();

        String savedToken = session.getToken();

        //setMonitoringTextView();

        proxy = ProxyBuilder.getProxy(getString(R.string.api_key),session.getToken());


        listView = (ExpandableListView)findViewById(R.id.messageList);


        initData();

        listAdapter = new com.example.walkingschoolbus.model.ExpandableListAdapter(this,listDataHeader,listHash);

        listView.setAdapter(listAdapter);


        // Make call
        Call<List<User>> callerForChildList = proxy.getMonitorsUsers(user.getId());
        ProxyBuilder.callProxy(MessageActivity.this, callerForChildList,
                returnedUsers -> responseForChildList(returnedUsers));

        Call<List<User>> callerForParentList = proxy.getMonitoredByUsers(user.getId());
        ProxyBuilder.callProxy(MessageActivity.this, callerForParentList,
                returnedUsers -> responseForParentList(returnedUsers));


        Call<User> callerForGropuList = proxy.getUserByEmail(user.getEmail());
        ProxyBuilder.callProxy(MessageActivity.this, callerForGropuList,
                returnedUser -> responseForGroupList(returnedUser));

        //user.getLeadsGroups()

        setupEmergency();

        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                //return false;
                if(groupPosition == 2) {
                    Long tempID = groupIdLeaderList.get(childPosition);

                    Intent intent = SendMessageActivity.makeIntent(MessageActivity.this, tempID);
                    startActivity(intent);
                }

                return false;

            }
        });

        /*
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Long tempID = monitoringUser.get(position);

                Intent intent = SendMessageActivity.makeIntent(MessageActivity.this, tempID );
                startActivity(intent);

            }
        });
        */

    }

    private void setupEmergency() {
        List<String> Emergency = new ArrayList<>();
        Emergency.add("I Have Emergency!");

        //listHash.put(listDataHeader.get(0),edmtDev);
        listHash.put(listDataHeader.get(3),Emergency);
    }

    private void responseForGroupList(User returnedUser) {

        groupLeaderList = returnedUser.getLeadsGroups();
       // returnedUser.getLeadsGroups();
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
        ProxyBuilder.callProxy(MessageActivity.this, caller,
                returnedGroupList -> responseForGroupCheck(returnedGroupList));
    }


    private void responseForGroupCheck(List<Group> returnedGroups) {

        List<String> groupList = new ArrayList<>();

        for (Group group : returnedGroups) {

            if (groupIdMemberList.contains(group.getId())) {
                //Log.w( TAG, getString( R.string.group_list) + " " + group.getId() );

                modifiedGroupMemberList.add(group);
                String groupInfo = getString(R.string.group_list) + " " + group.getGroupDescription();
                stringMemberGroupList.add(groupInfo);

            } else if (groupIdLeaderList.contains(group.getId())) {
                // Log.w( TAG, getString( R.string.group_list) + " " + group.getId() );

                modifiedGroupLeaderList.add(group);
                String groupInfo = getString(R.string.group_list) + " " + group.getGroupDescription();
                stringLeaderGroupList.add(groupInfo);

                groupList.add(groupInfo);
                monitoringUser.add(user.getId());

                //listHash.put(listDataHeader.get(0),edmtDev);
                listHash.put(listDataHeader.get(2),groupList);
            }

        }

    }

    private void responseForParentList(List<User> returnedUsers) {
        List<String> ParentList = new ArrayList<>();
        for (User user : returnedUsers) {
            // Log.w(TAG, "    User: " + user.toString());
            String userInfo = getString(R.string.monitoring_user_name) + " "  + user.getName() +"\n"+
                    getString(R.string.monitoring_user_email)+ " " + user.getEmail();

            // List<String> edmtDev = new ArrayList<>();
            ParentList.add(userInfo);
            monitoringUser.add(user.getId());

            //listHash.put(listDataHeader.get(0),edmtDev);
            listHash.put(listDataHeader.get(1),ParentList);
            //monitoringUser.add(userInfo);
            //ArrayAdapter adapter = new ArrayAdapter(MonitoringListActivity.this, R.layout.da_items, monitoringUser);
            //monitoringList.setAdapter(adapter);
        }


    }

    private void responseForChildList(List<User> returnedUsers) {
        List<String> childList = new ArrayList<>();
        for (User user : returnedUsers) {
           // Log.w(TAG, "    User: " + user.toString());
            String userInfo = getString(R.string.monitoring_user_name) + " "  + user.getName() +"\n"+
                    getString(R.string.monitoring_user_email)+ " " + user.getEmail();

           // List<String> edmtDev = new ArrayList<>();
            childList.add(userInfo);

            listHash.put(listDataHeader.get(0),childList);
            //monitoringUser.add(userInfo);
            //ArrayAdapter adapter = new ArrayAdapter(MonitoringListActivity.this, R.layout.da_items, monitoringUser);
            //monitoringList.setAdapter(adapter);
        }

    }

    private void initData() {

        listDataHeader.add("Children List");
        listDataHeader.add("Parents List");
        listDataHeader.add("My Group List");
        listDataHeader.add("Emergency");


        /*
        List<String> edmtDev = new ArrayList<>();
        edmtDev.add("This is Expandable ListView");

        List<String> androidStudio = new ArrayList<>();
        androidStudio.add("Expandable ListView");
        androidStudio.add("Google Map");
        androidStudio.add("Chat Application");
        androidStudio.add("Firebase ");

        List<String> xamarin = new ArrayList<>();
        xamarin.add("Xamarin Expandable ListView");
        xamarin.add("Xamarin Google Map");
        xamarin.add("Xamarin Chat Application");
        xamarin.add("Xamarin Firebase ");

        List<String> uwp = new ArrayList<>();
        uwp.add("UWP Expandable ListView");
        uwp.add("UWP Google Map");
        uwp.add("UWP Chat Application");
        uwp.add("UWP Firebase ");

        listHash.put(listDataHeader.get(0),edmtDev);
        listHash.put(listDataHeader.get(1),androidStudio);
        listHash.put(listDataHeader.get(2),xamarin);
        listHash.put(listDataHeader.get(3),uwp);
        */
    }


    public static Intent makeIntent(Context context){
        Intent intent = new Intent(context,MessageActivity.class);
        return intent;
    }

}
