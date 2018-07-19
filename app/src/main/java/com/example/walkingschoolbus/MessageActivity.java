package com.example.walkingschoolbus;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.walkingschoolbus.model.Group;
import com.example.walkingschoolbus.model.Message;
import com.example.walkingschoolbus.model.Session;
import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;

import static android.media.CamcorderProfile.get;

public class MessageActivity extends AppCompatActivity {

    private User user;
    private static WGServerProxy proxy;
    private Session session;


    private List<String> listDataHeader;
    private HashMap<String,List<String>> listHash;

    private final static String unread = "unread";
    private final static String read = "read";


    private ExpandableListView listView;
    private ExpandableListAdapter listAdapter;



    List<Long>unreadMessageIdList = new ArrayList<>();
    List<Long>oldMessageIdList = new ArrayList<>();

    String senderName;
    Long fromUserId;

    List<String> OldMessageString = new ArrayList<>();
    List<String> NewMessageStringList = new ArrayList<>();

     String messageContent;
    String tempMessageContent;

    private static Handler handler = new Handler();
    private static Runnable runnableCode;

    List<Message> OldMessage = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        listDataHeader = new ArrayList<>();
        listHash = new HashMap<>();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message2);
        session = Session.getInstance();
        user = session.getUser();

        proxy = ProxyBuilder.getProxy(getString(R.string.api_key),session.getToken());
        listView = (ExpandableListView)findViewById(R.id.messageList);

        initData();

        listAdapter = new com.example.walkingschoolbus.model.ExpandableListAdapter(this,listDataHeader,listHash);
        listView.setAdapter(listAdapter);

        makeHandlerRun();

       // setupGetUnReadMessage();

        setupGetReadMessage();

        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                if(groupPosition == 0) {
                    Long tempMessageID = unreadMessageIdList.get(childPosition);

                    Call<Message> caller = proxy.markMessageAsReadOrUnread(tempMessageID, true);
                    ProxyBuilder.callProxy(MessageActivity.this, caller,
                            returnedMessage -> responseForReadMark(returnedMessage));



                    updateUnreadList(childPosition);
                    //tempMessageContent = NewMessageStringList.get(childPosition);
                    updateReadList(childPosition, tempMessageContent);

                    // NewMessageStringList.remove(childPosition);
                    // OldMessageString.add(childPosition, messageContent);

                    Intent intent = MessageDetailActivity.makeIntent(MessageActivity.this, tempMessageID, fromUserId);
                    startActivity(intent);
                }

                if(groupPosition == 1) {

                }
                
                return false;

            }
        });
    }

    private void updateUnreadList(int index) {
        tempMessageContent =NewMessageStringList.get(index);
        NewMessageStringList.remove(index);
        session.setNumOfUnreadMessage(NewMessageStringList.size());
        listHash.put(listDataHeader.get(0), NewMessageStringList);
        listAdapter = new com.example.walkingschoolbus.model.ExpandableListAdapter(this,listDataHeader,listHash);
        listView.setAdapter(listAdapter);


    }

    private void updateReadList(int index, String content) {
        OldMessageString.add(index, content);
        listHash.put(listDataHeader.get(1), OldMessageString);
        listAdapter = new com.example.walkingschoolbus.model.ExpandableListAdapter(this,listDataHeader,listHash);
        listView.setAdapter(listAdapter);


    }


    private void setupGetReadMessage() {
        Call<List<Message>> callerForOldMessage = proxy.getMessageNotRead(user.getId(), read);
        ProxyBuilder.callProxy(MessageActivity.this, callerForOldMessage,
                returnedOldMessageList -> responseForOldMessage(returnedOldMessageList));

    }

    private void setupGetUnReadMessage() {
        Call<List<Message>> callerForUnreadMessage = proxy.getMessageNotRead(user.getId(), unread);
        ProxyBuilder.callProxy(MessageActivity.this, callerForUnreadMessage,
                returnedMessageList -> responseForUnreadMessage(returnedMessageList));
    }



    private void responseForOldMessage(List<Message> returnedOldMessageList) {

        for (Message message : returnedOldMessageList) {

            OldMessage.add(message);
            if(!(oldMessageIdList.contains(message.getId()))){
                oldMessageIdList.add(message.getId());
                Long fromUserId = message.getFromUser().getId();

                Call<User> callerForGroup = proxy.getUserById(fromUserId);
                ProxyBuilder.callProxy(MessageActivity.this, callerForGroup,
                        anotherUser-> responseForMessageSender(anotherUser, message));
            }


            listHash.put(listDataHeader.get(1), OldMessageString);
            listAdapter = new com.example.walkingschoolbus.model.ExpandableListAdapter(this,listDataHeader,listHash);
            listView.setAdapter(listAdapter);


        }
    }


    private void responseForUnreadMessage(List<Message> returnedMessageList) {

        for (Message message : returnedMessageList) {
    
            if (!(message.getFromUser().getId().equals(user.getId())) ) {
                Log.i("Show me fromid",message.getFromUser().getId().toString());
                Log.i("Show me userid", user.getId().toString());

                if (!unreadMessageIdList.contains(message.getId())) {
                    unreadMessageIdList.add(message.getId());
                    fromUserId = message.getFromUser().getId();
                    messageContent = "New Message" + " from " + fromUserId + "......";
                    NewMessageStringList.add(messageContent);
                    session.setNumOfUnreadMessage(NewMessageStringList.size());

                }
            }
        }
        listHash.put(listDataHeader.get(0), NewMessageStringList);
        listAdapter = new com.example.walkingschoolbus.model.ExpandableListAdapter(this,listDataHeader,listHash);
        listView.setAdapter(listAdapter);

    }



    private void responseForMessageSender(User anotherUser, Message message) {
        User tempuser = new User();
        tempuser.setEmail(anotherUser.getEmail());
        // List<String> OldMessageString = new ArrayList<>();
        Log.i("What is Sender ID??", tempuser.getEmail());
        senderName = tempuser.getEmail();
        String messageContent = senderName+": "+ message.getText();
        OldMessageString.add(messageContent);
        // listHash.put(listDataHeader.get(1), OldMessageString);

    }


    private void makeHandlerRun() {
        runnableCode = new Runnable(){
            public void run() {
                setupGetUnReadMessage();
                sendUnreadMessageNumberToMainpage();
                handler.postDelayed(this, 30000);
            }
        };
        handler.post(runnableCode);
    }

    private void sendUnreadMessageNumberToMainpage() {
        Intent intent = MainMenu.makeIntent(MessageActivity.this, 5);

    }


    private void initData() {
        listDataHeader.add("New Message");
        listDataHeader.add("Old Message");
       // listDataHeader.add("Group Message. For test now.");
    }

    public static Intent makeIntent(Context context){
        Intent intent = new Intent(context,MessageActivity.class);
        return intent;
    }

    private void responseForReadMark(Message returnedMessage) {

    }

}
