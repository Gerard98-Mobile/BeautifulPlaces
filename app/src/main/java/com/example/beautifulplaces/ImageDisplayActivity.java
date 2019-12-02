package com.example.beautifulplaces;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ImageDisplayActivity extends AppCompatActivity {

    public static final String KEY_EXTRA_IMAGE_PATH = "key_extra_image_path";
    public static final String KEY_EXTRA_IMAGE_LATITUDE = "key_extra_image_latitude";
    public static final String KEY_EXTRA_IMAGE_LONGITUDE = "longitude";

    public static final double DEFAULTVALUE = 999;

    private ImageView imageView;
    private Button showOnMap;
    private double latitude;
    private double longitude;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_image);

        imageView = findViewById(R.id.display_image_imageView);
        showOnMap = findViewById(R.id.show_on_map);

        latitude = getIntent().getDoubleExtra(KEY_EXTRA_IMAGE_LATITUDE, DEFAULTVALUE);
        longitude = getIntent().getDoubleExtra(KEY_EXTRA_IMAGE_LONGITUDE, DEFAULTVALUE);
        if(latitude != DEFAULTVALUE && longitude != DEFAULTVALUE){
            ((MapFragment) MapFragment.getInstance()).setCameraPosition(new GeoPoint(latitude,longitude));
        }
        else{
            showOnMap.setEnabled(false);
            showOnMap.setVisibility(View.INVISIBLE);
        }

        showOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ImageDisplayActivity.this, HomeActivity.class);
                intent.putExtra(HomeActivity.KEY_EXTRA_PARTICULAR_FRAGMENT, HomeActivity.MAP_FRAGMENT);
                startActivity(intent);

            }
        });

        String path = getIntent().getStringExtra(KEY_EXTRA_IMAGE_PATH);

        StorageReference ref = FirebaseStorage.getInstance().getReference(path);

        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getBaseContext()).load(uri).into(imageView);
            }
        });

    }


}
