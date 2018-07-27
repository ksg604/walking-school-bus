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

    List<String> authorizorName = new ArrayList<>();
    List<String> authorizorStatus = new ArrayList<>();
    List<String> authorizorEmail = new ArrayList<>();

    String problem;
    String permissionStatus ;
    String permissionAction ;

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

    private void response(List<PermissionRequest> returnedPermission) {

        SwipeMenuListView permissionsList = (SwipeMenuListView) findViewById(R.id.permissionList);

        for (PermissionRequest permission : returnedPermission) {

            permissionsListTemp.add(permission);
            permissionsMessage.add(permission.getMessage());
            authorizors = permission.getAuthorizors();
            Log.i("Testtesttest",authorizors.toString());

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
                        //TextView textView = (TextView) findViewById(R.id.PermissionDetails);
                        permissionStatus = permissionsListTemp.get(position).getStatus().toString();
                        permissionAction = permissionsListTemp.get(position).getMessage();

                        authorizorEmail.clear();
                        authorizorStatus.clear();
                        authorizorName.clear();


                        for(PermissionRequest.Authorizor temp : permissionsListTemp.get(position).getAuthorizors()) {

                            authorizorStatus.add(temp.getStatus().toString());

                            for(User tempUser : temp.getUsers()) {

                                Log.i("Test User ID again Here",tempUser.getId().toString());
                                Call<User> caller = proxy.getUserById(tempUser.getId());
                                ProxyBuilder.callProxy(PermissionListActivity.this, caller, returnedPermissions -> response2(returnedPermissions));
                            }
                        }
                        Log.i("Sssize of status",Integer.toString(authorizorStatus.size()));
                        Log.i("Sssize of name",Integer.toString(authorizorName.size()));
                        Log.i("Sssize of email",Integer.toString(authorizorEmail.size()));
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });
    }

    private void response2(User returnedNothing) {
        TextView textView = (TextView) findViewById(R.id.PermissionDetails);

        authorizorName.add(returnedNothing.getName());
        authorizorEmail.add(returnedNothing.getEmail());
        if(authorizorStatus.size() == authorizorName.size()) {
            String subInfo = "Authorizors:"+"\n";
            for(int counter =0; counter < authorizorStatus.size(); counter++) {
                subInfo += authorizorName.get(counter) +"("+ authorizorEmail.get(counter)+")"
                        +":"+ authorizorStatus.get(counter) + "\n" ;
            }
            problem = subInfo;
            String totalInfo = "Status: " +permissionStatus + "\n" + "Action: "+permissionAction + "\n" + problem;
            textView.setText( totalInfo);
            Log.i("Loop 1",problem);

        }
    }

    public static Intent makeIntent(Context context) {
        Intent intent = new Intent(context, PermissionListActivity.class);
        return intent;
    }
}
