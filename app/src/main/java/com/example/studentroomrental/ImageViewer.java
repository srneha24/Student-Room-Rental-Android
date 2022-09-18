package com.example.studentroomrental;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class ImageViewer extends AppCompatActivity {

    RecyclerView image_viewerHolder;
    ImageViewerAdapter imageViewerAdapter;
    ArrayList<AdImages> image_viewerList;

    FirebaseDatabase database;
    DatabaseReference dbRef;

    String ad_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        image_viewerHolder = findViewById(R.id.ImageViewerHolder);

        image_viewerHolder.setHasFixedSize(true);
        image_viewerHolder.setLayoutManager(new LinearLayoutManager(this));

        ad_id = getIntent().getStringExtra("key_ad_id");

        GetAdImages(ad_id);
    }

    void GetAdImages(String ad_id) {
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("ad_images").child(ad_id);

        image_viewerList = new ArrayList<>();
        imageViewerAdapter = new ImageViewerAdapter(this, image_viewerList, 1);

        image_viewerHolder.setAdapter(imageViewerAdapter);

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                image_viewerList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    AdImages adImages = dataSnapshot.getValue(AdImages.class);

                    image_viewerList.add(adImages);
                }

                imageViewerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}