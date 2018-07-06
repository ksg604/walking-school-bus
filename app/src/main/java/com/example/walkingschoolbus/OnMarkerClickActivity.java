package com.example.walkingschoolbus;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.walkingschoolbus.model.Group;
import com.example.walkingschoolbus.model.Session;
import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;
import com.google.android.gms.maps.GoogleMap;

import java.util.List;

import retrofit2.Call;

public class OnMarkerClickActivity extends AppCompatActivity {

    WGServerProxy proxy;
    private Session token = Session.getInstance();
    User user = User.getInstance();
    Group group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_marker_click);
        String tokenValue = token.getToken();
        proxy = ProxyBuilder.getProxy(getString(R.string.api_key),tokenValue);
        getGroupDetails();

        TextView prompt = findViewById(R.id.markerClickPrompt);
        prompt.setText(R.string.prompt);

        setupNoBtn();
        setupYesBtn();
    }

    private void getGroupDetails(){
        Intent intent = getIntent();
        Long groupId = intent.getExtras().getLong("id");

        Call<Group> groupCaller = proxy.getGroupById(groupId);
        ProxyBuilder.callProxy(OnMarkerClickActivity.this, groupCaller, group -> response(group));
    }
    private void response(Group returnedGroup){
        group = returnedGroup;
        Log.i("debugTag2",""+returnedGroup.getGroupDescription());
        TextView groupName = findViewById(R.id.groupNameMarkerClick);
        TextView groupLeader = findViewById(R.id.groupLeaderMarkerClick);
        groupName.setText(getString(R.string.group_name)+ " "+returnedGroup.getGroupDescription());
        groupLeader.setText(getString(R.string.group_leader)+ " "+returnedGroup.getLeader().getName());
    }

    private void setupYesBtn(){
        Button btn = findViewById(R.id.btnYes);
        btn.setText(R.string.yes);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Call<List<User>> caller = proxy.addGroupMember(group.getId(),user);
                ProxyBuilder.callProxy(OnMarkerClickActivity.this, caller, groupUsers -> response(groupUsers));
                OnMarkerClickActivity.this.finish();
            }
        });
    }
    private void response(List<User> groupUsers){
        Log.i("TAG50",""+groupUsers);
    }
    private void setupNoBtn(){
        Button btn = findViewById(R.id.btnNo);
        btn.setText(R.string.no);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnMarkerClickActivity.this.finish();
            }
        });
    }

    public static Intent makeIntent(Context context){
        Intent intent = new Intent( context, OnMarkerClickActivity.class );
        return intent;
    }
}
