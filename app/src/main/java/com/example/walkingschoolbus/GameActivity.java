package com.example.walkingschoolbus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.walkingschoolbus.model.EarnedRewards;
import com.example.walkingschoolbus.model.Session;
import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;

import java.util.ArrayList;

import retrofit2.Call;

public class GameActivity extends AppCompatActivity {

    private Session session = Session.getInstance();
    private ImageView[][] images = new ImageView[7][7];
    private static int postItNewWidth;
    private static int postItNewHeight;
    private User user;
    private static WGServerProxy proxy;
    private String token;
    private boolean[][] stickers = new boolean[7][7];
    int numRewards = 0;
    int priceReward = 500;
    EarnedRewards rewards = new EarnedRewards();
    private static final String TAG = "GameActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_game );
        user = session.getUser();
        token = session.getToken();
        proxy = ProxyBuilder.getProxy(getString( R.string.api_key),token);

        getSessionUser();

    }

    private void getSessionUser(){

        Call<User> sessionUserCaller = proxy.getUserById(session.getUser().getId());
        ProxyBuilder.callProxy( GameActivity.this, sessionUserCaller, returnedSessionUser -> responseForSessionUser( returnedSessionUser) );
    }

    private void responseForSessionUser(User theSessionUser){
        user = theSessionUser;
        //Update layout textviews
        TextView pointsTitle = findViewById(R.id.userPointsTxt);
        if(theSessionUser.getCurrentPoints() != null){
            user.setCurrentPoints( theSessionUser.getCurrentPoints() );
            pointsTitle.setText( getString( R.string.session_user_points ) + theSessionUser.getCurrentPoints() );

        }else{
            user.setCurrentPoints( 0 );
            pointsTitle.setText (getString( R.string.session_user_points ) + 0 );
        }

        if(theSessionUser.getRewards() !=null) {
            user.setRewards( theSessionUser.getRewards() );
            stickers = theSessionUser.getRewards().getStickers();
            for (int rowIdx = 0; rowIdx < 7; rowIdx++) {
                for (int colIdx = 0; colIdx < 7; colIdx++) {
                    if (stickers[rowIdx][colIdx] == true) {
                        numRewards++;
                    }
                }
            }
            priceReward = priceReward + (numRewards * 25);

        }else{
            EarnedRewards newReward = new EarnedRewards();
            newReward.setStickers( stickers );
            user.setRewards( newReward);
        }
        TextView price = findViewById( R.id.currentPriceTxt );
        TextView rewardsOwned = findViewById( R.id.rewardUserGetTxt );
        price.setText( getString(R.string.current_price) + priceReward );
        rewardsOwned.setText ( getString(R.string.current_num_rewards ) + numRewards + getString(R.string.stickers));
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
                 int COL_POST_IT = col;
                 int ROW_POST_IT = row;

                ImageView postItForGrid = new ImageView(this);
                TableRow.LayoutParams imageLayoutParams =  new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.MATCH_PARENT,1.0f);
                imageLayoutParams.setMargins(0,0,0,0 );
                postItForGrid.setLayoutParams( imageLayoutParams );
                postItForGrid.setPadding( 0,0,0,0 );

                postItForGrid.setImageResource( R.drawable.new_post_it);

                if( user.getRewards().getStickers()[row][col] == true){

                    postItForGrid.setVisibility( View.INVISIBLE );

                }else {
                    postItForGrid.setVisibility( View.VISIBLE);

                    postItForGrid.setOnClickListener( new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            gridImageClicked( COL_POST_IT, ROW_POST_IT );
                        }
                    } );

                }
                tableRow.addView( postItForGrid );
                images[row][col] = postItForGrid;
            }
        }
        ImageView theFace = findViewById( R.id.theFace );
        theFace.setImageResource( R.drawable.dr_brian_fraser );
    }

    private void gridImageClicked(int col, int row) {

        if (checkSpendPoints()){

            ImageView postIt = images[row][col];
            postIt.setVisibility( View.INVISIBLE );

            user.setCurrentPoints( user.getCurrentPoints()-priceReward );
            stickers[row][col] = true;
            if (user.getRewards() != null){
                rewards = user.getRewards();
            }
            rewards.setStickers( stickers );
            user.setRewards( rewards );
            numRewards++;
            priceReward += 25;
            updateTextView();
            Log.i(TAG,"user points after buying: "+user.getCurrentPoints());

            Call<User> caller = proxy.editUser( user.getId(), user );
            ProxyBuilder.callProxy( GameActivity.this, caller, returnedUser -> responseForUser( returnedUser ) );
        }

    }

    private boolean checkSpendPoints() {
        if (user.getCurrentPoints() < priceReward) {
            Log.i( TAG, "NO TRY TO GET REWARDS" );
            Toast.makeText(GameActivity.this,getString(R.string.toast_not_enough),Toast.LENGTH_SHORT ).show();
            return false;
        }
        return true;
    }


    private void updateTextView() {
        TextView pointsTitle = findViewById(R.id.userPointsTxt);
        TextView price = findViewById( R.id.currentPriceTxt );
        TextView rewardsOwned = findViewById( R.id.rewardUserGetTxt );
        pointsTitle.setText( getString( R.string.session_user_points ) + user.getCurrentPoints() );
        price.setText( getString(R.string.current_price) + priceReward );
        rewardsOwned.setText ( getString(R.string.current_num_rewards ) + numRewards + getString(R.string.stickers));

    }

    private void responseForUser(User returnedUser){
        session.setUser(returnedUser);

    }

    public static Intent makeIntent(Context context){
        Intent intent = new Intent(context, GameActivity.class);
        return intent;
    }
}
