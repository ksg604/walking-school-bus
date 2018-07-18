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

  //  private User tempUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);

        session = Session.getInstance();
        user = session.getUser();

        // Build the server proxy
        proxy = ProxyBuilder.getProxy(getString(R.string.api_key),session.getToken());

        message = new Message();


        //message.setIsRead()
        // Make call
        extractDataFromIntent();

        setupSendButton();
        setupGetButtonForTest();


    }

    private void setupGetButtonForTest() {
        Button btn = (Button) findViewById(R.id.btnGetMessage);
       // String temp;
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Call<List<Message>> caller = proxy.getMessageForUser( user.getId());
                ProxyBuilder.callProxy(SendMessageActivity.this, caller,
                        returnedMessageList -> responseMessage(returnedMessageList));

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
                message.setEmergency(false);
                message.setIsRead(false);
                message.setFromUser(user);

                Log.i("Check Message Here",message.getText());
                Log.i("Check groupID here", tempGroupID.toString());
                Call<List<Message>> caller = proxy.sendMessageToGroup( tempGroupID , message);
                ProxyBuilder.callProxy(SendMessageActivity.this, caller,
                        returnedNothing -> responseForSend(returnedNothing));

                Call<List<User>> callGroupMember = proxy.getGroupMembers(tempGroupID);
                ProxyBuilder.callProxy(SendMessageActivity.this, callGroupMember,
                        returnedGroupMember -> responseGetGroupMember(returnedGroupMember));





                //TODO:
                // Need to get group members' user ID
                // Then sendMessageToParents



            }
        });


    /*
        Call<Message> caller2 = proxy.getAllMessages();
        ProxyBuilder.callProxy(SendMessageActivity.this, caller2,
                returnedNothing -> responseForRecieive(returnedNothing));
                */
    }

    private void responseGetGroupMember(List<User> returnedGroupMember) {
        for(User userss : returnedGroupMember){
            Log.i("SHow infomation", userss.getId().toString());
            Call<List<Message>> callGroupMemberParents = proxy.sendMessageToParents( userss.getId() , message);
            ProxyBuilder.callProxy(SendMessageActivity.this, callGroupMemberParents,
                    returnedNothing -> responseFinish(returnedNothing));
        }
    }

    private void responseFinish(List<Message> returnedNothing) {
        Log.i("This part is good","Good!");
        notifyUserViaLogAndToast("Message sent.");
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
                    R.layout.da_items, messagesList );
            returnedMessageListView.setAdapter( adapterMember );

          //  returnedMessageListView.


        }

    }

    private void responseMessageSender(User returnedUser) {
         senderEmail.add(returnedUser.getEmail());
    }

    private void responseForSend(List<Message> returnedNothing) {
        notifyUserViaLogAndToast("Message sent!");
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
