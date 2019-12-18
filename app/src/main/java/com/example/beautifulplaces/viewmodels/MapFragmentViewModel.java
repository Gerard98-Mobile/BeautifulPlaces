package com.example.beautifulplaces.viewmodels;

import android.os.Bundle;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.beautifulplaces.models.MineImage;
import com.example.beautifulplaces.models.MineMarker;
import com.example.beautifulplaces.repositories.UserData;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static com.example.beautifulplaces.activities.ImageDisplayActivity.KEY_EXTRA_IMAGE_LATITUDE;
import static com.example.beautifulplaces.activities.ImageDisplayActivity.KEY_EXTRA_IMAGE_LONGITUDE;


public class MapFragmentViewModel extends ViewModel {

    public static final double COORDINATE_OFFSET = 0.00005f;

    private List<MineImage> images;

    private MutableLiveData<MineMarker> actuallyMarker = new MutableLiveData<>();
    public MutableLiveData<MineMarker> getActuallyMarker() {
        return actuallyMarker;
    }

    private List<LatLng> pointsOnTheMap;

    private LatLng cameraPosition;
    public LatLng getCameraPosition() {
        return cameraPosition;
    }

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference ref = db.collection("Images");

    private String userID;


    public void loadImages(){
        ref.get().addOnSuccessListener(m -> {
            images = m.toObjects(MineImage.class);
            setMarkers();
        });
    }


    public void setMarkers(){

        FirebaseUser user = UserData.getUser();
        if (user != null) {
            userID = user.getUid();
        }

        pointsOnTheMap = new LinkedList<>();

        for(MineImage image : images){
            GeoPoint point = image.getLocation();
            LatLng pointOnMap = new LatLng(point.getLatitude(),point.getLongitude());
            pointOnMap = offsetLocationIfAlreadyExist(pointOnMap);

            pointsOnTheMap.add(pointOnMap);

            MineMarker myMarker = new MineMarker(pointOnMap,image.getName(), image.getPath());
            if(this.userID.equals(image.getUserID())) {
                myMarker.setUserID(this.userID);
            }
            actuallyMarker.setValue(myMarker);

        }

    }

    public LatLng offsetLocationIfAlreadyExist(LatLng point){
        while(mapAlreadyHasMarkerForLocation(point)){
            point = new LatLng(point.latitude + COORDINATE_OFFSET, point.longitude);
        }
        return point;
    }

    public boolean mapAlreadyHasMarkerForLocation(LatLng point){
        Iterator<LatLng> it = pointsOnTheMap.iterator();
        while (it.hasNext()){
            LatLng itPoint = it.next();
            if(itPoint.longitude == point.longitude && itPoint.latitude == point.latitude){
                return true;
            }
        }
        return false;
    }

    public void setCameraPosition(LatLng pointOnMap){
        cameraPosition = pointOnMap;
    }

    public void setCameraPosition(Bundle bundle){
        if(bundle != null){
            double latitude = bundle.getDouble(KEY_EXTRA_IMAGE_LATITUDE);
            double longitude = bundle.getDouble(KEY_EXTRA_IMAGE_LONGITUDE);
            setCameraPosition(new LatLng(latitude,longitude));
        }
    }

}
