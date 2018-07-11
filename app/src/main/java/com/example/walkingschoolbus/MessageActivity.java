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

import com.example.walkingschoolbus.model.Session;
import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;

public class MessageActivity extends AppCompatActivity {

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


        user = User.getInstance();
        Session.getStoredSession(this);
        session = Session.getInstance();
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


        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                //return false;
                Long tempID = monitoringUser.get(childPosition);

                Intent intent = SendMessageActivity.makeIntent(MessageActivity.this, tempID );
                startActivity(intent);
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

    private void responseForParentList(List<User> returnedUsers) {
        List<String> androidStudio = new ArrayList<>();
        for (User user : returnedUsers) {
            // Log.w(TAG, "    User: " + user.toString());
            String userInfo = getString(R.string.monitoring_user_name) + " "  + user.getName() +"\n"+
                    getString(R.string.monitoring_user_email)+ " " + user.getEmail();

            // List<String> edmtDev = new ArrayList<>();
            androidStudio.add(userInfo);
            monitoringUser.add(user.getId());

            //listHash.put(listDataHeader.get(0),edmtDev);
            listHash.put(listDataHeader.get(1),androidStudio);
            //monitoringUser.add(userInfo);
            //ArrayAdapter adapter = new ArrayAdapter(MonitoringListActivity.this, R.layout.da_items, monitoringUser);
            //monitoringList.setAdapter(adapter);
        }


    }

    private void responseForChildList(List<User> returnedUsers) {
        List<String> edmtDev = new ArrayList<>();
        for (User user : returnedUsers) {
           // Log.w(TAG, "    User: " + user.toString());
            String userInfo = getString(R.string.monitoring_user_name) + " "  + user.getName() +"\n"+
                    getString(R.string.monitoring_user_email)+ " " + user.getEmail();

           // List<String> edmtDev = new ArrayList<>();
            edmtDev.add(userInfo);

            listHash.put(listDataHeader.get(0),edmtDev);
            //monitoringUser.add(userInfo);
            //ArrayAdapter adapter = new ArrayAdapter(MonitoringListActivity.this, R.layout.da_items, monitoringUser);
            //monitoringList.setAdapter(adapter);
        }

    }

    private void initData() {

        listDataHeader.add("Children List");
        listDataHeader.add("Parents List");
        listDataHeader.add("Group List");
        listDataHeader.add("I don't know");


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
