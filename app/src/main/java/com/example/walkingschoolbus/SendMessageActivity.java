/**
 * Activity allows user to send a non-emergency message
 */

package com.example.walkingschoolbus;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.walkingschoolbus.model.Message;
import com.example.walkingschoolbus.model.Session;
import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Path;

public class SendMessageActivity extends AppCompatActivity {

    private User user;
    private static WGServerProxy proxy;
    private Session session;
    private Message message;
    private List<Message> MessageListFromServer = new ArrayList< >( );
    private List<String> messagesList = new ArrayList< >( );
    private List<User> groupMemberList = new ArrayList<>();
    private Long tempGroupID;
    private List<String> senderEmail = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);
        session = Session.getInstance();
        user = session.getUser();
        // Build the server proxy
        proxy = ProxyBuilder.getProxy(getString(R.string.api_key),session.getToken());
        message = new Message();

        extractDataFromIntent();
        setupSendNormalButton();
        setupSendButton();
        setHelpText();
    }

    private void setupSendNormalButton() {
        Button sendButton = (Button) findViewById(R.id.btnsendRegularMessage);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText text = (EditText) findViewById(R.id.textMessage);
                String messageToSend = text.getText().toString();
                message.setText(messageToSend);
                message.setEmergency(false);
                message.setIsRead(false);
                message.setFromUser(user);

                Log.i("Check Message Here",message.getText());
                Log.i("Check groupID here", tempGroupID.toString());
                Call<List<Message>> caller = proxy.sendMessageToGroup( tempGroupID , message);
                ProxyBuilder.callProxy(SendMessageActivity.this, caller,
                        returnedNothing -> responseForSend(returnedNothing));
            }
        });
    }

    private void setupSendButton() {
        Button sendButton = (Button) findViewById(R.id.btnsendMessage);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText text = (EditText) findViewById(R.id.textMessage);
                String messageToSend = text.getText().toString();
                message.setText(messageToSend);
                message.setEmergency(true);
                message.setIsRead(false);
                message.setFromUser(user);

                Log.i("Check Message Here",message.getText());
                Log.i("Check groupID here", tempGroupID.toString());
                Call<List<Message>> caller = proxy.sendMessageToGroup( tempGroupID , message);
                ProxyBuilder.callProxy(SendMessageActivity.this, caller,
                        returnedNothing -> responseForSend(returnedNothing));
            }
        });
    }

    private void responseForSend(List<Message> returnedNothing) {
        notifyUserViaLogAndToast("Message sent!");
        Call<List<User>> callGroupMember = proxy.getGroupMembers(tempGroupID);
        ProxyBuilder.callProxy(SendMessageActivity.this, callGroupMember,
                returnedGroupMember -> responseGetGroupMember(returnedGroupMember));
    }

    private void responseGetGroupMember(List<User> returnedGroupMember) {
        for(User userss : returnedGroupMember){
            if(userss.getId() != user.getId()) {
                Log.i("SHow infomation", userss.getId().toString());
                Call<List<Message>> callGroupMemberParents = proxy.sendMessageToParents(userss.getId(), message);
                ProxyBuilder.callProxy(SendMessageActivity.this, callGroupMemberParents,
                        returnedNothing -> responseFinish(returnedNothing));
            }
        }
    }

    private void responseFinish(List<Message> returnedNothing) {
        Log.i("This part is good","Good!");
        notifyUserViaLogAndToast("Message sent.");
        finish();
    }

    private void responseMessage(List<Message> returnedMessageList) {
        SwipeMenuListView returnedMessageListView = (SwipeMenuListView) findViewById( R.id.messagesGot);
        for (Message message : returnedMessageList) {
            if(!(messagesList.contains(message.getText()))){
                String messages = message.getText();
                //get user by tempUserID
                Call<User> caller = proxy.getUserById( message.getFromUser().getId());

                ProxyBuilder.callProxy(SendMessageActivity.this, caller,
                        returnedUser -> responseMessageSender(returnedUser));

                messagesList.add(messages);
            }

            ArrayAdapter adapterMember = new ArrayAdapter( SendMessageActivity.this,
                    R.layout.swipe_listview, messagesList );
            returnedMessageListView.setAdapter( adapterMember );
        }
    }

    private void responseMessageSender(User returnedUser) {
         senderEmail.add(returnedUser.getEmail());
    }

    private void setHelpText() {
        TextView helpText = (TextView) findViewById( R.id.helpTextInSendMessages );
        helpText.setText(getString( R.string.broadcast_instructions ));
    }


    private void notifyUserViaLogAndToast(String message) {
       // Log.w(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void extractDataFromIntent() {
        Intent neededIntent = getIntent();
        long tempid = neededIntent.getLongExtra("ID", 0);
        tempGroupID = Long.valueOf(tempid);
    }

    public static Intent makeIntent(Context context, Long userIdToPass){
        Intent intent = new Intent(context, SendMessageActivity.class);
        intent.putExtra("ID", userIdToPass);
        return intent;
    }
}
