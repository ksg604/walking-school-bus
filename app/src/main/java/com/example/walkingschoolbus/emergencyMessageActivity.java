/**
 * This activity allows a user the send an "emergency message" to their parents and group
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

import com.example.walkingschoolbus.model.Group;
import com.example.walkingschoolbus.model.Message;
import com.example.walkingschoolbus.model.Session;
import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

public class emergencyMessageActivity extends AppCompatActivity {

    private User user;
    private static WGServerProxy proxy;
    private Session session;
    private Message message;
    private String successMessage;
    private List<Group> groupUserAsMemberList = new ArrayList<>();
    private List<Group> groupObjectList = new ArrayList<>();
    private List<Long> groupIdUserAsMemberList = new ArrayList<>();
    boolean emergencyFlag;

    private List<String> senderEmail = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        session = Session.getInstance();
        user = session.getUser();
        message = new Message();
        proxy = ProxyBuilder.getProxy(getString(R.string.api_key),session.getToken());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_message);
        TextView instructions = findViewById(R.id.txtViewEMessageInstruct);
        emergencyFlag = extractEmergencyFlag();
        if(emergencyFlag){
            instructions.setText(R.string.emergency_instruction);
        }else{
            instructions.setText(R.string.non_emergency_instructions);
        }
        setupSendButton();
    }

    private boolean extractEmergencyFlag() {
        Intent intent = getIntent();
        return intent.getBooleanExtra("ifEmergency", false);

    }

    private void setupSendButton() {
        Button sendButton = (Button) findViewById(R.id.btnSendEmergencyMessage);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(emergencyFlag == true) {
                    EditText text = (EditText) findViewById(R.id.emergencyText);
                    String messageToSend = text.getText().toString();
                    message.setText("|EMERGENCY|"+ messageToSend);
                    message.setEmergency(true);
                    message.setIsRead(false);
                    message.setFromUser(user);


                    Call<List<Message>> callGroupMemberParents = proxy.sendMessageToParents(user.getId(), message);
                    ProxyBuilder.callProxy(emergencyMessageActivity.this, callGroupMemberParents,
                            returnedNothing -> responseEmergency(returnedNothing));
                }
                if(emergencyFlag == false) {
                    EditText text = (EditText) findViewById(R.id.emergencyText);
                    String messageToSend = text.getText().toString();
                    message.setText(messageToSend);
                    message.setEmergency(false);
                    message.setIsRead(false);
                    message.setFromUser(user);


                    Call<List<Message>> callGroupMemberParents = proxy.sendMessageToParents(user.getId(), message);
                    ProxyBuilder.callProxy(emergencyMessageActivity.this, callGroupMemberParents,
                            returnedNothing -> responseEmergency(returnedNothing));
                }
            }
        });
    }

    private void responseEmergency(List<Message> returnedNothing) {
        notifyUserViaLogAndToast("message sent.");
        finish();
    }

    private void notifyUserViaLogAndToast(String message) {
        // Log.w(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public static Intent makeIntent(Context context,boolean ifEmergency){
        Intent intent = new Intent( context, emergencyMessageActivity.class );
        intent.putExtra("ifEmergency", ifEmergency);
        return intent;
    }
}
