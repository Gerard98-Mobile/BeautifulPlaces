package com.example.beautifulplaces.viewmodels;

import android.net.Uri;

import androidx.lifecycle.ViewModel;

import com.example.beautifulplaces.SingleLiveEvent;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ImageDisplayActivityViewModel extends ViewModel {


    private SingleLiveEvent<Uri> imageUri = new SingleLiveEvent<>();
    public SingleLiveEvent<Uri> getImageUri(){ return imageUri; }


    public void loadImage(String path){
        StorageReference ref = FirebaseStorage.getInstance().getReference(path);
        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                imageUri.setValue(uri);
            }
        });
    }
}
