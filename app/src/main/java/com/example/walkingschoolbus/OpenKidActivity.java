package com.example.walkingschoolbus;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.walkingschoolbus.model.Group;
import com.example.walkingschoolbus.model.Session;
import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

public class OpenKidActivity extends AppCompatActivity {

    private static final String TAG = "OpenKidActivity";
    private Session session;
    private User user;
    private User kidUser;
    private static WGServerProxy proxy;
    private ArrayList<String> kidsGroupList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_kid);

        session = Session.getInstance();
        user = User.getInstance();
        String savedToken = session.getToken();
        setupUpdateKidBtn();
        setupAddToGroupBtn();


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


        SwipeMenuListView groupList = (SwipeMenuListView) findViewById(R.id.kidGroupList);
        List<Group> kidMemberOfGroupList = kid.getMemberOfGroups();

        for(Group group : kidMemberOfGroupList){
            String groupInfo = getString(R.string.open_kid_group_name) + " " + group.getGroupDescription();

            kidsGroupList.add(groupInfo);
            ArrayAdapter adapter = new ArrayAdapter(OpenKidActivity.this, R.layout.da_items, kidsGroupList);
            groupList.setAdapter(adapter);

            SwipeMenuCreator creator = new SwipeMenuCreator() {
                @Override
                public void create(SwipeMenu menu) {
                    SwipeMenuItem removeItem = new SwipeMenuItem(getApplicationContext());
                    // set item background
                    removeItem.setBackground(new ColorDrawable(Color.rgb(220, 20,
                            60)));

                    // set item width
                    removeItem.setWidth(180);
                    // set item title
                    removeItem.setTitle(getString(R.string.mykids_remove_swipe));
                    // set item title fontsize
                    removeItem.setTitleSize(18);
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
                            Call<Void> caller = proxy.removeGroupMember(group.getId(),kid.getId());
                            ProxyBuilder.callProxy(OpenKidActivity.this, caller, returnedNothing -> response(returnedNothing));
                            groupList.removeViewsInLayout(position, 1);
                    }
                    return false;
                }
            });
        }


        //kid.getMemberOfGroups()
    }
    private void response(Void returnedNothing) {
        //notifyUserViaLogAndToast(MyKidsActivity.this.getString(R.string.mykids_notify_not_kid));
    }

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

    }

    private void setupAddToGroupBtn(){

    }
}
