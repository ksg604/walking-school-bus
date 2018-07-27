/**
 * Activity allows user to view all other users who monitor them.
 */

package com.example.walkingschoolbus;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.walkingschoolbus.model.Session;
import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

public class ViewUserMonitoredByActivity extends AppCompatActivity {

    private Session session;
    private static WGServerProxy proxy;
    private static final String TAG = "ViewUserMonitoredBy";
    private User theKid;
    private List<String> kidsParentsListInfo = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user_monitored_by);
        session = Session.getInstance();
        proxy = ProxyBuilder.getProxy(getString(R.string.api_key), session.getToken());
        getFromOpenKidGroup();
    }

    private void getFromOpenKidGroup(){
        Intent getFromOpenKidsIntent = getIntent();
        Long userId = getFromOpenKidsIntent.getExtras().getLong("U");
        Log.i("Tag 97","User id is: "+userId);

        Call<User> caller = proxy.getUserById(userId);
        ProxyBuilder.callProxy(ViewUserMonitoredByActivity.this, caller, returnedKid -> response(returnedKid));
    }

    private void response(User theReturnedUser){
        theKid = theReturnedUser;
        Log.i("Tag98","kid name: "+theKid.getName());

        if(theKid == null){
            Log.i("Tag98","user is null");
        }
        Call<List<User>> getUserParentListCaller = proxy.getMonitoredByUsers(theReturnedUser.getId());
        ProxyBuilder.callProxy(ViewUserMonitoredByActivity.this, getUserParentListCaller, returnedList -> response2(returnedList));
    }

    private void response2(List<User> theReturnedList){
        SwipeMenuListView userMonitoredBySwipeList = (SwipeMenuListView) findViewById(R.id.userParentList);

        for(User parentOfKid: theReturnedList){
            String userInfo = getString(R.string.mykids_user_name) + " " + parentOfKid.getName() + "\n" +
                    getString(R.string.mykids_user_email) + " " + parentOfKid.getEmail();

            kidsParentsListInfo.add(userInfo);
            ArrayAdapter adapter = new ArrayAdapter(ViewUserMonitoredByActivity.this, R.layout.swipe_listview, kidsParentsListInfo);
            userMonitoredBySwipeList.setAdapter(adapter);

            SwipeMenuCreator creator = new SwipeMenuCreator() {
                @Override
                public void create(SwipeMenu menu) {
                    // create "open" item
                    SwipeMenuItem viewParentInfoItem = new SwipeMenuItem(getApplicationContext());
                    // set item background
                    viewParentInfoItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9, 0xCE)));
                    // set item width
                    viewParentInfoItem.setWidth(180);
                    // set item title
                    viewParentInfoItem.setTitle(getString(R.string.view_parent_info));
                    // set item title fontsize
                    viewParentInfoItem.setTitleSize(18);
                    // set item title font color
                    viewParentInfoItem.setTitleColor(Color.WHITE);
                    // add to menu
                    menu.addMenuItem(viewParentInfoItem);
                }
            };

           userMonitoredBySwipeList.setMenuCreator(creator);
           userMonitoredBySwipeList.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
               @Override
               public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                   switch(index){
                       case 0:
                           Intent intentToViewInfo = ViewUserSettingsActivity.makeIntent(ViewUserMonitoredByActivity.this,theReturnedList.get(position).getId());
                           startActivity(intentToViewInfo);
                   }
                   return false;
               }
           });
        }
    }

    public static Intent makeIntent(Context context, Long userIdToPass) {
        Intent intent = new Intent(context, ViewUserMonitoredByActivity.class);
        intent.putExtra("U",userIdToPass);
        return intent;
    }
}
