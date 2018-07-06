package com.example.walkingschoolbus;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.walkingschoolbus.model.Group;
import com.example.walkingschoolbus.model.Session;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;
import com.google.android.gms.maps.GoogleMap;

import retrofit2.Call;

public class OnMarkerClickActivity extends AppCompatActivity {

    WGServerProxy proxy;
    private Session token = Session.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_marker_click);
        String tokenValue = token.getToken();
        proxy = ProxyBuilder.getProxy(getString(R.string.api_key),tokenValue);
        getGroupDetails();

        TextView prompt = findViewById(R.id.markerClickPrompt);
        prompt.setText(R.string.PROMPT);

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
        Log.i("debugTag2",""+returnedGroup.getGroupDescription());
        TextView groupName = findViewById(R.id.groupNameMarkerClick);
        TextView groupLeader = findViewById(R.id.groupLeaderMarkerClick);
        groupName.setText(R.string.groupName+ returnedGroup.getGroupDescription());
        groupLeader.setText(R.string.groupLeader+ returnedGroup.getLeader().getName());
    }

    private void setupYesBtn(){
        OnMarkerClickActivity.this.finish();
    }
    private void setupNoBtn(){
        OnMarkerClickActivity.this.finish();
    }

    public static Intent makeIntent(Context context){
        Intent intent = new Intent( context, OnMarkerClickActivity.class );
        return intent;
    }
}
