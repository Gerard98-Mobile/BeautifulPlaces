package com.example.beautifulplaces.viewmodels;

import androidx.lifecycle.ViewModel;

import com.example.beautifulplaces.adapters.ImageListAdapter;
import com.example.beautifulplaces.models.MineImage;
import com.example.beautifulplaces.repositories.UserData;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ImageListFragmentViewModel extends ViewModel {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference ref = db.collection("Images");

    public FirestoreRecyclerOptions<MineImage> getOptionsForAdapter(){

        FirebaseUser user = UserData.getUser();

        Query query = ref.whereEqualTo("userID",user.getUid());

        return new FirestoreRecyclerOptions.Builder<MineImage>()
                .setQuery(query, MineImage.class)
                .build();
    }
}
