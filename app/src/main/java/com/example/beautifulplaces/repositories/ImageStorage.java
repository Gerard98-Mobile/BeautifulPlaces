package com.example.beautifulplaces.repositories;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.beautifulplaces.models.MineImage;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.LinkedList;
import java.util.List;

public class ImageStorage {

    private static final String TAG = "image_storage";

    private static final ImageStorage instance = new ImageStorage();
    private List<MineImage> images;
    private String userID;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference ref = db.collection("Images");

    private ImageStorage() {
        images = new LinkedList<>();
    }

    public static ImageStorage getInstance() {
        return instance;
    }

    public void initImages() {
        ref.get().addOnSuccessListener(m -> {
            images = m.toObjects(MineImage.class);
        });

    }

    public List<MineImage> getImages(){
        return images;
    }


    public void insertNewImage(MineImage image){

        ref.add(image).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                deleteImageFile(image.getPath());

            }
        }).addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                deleteImageFile(image.getPath());
            }
        });
    }

    public void deleteImageFile(String path){
        StorageReference ref = FirebaseStorage.getInstance().getReference();
        StorageReference fileRef = ref.child(path);
        fileRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "File: " + path + "  Deleted Succesfully!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Deleting file: "+path + " failed");
            }
        });
    }

}
