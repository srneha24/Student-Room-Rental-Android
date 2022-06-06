package com.example.studentroomrental;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RentRequest extends AppCompatActivity implements RentRequestAdapter.RentRequestViewHolder.onRentRequestClickListener {

    RecyclerView rent_requestHolder;
    RentRequestAdapter RentRequestAdapter;
    ArrayList<Request> rent_requestList;

    String ad_id;

    FirebaseDatabase database;
    DatabaseReference dbRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rent_request);

        ad_id = getIntent().getStringExtra("key_ad_id");

        rent_requestHolder = findViewById(R.id.RentRequestHolder);

        rent_requestHolder.setHasFixedSize(true);
        rent_requestHolder.setLayoutManager(new LinearLayoutManager(this));

        getRequests(ad_id);
    }

    void getRequests(String ad_id) {
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("rent-request").child(ad_id);

        rent_requestList = new ArrayList<>();
        RentRequestAdapter = new RentRequestAdapter(this, rent_requestList, this::onRentRequestClick, ad_id);

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    Request request;

                    request = dataSnapshot.getValue(Request.class);
                    rent_requestList.add(request);

                    rent_requestHolder.setAdapter(RentRequestAdapter);

                    RentRequestAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onRentRequestClick(int position) {
        Intent intent_rent_requestAdPublisher = new Intent(this, AdPublisher.class);

        intent_rent_requestAdPublisher.putExtra("key_ad_publisher_id", rent_requestList.get(position).user_id);

        startActivity(intent_rent_requestAdPublisher);
    }
}