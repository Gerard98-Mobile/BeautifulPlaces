package com.example.beautifulplaces.models;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.firestore.GeoPoint;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@IgnoreExtraProperties
public class MineImage {

    private String path;
    private String date;
    private GeoPoint location;
    private String userID;
    private String name;

    public MineImage() {
    }

    public MineImage(String path, String date, double longitude, double latitude, String userID, String name) {
        this.path = path;
        this.date = date;
        location = new GeoPoint(latitude,longitude);
        this.userID = userID;
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDate() {
        return date;
    }

    public void setActuallyDate() {
        this.date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if(name == null || name.equals("")){
            this.name = "Image";
        }
        else{
            this.name = name;
        }
    }
}
