package com.example.walkingschoolbus;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.view.View;
import android.widget.Button;

public class WelcomeScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);

        setupDebugButton();
    }
//TODO Remove this before submission
    private void setupDebugButton() {
        Button btn = (Button) findViewById(R.id.buttonDebug);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = MainMenu.makeIntent(WelcomeScreen.this);
                startActivity(intent);
            }
        });
        setupSignUpButton();
    }

    private void setupSignUpButton() {
        Button btn = findViewById(R.id.btnSignUp);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = SignUpActivity.makeIntent(WelcomeScreen.this);
                startActivity(intent);
                Log.i("Sprint1","Sign up activity launched from welcome screen");
            }
        });
    }
}
