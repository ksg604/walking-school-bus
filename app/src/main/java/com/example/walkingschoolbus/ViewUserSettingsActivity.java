/*
This class controls an activity in which users can view their, or their childs, current account info
and launch an activity to update this information.
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

import com.example.walkingschoolbus.model.Session;
import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;

import java.util.List;

import retrofit2.Call;

public class ViewUserSettingsActivity extends AppCompatActivity {

    private static final String TAG = "ViewUserSettings";
    private Session session = Session.getInstance();
    private String userToken = session.getToken();
    private static WGServerProxy proxy;
    private TextView thisName;
    private TextView thisYOB;
    private TextView thisMOB;
    private TextView thisHPhone;
    private TextView thisCPhone;
    private TextView thisEmail;
    private TextView thisGrade;
    private TextView thisAddress;
    private TextView thisICE;
    private long thisUserID;
    private Boolean editable = false;
    private static final int REQUEST_CODE = 12345;
    private User sessionUser = session.getUser();
    private List<User> children = sessionUser.getMonitorsUsers();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user_settings);

        //setup textviews
        thisName = findViewById(R.id.txtViewUserThisName);
        thisHPhone = findViewById(R.id.txtViewUserThisHPhone);
        thisCPhone = findViewById(R.id.txtViewUserThisCPhone);
        thisEmail = findViewById(R.id.txtViewUserThisEmail);
        thisGrade = findViewById(R.id.txtViewUserThisGrade);
        thisAddress = findViewById(R.id.txtViewUserThisAddress);
        thisICE = findViewById(R.id.txtViewUserThisICE);
        thisYOB = findViewById(R.id.txtViewUserThisYOB);
        thisMOB = findViewById(R.id.txtViewUserThisMOB);

        //create proxy for server communication
        proxy = ProxyBuilder.getProxy(getString(R.string.api_key),userToken);
        //get userID from intent
        Intent intent = getIntent();
        thisUserID = intent.getLongExtra("ID",0);
        //Create proxy call to generate full user object from ID above
        Call<User> caller = proxy.getUserById(thisUserID);
        ProxyBuilder.callProxy(ViewUserSettingsActivity.this,caller,
                returnedUser ->responseForUser(returnedUser));
        setupMonitorsButton();
    }

    private void responseForUser(User user){
        thisName.setText(user.getName());
        thisHPhone.setText(user.getHomePhone());
        thisCPhone.setText(user.getCellPhone());
        thisEmail.setText(user.getEmail());
        thisGrade.setText(user.getTeacherName());
        thisAddress.setText(user.getAddress());
        thisICE.setText(user.getEmergencyContactInfo());

        //grab none-null DOB info and print
        try {
            if(!String.valueOf(user.getBirthYear()).equals("null")) {
                thisYOB.setText(String.valueOf(user.getBirthYear()));
            } else{
                thisYOB.setText("");
            }
        } catch (NullPointerException e){
            Log.e(TAG, "exception: ", e);
            thisYOB.setText("");
        }
        String monthText ="";
        int monthOfBirth = 0;

        try {
            monthOfBirth = user.getBirthMonth();
        } catch(NullPointerException e){
            Log.e(TAG,"exception: ", e);
        }
        switch(monthOfBirth){
            case 1:
                monthText = "Jan";          break;
            case 2:
                monthText = "Feb" ;         break;
            case 3:
                monthText = "Mar";          break;
            case 4:
                monthText = "Apr";          break;
            case 5:
                monthText = "May";          break;
            case 6:
                monthText = "Jun";          break;
            case 7:
                monthText = "Jul";          break;
            case 8:
                monthText = "Aug";          break;
            case 9:
                monthText = "Sep";          break;
            case 10:
                monthText = "Oct";          break;
            case 11:
                monthText = "Nov";          break;
            case 12:
                monthText = "Dec";          break;
        }
        thisMOB.setText(monthText);

        setEditable();
    }

    private void setEditable() {
        try {
            int length = children.size();
            for (int i = 0; i < length; i++) {
                if (children.get(i).getId() == thisUserID) {
                    editable = true;
                    Log.i(TAG, "editable true, child");
                }
            }
        }catch(NullPointerException e){
            e.printStackTrace();
            }

        if(session.getid() == thisUserID){
            editable = true;
            Log.i(TAG, "editable true, me" );
        }
        setupUpdateButton();
    }

    private void setupUpdateButton() {
        Button btn = findViewById(R.id.btnViewUserUpdate);

        if(editable) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = EditUserSettingsActivity.makeIntent(ViewUserSettingsActivity.this, thisUserID);
                    startActivityForResult(intent, REQUEST_CODE);
                }
            });
        }else{
            btn.setVisibility(View.INVISIBLE);
        }
    }

    private void setupMonitorsButton() {
        Button btn = findViewById(R.id.btnUserMonitors);
        btn.setOnClickListener(new View.OnClickListener(){
         @Override
         public void onClick(View v) {
                Intent intent = ViewUserMonitoredByActivity.makeIntent(
                    ViewUserSettingsActivity.this,thisUserID);
                startActivity(intent);
            }
        });
    }

    public static Intent makeIntent(Context context, long userID){
        Log.i(TAG,"makeIntent");
        Intent intent = new Intent(context, ViewUserSettingsActivity.class);
        intent.putExtra("ID",userID);
        return intent;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        switch(requestCode){
            case REQUEST_CODE:
            if(resultCode== Activity.RESULT_OK){
                finish();
                startActivity(getIntent());
            }
        }
    }
}
