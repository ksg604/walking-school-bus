package com.example.walkingschoolbus;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class EditUserSettingsActivity extends AppCompatActivity {
    Spinner monthSpinner;
    ArrayAdapter<CharSequence> adapter;
    private static final String TAG ="editUserSettings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_settings);

        setupMonthSpinner();
    }

    private void setupMonthSpinner() {
        monthSpinner = findViewById(R.id.SpinnerEditUserMOB);
        adapter = ArrayAdapter.createFromResource(this, R.array.months_array,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(adapter);

    }

    public static Intent makeIntent(Context context, long userID) {
        Log.i(TAG,"makeIntent");
        Intent intent = new Intent(context, ViewUserSettingsActivity.class);
        intent.putExtra("ID",userID);
        return intent;
}
}
