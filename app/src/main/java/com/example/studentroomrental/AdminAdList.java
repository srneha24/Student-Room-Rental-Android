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

public class AdminAdList extends AppCompatActivity implements AdminAdListAdapter.AdminAdListViewHolder.onAdminAdListClickListener {

    RecyclerView admin_ad_listHolder;
    AdminAdListAdapter AdminAdListAdapter;
    ArrayList<Ad> admin_ad_listList;

    FirebaseDatabase database;
    DatabaseReference dbRef;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_ad_list);

        firebaseAuth = FirebaseAuth.getInstance();

        admin_ad_listHolder = findViewById(R.id.AdminAdListHolder);

        admin_ad_listHolder.setHasFixedSize(true);
        admin_ad_listHolder.setLayoutManager(new LinearLayoutManager(this));

        ToBeReviewed();
    }

    void ToBeReviewed() {
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("ad");
        Query query = dbRef.orderByChild("approved").equalTo(0);

        admin_ad_listList = new ArrayList<>();
        AdminAdListAdapter = new AdminAdListAdapter(this, admin_ad_listList, this::onAdminAdListClick);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount() != 0) {
                    admin_ad_listList.clear();
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                        Ad admin_ad_list = new Ad();

                        admin_ad_list = dataSnapshot.getValue(Ad.class);
                        admin_ad_list.ad_id = dataSnapshot.getRef().getKey();
                        admin_ad_list.address_id = dataSnapshot.child("address_id").getValue().toString();
                        admin_ad_list.rent_id = dataSnapshot.child("rent_id").getValue().toString();
                        admin_ad_listList.add(admin_ad_list);

                        admin_ad_listHolder.setAdapter(AdminAdListAdapter);

                        AdminAdListAdapter.notifyDataSetChanged();
                    }
                }

                else {
                    Toast.makeText(AdminAdList.this, "No Ads To Be Reviewed", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onAdminAdListClick(int position) {
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("admin").child(firebaseAuth.getCurrentUser().getUid());

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Intent intent_admin_ad_listAdView = new Intent(AdminAdList.this, AdminAdReview.class);

                intent_admin_ad_listAdView.putExtra("key_ad_id", admin_ad_listList.get(position).ad_id);
                intent_admin_ad_listAdView.putExtra("key_address_id", admin_ad_listList.get(position).address_id);
                intent_admin_ad_listAdView.putExtra("key_rent_id", admin_ad_listList.get(position).rent_id);
                intent_admin_ad_listAdView.putExtra("key_admin_id", snapshot.child("admin_id").getValue().toString());

                startActivity(intent_admin_ad_listAdView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}