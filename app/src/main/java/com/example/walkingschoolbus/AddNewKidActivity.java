/**
 * This activity allows the user to monitor a new child by entering their email address
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.walkingschoolbus.model.PermissionRequest;
import com.example.walkingschoolbus.model.Session;
import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

public class AddNewKidActivity extends AppCompatActivity {
    private static final String TAG = "AddNewKidActivity";
    private Session session;
    private User parent;
    private User kid;
    WGServerProxy proxy;
    private List<User> parentKidsList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_kid);

        session = Session.getInstance();
        parent = session.getUser();
        proxy = ProxyBuilder.getProxy(getString(R.string.api_key), session.getToken(),true);
        setupTextViews();
        setupConfirmBtn();

    }

    public static Intent makeIntent(Context context){
        Intent intent = new Intent(context, AddNewKidActivity.class);
        return intent;
    }

    private void setupTextViews(){
        TextView title = findViewById(R.id.newKidTitle);
        title.setText(getString(R.string.new_kid_title));

        TextView kidEmail = findViewById(R.id.kidEmailTxt);
        kidEmail.setText(getString(R.string.kids_email));
    }

    private void registerKid(){

        //Get list of kids that parent (user) monitors.
        Log.i("debug1","Parent in addnewkidactivity ID: "+parent.getId());
        Call<List<User>> caller = proxy.getMonitorsUsers(parent.getId());
        ProxyBuilder.callProxy(AddNewKidActivity.this, caller, returnedKids -> response0(returnedKids));
    }

    /**
     * Parent calls the server via registering their kid's email to return all information about their kid.
     */
    private void response0(List<User> parentMonitorsList){

        parentKidsList = parentMonitorsList;
        parent.setMonitorsUsers(parentKidsList);
        Log.i("debug2","Initial parent monitors list in addnewkidactivity "+parentKidsList);

        //Parent inputs kid-to-monitor's email.
        EditText input = findViewById(R.id.kidsEmailEdit);
        String kid_email_input = input.getText().toString();

        //Call to server to return all kid object information via their email.
        Call<User> kidCaller = proxy.getUserByEmail(kid_email_input);
        ProxyBuilder.callProxy(AddNewKidActivity.this, kidCaller, returnedKid -> response1(returnedKid));
    }

    /**
     * This response will take the returned kid parameter and add that kid to the parent's monitored list.
     * @param returnedKid The kid to add to parent's monitors list returned from second call to the server
     */
    private void response1(User returnedKid){
        kid = returnedKid;
        Call<List<User>> caller = proxy.addToMonitorsUsers(parent.getId(),returnedKid);
        List<PermissionRequest> permissionList = new ArrayList<>();
        permissionList = returnedKid.getPendingPermissionRequests();
        for(PermissionRequest permissions : permissionList) {
//            Log.i("Show permission details", permissions.getAction());
        }
        ProxyBuilder.callProxy(AddNewKidActivity.this, caller, updatedMonitorsList -> response2(updatedMonitorsList));
    }

    private void response2(List<User> parentNewMonitorsList){
        Log.i("debug3","New parent monitors list in addnewkidactivity "+parentNewMonitorsList);
        Log.i(TAG,""+parent.getName()+",you are now monitoring "+kid.getName());
        Toast.makeText(AddNewKidActivity.this,getString(R.string.request_sent)+kid.getName()
        ,Toast.LENGTH_SHORT)
                .show();
        Intent intent = MyKidsActivity.makeIntent(AddNewKidActivity.this);
        intent.putExtra("result",RESULT_OK);
        setResult(Activity.RESULT_OK,intent);
        finish();
    }

    private void setupConfirmBtn(){
        Button confirmBtn = findViewById(R.id.confirmBtn);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerKid();
            }
        });
    }
}
