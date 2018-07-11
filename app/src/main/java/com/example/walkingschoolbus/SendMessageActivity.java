package com.example.walkingschoolbus;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.example.walkingschoolbus.model.Message;
import com.example.walkingschoolbus.model.Session;
import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;

import java.util.List;

import retrofit2.Call;

public class SendMessageActivity extends AppCompatActivity {

    private User user;
    private static WGServerProxy proxy;
    private Session session;
    private Message message;
    private Long tempID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);

        user = User.getInstance();
        session = Session.getInstance();
        // Build the server proxy
        proxy = ProxyBuilder.getProxy(getString(R.string.api_key),session.getToken());
        message = new Message();

        EditText text = (EditText) findViewById(R.id.textMessage);
        String messageToSend = text.toString();
        message.setText(messageToSend);
        //message.setIsRead()
        // Make call
        extractDataFromIntent();

        Call<Message> caller = proxy.sendMessageToParents( tempID, message);
        ProxyBuilder.callProxy(SendMessageActivity.this, caller,
                returnedNothing -> responseForSend(returnedNothing));

        Call<Message> caller2 = proxy.getAllMessages();
        ProxyBuilder.callProxy(SendMessageActivity.this, caller,
                returnedNothing -> responseForRecieive(returnedNothing));

    }

    private void responseForRecieive(Message returnedNothing) {
       // notifyUserViaLogAndToast(SendMessageActivity.this.getString(R.string.notify_delete));
        EditText text = (EditText) findViewById(R.id.getMessage);
        String temp = returnedNothing.getText();
        text.setText(temp);

    }

    private void responseForSend(Message returnedNothing) {
        notifyUserViaLogAndToast(SendMessageActivity.this.getString(R.string.notify_delete));
    }
    private void notifyUserViaLogAndToast(String message) {
       // Log.w(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void extractDataFromIntent() {
        Intent neededIntent = getIntent();
         long tempid = neededIntent.getLongExtra("ID", 0);
         tempID = Long.valueOf(tempid);
    }

    public static Intent makeIntent(Context context, Long userIdToPass){
        Intent intent = new Intent(context, SendMessageActivity.class);
        intent.putExtra("ID", userIdToPass);
        return intent;
    }
}
