/**
 * User settings activity allows user to change add/remove users they monitor from groups
 */
package com.example.walkingschoolbus;

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

import com.example.walkingschoolbus.model.Session;
import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;

import java.util.List;

import retrofit2.Call;


public class AddNewParentsActivity extends AppCompatActivity {

    private WGServerProxy proxy;
    private static final String TAG = "UserSetting";
    private String userEmail;
    private User user;
    private Session session;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_parents );

        session = Session.getInstance();
        user= session.getUser();

        // Build the server proxy
        proxy = ProxyBuilder.getProxy(getString(R.string.api_key),session.getToken());

        setupAddToMonitoredButton();
    }



    private void setupAddToMonitoredButton() {

        Button button = (Button) findViewById(R.id.btnAddNewParent);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText user_Email = (EditText) findViewById(R.id.editTxtEmailAddress);
                userEmail = user_Email.getText().toString();
                Call<User> callerForGettingUser = proxy.getUserByEmail(userEmail);
                Log.w("Test the pushed email:", userEmail);
                ProxyBuilder.callProxy(AddNewParentsActivity.this, callerForGettingUser,
                        returnedParent -> responseForParents(returnedParent));

            }
        });

    }


    private User responseForParents(User returnedUsers) {
        notifyUserViaLogAndToast("Server replied with user: " + returnedUsers.getEmail());
        Call<List<User>> caller = proxy.addToMonitoredByUsers(user.getId(), returnedUsers);
        ProxyBuilder.callProxy(AddNewParentsActivity.this, caller, returnedUser -> response(returnedUser));

        return returnedUsers;
    }

    private void response(List<User> returnedUser) {
        //notifyUserViaLogAndToast(getString(R.string.notify_monitoring_user_added));
    }

    private void notifyUserViaLogAndToast(String message) {
        Log.w(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public static Intent makeIntent(Context context){
        Intent intent = new Intent(context, AddNewParentsActivity.class);
        return intent;
    }
}