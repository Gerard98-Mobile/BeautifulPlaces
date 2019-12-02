package com.example.beautifulplaces;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class ImageListFragment extends Fragment {

    private static ImageListFragment instance = new ImageListFragment();

    public static ImageListFragment getInstance(){
        return instance;
    }

    private ImageAdapter adapter;


    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference ref = db.collection("Images");


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home,container,false);

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this.getContext());
        if(acct != null){
            setUpRecyclerView(view, acct.getId());
        }
        return view;

    }

    private void setUpRecyclerView(View view, String userID) {
        //Query query = ref.orderBy("userID", Query.Direction.DESCENDING);
        //query.whereEqualTo("userID",userID);
        Query query = ref.whereEqualTo("userID",userID);


        FirestoreRecyclerOptions<MineImage> options = new FirestoreRecyclerOptions.Builder<MineImage>()
                .setQuery(query, MineImage.class)
                .build();

        adapter = new ImageAdapter(options);

        RecyclerView recyclerView = view.findViewById(R.id.image_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

    }


    public class ImageHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private GeoPoint pointOnMap;
        private String path;
        private TextView location;
        private TextView date;
        private ImageView imageView;
        private ImageButton deleteButton;
        private TextView imageNameTextView;

        public ImageHolder(View view){
            super(view);
            location = view.findViewById(R.id.location_image_fragment);
            date = view.findViewById(R.id.date_image_fragment);
            imageView = view.findViewById(R.id.image_image_fragment);
            deleteButton = view.findViewById(R.id.delete_button);
            imageNameTextView = view.findViewById(R.id.image_name_text);

            imageView.setOnClickListener(this::onClick);

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    new AlertDialog.Builder(getContext())
                            .setTitle(R.string.deleting_picture_alert)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    adapter.deleteItem(getAdapterPosition());
                                    ImageStorage.getInstance().deleteImageFile(path);
                                }
                            })
                            .setNegativeButton(R.string.no, null)
                            .show();
                }
            });
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), ImageDisplayActivity.class);
            intent.putExtra(ImageDisplayActivity.KEY_EXTRA_IMAGE_PATH, path);
            intent.putExtra(ImageDisplayActivity.KEY_EXTRA_IMAGE_LONGITUDE,pointOnMap.getLongitude());
            intent.putExtra(ImageDisplayActivity.KEY_EXTRA_IMAGE_LATITUDE,pointOnMap.getLatitude());
            startActivity(intent);
        }
    }



    public class ImageAdapter extends FirestoreRecyclerAdapter<MineImage, ImageHolder> {


        public ImageAdapter(FirestoreRecyclerOptions<MineImage> options) {
            super(options);
        }

        @NonNull
        @Override
        public ImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            View view = layoutInflater.inflate(R.layout.fragment_image, parent, false);

            return new ImageHolder(view);
        }

        @Override
        protected void onBindViewHolder(ImageHolder imageHolder, int i, MineImage image) {
            imageHolder.date.setText(image.getDate());
            imageHolder.path = image.getPath();
            imageHolder.pointOnMap = image.getLocation();
            String addresses = convertLocation(image.getLocation());
            imageHolder.location.setText(addresses);
            imageHolder.imageNameTextView.setText(image.getName());
            StorageReference ref = FirebaseStorage.getInstance().getReference(image.getPath());

            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    try {
                        Glide.with(getContext()).load(uri).into(imageHolder.imageView);
                    }
                    catch (NullPointerException ex){
                        Log.d("downloading_url","There is exception in downloading url! " + ex.getStackTrace());
                    }
                }
            });

        }

        public void deleteItem(int position){
            getSnapshots().getSnapshot(position).getReference().delete();
        }

    }

    private String convertLocation(GeoPoint point){
        try {
            Geocoder geocoder = new Geocoder(getActivity().getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(point.getLatitude(), point.getLongitude(), 1);
            if(addresses.isEmpty()){
                return "Latitude: " + point.getLatitude() + "\nLongitude: " + point.getLongitude();
            }
            else{
                StringBuffer address = new StringBuffer("");
                if(addresses.get(0).getFeatureName() != null){
                    address.append(addresses.get(0).getFeatureName() + "\n");
                }
                if(addresses.get(0).getLocality() != null){
                    address.append(addresses.get(0).getLocality() + "\n");
                }
                if(addresses.get(0).getAdminArea() != null){
                    address.append(addresses.get(0).getAdminArea() + "\n");
                }
                if(addresses.get(0).getCountryName() != null){
                    address.append(addresses.get(0).getCountryName() + "\n");
                }
                return address.toString();
            }
        }
        catch (IOException ex){
            Log.d("convert_location","Convert location failed! + " + ex.getMessage());
            return "Alo";
        }
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
