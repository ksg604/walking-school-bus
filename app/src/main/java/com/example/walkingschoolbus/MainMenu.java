package com.example.walkingschoolbus;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;

public class MainMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main_menu );

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle( R.string.main_menu );

        setupLayoutMaps();
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


    /**Make intent for main menu activity
     *
     * @param context
     * @return
     */
    public static Intent makeIntent(Context context){
        Intent intent = new Intent( context, MainMenu.class );
        return intent;
    }




}
