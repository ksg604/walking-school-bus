/**
 * Activity is the "open message" feature of the app
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

import com.example.walkingschoolbus.model.LocalMessageContainer;
import com.example.walkingschoolbus.model.Message;
import com.example.walkingschoolbus.model.Session;
import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;

import java.util.List;

import retrofit2.Call;

public class MessageDetailActivity extends AppCompatActivity {

    private Long messageId;
    private Long messageSenderID;
    private String senderName;

    private User user;
    private static WGServerProxy proxy;
    private Session session;
    private LocalMessageContainer messageContainer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_detail);
        messageContainer = LocalMessageContainer.getInstance();
        session = Session.getInstance();
        user = session.getUser();
        proxy = ProxyBuilder.getProxy(getString(R.string.api_key),session.getToken());

        extractDataFromIntent();
        getMessageDetails();
        notifyUserViaLogAndToast("Message has been read.");
    }

    private void getMessageDetails() {
       // EditText editText = (EditText) findViewById(R.id.messageDetailTextview);
        TextView editText = findViewById(R.id.multiAutoCompleteTextView);
        Call<Message> callerForMessage = proxy.getMessageById(messageId);
        ProxyBuilder.callProxy(MessageDetailActivity.this, callerForMessage,
                new ProxyBuilder.SimpleCallback<Message>() {
                    @Override
                    public void callback(Message returnedMessage) {

                        Call<User> callerForGroup = proxy.getUserById(messageSenderID);
                        ProxyBuilder.callProxy(MessageDetailActivity.this, callerForGroup,
                                new ProxyBuilder.SimpleCallback<User>() {
                                    @Override
                                    public void callback(User user) {
                                        User messageSender = new User();
                                        messageSender = user;
                                        senderName = messageSender.getName();
                                        String messageContent = senderName + ":" + "\n" +
                                                returnedMessage.getText() + "\n" +
                                                "Emergency: " + returnedMessage.isEmergency() + "\n";
                                        editText.setText(messageContent);
                                        messageContainer.setMessageContent(messageContent);
                                    }
                                });
                    }
                });
    }

    private void extractDataFromIntent() {
        Intent neededIntent = getIntent();
        long tempMessageid = neededIntent.getLongExtra("messageID", 0);
        messageId = Long.valueOf(tempMessageid);
        long tempSenderId = neededIntent.getLongExtra("senderName",0);
        messageSenderID = Long.valueOf(tempSenderId);
    }

    public static Intent makeIntent(Context context, Long messageIdToPass, Long senderIdToPass){
        Intent intent = new Intent(context, MessageDetailActivity.class);
        intent.putExtra("messageID", messageIdToPass);
        intent.putExtra("senderName", senderIdToPass);
        return intent;
    }

    private void notifyUserViaLogAndToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}