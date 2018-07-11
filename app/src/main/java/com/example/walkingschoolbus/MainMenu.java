package com.example.walkingschoolbus;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.walkingschoolbus.model.Session;
import com.example.walkingschoolbus.model.User;

import java.util.List;

/**
 * Main menu screen to give users highest level option after log in
 */
public class MainMenu extends AppCompatActivity {

    public static final String USER_TOKEN = "User token";
    Session session = Session.getInstance();
    User user = User.getInstance();
    String token = session.getToken();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        setupLayoutGroups();
        setupLayoutMaps();
        setupLayoutMessages();
        setupLayoutMyParents();
        setupLogOutButton();

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run(){
                setTextViewMessage();
            }
        },2000);
        setTextViewMessage();
    }

    /**
     * setup logout button to finish this app.
     */
    private void setupLogOutButton(){
    Button btn = findViewById(R.id.btnLogOut);
    btn.setOnClickListener(new View.OnClickListener(){
        @Override
        public void onClick(View v){
            session.deleteToken();
            session.storeSession(MainMenu.this);
            Intent intent = WelcomeScreen.makeIntent(MainMenu.this);
            startActivity(intent);
            finish();
        }
    });

    }


    /**
     * setup linear layout to redirect to settings page on click
     */
    private void setupLayoutMessages() {
        LinearLayout setting = (LinearLayout) findViewById(R.id.linearLayoutMessages);
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = MessageActivity.makeIntent(MainMenu.this);
                startActivity(intent);

            }
        });
    }

    /**
     * setup linear layout to redirect to group management page on click
     */
    private void setupLayoutGroups() {
        LinearLayout group = (LinearLayout)findViewById(R.id.linearLayoutGroup);
        group.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = GroupManagementActivity.makeIntent(MainMenu.this);
                startActivity(intent);
                Log.w("Main Menu","Group Activity Launched");
            }
        });
    }


    /**
     * setup linear layout to redirect to my parents page on click
     */
    private void setupLayoutMyParents() {
        LinearLayout setting = (LinearLayout) findViewById(R.id.linearLayoutMyParents);
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = MonitoredListActivity.makeIntent(MainMenu.this);
                Log.w("Maintest", "   --> NOW HAVE TOKEN(output3): " + token);
                startActivity(intent);
            }
        });
    }


    /**
     * setup linear layout to redirect to my kids page on click
     */
    private void setupLayoutMyKids() {
        LinearLayout setting = (LinearLayout) findViewById(R.id.linearLayoutMyKids);
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = MonitoringListActivity.makeIntent(MainMenu.this);
                Log.w("Maintest", "   --> NOW HAVE TOKEN(output3): " + token);
                startActivity(intent);
            }
        });
    }



    /**
     *Show welcome message to the user loggged in
     */
    private void setTextViewMessage( )
    {
        String welcomeMessage;
        TextView welcome = (TextView) findViewById( R.id.mainMenuWelcomeMessage );
        if(session.getName()!=null) {
            welcomeMessage = getString(R.string.hello) + " " + session.getName();
        }else{
            welcomeMessage = getString(R.string.hello);
        }
        welcome.setText( welcomeMessage );

    }


    /**
     * setup linear layout to redirect to map activity
     */
    private void setupLayoutMaps() {
        LinearLayout maps = (LinearLayout)findViewById(R.id.linearLayoutMaps);
        maps.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = MapsActivity.makeIntent(MainMenu.this);
                startActivity(intent);
                Log.i("Sprint1","Map activity Launched");
            }
        });
    }

    public static Intent makeIntent(Context context){

        return new Intent(context,MainMenu.class);
    }




}
