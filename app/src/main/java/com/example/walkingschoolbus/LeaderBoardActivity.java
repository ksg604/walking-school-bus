package com.example.walkingschoolbus;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.walkingschoolbus.model.Session;
import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

import retrofit2.Call;

public class LeaderBoardActivity extends AppCompatActivity {

    Session session;
    User user;
    private String userToken;
    private static WGServerProxy proxy;
    private List<User> listUsersSortedByPoints;
    private List<String> listSortedStringUserInfo = new ArrayList<>( );
    String userFirstName;
    String userLastName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_leader_board );

        session = Session.getInstance();
        userToken = session.getToken();
        user = new User();
        proxy = ProxyBuilder.getProxy(getString( R.string.api_key),userToken);

        Call<List<User>> caller = proxy.getUsers();
        ProxyBuilder.callProxy(LeaderBoardActivity.this, caller, returnedUsers-> responseForList(returnedUsers));
    }

    private void responseForList(List<User> returnedUsers) {

        listUsersSortedByPoints = new ArrayList<>( returnedUsers );
        for (User user : listUsersSortedByPoints){
            if (user.getTotalPointsEarned() == null){
                user.setTotalPointsEarned( 0 );
            }
        }
        
        Collections.sort(listUsersSortedByPoints, new Comparator<User>() {
            @Override
            public int compare(User user1, User user2) {
                return Integer.valueOf(user2.getTotalPointsEarned()).compareTo(user1.getTotalPointsEarned());
            }
        });

        populateListView(listUsersSortedByPoints);
    }

    private void populateListView(List<User> userList) {

        for ( int i = 0; i < userList.size(); i++ ) {
            if( i < 100) {
                user = userList.get( i );
                String userName = user.getName();

                StringTokenizer stringToken = new StringTokenizer(userName);
                int flag = 0;
                while (stringToken.hasMoreTokens()) {
                    if( flag == 0){
                        userFirstName = stringToken.nextToken();

                    }else{
                        userLastName = stringToken.nextToken();
                    }
                    flag++;
                }
                int order = i+1;
                String orderNum;
                if (order == 1 || order == 21 || order == 31 || order == 41 || order == 51 || order == 61 || order == 71
                        || order == 81 || order == 91){
                    orderNum = order + getString( R.string.st );
                }else if( order == 2 || order == 22 || order == 32 || order == 42 || order == 52 || order == 62 || order == 72
                        || order == 82 || order == 92){
                    orderNum = order + getString( R.string.nd );
                }else if( order == 3 || order == 23 || order == 33 || order == 43 || order == 53 || order == 63 || order == 73
                        || order == 83 || order == 93){
                    orderNum = order + getString( R.string.rd );
                }else{
                    orderNum = order + getString( R.string.th );
                }


                String stringForList;
                if(flag > 1) {
                    stringForList = orderNum  + " " + userFirstName + " " + userLastName.charAt( 0 ) + getString(R.string.colon)+ " "
                            + user.getTotalPointsEarned()+ " " + getString(R.string.user_point_total);
                }
                else{
                    stringForList = orderNum + " " + userFirstName + getString(R.string.colon)+ "  " + user.getTotalPointsEarned()
                            +" " + getString(R.string.user_point_total) ;
                }
                listSortedStringUserInfo.add( stringForList );
            }else{
                break;
            }
        }
        ArrayAdapter<String> adapterForLeaderBoard = new ArrayAdapter<String>(this, R.layout.swipe_listview, listSortedStringUserInfo);

        //Configure the list view
        ListView list = (ListView) findViewById( R.id.listViewForLeaderBoard );
        list.setAdapter( adapterForLeaderBoard);
    }

    public static Intent makeIntent(Context context) {
        return new Intent( context, LeaderBoardActivity.class );
    }
}
