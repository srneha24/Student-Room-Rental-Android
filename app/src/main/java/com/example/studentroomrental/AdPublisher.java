package com.example.studentroomrental;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

public class AdPublisher extends AppCompatActivity {

    String ad_publisher_id;

    FirebaseAuth firebaseAuth;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_publisher);

        firebaseAuth = FirebaseAuth.getInstance();

        ad_publisher_id = getIntent().getStringExtra("key_ad_publisher_id");

        AdPublisherDetails(ad_publisher_id);

        String user_id = firebaseAuth.getCurrentUser().getUid();
        ImageButton user_profile = findViewById(R.id.UserProfile);
        user_profile.setOnClickListener(view -> {
            GoToProfile();
        });
    }

    void AdPublisherDetails(String user_id) {
        TextView name = findViewById(R.id.AdPubName);
        TextView phone = findViewById(R.id.AdPubPhone);
        TextView email = findViewById(R.id.AdPubEmail);
        TextView gender = findViewById(R.id.AdPubGender);
        TextView district = findViewById(R.id.AdPubDistrict);
        TextView institute = findViewById(R.id.AdPubInstitute);
        TextView institute_label = findViewById(R.id.AdPubInstituteLabel);


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = database.getReference("user").child(user_id);

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                name.setText(snapshot.child("user_name").getValue().toString());
                phone.setText(snapshot.child("user_phone").getValue().toString());
                email.setText(snapshot.child("user_email").getValue().toString());
                gender.setText(snapshot.child("gender").getValue().toString());
                district.setText(snapshot.child("district").getValue().toString());

                ImageView profile_image = findViewById(R.id.AdPublisherProfileImage);
                storageReference = FirebaseStorage.getInstance().getReference().child("User Profile Images/" + user_id + ".jpg");

                try {
                    final File ad_publisherProfileImage = File.createTempFile("ad_publisher_image", ".jpg");
                    storageReference.getFile(ad_publisherProfileImage).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Bitmap bitmap = BitmapFactory.decodeFile(ad_publisherProfileImage.getAbsolutePath());
                            profile_image.setImageBitmap(bitmap);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //Toast.makeText(AdPublisher.this, "Failed To Load Profile Image", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }


                if (snapshot.child("institute").getValue().toString().isEmpty()) {
                    institute.setVisibility(View.GONE);
                    institute_label.setVisibility(View.GONE);
                }

                else {
                    institute.setText(snapshot.child("institute").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void GoToProfile() {
        String user_id = firebaseAuth.getCurrentUser().getUid();

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("user").child(user_id);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() == null) {
                    Intent intent_user_profile = new Intent(AdPublisher.this, AdminProfile.class);

                    startActivity(intent_user_profile);
                }
                else {
                    Intent intent_user_profile = new Intent(AdPublisher.this, UserProfile.class);

                    startActivity(intent_user_profile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}