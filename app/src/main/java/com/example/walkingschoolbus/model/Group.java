package com.example.walkingschoolbus.model;

import android.location.Location;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Store information about the walking groups.
 *
 * WARNING: INCOMPLETE! Server returns more information than this.
 * This is just to be a placeholder and inspire you how to do it.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Group {

    private long id;
    private String Name;
    private Location location;
    private Boolean hasFullData;
    private String href;
    private Location meetingPlace;

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



    public Group(long id, String name, Location location) {
        this.id = id;
        this.Name = name;
        this.location =location;
    }
    public Group(String name, Location location, Location meetingPlace) {
        Name = name;
        this.location = location;
        this.meetingPlace = meetingPlace;
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
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public String getHref() {
        return href;
    }
    public void setHref(String href) {
        this.href = href;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Location getMeetingPlace() {
        return meetingPlace;
    }

    public void setMeetingPlace(Location meetingPlace) {
        this.meetingPlace = meetingPlace;
    }
}

