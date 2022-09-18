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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserRental extends AppCompatActivity implements UserRentalAdapter.UserRentalViewHolder.onUserRentalClickListener {

    RecyclerView user_rentalHolder;
    UserRentalAdapter UserRentalAdapter;
    ArrayList<Ad> user_rentalList;

    String user_id;

    FirebaseDatabase database;
    DatabaseReference dbRef;

    ArrayList<String> ad_ids;
    ArrayList<String> ad_rent_dates;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_rental);

        firebaseAuth = FirebaseAuth.getInstance();

        user_id = firebaseAuth.getCurrentUser().getUid();

        user_rentalHolder = findViewById(R.id.UserRentalHolder);

        user_rentalHolder.setHasFixedSize(true);
        user_rentalHolder.setLayoutManager(new LinearLayoutManager(this));

        UserRentals(user_id);
    }

    void UserRentals(String user_id) {
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("rental").child(user_id);

        ad_ids = new ArrayList<>();
        ad_rent_dates = new ArrayList<>();

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                        ad_rent_dates.add(dataSnapshot.getValue().toString());
                        ad_ids.add(dataSnapshot.getKey());
                    }

                    RentalAds();
                }

                else {
                    Toast.makeText(UserRental.this, "You Have Not Taken Any Rentals", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void RentalAds() {
        for (String ad_id : ad_ids) {
            database = FirebaseDatabase.getInstance();
            dbRef = database.getReference("ad").child(ad_id);

            user_rentalList = new ArrayList<>();
            UserRentalAdapter = new UserRentalAdapter(this, user_rentalList, this::onUserRentalClick);

            dbRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Ad user_rental = new Ad();

                    user_rental = snapshot.getValue(Ad.class);
                    user_rental.ad_id = snapshot.getRef().getKey();
                    user_rental.address_id = snapshot.child("address_id").getValue().toString();
                    user_rental.rent_id = snapshot.child("rent_id").getValue().toString();
                    user_rental.rent_date = ad_rent_dates.get(ad_ids.indexOf(ad_id));
                    user_rentalList.add(user_rental);

                    user_rentalHolder.setAdapter(UserRentalAdapter);

                    UserRentalAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    @Override
    public void onUserRentalClick(int position) {
        Intent intent_user_rentalAdView = new Intent(this, AdView.class);

        intent_user_rentalAdView.putExtra("key_ad_id", user_rentalList.get(position).ad_id);
        intent_user_rentalAdView.putExtra("key_address_id", user_rentalList.get(position).address_id);
        intent_user_rentalAdView.putExtra("key_rent_id", user_rentalList.get(position).rent_id);
        intent_user_rentalAdView.putExtra("key_adloc", 2);

        startActivity(intent_user_rentalAdView);
    }
}