package com.example.walkingschoolbus.model;

import android.location.Location;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Store information about the walking groups.
 *
 * WARNING: INCOMPLETE! Server returns more information than this.
 * This is just to be a placeholder and inspire you how to do it.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Group {

    private Long id;

    private String groupDescription;

    private List<Double> routeLatArray = new ArrayList<>();

    private List<Double> routeLngArray = new ArrayList<>();

    private User leader;

    private List<User> memberUsers = new ArrayList<>();

    private Boolean hasFullData;

    private String href;

    /*
    Singleton Support
     */
    private static Group instance;
    private Group() {

    }
    public static Group getInstance() {
        if(instance == null) {
            instance = new Group();
        }
        return instance;
    }
    //Constructor

    public Group(long id, String groupDescription, List<Double> routeLatArray,
                 List<Double> routeLngArray, User leader, List<User> memberUsers, String href) {
        this.id = id;
        this.groupDescription = groupDescription;
        this.routeLatArray = routeLatArray;
        this.routeLngArray = routeLngArray;
        this.leader = leader;
        this.memberUsers = memberUsers;
        this.hasFullData = true;
        this.href=href;
    }

    public Group(String groupDescription, List<Double> routeLatArray,
                 List<Double> routeLngArray, User leader){
        this.groupDescription = groupDescription;
        this.routeLatArray = routeLatArray;
        this.routeLngArray = routeLngArray;
        this.leader = leader;
    }

    // Check if full data
    // -------------------------------------------------------------------------------------------
    // Server often replies with stub objects instead of full data.
    // If server sends back just an ID then it's a stub; otherwise you have full data about
    // *this* object. Objects it refers to, such as other users or groups, may not be filled in
    // (and hence those will have hasFullData set to false for them).
    public Boolean hasFullData() {
        return hasFullData;
    }
    public void setHasFullData(Boolean hasFullData) {
        this.hasFullData = hasFullData;
    }

    // Basic User Data
    // -------------------------------------------------------------------------------------------
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getHref() {
        return href;
    }
    public void setHref(String href) {
        this.href = href;
    }

    public String getGroupDescription() {
        return this.groupDescription;
    }

    public void setGroupDescription(String name) {
        this.groupDescription = name;
    }

    public List<Double> getRouteLatArray() {
        return routeLatArray;
    }

    public void setRouteLatArray(List<Double> routeLatArray) {
        this.routeLatArray = routeLatArray;
    }

    public List<Double> getRouteLngArray() {
        return routeLngArray;
    }

    public void setRouteLngArray(List<Double> routeLngArray) {
        this.routeLngArray = routeLngArray;
    }

    public User getLeader() {
        return leader;
    }

    public void setLeader(User leader) {
        this.leader = leader;
    }

    public List<User> getMemberUsers() {
        return memberUsers;
    }

    public void setMemberUsers(List<User> memberUsers) {
        this.memberUsers = memberUsers;
    }

    @Override
    public String toString() {
        return "Group{" +
                "id=" + id +
                ", groupDescription='" + groupDescription + '\'' +
                ", routeLatArray='" + routeLatArray + '\'' +
                ", routeLngArray='" + routeLngArray + '\'' +
                // ", currentPoints=" + currentPoints +
                //", totalPointsEarned=" + totalPointsEarned +
                ", leader=" + leader +
                ", memberUsers=" + memberUsers +
                ", hasFullData=" + hasFullData +
                ", href='" + href + '\'' +
                '}';
    }
}

