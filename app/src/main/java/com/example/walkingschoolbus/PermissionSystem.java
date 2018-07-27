package com.example.walkingschoolbus;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
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



public class PermissionSystem extends AppCompatActivity {


    private User user;
    private static WGServerProxy proxy;
    private Session session;


    private ArrayList<PermissionRequest> permissionsListTemp = new ArrayList<>();
    private List<String> permissionsMessage = new ArrayList<>();

    private static Handler handler = new Handler();
    private static Runnable runnableCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_system);
        makeHandlerRun();
        Session.getStoredSession(this);
        session = Session.getInstance();
        user = session.getUser();

        //setPermissionTextView();
        setupPermissionListButton();

        proxy = ProxyBuilder.getProxy(getString(R.string.api_key),session.getToken(),true);
    }

    private void setupPermissionListButton() {
        ImageButton btn = (ImageButton) findViewById(R.id.imageBtnPermissonLIst);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = PermissionListActivity.makeIntent(PermissionSystem.this);
                startActivity(intent);
            }
        });
    }

/*//        TextView monitoringList = (TextView) findViewById( R.id.myPermissionList );
  //      String monitoring = getString(R.string.monitoring_title);
    //    monitoringList.setText( monitoring );
    }
*/

    public static Intent makeIntent(Context context) {
        Intent intent = new Intent(context, PermissionSystem.class);
        return intent;
    }

    private void response(List<PermissionRequest> returnedPermission) {
        SwipeMenuListView permissionsList = (SwipeMenuListView) findViewById(R.id.myPermissionList);

        for (PermissionRequest permission : returnedPermission) {
               if(permission.getStatus().equals(WGServerProxy.PermissionStatus.PENDING)&&
                       (!permissionsListTemp.contains(permission))) {
                   permissionsListTemp.add(permission);
                   permissionsMessage.add(permission.getMessage());
                   ArrayAdapter adapter = new ArrayAdapter(PermissionSystem.this, R.layout.swipe_listview, permissionsMessage);
                   permissionsList.setAdapter(adapter);
               }
        }
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem( getApplicationContext());
                // set item background
                openItem.setBackground(new ColorDrawable(Color.rgb(5, 220, 5)));
                // set item width
                openItem.setWidth(180);
                // set item title
                openItem.setTitle("Approve");
                // set item title fontsize
                openItem.setTitleSize(12);
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
                deleteItem.setTitle("Deny");
                // set item title fontsize
                deleteItem.setTitleSize(12);
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
                        Call<List<PermissionRequest>> caller = proxy.approveOrDenyPermissionRequest(permissionsListTemp.get(position).getId(),
                                WGServerProxy.PermissionStatus.APPROVED  );
                        Log.i("Show me here",permissionsListTemp.get(position).getId().toString());
                        ProxyBuilder.callProxy(PermissionSystem.this, caller, returnedUsers -> response2(returnedUsers));
                      //  monitoringList.removeViewsInLayout(position,1);
                        permissionsList.removeViewsInLayout(position,1);
                        break;

                    case 1:
                        Call<List<PermissionRequest>> caller2 = proxy.approveOrDenyPermissionRequest(permissionsListTemp.get(position).getId(),
                                WGServerProxy.PermissionStatus.DENIED  );
                        ProxyBuilder.callProxy(PermissionSystem.this, caller2, returnedUsers -> response2(returnedUsers));
                        permissionsList.removeViewsInLayout(position,1);
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });
    }

    private void makeHandlerRun() {
        runnableCode = new Runnable(){
            public void run() {
                setupGetUnreadPermissions();
               // setupAlert();
                handler.postDelayed(this, 30000);
            }
        };
        handler.post(runnableCode);
    }

    private void setupGetUnreadPermissions() {
        Call<List<PermissionRequest>> caller = proxy.getPermissionForUserPending(user.getId(),
                WGServerProxy.PermissionStatus.PENDING);
        Log.i("My id:::::",user.getId().toString());
        ProxyBuilder.callProxy(PermissionSystem.this, caller, returnedUsers -> response(returnedUsers));
    }

    private void response2(List<PermissionRequest> returnedNothing) {
    }
}
