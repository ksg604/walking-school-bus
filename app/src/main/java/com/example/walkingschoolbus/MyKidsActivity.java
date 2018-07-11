package com.example.walkingschoolbus;

import android.content.Intent;
import android.graphics.Color;
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
import com.example.walkingschoolbus.model.Session;
import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

public class MyKidsActivity extends AppCompatActivity {

    private static final String TAG = "MyKidsActivity";
    private Session session;
    private User user;
    private static WGServerProxy proxy;
    private ArrayList<String> myKidsList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_kids);

        session = Session.getInstance();
        user = User.getInstance();
        String savedToken = session.getToken();
        setupMyKidsTextView();
        setupAddNewKidBtn();
        proxy = ProxyBuilder.getProxy(getString(R.string.api_key), session.getToken());

        Call<List<User>> caller = proxy.getMonitorsUsers(user.getId());
        ProxyBuilder.callProxy(MyKidsActivity.this, caller, returnedKids -> response(returnedKids));
    }

    private void response(List<User> returnedKids) {

        SwipeMenuListView kidsList = (SwipeMenuListView) findViewById(R.id.myKidsList);

        for (User user : returnedKids) {
            Log.w(TAG, "    User: " + user.toString());
            String userInfo = getString(R.string.mykids_user_name) + " " + user.getName() + "\n" +
                    getString(R.string.mykids_user_email) + " " + user.getEmail();

            myKidsList.add(userInfo);
            ArrayAdapter adapter = new ArrayAdapter(MyKidsActivity.this, R.layout.da_items, myKidsList);
            kidsList.setAdapter(adapter);

            SwipeMenuCreator creator = new SwipeMenuCreator() {
                @Override
                public void create(SwipeMenu menu) {
                    // create "open" item
                    SwipeMenuItem openItem = new SwipeMenuItem(getApplicationContext());
                    // set item background
                    openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9, 0xCE)));
                    // set item width
                    openItem.setWidth(180);
                    // set item title
                    openItem.setTitle(getString(R.string.mykids_open_swipe));
                    // set item title fontsize
                    openItem.setTitleSize(18);
                    // set item title font color
                    openItem.setTitleColor(Color.WHITE);
                    // add to menu
                    menu.addMenuItem(openItem);


                    // create "delete" item
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
            kidsList.setMenuCreator(creator);

            kidsList.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                    switch (index) {
                        case 0:
                            Intent intentForOpen = OpenKidActivity.makeIntent(MyKidsActivity.this,
                                    returnedKids.get(position).getEmail());
                            startActivity(intentForOpen);
                        case 1:
                            Call<Void> caller = proxy.removeFromMonitorsUsers(user.getId(), returnedKids.get(position).getId());
                            ProxyBuilder.callProxy(MyKidsActivity.this, caller, returnedNothing -> response(returnedNothing));
                            kidsList.removeViewsInLayout(position, 1);
                    }
                    return false;
                }
            });
        }
    }

    private void response(Void returnedNothing) {
        notifyUserViaLogAndToast(MyKidsActivity.this.getString(R.string.mykids_notify_not_kid));
    }

    private void setupAddNewKidBtn(){
        Button addNewKidBtn = findViewById(R.id.addNewBtn);
        addNewKidBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentForAddNewKids = AddNewKidActivity.makeIntent(MyKidsActivity.this);
                startActivity(intentForAddNewKids);

            }
        });
    }

    private void notifyUserViaLogAndToast(String message) {
        Log.w(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void setupMyKidsTextView() {
        TextView myKidsList = (TextView) findViewById( R.id.myKidsTxt );
        String kids = getString(R.string.my_kids_txt);
        myKidsList.setText( kids );

    }

}

