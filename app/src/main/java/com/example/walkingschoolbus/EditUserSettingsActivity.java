/*
Class allows user to edit information on their own or their childs account. The class is called by
the View User Settings Class. All existing information will prepopulate the editable fields
so the user can see the current information.
 */

package com.example.walkingschoolbus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.walkingschoolbus.model.Session;
import com.example.walkingschoolbus.model.User;
import com.example.walkingschoolbus.proxy.ProxyBuilder;
import com.example.walkingschoolbus.proxy.WGServerProxy;

import java.lang.reflect.Proxy;

import retrofit2.Call;

public class EditUserSettingsActivity extends AppCompatActivity {
    //variables for server
    private Session session = Session.getInstance();
    private String userToken = session.getToken();
    private static WGServerProxy proxy;
    private static final String TAG ="editUserSettings";
    private long thisUserID;
    private User updatedUser;
    private User sessionUser=session.getUser();

    //spinner variables
    Spinner monthSpinner;
    ArrayAdapter<CharSequence> adapter;

    //layout text setup dynamically
    private EditText editName;
    private EditText editYearOfBirth;
    private EditText editHomePhone;
    private EditText editCellPhone;
    private EditText editEmail;
    private EditText editAddress;
    private EditText editTeacher;
    private EditText editICE;
    private int MOB=0;//0 is default value to set spinner later


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_settings);

        //link editText Variables to layout

        editName = findViewById(R.id.editTxtInputName);
        editYearOfBirth = findViewById(R.id.editTxtUserThisYOB);
        editHomePhone = findViewById(R.id.editTxtUserThisHPhone);
        editCellPhone = findViewById(R.id.editTxtUserThisCPhone);
        editEmail = findViewById(R.id.editTxtUserEmail);
        editAddress = findViewById(R.id.editTxtUserThisAddress);
        editTeacher = findViewById(R.id.editTextUserThisGrade);
        editICE = findViewById(R.id.editTxtUserThisICE);



        proxy = ProxyBuilder.getProxy(getString(R.string.api_key),userToken);

        Intent intent = getIntent();
        thisUserID=intent.getLongExtra("ID",0);

        updatedUser = new User(thisUserID);

        Call<User> caller = proxy.getUserById(thisUserID);
        ProxyBuilder.callProxy(EditUserSettingsActivity.this,caller,
                returnedUser -> responseForUser(returnedUser));

        setupMonthSpinner();
        setupSaveButton();
    }

    private void responseForUser(User user) {
        //try to set each field individually

        try{
            editName.setText(sessionUser.getName());
        } catch(NullPointerException e){
            Log.e(TAG, "exception: ", e);
        }
        try{
            if(!String.valueOf(user.getBirthYear()).equals("null")) {
                editYearOfBirth.setText(String.valueOf(user.getBirthYear()));
            }
            //editYearOfBirth.setText(String.valueOf(user.getBirthYear()));
        } catch(NullPointerException e){
            Log.e(TAG, "exception: ", e);
        }
        try{
            editHomePhone.setText(user.getHomePhone());
        } catch(NullPointerException e){
            Log.e(TAG, "exception", e);
        }
        try{
            editCellPhone.setText(user.getCellPhone());
        }catch(NullPointerException e){
            Log.e(TAG,"exception: ", e);
        }
        try {
            editEmail.setText(user.getEmail());
        } catch(NullPointerException e){
            Log.e(TAG, "exception: ", e);
        }
        try {
            editAddress.setText(user.getAddress());
        } catch(NullPointerException e){
            Log.e(TAG,"exception: ", e);
        }
        try {
            editTeacher.setText(user.getTeacherName());
        } catch(NullPointerException e){
            Log.e(TAG, "exception: ", e);
        }
        try {
            editICE.setText(user.getEmergencyContactInfo());
        } catch(NullPointerException e){
            Log.e(TAG, "exception: ", e);
        }
        try {
            MOB = user.getBirthMonth();
        }
        catch(NullPointerException e){
            Log.e(TAG, "exception: ", e);
        }

        updatedUser.makeCopyOf(user);
    }

    private void setupMonthSpinner() {
        monthSpinner = findViewById(R.id.SpinnerEditUserMOB);
        adapter = ArrayAdapter.createFromResource(this, R.array.months_array,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(adapter);

        monthSpinner.setSelection(MOB);
    }

    private void setupSaveButton() {
        Button btn = findViewById(R.id.btnEditUserSave);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Clicked Save");
                updateUser();
            }
        });
    }
    private void updateUser(){
        updatedUser.setName(editName.getText().toString());
        try {
            updatedUser.setBirthYear(Integer.parseInt(editYearOfBirth.getText().toString()));
        } catch(NumberFormatException e){
            Log.e(TAG,"updateUser:", e);
        }

        updatedUser.setBirthMonth(monthSpinner.getSelectedItemPosition());
        updatedUser.setHomePhone(editHomePhone.getText().toString());
        updatedUser.setCellPhone(editCellPhone.getText().toString());
        updatedUser.setEmail(editEmail.getText().toString());
        updatedUser.setAddress(editAddress.getText().toString());
        updatedUser.setEmergencyContactInfo(editICE.getText().toString());
        updatedUser.setTeacherName(editTeacher.getText().toString());

        Call<User> caller =proxy.editUser(thisUserID,updatedUser);
        ProxyBuilder.callProxy(EditUserSettingsActivity.this,caller,
                returnedUser -> responseForUpdatedUser(returnedUser));
    }

    private void responseForUpdatedUser(User returnedUser) {
        if(returnedUser.getId()==session.getid() && !returnedUser.getEmail().equals(session.getEmail()))
        {
            session.deleteTokenAndVariables();
            session.storeSession(EditUserSettingsActivity.this);
            Intent intent = WelcomeScreen.makeIntent(EditUserSettingsActivity.this);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |Intent.FLAG_ACTIVITY_CLEAR_TASK| intent.FLAG_ACTIVITY_NEW_TASK);
            Log.i(TAG,"set flag intent");
            startActivity(intent);
            //Toast.makeText(this, getResources().getString(R.string.user_logout_message),Toast.LENGTH_LONG).show();
            finish();
        } else{
            Intent intent = ViewUserSettingsActivity.makeIntent(EditUserSettingsActivity.this, thisUserID);
            intent.putExtra("result", RESULT_OK);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    public static Intent makeIntent(Context context, long userID) {
        Log.i(TAG,"makeIntent");
        Intent intent = new Intent(context, EditUserSettingsActivity.class);
        intent.putExtra("ID",userID);
        return intent;
    }
}
