package com.example.studentroomrental;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Homepage extends AppCompatActivity implements AdAdapter.AdViewHolder.onAdClickListener {

    private Spinner location_searchSpinner;
    private Spinner seat_searchSpinner;
    RecyclerView adHolder;

    private ProgressDialog progressDialog;

    FloatingActionButton ad_postButton;

    DatabaseReference dbRef;

    AdAdapter adAdapter;
    ArrayList<Ad> adList;

    String user_id;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Opening App");
        progressDialog.show();

        firebaseAuth = FirebaseAuth.getInstance();

        location_searchSpinner = findViewById(R.id.LocationSearch);
        ArrayList<String> locations_search = new ArrayList<>();
        seat_searchSpinner = findViewById(R.id.SeatSearch);
        ArrayList<String> seat_search = new ArrayList<>();
        adHolder = findViewById(R.id.AdHolder);

        adHolder.setHasFixedSize(true);
        adHolder.setLayoutManager(new LinearLayoutManager(this));

        locations_search.add("Select Location");
        locations_search.add("Mirpur - 1");
        locations_search.add("Mirpur - 2");
        locations_search.add("Dhanmondi - 27");
        locations_search.add("Rupnagar");
        locations_search.add("Rayer Bazar");

        ArrayAdapter<String> location_searchAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                locations_search
        );

        location_searchSpinner.setAdapter(location_searchAdapter);

        seat_search.add("No. of Seats");
        for(int i=1; i<=4; i++){
            seat_search.add(String.valueOf(i));
        }

        ArrayAdapter<String> seat_searchAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                seat_search
        );

        seat_searchSpinner.setAdapter(seat_searchAdapter);

        ImageButton user_profile = findViewById(R.id.UserProfile);
        user_profile.setOnClickListener(view -> {
            GoToProfile();
        });

        ad_postButton = findViewById(R.id.AdPostButton);

        ad_postButton.setOnClickListener(view -> {
            Intent intent_ad_postButton = new Intent(this, AdPost.class);

            startActivity(intent_ad_postButton);
        });

        ImageButton search_ad = findViewById(R.id.SearchAdButton);

        search_ad.setOnClickListener(view -> {
            if(location_searchSpinner.getSelectedItem().toString().equals("Select Location") || seat_searchSpinner.getSelectedItem().toString().equals("No. of Seats")) {
                Toast.makeText(Homepage.this, "Please Select A Location/No. of Seats", Toast.LENGTH_LONG).show();
            }
            else {
                SearchResults(location_searchSpinner.getSelectedItem().toString(), seat_searchSpinner.getSelectedItem().toString());
            }
        });

        homepageAds();

        progressDialog.dismiss();

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser CurrentUser = firebaseAuth.getCurrentUser();

        if (CurrentUser != null) {
            user_id = firebaseAuth.getCurrentUser().getUid();
            CheckUserType();
        }

        else {
            startActivity(new Intent(this, Login.class));
            finish();
        }
    }

    private void homepageAds() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("ad");
        Query query = dbRef.orderByChild("post_date").limitToLast(10);

        adList = new ArrayList<>();
        adAdapter = new AdAdapter(this, adList, this::onAdClick);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adList.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    if (Integer.parseInt(dataSnapshot.child("approved").getValue().toString()) == 1 && Integer.parseInt(dataSnapshot.child("rented").getValue().toString()) == 0) {
                        Ad ad = new Ad();

                        ad = dataSnapshot.getValue(Ad.class);
                        ad.ad_id = dataSnapshot.getRef().getKey();
                        ad.address_id = dataSnapshot.child("address_id").getValue().toString();
                        ad.rent_id = dataSnapshot.child("rent_id").getValue().toString();
                        adList.add(ad);

                        adHolder.setAdapter(adAdapter);
                    }
                }

                adAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void SearchResults(String search_loc, String search_seat) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("ad");
        Query query = dbRef.orderByChild("location").equalTo(search_loc);

        adList = new ArrayList<>();
        adAdapter = new AdAdapter(this, adList, this::onAdClick);

        ArrayList<String> ad_ids = new ArrayList<>();

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    adList.clear();
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                        if (Integer.parseInt(dataSnapshot.child("approved").getValue().toString()) == 1 && Integer.parseInt(dataSnapshot.child("rented").getValue().toString()) == 0) {
                            if (Integer.parseInt(dataSnapshot.child("seats").getValue().toString()) >= Integer.parseInt(search_seat)) {
                                ad_ids.add(dataSnapshot.getRef().getKey());
                            }
                        }

                    }

                    adAdapter.notifyDataSetChanged();
                }

                else {
                    Toast.makeText(Homepage.this, "No Ads Found", Toast.LENGTH_LONG).show();
                }

                if (ad_ids.isEmpty()) {
                    Toast.makeText(Homepage.this, "No Ads Found", Toast.LENGTH_LONG).show();
                }

                else {
                    for (String ad_id: ad_ids) {
                        dbRef.child(ad_id).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Ad ad = new Ad();

                                ad = snapshot.getValue(Ad.class);
                                ad.ad_id = snapshot.getRef().getKey();
                                ad.address_id = snapshot.child("address_id").getValue().toString();
                                ad.rent_id = snapshot.child("rent_id").getValue().toString();
                                adList.add(ad);

                                adHolder.setAdapter(adAdapter);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void GoToProfile() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("user").child(user_id);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // ADMIN
                if (snapshot.getValue() == null) {
                    Intent intent_user_profile = new Intent(Homepage.this, AdminProfile.class);

                    startActivity(intent_user_profile);
                }
                // STUDENT
                else {
                    Intent intent_user_profile = new Intent(Homepage.this, UserProfile.class);

                    startActivity(intent_user_profile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void CheckUserType() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("user").child(user_id);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // ADMIN
                if (snapshot.getValue() == null) {
                    ad_postButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onAdClick(int position) {
        Intent intent_adView = new Intent(this, AdView.class);

        intent_adView.putExtra("key_ad_id", adList.get(position).ad_id);
        intent_adView.putExtra("key_address_id", adList.get(position).address_id);
        intent_adView.putExtra("key_rent_id", adList.get(position).rent_id);
        intent_adView.putExtra("key_adloc", 1);

        startActivity(intent_adView);
    }
}