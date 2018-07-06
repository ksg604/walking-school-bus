package com.example.walkingschoolbus;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
public class MonitorActivity extends AppCompatActivity {

    private User user;
    private Session session;
    private static WGServerProxy proxy;
    private static String userEmail;
    private static final String TAG = "Monitor";


    private Long groupID;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);

        user = User.getInstance();
        session = Session.getInstance();

        // Build the server proxy
        proxy = ProxyBuilder.getProxy(getString(R.string.api_key),session.getToken());

        setupAddUserToGroups();

    }


    private void setupAddUserToGroups() {

        Button buttonAddUserToGroup = (Button) findViewById(R.id.btnAddUserToGroup);
        buttonAddUserToGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText groupDes = (EditText) findViewById(R.id.editTextGroupToJoin);

                //extractIntent();
                Intent neededIntent = getIntent();
                String tempString = neededIntent.getStringExtra("E");

                groupID = Long.valueOf(groupDes.getText().toString());
                Call<User> caller =proxy.getUserByEmail(tempString);

                ProxyBuilder.callProxy(MonitorActivity.this, caller,
                        returnedInputUser -> responseForAdd(returnedInputUser));
            }


        });

    }

    private void responseForAdd(User returnedInputUser) {


        Call<List<User>> caller = proxy.addGroupMember(groupID, returnedInputUser);
        ProxyBuilder.callProxy(MonitorActivity.this,
                        caller, returnedUser -> responseMessage(returnedUser));

    }


        private void notifyUserViaLogAndToast(String message) {
        Log.w(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }


    private void responseMessage(List<User> returnedUser) {
        notifyUserViaLogAndToast("User has been added to group.");
    }

    public static Intent makeIntentt(Context context, String userEmailToPass){
        Intent intent = new Intent(context,MonitorActivity.class);
        intent.putExtra("E",userEmailToPass);
        return intent;
    }


    public static Intent makeIntent(Context context){
        Intent intent = new Intent(context,MonitorActivity.class);
        return intent;
    }
}
