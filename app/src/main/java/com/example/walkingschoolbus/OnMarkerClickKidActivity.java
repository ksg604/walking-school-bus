/**
 * Activity launches from map activity. Allows user to have their kid join a walking group that was
 * clicked on in the map
 */
package com.example.walkingschoolbus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.walkingschoolbus.model.Group;
import com.example.walkingschoolbus.model.Session;
import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

public class OnMarkerClickKidActivity extends AppCompatActivity {

    WGServerProxy proxy;
    private Session token = Session.getInstance();
    User user = token.getUser();
    private User kidUser;
    Group group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_marker_click_kid);

        String tokenValue = token.getToken();
        proxy = ProxyBuilder.getProxy(getString(R.string.api_key),tokenValue);
        getGroupDetails();
        getKidDetails();
    }

    private void getKidDetails(){
        Intent intent = getIntent();
        String kidEmail = intent.getExtras().getString("theKidEmail");

        Call<User> kidCaller = proxy.getUserByEmail(kidEmail);
        ProxyBuilder.callProxy(OnMarkerClickKidActivity.this, kidCaller, returnedKid -> responseKid(returnedKid));
    }
    private void responseKid(User theReturnedKid){
        kidUser = theReturnedKid;
        if(kidUser != null && kidUser.getEmail() != null){
            Log.i("Debug43","Successfully retrieved kidUser");
        }else{
            Log.i("Debug43","kidUser was null");
        }

        TextView prompt = findViewById(R.id.markerClickPromptKid);
        prompt.setText("Would you like your kid, "+kidUser.getName()+", to join this group?");
        setupNoBtn();
        setupYesBtn();
    }

    private void getGroupDetails(){
        Intent intent = getIntent();
        Long groupId = intent.getExtras().getLong("id");
        Call<Group> groupCaller = proxy.getGroupById(groupId);
        ProxyBuilder.callProxy(OnMarkerClickKidActivity.this, groupCaller, group -> response(group));
    }
    private void response(Group returnedGroup){
        group = returnedGroup;
        Log.i("debugTag2",""+returnedGroup.getGroupDescription());
        TextView groupName = findViewById(R.id.groupNameMarkerClickKid);
        groupName.setText(getString(R.string.group_name)+ " "+returnedGroup.getGroupDescription());
        TextView groupId = findViewById(R.id.groupIdMarkerClickKid);
        groupId.setText(getString(R.string.group_id_kid)+ " "+returnedGroup.getId());
    }

    private void setupYesBtn(){
        Button btn = findViewById(R.id.btnYesKid);
        btn.setText(R.string.yes);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addKidToGroup();
            }
        });
    }
    private void addKidToGroup(){
        Toast.makeText(OnMarkerClickKidActivity.this,"Your kid is now a member of the group, "+group.getGroupDescription()+".",Toast.LENGTH_LONG)
                .show();
        Call<List<User>> caller = proxy.addGroupMember(group.getId(),kidUser);
        ProxyBuilder.callProxy(OnMarkerClickKidActivity.this, caller, groupUsers -> response25(groupUsers));
    }

    private void response25(List<User> groupUsers){
        Log.i("TAG50",""+groupUsers);
        Intent intent = OpenKidActivity.makeIntent(OnMarkerClickKidActivity.this);
        intent.putExtra("result",RESULT_OK);
        setResult(Activity.RESULT_OK,intent);
        finish();

    }

    private void setupNoBtn(){
        Button btn = findViewById(R.id.btnNoKid);
        btn.setText(R.string.no);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = OpenKidActivity.makeIntent(OnMarkerClickKidActivity.this);
                intent.putExtra("result",RESULT_OK);
                setResult(Activity.RESULT_OK,intent);
                finish();
            }
        });
    }

    public static Intent makeIntent(Context context){
        Intent intent = new Intent(context, OnMarkerClickKidActivity.class);
        return intent;
    }
}
