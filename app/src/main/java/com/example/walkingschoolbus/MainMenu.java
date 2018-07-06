package com.example.walkingschoolbus;

import android.content.Context;
import android.content.Intent;
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
        setupLayoutSetting();
        setupLogOutButton();

        //TODO: delete this before push to main
        Toast toast = Toast.makeText(this, session.getEmail() +"||" + session.getName()+"||"+session.getid(),Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();

        Toast toast2 = Toast.makeText(this, user.getEmail() +"||" + user.getName()+"||"+user.getId(),Toast.LENGTH_LONG);
        toast2.setGravity(Gravity.CENTER|Gravity.CENTER_HORIZONTAL, 0, 0);
        toast2.show();

        setTextViewMessage();

    }

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

    private void setupLayoutSetting() {
        LinearLayout setting = (LinearLayout) findViewById(R.id.linearLayoutSetting);
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = UserSettingActivity.makeIntent(MainMenu.this);
                Log.w("Maintest", "   --> NOW HAVE TOKEN(output3): " + token);
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
                Log.w("Sprint1","Group Activity Launched");
            }
        });
    }

    /**setup linear layout to redirect to map activity
     *
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

    /**
     *Show welcome message to the user loggged in
     */
    private void setTextViewMessage( )
    {
        TextView welcome = (TextView) findViewById( R.id.mainMenuMessage );
        String welcomeMessage = getString( R.string.hello ) + " " + user.getName();
        welcome.setText( welcomeMessage );

    }

    public static Intent makeIntent(Context context){
        return new Intent(context,MainMenu.class);
    }




}
