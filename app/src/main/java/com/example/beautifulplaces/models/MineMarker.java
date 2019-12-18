package com.example.beautifulplaces.models;

import com.google.android.gms.maps.model.LatLng;

public class MineMarker {

    private String userID;
    private LatLng pointOnMap;
    private String name;
    private String path;


    public MineMarker(String userID, LatLng pointOnMap, String name, String path) {
        this.userID = userID;
        this.pointOnMap = pointOnMap;
        this.name = name;
    }

    public MineMarker(LatLng pointOnMap, String name, String path) {
        this.pointOnMap = pointOnMap;
        this.name = name;
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public LatLng getPointOnMap() {
        return pointOnMap;
    }

    public void setPointOnMap(LatLng pointOnMap) {
        this.pointOnMap = pointOnMap;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
