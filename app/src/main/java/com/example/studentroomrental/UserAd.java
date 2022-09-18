package com.example.studentroomrental;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserAd extends AppCompatActivity implements UserAdAdapter.UserAdViewHolder.onUserAdClickListener {

    RecyclerView user_adHolder;
    UserAdAdapter user_adAdapter;
    ArrayList<Ad> user_adList;

    String user_id;

    FirebaseDatabase database;
    DatabaseReference dbRef;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_ad);

        firebaseAuth = FirebaseAuth.getInstance();

        user_id = firebaseAuth.getCurrentUser().getUid();

        user_adHolder = findViewById(R.id.UserAdHolder);

        user_adHolder.setHasFixedSize(true);
        user_adHolder.setLayoutManager(new LinearLayoutManager(this));

        UserAds(user_id);

        Button ad_archive = findViewById(R.id.UserAdArchive);

        ad_archive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_ad_archive = new Intent(UserAd.this, UserAdArchive.class);

                startActivity(intent_ad_archive);
            }
        });
    }

    void UserAds(String user_id) {
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("ad");
        Query query = dbRef.orderByChild("user_id").equalTo(user_id);

        user_adList = new ArrayList<>();
        user_adAdapter = new UserAdAdapter(this, user_adList, this::onUserAdClick);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                        if (dataSnapshot.child("rented").getValue().toString().equals("0")) {
                            Ad user_ad = new Ad();

                            user_ad = dataSnapshot.getValue(Ad.class);
                            user_ad.ad_id = dataSnapshot.getRef().getKey();
                            user_ad.address_id = dataSnapshot.child("address_id").getValue().toString();
                            user_ad.rent_id = dataSnapshot.child("rent_id").getValue().toString();
                            user_adList.add(user_ad);

                            user_adHolder.setAdapter(user_adAdapter);

                            user_adAdapter.notifyDataSetChanged();
                        }
                    }
                }

                else {
                    Toast.makeText(UserAd.this, "You Do Not Have Any Actively Posted Ads", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onUserAdClick(int position) {
        Intent intent_user_adAdEdit = new Intent(this, AdEdit.class);

        intent_user_adAdEdit.putExtra("key_ad_id", user_adList.get(position).ad_id);
        intent_user_adAdEdit.putExtra("key_address_id", user_adList.get(position).address_id);
        intent_user_adAdEdit.putExtra("key_rent_id", user_adList.get(position).rent_id);
        startActivity(intent_user_adAdEdit);
        finish();
    }
}