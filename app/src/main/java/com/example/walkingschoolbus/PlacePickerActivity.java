package com.example.walkingschoolbus;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

public class PlacePickerActivity extends AppCompatActivity {

    int PLACE_PICKER_REQUEST = 1;
    private final static String TAG = "Place Picker Activity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_picker);
        setupPrimaryLocationButton();
        setupMeetupLocButton();
        }

    private void setupPrimaryLocationButton() {
        Button btn = findViewById(R.id.btnMeetingLoc);
        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                goPlacePicker();
            }
        });
    }

    private void setupMeetupLocButton() {
        Button btn = findViewById(R.id.btnPrimaryLoc);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goPlacePicker();
            }
        });
    }

    //Source https://www.youtube.com/channel/UCYN1_QmpaIGZNiwGSzFbE2w
    public void goPlacePicker(){
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try{
            startActivityForResult(builder.build(PlacePickerActivity.this),PLACE_PICKER_REQUEST);
            Log.i(TAG,"Start Place Picker");
        }catch(GooglePlayServicesRepairableException e){
            e.printStackTrace();
            Log.i(TAG,"repairable Exception");
        }catch(GooglePlayServicesNotAvailableException e){
            e.printStackTrace();
            Log.i(TAG,"Not Available Exception");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        Log.i(TAG,"result code: " +Integer.toString(resultCode));
        if(requestCode ==PLACE_PICKER_REQUEST){
            if(requestCode== RESULT_OK){
                Place place = PlacePicker.getPlace(PlacePickerActivity.this, data);
            }
        }
    }

    public static Intent makeIntent(Context context){
            Intent intent = new Intent( context, PlacePickerActivity.class );
            return intent;

    }
}
