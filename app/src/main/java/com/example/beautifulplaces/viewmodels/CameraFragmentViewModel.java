package com.example.beautifulplaces.viewmodels;


import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.beautifulplaces.SingleLiveEvent;
import com.example.beautifulplaces.repositories.ImageStorage;
import com.example.beautifulplaces.models.MineImage;
import com.example.beautifulplaces.repositories.UserData;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class CameraFragmentViewModel extends ViewModel {

    private static final String FILENAME = "images/";

    public SingleLiveEvent<Boolean> failure = new SingleLiveEvent<>();
    public SingleLiveEvent<Boolean> success = new SingleLiveEvent<>();
    public SingleLiveEvent<Boolean> progress = new SingleLiveEvent<>();

    public MineImage mineImage = new MineImage();
    public SingleLiveEvent<File> imageFile = new SingleLiveEvent<>();


    /**
     * Upload file to Firebase storage
     */

    public boolean uploadFileToDatabase(String imageName){
        if(mineImage.getLocation() != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            // Create a storage reference from our app
            StorageReference storageRef = storage.getReference();

            Uri file = Uri.fromFile(imageFile.getValue());
            String path = FILENAME + file.getLastPathSegment();
            StorageReference riversRef = storageRef.child(path);
            mineImage.setPath(path);
            UploadTask uploadTask = riversRef.putFile(file);

            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {

                    failure.setValue(true);
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    success.setValue(true);
                    uploadImageToFireStore(imageName);
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {

                    progress.setValue(true);
                }
            });
            return true;
        }
        return false;
    }

    /**
     *  Uploading a MineImage object into firestore
     */

    private void uploadImageToFireStore(String imageName){

        mineImage.setActuallyDate();
        mineImage.setName(imageName);
        FirebaseUser user = UserData.getUser();
        if(user!= null){
            mineImage.setUserID(user.getUid());
            ImageStorage.getInstance().insertNewImage(mineImage);
        }

    }

    /**
     * Creating a file for a photo
     */

    public void createImageFile(File storageDir){

        String timeStamp = SimpleDateFormat.getDateInstance().format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        try {
            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
            imageFile.setValue(image);
        }catch (IOException ex){
            imageFile.setValue(null);
            Log.d("creating_file",ex.getMessage());
        }

    }



}
