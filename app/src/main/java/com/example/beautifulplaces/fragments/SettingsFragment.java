package com.example.beautifulplaces.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.beautifulplaces.HomeActivity;
import com.example.beautifulplaces.R;
import com.example.beautifulplaces.repositories.UserData;
import com.example.beautifulplaces.auth.LoginActivity;
import com.example.beautifulplaces.viewmodels.SettingsFragmentViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsFragment extends Fragment {

    private ImageView imageView;
    private TextView name, email;
    private Button signOut;

    private SettingsFragmentViewModel viewModel;

    private Observer<FirebaseUser> userObserver = new Observer<FirebaseUser>() {
        @Override
        public void onChanged(FirebaseUser user) {
            if(user != null){
                String personName = user.getDisplayName();
                String personEmail = user.getEmail();
                Uri personPhoto = user.getPhotoUrl();

                imageView.setImageURI(personPhoto);
                name.setText(personName);
                email.setText(personEmail);

                Glide.with(getContext()).load(String.valueOf(personPhoto)).into(imageView);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);

        imageView = v.findViewById(R.id.profile_image);
        name = v.findViewById(R.id.textName);
        email = v.findViewById(R.id.textEmail);
        signOut = v.findViewById(R.id.signOut);

        viewModel = ViewModelProvider.AndroidViewModelFactory
                .getInstance(getActivity().getApplication())
                .create(SettingsFragmentViewModel.class);

        viewModel.getUser().observe(getViewLifecycleOwner(),userObserver);

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.signOut:
                        signOut();
                        break;
                }
            }
        });

        viewModel.loadUser();

        return v;

    }

    private void signOut() {
        viewModel.signOut();
        Intent intent = new Intent(this.getActivity().getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(HomeActivity.KEY_EXTRA_FRAGMENT_INDEX, HomeActivity.SETTINGS_FRAGMENT);
    }
}

