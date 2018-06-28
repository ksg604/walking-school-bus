package com.example.walkingschoolbus;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;

public class MainMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main_menu );

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle( R.string.main_menu );



    }



    //Make a intent to move to this activity
    public static Intent makeIntent(Context context){
        Intent intent = new Intent( context, MainMenu.class );
        return intent;
    }




}
