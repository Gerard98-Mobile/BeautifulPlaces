package com.example.beautifulplaces;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsFragment extends Fragment {

    private static SettingsFragment settingsFragment = new SettingsFragment();

    public static SettingsFragment getInstance(){
        return settingsFragment;
    }


    private ImageView imageView;
    private TextView name, email;
    private Button signOut;

    private FirebaseUser user;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);

        this.user = UserData.getUser();


        imageView = v.findViewById(R.id.profile_image);
        name = v.findViewById(R.id.textName);
        email = v.findViewById(R.id.textEmail);
        signOut = v.findViewById(R.id.signOut);
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    // ...
                    case R.id.signOut:
                        signOut();
                        break;
                    // ...
                }
            }
        });


        if(user != null){
            String personName = user.getDisplayName();
            String personEmail = user.getEmail();
            Uri personPhoto = user.getPhotoUrl();

            imageView.setImageURI(personPhoto);
            name.setText(personName);
            email.setText(personEmail);

            Glide.with(this).load(String.valueOf(personPhoto)).into(imageView);


        }



        return v;

    }

    private void signOut() {

        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this.getActivity().getApplicationContext(),LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }


}

