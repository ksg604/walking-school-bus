package com.example.walkingschoolbus.model;

import android.location.Location;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Class for holding GPSLocation
 * Needed for Google Maps
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GpsLocation {

    private Double lat;
    private Double lng;
    private String timestamp;

    public Double getLat() {
        return lat;
    }
    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }
    public void setLng(Double lng) {
        this.lng = lng;
    }

    public String getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "GpsLocation{" +
                "lat=" + lat +
                ", lng=" + lng +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
