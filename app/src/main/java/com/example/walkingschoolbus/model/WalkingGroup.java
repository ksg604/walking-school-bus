package com.example.walkingschoolbus.model;

import android.location.Location;

import java.util.ArrayList;
import java.util.List;

public class WalkingGroup {


    private String groupName;

    private List<UserInfo>  walkingGroup = new ArrayList<>();

    //should have private field for location. But I am not sure if it is Location type
    private Location myLocation;

    private UserInfo groupLeader;

    /*
    Singleton Support
     */
    private static WalkingGroup instance;
    private  WalkingGroup() {

    }

    public WalkingGroup(String groupName, List<UserInfo> walkingGroup, Location myLocation, UserInfo groupLeader) {
        this.groupName = groupName;
        this.walkingGroup = walkingGroup;
        this.myLocation = myLocation;
        this.groupLeader = groupLeader;
    }

    public WalkingGroup(String groupName, Location myLocation) {
        this.groupName = groupName;
        this.myLocation = myLocation;
    }

    public static WalkingGroup getInstance() {
        if(instance == null) {
            instance = new WalkingGroup();
        }
        return instance;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<UserInfo> getWalkingGroup() {
        return walkingGroup;
    }

    public void setWalkingGroup(List<UserInfo> walkingGroup) {
        this.walkingGroup = walkingGroup;
    }

    public Location getMyLocation() {
        return myLocation;
    }

    public void setMyLocation(Location myLocation) {
        this.myLocation = myLocation;
    }

    public static void setInstance(WalkingGroup instance) {
        WalkingGroup.instance = instance;
    }


    public void joinWalkingGroup (UserInfo user) {
        walkingGroup.add(user);
    }

    public void leaveWalkingGroup (UserInfo user) {
        walkingGroup.remove(user);
    }

    public void removeMyMonitors (UserInfo user) {
        walkingGroup.remove(user);
    }

    public void removeByGroupLeader (UserInfo user) {
        walkingGroup.remove(user);
    }




}
