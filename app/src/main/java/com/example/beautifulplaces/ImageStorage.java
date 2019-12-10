package com.example.beautifulplaces;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.core.FirestoreClient;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firestore.v1.WriteResult;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ImageStorage {

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


    public void insertNewImage(MineImage image, Context context){

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
                Log.d("deleting_file_storage", "File: " + path + "  Deleted Succesfully!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("deleting_file_storage", "Deleting file: "+path + " failed");
            }
        });
    }

}
