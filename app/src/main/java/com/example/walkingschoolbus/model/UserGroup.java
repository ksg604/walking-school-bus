package com.example.walkingschoolbus.model;

import java.util.ArrayList;
import java.util.List;

public class UserGroup {

    private List<UserInfo> userMonitoringMe = new ArrayList<>();
    private List<UserInfo> userMonitoredByMe = new ArrayList<>();

    /*
    Singleton Support
     */
    private static UserGroup instance;
    private  UserGroup() {

    }

    public UserGroup(List<UserInfo> userMonitoringMe, List<UserInfo> userMonitoredByMe) {
        this.userMonitoringMe = userMonitoringMe;
        this.userMonitoredByMe = userMonitoredByMe;
    }

    public static UserGroup getInstance() {
        if(instance == null) {
            instance = new UserGroup();
        }
        return instance;
    }


    public List<UserInfo> getUserMonitoringMe() {
        return userMonitoringMe;
    }

    public void setUserMonitoringMe(List<UserInfo> userMonitoringMe) {
        this.userMonitoringMe = userMonitoringMe;
    }

    public List<UserInfo> getUserMonitoredByMe() {
        return userMonitoredByMe;
    }

    public void setUserMonitoredByMe(List<UserInfo> userMonitoredByMe) {
        this.userMonitoredByMe = userMonitoredByMe;
    }

    public void addUserToMonitor(UserInfo userToAdd) {
        userMonitoredByMe.add(userToAdd);
    }

    public void addUserMonitorMe(UserInfo userToAdd) {
        userMonitoringMe.add(userToAdd);
    }

    public void removeUserMonitored(UserInfo userToRemove) {
        userMonitoredByMe.remove(userToRemove);
    }

    public void removeUserMonitoringMe(UserInfo userToRemove) {
        userMonitoringMe.remove(userToRemove);
    }


}
