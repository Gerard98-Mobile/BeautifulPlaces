package com.example.beautifulplaces;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.IOException;

public class HomeActivity extends AppCompatActivity {

    public static final int IMAGE_LIST_FRAGMENT = 1;
    public static final int CAMERA_FRAGMENT = 2;
    public static final int MAP_FRAGMENT = 3;
    public static final int SETTINGS_FRAGMENT = 4;

    public static final String KEY_EXTRA_PARTICULAR_FRAGMENT = "key_extra_particular_fragment";
    public static final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 1;


    private Fragment actuallyFragment;
    private int actuallyFragmentIndex = 1;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        getPermissionLocalization();
        bottomNavigationView = findViewById(R.id.navigation);

        Fragment firstFragment = HomeFragment.getInstance();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame, firstFragment);
        transaction.commit();

        //ImageStorage.getInstance().initImages();
        try {
            int particular_fragment = getIntent().getExtras().getInt(KEY_EXTRA_PARTICULAR_FRAGMENT);
            setNewFragment(particular_fragment);
            bottomNavigationView.setSelectedItemId(R.id.menu_map);
        }
        catch (NullPointerException ex){

        }


        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                actuallyFragment = null;

                switch (menuItem.getItemId()){
                    case R.id.menu_home:
                        setNewFragment(IMAGE_LIST_FRAGMENT);
                        break;
                    case R.id.menu_camera:
                        if(getPermissionLocalization()){
                            setNewFragment(CAMERA_FRAGMENT);
                        }
                        else{
                            setNewFragment(IMAGE_LIST_FRAGMENT);
                            bottomNavigationView.setSelectedItemId(R.id.menu_home);
                        }
                        break;
                    case R.id.menu_map:
                        setNewFragment(MAP_FRAGMENT);
                        break;
                    case R.id.menu_settings:
                        setNewFragment(SETTINGS_FRAGMENT);
                        break;

                }

                return true;
            }
        });

    }

    public void setNewFragment(int newFragmentIndex){
        actuallyFragmentIndex = newFragmentIndex;
        switch (actuallyFragmentIndex){
            case IMAGE_LIST_FRAGMENT:
                actuallyFragment = ImageListFragment.getInstance();
                break;
            case CAMERA_FRAGMENT:
                actuallyFragment = CameraFragment.getInstance();
                break;
            case MAP_FRAGMENT:
                actuallyFragment = MapFragment.getInstance();
                break;
            case SETTINGS_FRAGMENT:
                actuallyFragment = SettingsFragment.getInstance();
                break;
            default:
                actuallyFragment = ImageListFragment.getInstance();
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame, actuallyFragment);
        transaction.commit();
    }

    protected void changeSelectedItem(int itemID){
        bottomNavigationView.setSelectedItemId(itemID);
    }


    public boolean getPermissionLocalization(){

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // permission denied

                new AlertDialog.Builder(this)
                        .setTitle(R.string.localization_permission_denied_title)
                        .setMessage(R.string.localization_permission_denied_message)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(HomeActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_ACCESS_FINE_LOCATION);

                                setNewFragment(IMAGE_LIST_FRAGMENT);
                                bottomNavigationView.setSelectedItemId(R.id.menu_home);
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                setNewFragment(IMAGE_LIST_FRAGMENT);
                                bottomNavigationView.setSelectedItemId(R.id.menu_home);
                            }
                        })
                        .show();

            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_ACCESS_FINE_LOCATION);

            }
        } else {
            return true;
        }

        return false;

    }




}
