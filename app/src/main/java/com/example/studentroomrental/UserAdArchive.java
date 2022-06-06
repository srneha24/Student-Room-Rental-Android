package com.example.studentroomrental;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserAdArchive extends AppCompatActivity implements UserAdArchiveAdapter.UserAdArchiveViewHolder.onUserAdArchiveClickListener {

    RecyclerView ad_archiveHolder;
    UserAdArchiveAdapter ad_archiveAdapter;
    ArrayList<Ad> ad_archiveList;

    String user_id;

    FirebaseDatabase database;
    DatabaseReference dbRef;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_ad_archive);

        firebaseAuth = FirebaseAuth.getInstance();

        user_id = firebaseAuth.getCurrentUser().getUid();

        ad_archiveHolder = findViewById(R.id.UserAdArchiveHolder);

        ad_archiveHolder.setHasFixedSize(true);
        ad_archiveHolder.setLayoutManager(new LinearLayoutManager(this));

        ArchivedAds(user_id);
    }

    void ArchivedAds(String user_id) {
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("ad");
        Query query = dbRef.orderByChild("user_id").equalTo(user_id);

        ad_archiveList = new ArrayList<>();
        ad_archiveAdapter = new UserAdArchiveAdapter(this, ad_archiveList, this::onUserAdArchiveClick);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                        if (dataSnapshot.child("rented").getValue().toString().equals("1")) {
                            Ad ad_archive = new Ad();

                            ad_archive = dataSnapshot.getValue(Ad.class);
                            ad_archive.ad_id = dataSnapshot.getRef().getKey();
                            ad_archive.address_id = dataSnapshot.child("address_id").getValue().toString();
                            ad_archive.rent_id = dataSnapshot.child("rent_id").getValue().toString();
                            ad_archive.rent_date = dataSnapshot.child("rent_date").getValue().toString();;
                            ad_archive.rented_by = dataSnapshot.child("rented_by").getValue().toString();;
                            ad_archiveList.add(ad_archive);

                            ad_archiveHolder.setAdapter(ad_archiveAdapter);

                            ad_archiveAdapter.notifyDataSetChanged();
                        }
                    }
                }

                else {
                    Toast.makeText(UserAdArchive.this, "You Do Not Have Any Ads That Have Been Rented", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onUserAdArchiveClick(int position) {
        Intent intent_user_adAdView = new Intent(this, AdView.class);

        intent_user_adAdView.putExtra("key_ad_id", ad_archiveList.get(position).ad_id);
        intent_user_adAdView.putExtra("key_address_id", ad_archiveList.get(position).address_id);
        intent_user_adAdView.putExtra("key_rent_id", ad_archiveList.get(position).rent_id);
        intent_user_adAdView.putExtra("key_adloc", 3);

        startActivity(intent_user_adAdView);
    }
}