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
import com.example.walkingschoolbus.model.PermissionRequest;
import com.example.walkingschoolbus.model.Session;
import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;




import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

import static com.example.walkingschoolbus.proxy.WGServerProxy.PermissionStatus.APPROVED;

public class PermissionSystem extends AppCompatActivity {


    private User user;
    private static WGServerProxy proxy;
    private Session session;


    private ArrayList<PermissionRequest> permissionsListTemp = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_system);

        Session.getStoredSession(this);
        session = Session.getInstance();
        user = session.getUser();
        String savedToken = session.getToken();

        setPermissionTextView();

        proxy = ProxyBuilder.getProxy(getString(R.string.api_key),session.getToken());

        // Make call
        Call<List<PermissionRequest>> caller = proxy.getPermissionForUser(user.getId());
        ProxyBuilder.callProxy(PermissionSystem.this, caller, returnedUsers -> response(returnedUsers));
    }

    private void setPermissionTextView() {
//        TextView monitoringList = (TextView) findViewById( R.id.myPermissionList );
  //      String monitoring = getString(R.string.monitoring_title);
    //    monitoringList.setText( monitoring );
    }


    public static Intent makeIntent(Context context) {
        Intent intent = new Intent(context, PermissionSystem.class);
        return intent;
    }


    private void response(List<PermissionRequest> returnedPermission) {


        SwipeMenuListView permissionsList = (SwipeMenuListView) findViewById(R.id.myPermissionList);

        for (PermissionRequest permission : returnedPermission) {
           // Log.w(TAG, "    User: " + user.toString());
            //String userInfo = getString(R.string.monitoring_user_name) + " "  + user.getName() +"\n"+
              //      getString(R.string.monitoring_user_email)+ " " + user.getEmail();
            String permissionInfo = permission.getStatus().toString();
            PermissionRequest permissionRequest = new PermissionRequest();
            permissionRequest = permission;
            permissionsListTemp.add(permissionRequest);
            ArrayAdapter adapter = new ArrayAdapter(PermissionSystem.this, R.layout.swipe_listview, permissionsListTemp);
            permissionsList.setAdapter(adapter);

        }
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem( getApplicationContext());
                // set item background
                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9, 0xCE)));
                // set item width
                openItem.setWidth(180);
                // set item title
                openItem.setTitle("Aprove");
                // set item title fontsize
                openItem.setTitleSize(18);
                // set item title font color
                openItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(openItem);


                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem( getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(220, 20,
                        60)));

                // set item width
                deleteItem.setWidth(180);
                // set item title
                deleteItem.setTitle(getString(R.string.add_to_group_swipe));
                // set item title fontsize
                deleteItem.setTitleSize(18);
                // set item title font color
                deleteItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(deleteItem);


                // create "group" list item
                SwipeMenuItem goGroup = new SwipeMenuItem( getApplicationContext());
                // set item background
                goGroup.setBackground(new ColorDrawable(Color.rgb(120, 120,
                        20)));

                // set item width
                goGroup.setWidth(180);
                // set item title
                goGroup.setTitle(getString(R.string.gorup_swipe));
                // set item title fontsize
                goGroup.setTitleSize(18);
                // set item title font color
                goGroup.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(goGroup);
            }
        };

        // set creator
        permissionsList.setMenuCreator(creator);

        permissionsList.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        Call<List<PermissionRequest>> caller = proxy.approveOrDenyPermissionRequest(permissionsListTemp.get(position).getId(), WGServerProxy.PermissionStatus.APPROVED);
                        ProxyBuilder.callProxy(PermissionSystem.this, caller, returnedNothing -> response(returnedNothing));
                      //  monitoringList.removeViewsInLayout(position,1);
                        break;

                    case 1:


                        break;

                    case 2:

                        break;

                }



                // false : close the menu; true : not close the menu
                return false;
            }

        });


    }
    private void response(Void returnedNothing) {
       // notifyUserViaLogAndToast(MonitoringListActivity.this.getString(R.string.notify_not_monitor));
    }



}
