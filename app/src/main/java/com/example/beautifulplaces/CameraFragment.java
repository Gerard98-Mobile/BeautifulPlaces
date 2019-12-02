package com.example.beautifulplaces;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CameraFragment extends Fragment{

    private static final String FILENAME = "images/";
    private static final int RESULT_OK = -1;

    private static CameraFragment cameraFragment = new CameraFragment();
    public static CameraFragment getInstance(){
        return cameraFragment;
    }

    private Context context;
    private String currentPhotoPath;

    private File imageFile;
    private String imageName;
    private MineImage mineImage;

    private ImageView imageView;
    private Button addImageButton;
    private EditText nameImageEditText;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;

    private FusedLocationProviderClient fusedLocationClient;

    BottomNavigationView bottomNavigationView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_camera, parent, false);

        imageView = v.findViewById(R.id.image_from_camera);
        addImageButton = v.findViewById(R.id.add_image_button);
        nameImageEditText = v.findViewById(R.id.image_name);

        context = v.getContext();
        dispatchTakePictureIntent();
        mineImage = new MineImage();

        this.bottomNavigationView = v.findViewById(R.id.navigation);

        setLocationListener();
        return v;
    }

    private PackageManager getPackageManager(){
        return context.getPackageManager();
    }


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

                mineImage.setLocation(new GeoPoint(location.getLatitude(),location.getLongitude()));
            }
        };

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext().getApplicationContext());

        try {
            mFusedLocationClient.requestLocationUpdates(this.locationRequest,
                    this.locationCallback, Looper.myLooper());
        }catch (SecurityException ex){
            Log.d("location",ex.getMessage());
        }

    }

    public static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.d("create_file","Creating file failded! " + ex.getMessage());
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(context,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //setPic();
            Uri uri = Uri.fromFile(imageFile);
            imageView.setImageURI(uri);
            addImageButton.setOnClickListener(m -> {
                if(uploadFileToDatabase()){
                    //Intent intent = new Intent(getActivity(), HomeActivity.class);
                    //startActivity(intent);
                    changeFragmentToHome();

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

    // Creating a file for a photo

    private File createImageFile() throws IOException{

        // Create an mineImage file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        imageFile = image;
        this.imageName = imageFileName;
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;

    }


    public boolean uploadFileToDatabase(){
        if(mineImage.getLocation() != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            // Create a storage reference from our app
            StorageReference storageRef = storage.getReference();

            Uri file = Uri.fromFile(imageFile);
            String path = FILENAME + file.getLastPathSegment();
            StorageReference riversRef = storageRef.child(path);
            mineImage.setPath(path);
            UploadTask uploadTask = riversRef.putFile(file);

            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Log.d("upload_file", "There was the problem with uploading file");
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...
                    uploadImageInformationToFireStore();
                    Log.d("upload_file", "File uploaded successfully");
                }
            });
            return true;
        }
        else{
            Toast.makeText(getContext(), R.string.location_problem, Toast.LENGTH_SHORT).show();
            return false;
        }

    }

    private void uploadImageInformationToFireStore(){

        mineImage.setActuallyDate();
        mineImage.setName(nameImageEditText.getText().toString());
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(context);
        if(acct!= null){
            mineImage.setUserID(acct.getId());
            ImageStorage.getInstance().insertNewImage(mineImage);
        }
        else{
            Toast.makeText(getContext(), "There is problem with getting data from your account! Uploading image failed! ", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        this.mFusedLocationClient.removeLocationUpdates(this.locationCallback);
    }

}
