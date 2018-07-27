/**
 * Activity shows all groups a child is a member of and gives the user the option to update their
 * child's information
 */
package com.example.walkingschoolbus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Path;
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

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

public class OpenKidActivity extends AppCompatActivity {

    private static final String TAG = "OpenKidActivity";
    private Session session;
    private User user;
    private User kidUser;
    private static WGServerProxy proxy;
    private List<String> kidsGroupList = new ArrayList<>();
    private List<Group> listOfKidGroups = new ArrayList<>();
    private static final int REQUEST_CODE = 1111;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_kid);
        session = Session.getInstance();
        user = session.getUser();
        String savedToken = session.getToken();
        proxy = ProxyBuilder.getProxy(getString(R.string.api_key), session.getToken());
        Intent getFromMyKidsIntent = getIntent();
        String kidsEmail = getFromMyKidsIntent.getExtras().getString("E");
        Call<User> caller = proxy.getUserByEmail(kidsEmail);
        ProxyBuilder.callProxy(OpenKidActivity.this, caller, returnedKid -> response(returnedKid));
    }

    private void response(User kid){
        kidUser = kid;

        TextView openKidsTitle = (TextView) findViewById( R.id.openKidTitle );
        String kidTitle = getString(R.string.open_kid_title_part1) + " " + kid.getName() + " " + getString(R.string.open_kid_title_part2);
        openKidsTitle.setText( kidTitle );

        List<Group> groupListKid = kid.getMemberOfGroups();

        for(Group group: groupListKid){
            Call<Group> caller = proxy.getGroupById(group.getId());
            ProxyBuilder.callProxy(OpenKidActivity.this, caller,
                    returnedGroup->responseForGroup(returnedGroup, groupListKid, kidUser));
        }
        setupUpdateKidBtn();
        setupAddToGroupBtn();
    }

    private void responseForGroup(Group group,List<Group> groupListKids, User kid) {
        SwipeMenuListView groupList = (SwipeMenuListView) findViewById(R.id.kidGroupList);
        Log.i("Debug66","Name: "+group.getId());

        String groupInfo = getString(R.string.open_kid_group_id) + " " + group.getId()+"\n"+
                 getString(R.string.open_kid_group_description) + group.getGroupDescription();
        kidsGroupList.add(groupInfo);

        ArrayAdapter adapter = new ArrayAdapter(OpenKidActivity.this, R.layout.swipe_listview, kidsGroupList);
        groupList.setAdapter(adapter);

        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                // create "map" item
                SwipeMenuItem mapItem = new SwipeMenuItem(getApplicationContext());
                // set item background
                mapItem.setBackground(new ColorDrawable(Color.rgb(220, 0, 220)));
                // set item width
                mapItem.setWidth(180);
                // set item title
                mapItem.setTitle(getString(R.string.map_open_kid));
                // set item title fontsize
                mapItem.setTitleSize(12);
                // set item title font color
                mapItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(mapItem);
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem(getApplicationContext());
                // set item background
                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9, 0xCE)));
                // set item width
                openItem.setWidth(180);
                // set item title
                openItem.setTitle(getString(R.string.mykids_open_swipe));
                // set item title fontsize
                openItem.setTitleSize(12);
                // set item title font color
                openItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(openItem);
                SwipeMenuItem removeItem = new SwipeMenuItem(getApplicationContext());
                // set item background
                removeItem.setBackground(new ColorDrawable(Color.rgb(220, 20,
                        60)));
                // set item width
                removeItem.setWidth(180);
                // set item title
                removeItem.setTitle(getString(R.string.mykids_remove_swipe));
                // set item title fontsize
                removeItem.setTitleSize(12);
                // set item title font color
                removeItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(removeItem);
            }
        };

        groupList.setMenuCreator(creator);

        groupList.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch(index){
                    case 0:
                        Intent intentForMap = ParentsDashboardActivity.makeIntent(OpenKidActivity.this,groupListKids.get(position).getId());
                        startActivity(intentForMap);
                        break;
                    case 1:
                        Intent intentForOpen = OpenKidGroupActivity.makeIntent(OpenKidActivity.this,
                                groupListKids.get(position).getId());
                        startActivity(intentForOpen);
                        break;

                    case 2:
                        Call<Void> caller = proxy.removeGroupMember(group.getId(),kid.getId());
                        ProxyBuilder.callProxy(OpenKidActivity.this, caller, returnedNothing -> response(returnedNothing));
                        groupList.removeViewsInLayout(position, 1);
                        Toast.makeText(OpenKidActivity.this,getString(R.string.open_kid_title_part1)+kid.getName() +getString(R.string.open_kid_title_part2),Toast.LENGTH_LONG)
                                .show();
                        break;
                }
                return false;
            }
        });
    }

    private void response(Void returnedNothing) { }

    public static Intent makeIntent(Context context, String userEmailToPass) {
        Intent intent = new Intent(context, OpenKidActivity.class);
        intent.putExtra("E",userEmailToPass);
        return intent;
    }

    public static Intent makeIntent(Context context) {
        Intent intent = new Intent(context, OpenKidActivity.class);
        return intent;
    }

    private void setupUpdateKidBtn(){
        Button updateKidBtn = findViewById(R.id.updateKidBtn);
        updateKidBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = ViewUserSettingsActivity.makeIntent(OpenKidActivity.this,kidUser.getId());
                startActivity(intent);
            }
        });
    }

    private void setupAddToGroupBtn(){
        Button addToGroupBtn = findViewById(R.id.addKidToGroupBtn);
        addToGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = AddKidToGroupActivity.makeIntent(OpenKidActivity.this);
                intent.putExtra("kidEmail",kidUser.getEmail());
                startActivity(intent);
            }
        });
    }

    @Override
    public void onRestart()
    {
        super.onRestart();
        finish();
        startActivity(getIntent());
    }
}