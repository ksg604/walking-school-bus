package com.example.walkingschoolbus.model;


import android.graphics.Color;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EarnedRewards {


    private String title = "Dragon slayer";
    private List<File> possibleBackgroundFiles = new ArrayList<>();
    private Integer selectedBackground = 1;
    private Integer titleColor = Color.BLUE;

    // Needed for JSON deserialization
    public EarnedRewards() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<File> getPossibleBackgroundFiles() {
        return possibleBackgroundFiles;
    }

    public void setPossibleBackgroundFiles(List<File> possibleBackgroundFiles) {
        this.possibleBackgroundFiles = possibleBackgroundFiles;
    }

    public int getSelectedBackground() {
        return selectedBackground;
    }

    public void setSelectedBackground(int selectedBackground) {
        this.selectedBackground = selectedBackground;
    }

    public int getTitleColor() {
        return titleColor;
    }

    public void setTitleColor(int titleColor) {
        this.titleColor = titleColor;
    }

    @Override
    public String toString() {
        return "EarnedRewards{" +
                "title='" + title + '\'' +
                ", possibleBackgroundFiles=" + possibleBackgroundFiles +
                ", selectedBackground=" + selectedBackground +
                ", titleColor=" + titleColor +
                '}';
    }
}
