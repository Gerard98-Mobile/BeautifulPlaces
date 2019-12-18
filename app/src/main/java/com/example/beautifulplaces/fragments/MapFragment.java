package com.example.beautifulplaces.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.beautifulplaces.activities.ImageDisplayActivity;
import com.example.beautifulplaces.models.MineMarker;
import com.example.beautifulplaces.repositories.ImageStorage;
import com.example.beautifulplaces.R;
import com.example.beautifulplaces.viewmodels.MapFragmentViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "map_fragment";

    private GoogleMap mMap;

    private MapFragmentViewModel viewModel;

    private Observer<MineMarker> mineMarkerObserver = new Observer<MineMarker>() {
        @Override
        public void onChanged(MineMarker mineMarker) {
            Marker marker = mMap.addMarker(new MarkerOptions().position(mineMarker.getPointOnMap())
                    .title(mineMarker.getName()));
            if(mineMarker.getUserID() != null) {
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
            }
            marker.setTag(mineMarker.getPath());
        }
    };

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
            Log.d(TAG,"There were problem with loading map  " + ex.getStackTrace());
        }

        viewModel = ViewModelProvider.AndroidViewModelFactory
                .getInstance(getActivity().getApplication())
                .create(MapFragmentViewModel.class);

        viewModel.setCameraPosition(this.getArguments());

        viewModel.getActuallyMarker().observe(getViewLifecycleOwner(),mineMarkerObserver);

        viewModel.loadImages();

        return v;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                String path = (String) marker.getTag();

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

        if(viewModel.getCameraPosition() != null) {
            mMap.moveCamera(CameraUpdateFactory.zoomTo(10.0f));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(viewModel.getCameraPosition()));
        }
    }

    public void setCameraPosition(LatLng pointOnMap){
        viewModel.setCameraPosition(pointOnMap);
    }

}
