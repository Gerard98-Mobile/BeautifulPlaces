package com.example.beautifulplaces;

import android.Manifest;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static MapFragment mapFragment = new MapFragment();
    public static MapFragment getInstance(){
        return mapFragment;
    }

    public static final double COORDINATE_OFFSET = 0.00005f;

    private List<MineImage> images;
    private GoogleMap mMap;
    private LatLng cameraPosition;

    private List<LatLng> pointsOnTheMap = new LinkedList<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference ref = db.collection("Images");

    private String userID;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map, container, false);
        ImageStorage.getInstance().initImages();
        try {
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.map);

            mapFragment.getMapAsync(this);
        }catch (NullPointerException ex){
            Log.d("loading_map","There were problem with loading map  " + ex.getStackTrace());
        }

        ref.get().addOnSuccessListener(m -> {
            images = m.toObjects(MineImage.class);
            setMarkers();
        });

        return v;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                String path = (String) marker.getTag();
                //marker.getTag();

                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.see_image)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(getActivity(), ImageDisplayActivity.class);
                                intent.putExtra(ImageDisplayActivity.KEY_EXTRA_IMAGE_PATH, path);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(R.string.no, null)
                        .show();




                return false;
            }
        });

        //images = ImageStorage.getInstance().getImages();


        if(cameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.zoomTo(6.0f));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(cameraPosition));
        }
    }

    public void setMarkers(){

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this.getContext());
        if (acct != null) {
            userID = acct.getId();
        }

        pointsOnTheMap.clear();

        for(MineImage image : images){
            GeoPoint point = image.getLocation();
            LatLng pointOnMap = new LatLng(point.getLatitude(),point.getLongitude());
            pointOnMap = offsetLocationIfAlreadyExist(pointOnMap);
            pointsOnTheMap.add(pointOnMap);
            Marker marker = mMap.addMarker(new MarkerOptions().position(pointOnMap)
                    .title(image.getName()));
            if(this.userID.equals(image.getUserID())) {
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
            }
            marker.setTag(image.getPath());

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

    public void setCameraPosition(GeoPoint point){
        LatLng pointOnMap = new LatLng(point.getLatitude(),point.getLongitude());
        cameraPosition = pointOnMap;
    }

}
