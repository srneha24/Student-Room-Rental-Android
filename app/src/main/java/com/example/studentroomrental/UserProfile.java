package com.example.studentroomrental;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

public class UserProfile extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    StorageReference storageReference;
    String user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        firebaseAuth = FirebaseAuth.getInstance();

        TextView username = findViewById(R.id.UserName);
        user_id = firebaseAuth.getCurrentUser().getUid();
        ImageView profile_image = findViewById(R.id.UserProfileImage);

        storageReference = FirebaseStorage.getInstance().getReference().child("User Profile Images/" + user_id + ".jpg");

        try {
            final File userProfileImage = File.createTempFile("profile_image", ".jpg");
            storageReference.getFile(userProfileImage).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap = BitmapFactory.decodeFile(userProfileImage.getAbsolutePath());
                    profile_image.setImageBitmap(bitmap);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //Toast.makeText(UserProfile.this, "Failed To Load Profile Image", Toast.LENGTH_LONG).show();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = database.getReference("user").child(user_id);

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                username.setText(snapshot.child("user_name").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Button user_ad = findViewById(R.id.UserAds);

        user_ad.setOnClickListener(view -> {
            Intent intent_user_ad = new Intent(this, UserAd.class);

            startActivity(intent_user_ad);
        });

        Button user_rent = findViewById(R.id.UserRentalButton);

        user_rent.setOnClickListener(view -> {
            Intent intent_user_rent = new Intent(this, UserRental.class);

            startActivity(intent_user_rent);
        });

        Button user_info_edit = findViewById(R.id.UserInfoEdit);

        user_info_edit.setOnClickListener(view -> {
            Intent intent_user_info_edit = new Intent(this, UserProfileEdit.class);

            startActivity(intent_user_info_edit);
        });

        Button logout = findViewById(R.id.LogOutButton);

        logout.setOnClickListener(view -> {
            firebaseAuth.signOut();

            Intent intent_logout = new Intent(this, Login.class);

            startActivity(intent_logout);
            finish();
        });

        ImageButton user_profile = findViewById(R.id.UserProfile);
        user_profile.setOnClickListener(view -> {
            GoToProfile();
        });
    }

    void GoToProfile() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("user").child(user_id);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Intent intent_user_profile = new Intent(UserProfile.this, UserProfile.class);

                startActivity(intent_user_profile);
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}