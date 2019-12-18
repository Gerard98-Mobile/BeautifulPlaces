package com.example.beautifulplaces.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresPermission;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.beautifulplaces.HomeActivity;
import com.example.beautifulplaces.R;
import com.example.beautifulplaces.viewmodels.CameraFragmentViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.firebase.firestore.GeoPoint;

import java.io.File;



public class CameraFragment extends Fragment{

    private static final String TAG = "camera_fragment";
    private static final int RESULT_OK = -1;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private Context context;

    private ImageView imageView;
    private Button addImageButton;
    private EditText nameImageEditText;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;

    private CameraFragmentViewModel viewModel;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_camera, parent, false);

        imageView = v.findViewById(R.id.image_from_camera);
        addImageButton = v.findViewById(R.id.add_image_button);
        nameImageEditText = v.findViewById(R.id.image_name);

        context = v.getContext();

        viewModel = ViewModelProvider.AndroidViewModelFactory
                .getInstance(getActivity().getApplication())
                .create(CameraFragmentViewModel.class);

        dispatchTakePictureIntent();
        viewModel.failure.observe(getViewLifecycleOwner(),failureObserver);
        viewModel.success.observe(getViewLifecycleOwner(),successObserver);
        viewModel.progress.observe(getViewLifecycleOwner(),uploadingObserver);
        viewModel.imageFile.observe(getViewLifecycleOwner(),creatingFileObserver);

        setLocationListener();
        return v;
    }

    /**
     * Observers for uploading file to storage ( Bad Solution !!! )
     */

    private Observer<Boolean> failureObserver = new Observer<Boolean>() {
        @Override
        public void onChanged(Boolean aBoolean) {
            Log.d(TAG, "There was the problem with uploading file");
            Toast.makeText(context, "Image not uploaded!", Toast.LENGTH_SHORT).show();
        }
    };

    private Observer<Boolean> successObserver = new Observer<Boolean>() {
        @Override
        public void onChanged(Boolean aBoolean) {
            Log.d(TAG, "File uploaded successfully");
            Toast.makeText(context, "Image uploaded!", Toast.LENGTH_SHORT).show();

        }
    };

    private Observer<Boolean> uploadingObserver = new Observer<Boolean>() {
        @Override
        public void onChanged(Boolean aBoolean) {
            Log.d(TAG, "File is uploading");
            Toast.makeText(context, "Uploading your Image...", Toast.LENGTH_SHORT).show();
        }
    };

    private Observer<File> creatingFileObserver = new Observer<File>() {
        @Override
        public void onChanged(File file) {
            if (file != null) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                Uri photoURI = FileProvider.getUriForFile(context,
                        "com.example.android.fileprovider",
                        file);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    };


    private PackageManager getPackageManager(){
        return context.getPackageManager();
    }

    /**
     * Setting LocationListener, in callback location is assigning to mineImage
     */

    private void setLocationListener(){
        // location
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(this.locationRequest);
        this.locationSettingsRequest = builder.build();

        this.locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();

                viewModel.mineImage.setLocation(new GeoPoint(location.getLatitude(),location.getLongitude()));
            }
        };

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext().getApplicationContext());

        try {
            mFusedLocationClient.requestLocationUpdates(this.locationRequest,
                    this.locationCallback, Looper.myLooper());
        }catch (SecurityException ex){
            Log.d(TAG,ex.getMessage());
        }
    }



    /**
     *  Checking that file is created and if is starting camera activity
     */

    private void dispatchTakePictureIntent(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            viewModel.createImageFile(storageDir);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Uri uri = Uri.fromFile(viewModel.imageFile.getValue());
            imageView.setImageURI(uri);
            addImageButton.setOnClickListener(m -> {
                if(viewModel.uploadFileToDatabase(nameImageEditText.getText().toString())){
                    changeFragmentToHome();
                }
                else{
                    Toast.makeText(getContext(), R.string.location_problem, Toast.LENGTH_SHORT).show();
                    try {
                        mFusedLocationClient.requestLocationUpdates(this.locationRequest,
                                this.locationCallback, Looper.myLooper());
                    }catch (SecurityException ex){
                        Log.d(TAG,ex.getMessage());
                    }
                }
            });
        }
        else{
            changeFragmentToHome();
        }
    }

    public void changeFragmentToHome(){
        ((HomeActivity) getActivity() ).setNewFragment(HomeActivity.IMAGE_LIST_FRAGMENT);
        ((HomeActivity) getActivity() ).changeSelectedItem(R.id.menu_home);
    }


    @Override
    public void onStop() {
        super.onStop();
        this.mFusedLocationClient.removeLocationUpdates(this.locationCallback);
    }


}
