package com.example.beautifulplaces.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beautifulplaces.models.MineImage;
import com.example.beautifulplaces.R;
import com.example.beautifulplaces.repositories.UserData;
import com.example.beautifulplaces.adapters.ImageListAdapter;
import com.example.beautifulplaces.viewmodels.CameraFragmentViewModel;
import com.example.beautifulplaces.viewmodels.ImageListFragmentViewModel;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


public class ImageListFragment extends Fragment {


    private ImageListAdapter adapter;
    private ImageListFragmentViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home,container,false);

        viewModel = ViewModelProvider.AndroidViewModelFactory
                .getInstance(getActivity().getApplication())
                .create(ImageListFragmentViewModel.class);

        setUpRecyclerView(view);
        return view;

    }

    private void setUpRecyclerView(View view) {

        adapter = new ImageListAdapter(viewModel.getOptionsForAdapter(), getContext());

        RecyclerView recyclerView = view.findViewById(R.id.image_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
