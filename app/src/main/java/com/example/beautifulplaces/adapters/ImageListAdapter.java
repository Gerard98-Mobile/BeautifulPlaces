package com.example.beautifulplaces.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.beautifulplaces.activities.ImageDisplayActivity;
import com.example.beautifulplaces.repositories.ImageStorage;
import com.example.beautifulplaces.models.MineImage;
import com.example.beautifulplaces.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ImageListAdapter  extends FirestoreRecyclerAdapter<MineImage, ImageListAdapter.ImageHolder> {

    private Context mContext;

    public ImageListAdapter(@NonNull FirestoreRecyclerOptions<MineImage> options, Context context) {
        super(options);
        mContext = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ImageHolder holder, int position, @NonNull MineImage image) {
        try {
            holder.date.setText(image.getDate());
            holder.path = image.getPath();
            holder.pointOnMap = image.getLocation();
            String addresses = convertLocation(image.getLocation());
            holder.location.setText(addresses);
            holder.imageNameTextView.setText(image.getName());
            StorageReference ref = FirebaseStorage.getInstance().getReference(image.getPath());

            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    try {
                        Glide.with(mContext).load(uri).into(holder.imageView);
                    } catch (NullPointerException ex) {
                        Log.d("downloading_url", "There is exception in downloading url! " + ex.getStackTrace());
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Glide.with(mContext).load(R.mipmap.ic_image).into(holder.imageView);
                }
            });
        }
        catch (IllegalArgumentException ex){
            Log.d("bind_view_holder", "IllegalArgumentException in binding item");
            // delete image if exist in database
        }
    }

    @NonNull
    @Override
    public ImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.fragment_image, parent, false);

        return new ImageHolder(view);
    }

    public void deleteItem(int position){
        getSnapshots().getSnapshot(position)
                .getReference()
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(mContext,"Item deleted!",Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(mContext,"Deleting item failed!",Toast.LENGTH_SHORT).show();
            }
        });

    }

    private String convertLocation(GeoPoint point){
        try {
            Geocoder geocoder = new Geocoder(mContext.getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(point.getLatitude(), point.getLongitude(), 1);
            if(addresses.isEmpty()){
                return "Latitude: " + point.getLatitude() + "\nLongitude: " + point.getLongitude();
            }
            else{
                StringBuffer address = new StringBuffer("");

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

    public class ImageHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

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

                    new AlertDialog.Builder(mContext)
                            .setTitle(R.string.deleting_picture_alert)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteItem(getAdapterPosition());
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
            Intent intent = new Intent(v.getContext(), ImageDisplayActivity.class);
            intent.putExtra(ImageDisplayActivity.KEY_EXTRA_IMAGE_PATH, path);
            intent.putExtra(ImageDisplayActivity.KEY_EXTRA_IMAGE_LONGITUDE,pointOnMap.getLongitude());
            intent.putExtra(ImageDisplayActivity.KEY_EXTRA_IMAGE_LATITUDE,pointOnMap.getLatitude());
            v.getContext().startActivity(intent);
        }
    }
}
