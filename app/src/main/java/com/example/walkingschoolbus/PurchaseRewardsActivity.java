package com.example.walkingschoolbus;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.walkingschoolbus.model.EarnedRewards;

import java.io.File;
import java.util.List;

public class PurchaseRewardsActivity extends AppCompatActivity {

    List<File> backgroundFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_rewards);
    }

    private void getUserRewards(){

        String path = "C:\\Users\\Kevin\\Documents\\AndroidProjects\\CMPT276Proj\\app\\src\\main\\assets";
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++)
        {
            backgroundFiles.add(files[i]);
            Log.d("Files", "FileName:" + files[i].getName());
        }

    }
}
