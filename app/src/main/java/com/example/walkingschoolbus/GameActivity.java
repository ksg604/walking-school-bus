package com.example.walkingschoolbus;

import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.example.walkingschoolbus.model.Session;
import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;

import retrofit2.Call;

public class GameActivity extends AppCompatActivity {

    private Session session = Session.getInstance();
    private ImageView[][] images = new ImageView[7][7];
    private User user;
    private static WGServerProxy proxy;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_game );
        user = session.getUser();
        token = session.getToken();
        proxy = ProxyBuilder.getProxy(getString( R.string.api_key),token);



        populatePostIt();
    }

    private void populatePostIt() {
        TableLayout table = findViewById( R.id.tableWithPostIt );
        for (int row = 0 ; row < 7 ; row++){
            TableRow tableRow = new TableRow(this);
            TableLayout.LayoutParams tableRowLayoutParams =  new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.MATCH_PARENT,1.0f);

            tableRowLayoutParams.setMargins( 0,0,0,0 );

            tableRow.setLayoutParams( tableRowLayoutParams );
            tableRow.setPadding( 0,0,0,0 );



            table.addView( tableRow );

            for(int col =0; col < 7; col++) {
                final int COL_POSTIT = col;
                final int ROW_POSTIT = row;
                ImageView postIt = new ImageView(this);
                TableRow.LayoutParams imagelLayoutParams =  new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.MATCH_PARENT,1.0f);
                imagelLayoutParams.setMargins(0,0,0,0 );
                postIt.setLayoutParams( imagelLayoutParams );
                postIt.setPadding( 0,0,0,0 );

                postIt.setImageResource( R.drawable.new_post_it );

                postIt.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        gridImageClicked( COL_POSTIT, ROW_POSTIT);

                    }
                } );
                tableRow.addView( postIt );
                images[row][col] = postIt;

            }
        }



    }

    private void gridImageClicked(int col, int row) {
        ImageView postIt = images[row][col];
        postIt.setVisibility( View.INVISIBLE );

        Call<User> caller = proxy.editUser(user.getId(), user);

    }


    public static Intent makeIntent(Context context){
        Intent intent = new Intent(context, GameActivity.class);
        return intent;
    }



}
