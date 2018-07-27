//Activity to list all children you monitor
package com.example.walkingschoolbus;

import android.app.Activity;
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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.walkingschoolbus.model.Message;
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
    static WGServerProxy  proxy;
    private String numberOfMessages;
    private List<String> myKidsList = new ArrayList<>();
    private static final int REQUEST_CODE = 2222;
    private String userToken;
    private long sessionID;
    private final static String unread = "unread";
    private Handler handler = new Handler();
    private Runnable runnableCode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_kids);
        session = Session.getInstance();
        userToken = session.getToken();
        sessionID = session.getid();
        user = session.getUser();
        setupMyKidsTextView();
        proxy = ProxyBuilder.getProxy(getString(R.string.api_key), session.getToken());
        Call<List<User>> caller = proxy.getMonitorsUsers(sessionID);
        ProxyBuilder.callProxy(MyKidsActivity.this, caller, returnedKids -> response(returnedKids));
        setupAddNewKidBtn();
        makeHandlerRun();
        setupLinearLayoutMessages();

    }

    private void setupLinearLayoutMessages() {
        LinearLayout messages = findViewById( R.id.linearLayoutMessagesInMyKids );
        messages.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = MessageActivity.makeIntent(MyKidsActivity.this);
                startActivity(intent);
            }
        } );
    }

    private void getNumberOfMessagesFromSession(){
        numberOfMessages = session.getNumberOfMessages();
        TextView messages = (TextView) findViewById( R.id.messagesInMyKids );
        messages.setText( numberOfMessages);
    }

    private void makeHandlerRun() {
        runnableCode = new Runnable() {
            public void run() {
                getNumberOfMessagesFromSession();
                handler.postDelayed( this, 60000 );
            }
        };
        handler.post( runnableCode );
    }

    private void response(List<User> returnedKids) {

        SwipeMenuListView kidsList = (SwipeMenuListView) findViewById(R.id.myKidsList);

        for (User kid : returnedKids) {
            Log.w(TAG, "    User: " + kid.toString());
            String userInfo = getString(R.string.mykids_user_name) + " " + kid.getName() + "\n" +
                    getString(R.string.mykids_user_email) + " " + kid.getEmail();

            myKidsList.add(userInfo);
            ArrayAdapter adapter = new ArrayAdapter(MyKidsActivity.this, R.layout.swipe_listview, myKidsList);
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
                    openItem.setTitleSize(12);
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
                    removeItem.setTitleSize(12);
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
                            break;
                        case 1:
                            Call<Void> caller = proxy.removeFromMonitorsUsers(sessionID, returnedKids.get(position).getId());
                            ProxyBuilder.callProxy(MyKidsActivity.this, caller, returnedNothing -> removeResponse(returnedNothing));
                            kidsList.removeViewsInLayout(position, 1);
                            break;
                    }
                    return false;
                }
            });
        }
        Log.i("response debug",""+returnedKids);
    }

    private void removeResponse(Void returnedNothing) {
        notifyUserViaLogAndToast(MyKidsActivity.this.getString(R.string.mykids_notify_not_kid));
    }

    private void setupAddNewKidBtn(){
        Button addNewKidBtn = findViewById(R.id.addNewBtn);
        addNewKidBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentForAddNewKids = AddNewKidActivity.makeIntent(MyKidsActivity.this);
                startActivityForResult(intentForAddNewKids, REQUEST_CODE);
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

    public static Intent makeIntent(Context context) {
        Intent intent = new Intent(context, MyKidsActivity.class);
        return intent;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.i(TAG,"request code: "+requestCode + "resultcode: "+resultCode);
        switch(requestCode){
            case REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    finish();
                    Log.i("tag50","entered");
                    startActivity(getIntent());
                }
                break;
        }
    }
}




