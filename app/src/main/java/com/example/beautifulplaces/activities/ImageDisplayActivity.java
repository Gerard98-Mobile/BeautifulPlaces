package com.example.beautifulplaces.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.beautifulplaces.HomeActivity;
import com.example.beautifulplaces.R;
import com.example.beautifulplaces.viewmodels.ImageDisplayActivityViewModel;


public class ImageDisplayActivity extends AppCompatActivity {

    public static final String KEY_EXTRA_IMAGE_PATH = "key_extra_image_path";
    public static final String KEY_EXTRA_IMAGE_LATITUDE = "key_extra_image_latitude";
    public static final String KEY_EXTRA_IMAGE_LONGITUDE = "key_extra_image_longitude";
    public static final String KEY_EXTRA_BUNDLE_IMAGE = "key_extra_bundle_image";

    public static final double DEFAULTVALUE = 999;

    private ImageView imageView;
    private Button showOnMap;
    private double latitude;
    private double longitude;

    private ImageDisplayActivityViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_image);

        imageView = findViewById(R.id.display_image_imageView);
        showOnMap = findViewById(R.id.show_on_map);

        viewModel = ViewModelProvider.AndroidViewModelFactory
                .getInstance(this.getApplication())
                .create(ImageDisplayActivityViewModel.class);

        latitude = getIntent().getDoubleExtra(KEY_EXTRA_IMAGE_LATITUDE, DEFAULTVALUE);
        longitude = getIntent().getDoubleExtra(KEY_EXTRA_IMAGE_LONGITUDE, DEFAULTVALUE);
        if(latitude == DEFAULTVALUE || longitude == DEFAULTVALUE){
            showOnMap.setEnabled(false);
            showOnMap.setVisibility(View.INVISIBLE);
        }

        showOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ImageDisplayActivity.this, HomeActivity.class);
                Bundle bundle = new Bundle();
                bundle.putDouble(KEY_EXTRA_IMAGE_LATITUDE, latitude);
                bundle.putDouble(KEY_EXTRA_IMAGE_LONGITUDE, longitude);
                intent.putExtra(KEY_EXTRA_BUNDLE_IMAGE, bundle);
                startActivity(intent);

            }
        });

        String path = getIntent().getStringExtra(KEY_EXTRA_IMAGE_PATH);

        viewModel.getImageUri().observe(this, imageUriObserver);
        viewModel.loadImage(path);

    }

    private Observer<Uri> imageUriObserver = new Observer<Uri>() {
        @Override
        public void onChanged(Uri uri) {
            if(uri != null) {
                Glide.with(getBaseContext()).load(uri).into(imageView);
            }
            else{
                Glide.with(getBaseContext()).load(R.mipmap.ic_image).into(imageView);
            }
        }
    };

}
