/**
 * Place picker activity allows user to select to locations on a map and use to that to generate
 * a new walking group.
 */

package com.example.walkingschoolbus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.walkingschoolbus.model.Group;
import com.example.walkingschoolbus.model.Session;
import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

public class PlacePickerActivity extends AppCompatActivity {

    private int PLACE_PICKER_LOC_REQUEST = 1;
    private int PLACE_PICKER_MEET_REQUEST = 2;
    private final static String TAG = "Place Picker Activity";
    private LatLng primaryLocation;
    private LatLng meetupLocation;
    private Session session = Session.getInstance();
    private String token = session.getToken();
    private User user = session.getUser();
    private static WGServerProxy proxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_picker);
        proxy = ProxyBuilder.getProxy(getString(R.string.api_key), token);
        setupPrimaryLocationButton();
        setupMeetupLocButton();
        setupCreateGroupButton();
    }

    private void setupPrimaryLocationButton() {
        Button btn = findViewById(R.id.btnMeetingLoc);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goPlacePicker(PLACE_PICKER_LOC_REQUEST);
            }
        });
    }

    private void setupMeetupLocButton() {
        Button btn = findViewById(R.id.btnPrimaryLoc);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goPlacePicker(PLACE_PICKER_MEET_REQUEST);
            }
        });
    }

    private void setupCreateGroupButton() {
        Button btn = findViewById(R.id.btnCreateGroup);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "create group");
                makeGroupFromUserData();
            }
        });
    }

    private void makeGroupFromUserData() {
        EditText userEnteredName = (EditText) findViewById(R.id.editTxtGroupName);
        List<Double> latArray = new ArrayList<>();
        List<Double> lngArray = new ArrayList<>();
        String groupDescription = userEnteredName.getText().toString();
        try{
            latArray.add(primaryLocation.latitude);
            latArray.add(meetupLocation.latitude);
            lngArray.add(primaryLocation.longitude);
            lngArray.add(meetupLocation.longitude);
            Group group = new Group(groupDescription, latArray, lngArray, user);
            Call<Group> caller = proxy.createGroup(group);
            ProxyBuilder.callProxy(PlacePickerActivity.this, caller, returnedGroup -> response(returnedGroup));}
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private void response(Group group) {
        Toast.makeText(this, "Group " + group.getGroupDescription() + " created",
                Toast.LENGTH_LONG).show();
        user.getLeadsGroups();
        Call<User> caller = proxy.getUserById(session.getid());
        ProxyBuilder.callProxy(PlacePickerActivity.this, caller, returnedUser -> responseForUser(returnedUser));
    }

    private void responseForUser(User returnedUser) {
        Log.i(TAG, "updating user after creating new group");
        user.makeCopyOf(returnedUser);
        Intent intent = GroupManagementActivity.makeIntent(PlacePickerActivity.this);
        int resultCode = Activity.RESULT_OK;
        intent.putExtra("result", RESULT_OK);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    //Source https://www.youtube.com/channel/UCYN1_QmpaIGZNiwGSzFbE2w
    public void goPlacePicker(int reqCode) {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(PlacePickerActivity.this), reqCode);
            Log.i(TAG, "Start Place Picker");
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
            Log.i(TAG, "repairable Exception");
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
            Log.i(TAG, "Not Available Exception");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "result code: " + Integer.toString(resultCode));
        TextView location = (TextView) findViewById(R.id.txtViewPrimaryLoc);
        TextView meetupLoc = (TextView) findViewById(R.id.txtViewMeetupLoc);
        if (requestCode == PLACE_PICKER_LOC_REQUEST || requestCode == PLACE_PICKER_MEET_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(PlacePickerActivity.this, data);
                if (requestCode == PLACE_PICKER_MEET_REQUEST) {
                    location.setText(place.getAddress());
                    primaryLocation = place.getLatLng();
                } else {
                    meetupLoc.setText(place.getAddress());
                    meetupLocation = place.getLatLng();
                }
            }
        }
    }

    public static Intent makeIntent(Context context) {
        Intent intent = new Intent(context, PlacePickerActivity.class);
        return intent;
    }
}
