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

public class MessageActivity extends AppCompatActivity {

    private List<String> stringMemberGroupList = new ArrayList< >( );
    private List<String> stringLeaderGroupList = new ArrayList< >( );
    private List<Group> groupLeaderList = new ArrayList<>();
    private List<Group> modifiedGroupLeaderList = new ArrayList<>(  );
    private List<Long> groupIdLeaderList = new ArrayList<>();
    private List<Group> groupMemberList = new ArrayList<>();
    private List<Group> modifiedGroupMemberList = new ArrayList<>( );
    private List<Long> groupIdMemberList = new ArrayList<>();
    private ArrayList<Long> monitoringUser = new ArrayList<>();



    private User user;
    private static WGServerProxy proxy;
    private Session session;


    private ExpandableListView listView;
    private ExpandableListAdapter listAdapter;
    private List<String> listDataHeader;
    private HashMap<String,List<String>> listHash;

    private final static String unread = "unread";
    private final static String read = "read";


    List<Long>unreadMessageIdList = new ArrayList<>();
    List<Long>oldMessageIdList = new ArrayList<>();

    String senderName;

    List<String> OldMessageString = new ArrayList<>();

    List<String> NewMessageStringList = new ArrayList<>();

    private static Handler handler = new Handler();
    private static Runnable runnableCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        listDataHeader = new ArrayList<>();
        listHash = new HashMap<>();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message2);

       // Session.getStoredSession(this);
        //user = User.getInstance();
       // user = new User();
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

        //TODO
        //Remove this part to group activity to send messages to groups
        Call<User> callerForGroup = proxy.getUserById(user.getId());
        ProxyBuilder.callProxy(MessageActivity.this, callerForGroup,
                currentUser -> responseForGroupList(currentUser));


        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                //return false;
                if(groupPosition == 2) {
                    Long tempID = groupIdLeaderList.get(childPosition);

                    Intent intent = SendMessageActivity.makeIntent(MessageActivity.this, tempID);
                    startActivity(intent);
                }
                if(groupPosition == 0 || groupPosition ==1){
                    Long tempMessageID = unreadMessageIdList.get(childPosition);

                }
                return false;

            }
        });
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
        List<Message> OldMessage = new ArrayList<>();
        //List<String> OldMessageString = new ArrayList<>();
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

        }
    }


    private void responseForUnreadMessage(List<Message> returnedMessageList) {

        for (Message message : returnedMessageList) {

            unreadMessageIdList.add(message.getId());
            Long fromUserId = message.getFromUser().getId();
            Call<User> callerForGroup = proxy.getUserById(fromUserId);
            ProxyBuilder.callProxy(MessageActivity.this, callerForGroup,
                        new ProxyBuilder.SimpleCallback<User>() {
                            @Override
                            public void callback(User user) {
                                User messageSender = new User();
                                messageSender = user;
                                senderName = messageSender.getName();
                                String messageContent = senderName+": "+ message.getText();
                                NewMessageStringList.add(messageContent);
                                // listHash.put(listDataHeader.get(1), OldMessageString);
                            }
                        });
            }

        listHash.put(listDataHeader.get(0), NewMessageStringList);
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

    private void responseForGroupList(User returnedUser) {

        groupLeaderList = returnedUser.getLeadsGroups();
       // returnedUser.getLeadsGroups();
        for(Group group : groupLeaderList ){
            groupIdLeaderList.add(group.getId());
        }

        groupMemberList = returnedUser.getMemberOfGroups();
        for( Group group : groupMemberList ){
            groupIdMemberList.add(group.getId());
        }

        user.setId(returnedUser.getId());

        // Make call
        Call<List<Group>> caller = proxy.getGroups();
        ProxyBuilder.callProxy(MessageActivity.this, caller,
                returnedGroupList -> responseForGroupCheck(returnedGroupList));
    }


    private void responseForGroupCheck(List<Group> returnedGroups) {

        List<String> groupList = new ArrayList<>();

        for (Group group : returnedGroups) {

            if (groupIdMemberList.contains(group.getId())) {
                //Log.w( TAG, getString( R.string.group_list) + " " + group.getId() );

                modifiedGroupMemberList.add(group);
                String groupInfo = getString(R.string.group_list) + " " + group.getGroupDescription();
                stringMemberGroupList.add(groupInfo);

            } else if (groupIdLeaderList.contains(group.getId())) {
                // Log.w( TAG, getString( R.string.group_list) + " " + group.getId() );

                modifiedGroupLeaderList.add(group);
                String groupInfo = getString(R.string.group_list) + " " + group.getGroupDescription();
                stringLeaderGroupList.add(groupInfo);

                groupList.add(groupInfo);
                monitoringUser.add(user.getId());

                //listHash.put(listDataHeader.get(0),edmtDev);
                listHash.put(listDataHeader.get(2),groupList);
            }

        }

    }

    private void makeHandlerRun() {
        runnableCode = new Runnable(){
            public void run() {
                setupGetUnReadMessage();
                handler.postDelayed(this, 30000);
            }
        };
        handler.post(runnableCode);
    }


    private void initData() {
        listDataHeader.add("New Message");
        listDataHeader.add("Old Message");
        listDataHeader.add("Group Message. For test now.");
    }


    public static Intent makeIntent(Context context){
        Intent intent = new Intent(context,MessageActivity.class);
        return intent;
    }

}
