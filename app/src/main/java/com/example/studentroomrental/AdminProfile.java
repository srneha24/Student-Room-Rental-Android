package com.example.studentroomrental;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminProfile extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    String user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile);

        firebaseAuth = FirebaseAuth.getInstance();

        TextView admin_name = findViewById(R.id.AdminName);
        TextView admin_id = findViewById(R.id.AdminID);

        user_id = firebaseAuth.getCurrentUser().getUid();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = database.getReference("admin").child(user_id);

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                admin_name.setText(snapshot.child("admin_name").getValue().toString());
                admin_id.setText(snapshot.child("admin_id").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Button ad_review = findViewById(R.id.AdminReviewAds);

        ad_review.setOnClickListener(view -> {
            Intent intent_ad_review = new Intent(this, AdminAdList.class);

            startActivity(intent_ad_review);
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

        Button report = findViewById(R.id.ReportGeneration);

        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_report = new Intent(AdminProfile.this, Report.class);

                intent_report.putExtra("key_admin_id", admin_id.getText().toString());
                intent_report.putExtra("key_admin_name", admin_name.getText().toString());

                startActivity(intent_report);
            }
        });
    }

    void GoToProfile() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("admin").child(user_id);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Intent intent_user_profile = new Intent(AdminProfile.this, AdminProfile.class);

                startActivity(intent_user_profile);
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}