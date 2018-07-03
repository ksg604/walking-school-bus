package com.example.walkingschoolbus;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.walkingschoolbus.model.Group;
import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;

import java.util.List;

import retrofit2.Call;

public class GroupManagementActivity extends AppCompatActivity {

    private static WGServerProxy proxy;
    private String TAG = "GroupManagementActivity";
    private Group group;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_management);


        proxy = ProxyBuilder.getProxy( getString( R.string.api_key ));
        setupCreateGroupButton();

        populateListView();

    }




    /**
     * Populate the list view of groups user belongs to
     */
    private void populateListView() {
        User user = User.getInstance();
        List<Group> groups;
        groups = user.getMemberOfGroups();
        String[] groupNames = new String[groups.size()];

        //grab names of all groups member belongs too
        for(int i =0; i<groups.size();i++){
            groupNames[i]=groups.get(i).getName();
        }
        //create array adaptor
        ArrayAdapter<String> adaptor = new ArrayAdapter<>(this, R.layout.groups_listview,
                groupNames);

        //configure list view for layout
        ListView list = findViewById(R.id.listViewGroups);
        list.setAdapter(adaptor);
    }



    /**
     * set up the button to create group
     */
    private void setupCreateGroupButton() {

        Button createGroupButton = (Button) findViewById( R.id.btnAddGroup );
        createGroupButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                group = Group.getInstance();

                // Make call


                Call< Group> caller = proxy.createGroup(group);
                ProxyBuilder.callProxy(GroupManagementActivity.this, caller, returnedGroup -> response(returnedGroup));

            }
        } );



    }



    // Handle the token by generating a new Proxy which is encoded with it.
    private void onReceiveToken(String token) {
        // Replace the current proxy with one that uses the token!
        Log.w(TAG, "   --> NOW HAVE TOKEN: " + token);
        proxy = ProxyBuilder.getProxy(getString(R.string.api_key), token);
    }

    private void response(Group returnedGroup) {
        notifyUserViaLogAndToast("Server replied to login request (no content was expected).");
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
        return new Intent(context,GroupManagementActivity.class);
    }


}
