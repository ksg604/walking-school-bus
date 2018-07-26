package com.example.walkingschoolbus;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.walkingschoolbus.model.Message;
import com.example.walkingschoolbus.model.PermissionRequest;
import com.example.walkingschoolbus.model.Session;
import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import retrofit2.Call;

public class PermissionListActivity extends AppCompatActivity {

    private Session session;
    private User user;
    private static WGServerProxy proxy;

    private ArrayList<PermissionRequest> permissionsListTemp = new ArrayList<>();
    private List<String> permissionsMessage = new ArrayList<>();
    private Set<PermissionRequest.Authorizor> authorizors = new HashSet<>();
    private Set<User> userSet = new HashSet<>();
   // private SwipeMenuListView permissionsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_list);
        Session.getStoredSession(this);
        session = Session.getInstance();
        user = session.getUser();
        String savedToken = session.getToken();

        proxy = ProxyBuilder.getProxy(getString(R.string.api_key),session.getToken(),true);



        // Make call
        Call<List<PermissionRequest>> caller = proxy.getPermissionForUser(user.getId());

        ProxyBuilder.callProxy(PermissionListActivity.this, caller, returnedPermissions -> response(returnedPermissions));
    }


    private void setupPermissionDetails() {
        /*permissionsList.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                Log.i("Hey! I am clicked","Hey!!!");
                TextView textView = (TextView) findViewById(R.id.PermissionDetails);
                String permissionStatus = permissionsListTemp.get(position).getStatus().toString();
                String permissionAction = permissionsListTemp.get(position).getAction();
                Set permissionUserInfo = permissionsListTemp.get(position).getAuthorizors();
                //permissionUserInfo.
                String totalInfo = permissionStatus + permissionAction;
                textView.setText( totalInfo);
                return false;
            }
        });*/


    }


    private void response(List<PermissionRequest> returnedPermission) {

        SwipeMenuListView permissionsList = (SwipeMenuListView) findViewById(R.id.permissionList);

        for (PermissionRequest permission : returnedPermission) {

            permissionsListTemp.add(permission);
            permissionsMessage.add(permission.getMessage());
            authorizors = permission.getAuthorizors();
            Log.i("Testtesttest",authorizors.toString());
/*
            Call<com.example.walkingschoolbus.model.PermissionRequest> caller = proxy.getPermissionById(permission.getId());

            ProxyBuilder.callProxy(PermissionListActivity.this, caller,
                    new ProxyBuilder.SimpleCallback<PermissionRequest>() {
                        @Override
                        public void callback(PermissionRequest returnedPermissions) {
                            Log.i("GOOOOOD", returnedPermissions.toString());
                        }
                    });

            */


            //userSet = authorizors
            ArrayAdapter adapter = new ArrayAdapter(PermissionListActivity.this, R.layout.swipe_listview, permissionsMessage);
            permissionsList.setAdapter(adapter);
        }
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(220, 20, 60)));
                // set item width
                deleteItem.setWidth(180);
                // set item title
                deleteItem.setTitle("Details");
                // set item title fontsize
                deleteItem.setTitleSize(18);
                // set item title font color
                deleteItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(deleteItem);

            }
        };

        // set creator
        permissionsList.setMenuCreator(creator);

        permissionsList.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        Log.i("Hey! I am clicked","Hey!!!");
                        TextView textView = (TextView) findViewById(R.id.PermissionDetails);
                        String permissionStatus = permissionsListTemp.get(position).getStatus().toString();
                        String permissionAction = permissionsListTemp.get(position).getMessage();
                        List<String> authorizorName = new ArrayList<>();
                        List<WGServerProxy.PermissionStatus> authorizorStatus = new ArrayList<>();
                        List<String> authorizorEmail = new ArrayList<>();
                        String authorizorInfo;
                        String moreInfo;
                        for(PermissionRequest.Authorizor temp : permissionsListTemp.get(position).getAuthorizors()) {
                            Log.i("Show me status!!",temp.getStatus().toString());
                           // authorizorStatus.add(temp.getStatus());
                            //temp.getUsers()\
                            authorizorInfo = temp.getStatus().toString();
                            Log.i("TEST1",authorizorInfo);
                            for(User tempUser : temp.getUsers()) {

                                Log.i("Test User ID again Here",tempUser.getId().toString());
                                Call<User> callerForGroup = proxy.getUserById(tempUser.getId());
                                ProxyBuilder.callProxy(PermissionListActivity.this, callerForGroup,
                                        new ProxyBuilder.SimpleCallback<User>() {
                                            @Override
                                            public void callback(User user) {

                                                String userName = user.getName();
                                                Log.i("Show me user name",userName);
                                                //authorizorName.add(userName);
                                                //authorizorEmail.add(user.getEmail());
                                                String temp = user.getEmail()+user.getName() +"\n";
                                                Log.i("TEST2",temp);
                                            }

                                        });
                            }
                        }

                        for(int iiiii= 0; iiiii < authorizorEmail.size(); iiiii++) {
                            String s1 = authorizorName.get(iiiii);
                            String s2 = authorizorEmail.get(iiiii);
                            String s3 = authorizorStatus.get(iiiii).toString();
                            Log.i("wwwwwwww", s1+s2+s3);
                           // authorizorInfo.add(s1+s2+s3+"\n");

                        }
                        String totalInfo = permissionStatus + "\n" + permissionAction;
                        textView.setText( totalInfo);

                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });


    }





    public static Intent makeIntent(Context context) {
        Intent intent = new Intent(context, PermissionListActivity.class);
        return intent;
    }

}
