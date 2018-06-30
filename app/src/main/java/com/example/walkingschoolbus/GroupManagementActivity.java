package com.example.walkingschoolbus;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.walkingschoolbus.model.Group;
import com.example.walkingschoolbus.model.User;

import java.util.List;

public class GroupManagementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_management);

        populateListView();
    }

    /**
     * Populate the list view of groups user belongs to
     */
    private void populateListView() {
        User user = User.getInstance();
        List<Group> groups;
        groups = user.getMemberOfGroups();
        String[] groupNames = new String[groups.size()];

        //grab names of all groups member belongs too
        for(int i =0; i<groups.size();i++){
            groupNames[i]=groups.get(i).getName();
        }
        //create array adaptor
        ArrayAdapter<String> adaptor = new ArrayAdapter<>(this, R.layout.groups_listview,
                groupNames);

        //configure list view for layout
        ListView list = findViewById(R.id.listViewGroups);
        list.setAdapter(adaptor);
    }

    /**
     * Create intent for this activity
     * @param context
     * @return
     */
    public static Intent makeIntent(Context context){
        return new Intent(context,GroupManagementActivity.class);
    }

}
