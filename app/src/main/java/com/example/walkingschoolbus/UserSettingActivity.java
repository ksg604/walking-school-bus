package com.example.walkingschoolbus;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class UserSettingActivity extends AppCompatActivity {

    public static final String USER_TOKEN = "User Token";
    private String userToken2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_setting);
        userToken2 = extractDataFromIntent();
        setupMonitoringListButton();
        setupMonitoredListButton();
    }

    private void setupMonitoredListButton() {
        Button btnMonitoring = (Button) findViewById(R.id.btnMonitoredList);
        btnMonitoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = MonitoredListActivity.makeIntent(UserSettingActivity.this, userToken2);
                Log.w("UserSettingTest", "   --> NOW HAVE TOKEN(output4): " + userToken2);
                startActivity(intent);
            }
        });
    }

    private String extractDataFromIntent() {
        Intent intent = getIntent();
        return intent.getStringExtra(USER_TOKEN);
    }

    private void setupMonitoringListButton() {
        Button btnMonitoring = (Button) findViewById(R.id.btnMonitoringList);
        btnMonitoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = MonitoringListActivity.makeIntent(UserSettingActivity.this, userToken2);
                Log.w("UserSettingTest", "   --> NOW HAVE TOKEN(output4): " + userToken2);
                startActivity(intent);
            }
        });
    }


    public static Intent makeIntent(Context context, String tokenToPass){
        Intent intent = new Intent(context, UserSettingActivity.class);
        intent.putExtra(USER_TOKEN, tokenToPass);
        return intent;
    }
}
