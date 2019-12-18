package com.example.beautifulplaces;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.beautifulplaces.activities.ImageDisplayActivity;
import com.example.beautifulplaces.fragments.CameraFragment;
import com.example.beautifulplaces.fragments.ImageListFragment;
import com.example.beautifulplaces.fragments.MapFragment;
import com.example.beautifulplaces.fragments.SettingsFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import static com.example.beautifulplaces.activities.ImageDisplayActivity.KEY_EXTRA_IMAGE_LATITUDE;
import static com.example.beautifulplaces.activities.ImageDisplayActivity.KEY_EXTRA_IMAGE_LONGITUDE;


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

        try {
            int particularFragment = getIntent().getExtras().getInt(KEY_EXTRA_PARTICULAR_FRAGMENT);


            double latitude = getIntent().getDoubleExtra(KEY_EXTRA_IMAGE_LATITUDE, ImageDisplayActivity.DEFAULTVALUE);
            double longitude = getIntent().getDoubleExtra(ImageDisplayActivity.KEY_EXTRA_IMAGE_LONGITUDE, ImageDisplayActivity.DEFAULTVALUE);
            if(latitude != ImageDisplayActivity.DEFAULTVALUE && longitude != ImageDisplayActivity.DEFAULTVALUE){
                Bundle bundle = new Bundle();
                bundle.putDouble(KEY_EXTRA_IMAGE_LATITUDE, latitude);
                bundle.putDouble(KEY_EXTRA_IMAGE_LONGITUDE, longitude);

                MapFragment mapFragment = new MapFragment();
                mapFragment.setArguments(bundle);

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.frame, mapFragment);
                transaction.commit();
                bottomNavigationView.setSelectedItemId(R.id.menu_map);

            }
        }
        catch (NullPointerException ex){
            Fragment firstFragment = new ImageListFragment();

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame, firstFragment);
            transaction.commit();
            Log.d("home_activity", "There was no extra particular_fragment");
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


    /**
     * changing displayed fragment
     * @param newFragmentIndex
     */

    public void setNewFragment(int newFragmentIndex){
        actuallyFragmentIndex = newFragmentIndex;
        switch (actuallyFragmentIndex){
            case IMAGE_LIST_FRAGMENT:
                actuallyFragment = new ImageListFragment();
                break;
            case CAMERA_FRAGMENT:
                actuallyFragment = new CameraFragment();
                break;
            case MAP_FRAGMENT:
                actuallyFragment = new MapFragment();
                break;
            case SETTINGS_FRAGMENT:
                actuallyFragment = new SettingsFragment();
                break;
            default:
                actuallyFragment = new ImageListFragment();
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame, actuallyFragment);
        transaction.commit();
    }


    public void changeSelectedItem(int itemID){
        bottomNavigationView.setSelectedItemId(itemID);
    }


    /**
     * request localization permission, if denied showing AlertDialog
     * that user have to accept localization to use CameraFragment
     *
     * @return false if user denied accepting permission, else function return true
     */

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
