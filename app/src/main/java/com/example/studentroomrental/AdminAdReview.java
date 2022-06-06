package com.example.studentroomrental;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;

public class AdminAdReview extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseDatabase database;
    DatabaseReference dbRef;
    StorageReference storageReference;

    String admin_id;
    ArrayList<String> url;
    int total_images = 0;
    int count = 1;

    private ProgressDialog progressDialog;

    private static final String TAG = "Notification";
    private static final int ERROR_DIALOG_REQUEST = 9001;

    Button ad_publisher;
    TextView title, house_no, road_no, block_no, section_no, floor_no, rent, water_bill, gas_bill, electricity_bill, internet_bill, description, location, seat, gender_pref;
    TextView house_no_label, road_no_label, block_no_label, section_no_label, floor_no_label, rent_label, water_bill_label, gas_bill_label, electricity_bill_label, internet_bill_label, description_label;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_ad_review);

        ReviewAdCheck(getIntent().getStringExtra("key_ad_id"));

        firebaseAuth = FirebaseAuth.getInstance();

        String ad_id = getIntent().getStringExtra("key_ad_id");
        String address_id = getIntent().getStringExtra("key_address_id");
        String rent_id = getIntent().getStringExtra("key_rent_id");
        admin_id = getIntent().getStringExtra("key_admin_id");

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deleting Ad");

        title = findViewById(R.id.ReviewAdTitle);
        house_no = findViewById(R.id.ReviewAdHouseNo);
        road_no = findViewById(R.id.ReviewAdRoadNo);
        block_no = findViewById(R.id.ReviewAdBlockNo);
        section_no = findViewById(R.id.ReviewAdSectionNo);
        floor_no = findViewById(R.id.ReviewAdFloorNo);
        rent = findViewById(R.id.ReviewAdRent);
        water_bill = findViewById(R.id.ReviewAdWaterBill);
        gas_bill = findViewById(R.id.ReviewAdGasBill);
        electricity_bill = findViewById(R.id.ReviewAdElectricityBill);
        internet_bill = findViewById(R.id.ReviewAdInternetBill);
        description = findViewById(R.id.ReviewAdDescription);
        location = findViewById(R.id.ReviewAdLocation);
        gender_pref = findViewById(R.id.ReviewAdGenderPref);
        seat = findViewById(R.id.ReviewAdSeats);
        ad_publisher = findViewById(R.id.ReviewAdPublisher);

        house_no_label = findViewById(R.id.ReviewHouseNoLabel);
        road_no_label = findViewById(R.id.ReviewRoadNoLabel);
        block_no_label = findViewById(R.id.ReviewBlockNoLabel);
        section_no_label = findViewById(R.id.ReviewSectionNoLabel);
        floor_no_label = findViewById(R.id.ReviewFloorNoLabel);
        rent_label = findViewById(R.id.ReviewRentLabel);
        water_bill_label = findViewById(R.id.ReviewWaterBillLabel);
        gas_bill_label = findViewById(R.id.ReviewGasBillLabel);
        electricity_bill_label = findViewById(R.id.ReviewElectricityBillLabel);
        internet_bill_label = findViewById(R.id.ReviewInternetBillLabel);
        description_label = findViewById(R.id.ReviewDescriptionLabel);

        ad_publisher.setOnClickListener(view -> {
            database = FirebaseDatabase.getInstance();
            dbRef = database.getReference("ad").child(ad_id);

            dbRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Intent intent_ad_publisher = new Intent(AdminAdReview.this, AdPublisher.class);

                    intent_ad_publisher.putExtra("key_ad_publisher_id", snapshot.child("user_id").getValue().toString());

                    startActivity(intent_ad_publisher);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        });

        SetAdDetails(ad_id, address_id, rent_id);

        Button ad_images = findViewById(R.id.AdminAdViewImage);

        ad_images.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_image_viewer = new Intent(AdminAdReview.this, ImageViewer.class);

                intent_image_viewer.putExtra("key_ad_id", ad_id);
                intent_image_viewer.putExtra("key_loc", 0);

                startActivity(intent_image_viewer);
            }
        });

        Button approve_ad = findViewById(R.id.ApproveAdButton);

        approve_ad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog ad_approve_conf = new AlertDialog.Builder(AdminAdReview.this).create();
                ad_approve_conf.setTitle("Approve Ad?");
                ad_approve_conf.setMessage("Ad Will Be Made Public Once Approved");
                ad_approve_conf.setButton(AlertDialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ApproveAd(ad_id);
                    }
                });

                ad_approve_conf.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ad_approve_conf.dismiss();
                    }
                });

                ad_approve_conf.show();
            }
        });

        Button delete_ad = findViewById(R.id.ReviewDeleteAd);

        delete_ad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog ad_delete_conf = new AlertDialog.Builder(AdminAdReview.this).create();
                ad_delete_conf.setTitle("Delete Ad?");
                ad_delete_conf.setMessage("Are You Sure You Want To Delete This Ad?");
                ad_delete_conf.setButton(AlertDialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DeleteAd(ad_id, address_id, rent_id);
                    }
                });

                ad_delete_conf.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ad_delete_conf.dismiss();
                    }
                });

                ad_delete_conf.show();
            }
        });

        ImageButton map_post = findViewById(R.id.ReviewAdMapButton);

        map_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "isServicesOk: checking google services version");

                int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(AdminAdReview.this);

                if (available == ConnectionResult.SUCCESS) {
                    Log.d(TAG, "isServicesOk: Google Play Services is working");

                    Intent intent_ad_map_set = new Intent(AdminAdReview.this, AdMap.class);

                    startActivity(intent_ad_map_set);
                }
                else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
                    Log.d(TAG, "isServicesOk: an error occured");
                    Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(AdminAdReview.this, available, ERROR_DIALOG_REQUEST);
                    dialog.show();
                }
                else {
                    Toast.makeText(AdminAdReview.this, "CANNOT MAKE MAP REQUESTS", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    void ReviewAdCheck(String ad_id) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = database.getReference("ad").child(ad_id);

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(AdminAdReview.this, "Ad Has Been Deleted", Toast.LENGTH_LONG).show();

                    Intent intent_adminadreturn = new Intent(AdminAdReview.this, AdminAdList.class);

                    startActivity(intent_adminadreturn);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void SetAdDetails(String ad_id, String address_id, String rent_id) {
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("ad").child(ad_id);

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                title.setText(snapshot.child("heading").getValue().toString());
                location.setText(snapshot.child("location").getValue().toString());
                seat.setText(snapshot.child("seats").getValue().toString());
                gender_pref.setText(snapshot.child("gender_pref").getValue().toString());
                ad_publisher.setText(snapshot.child("user_name").getValue().toString());

                if (snapshot.child("description").getValue().toString().isEmpty()) {
                    description.setVisibility(View.GONE);
                    description_label.setVisibility(View.GONE);
                }

                else {
                    description.setText(snapshot.child("description").getValue().toString());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        dbRef = database.getReference("address").child(address_id);
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("house_no").getValue().toString().isEmpty()) {
                    house_no.setVisibility(View.GONE);
                    house_no_label.setVisibility(View.GONE);
                }

                else {
                    house_no.setText(snapshot.child("house_no").getValue().toString());
                }

                if (snapshot.child("road_no").getValue().toString().isEmpty()) {
                    road_no.setVisibility(View.GONE);
                    road_no_label.setVisibility(View.GONE);
                }

                else {
                    road_no.setText(snapshot.child("road_no").getValue().toString());
                }

                if (snapshot.child("block_no").getValue().toString().isEmpty()) {
                    block_no.setVisibility(View.GONE);
                    block_no_label.setVisibility(View.GONE);
                }

                else {
                    block_no.setText(snapshot.child("block_no").getValue().toString());
                }

                if (snapshot.child("section").getValue().toString().isEmpty()) {
                    section_no.setVisibility(View.GONE);
                    section_no_label.setVisibility(View.GONE);
                }

                else {
                    section_no.setText(snapshot.child("section").getValue().toString());
                }

                if (snapshot.child("floor_no").getValue().toString().isEmpty()) {
                    floor_no.setVisibility(View.GONE);
                    floor_no_label.setVisibility(View.GONE);
                }

                else {
                    floor_no.setText(snapshot.child("floor_no").getValue().toString());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        dbRef = database.getReference("rent").child(rent_id);
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("rent").getValue().toString().isEmpty()) {
                    rent.setVisibility(View.GONE);
                    rent_label.setVisibility(View.GONE);
                }

                else {
                    rent.setText(snapshot.child("rent").getValue().toString());
                }

                if (snapshot.child("water_bill").getValue().toString().isEmpty()) {
                    water_bill.setVisibility(View.GONE);
                    water_bill_label.setVisibility(View.GONE);
                }

                else {
                    water_bill.setText(snapshot.child("water_bill").getValue().toString());
                }

                if (snapshot.child("gas_bill").getValue().toString().isEmpty()) {
                    gas_bill.setVisibility(View.GONE);
                    gas_bill_label.setVisibility(View.GONE);
                }

                else {
                    gas_bill.setText(snapshot.child("gas_bill").getValue().toString());
                }

                if (snapshot.child("electricity_bill").getValue().toString().isEmpty()) {
                    electricity_bill.setVisibility(View.GONE);
                    electricity_bill_label.setVisibility(View.GONE);
                }

                else {
                    electricity_bill.setText(snapshot.child("electricity_bill").getValue().toString());
                }

                if (snapshot.child("internet_bill").getValue().toString().isEmpty()) {
                    internet_bill.setVisibility(View.GONE);
                    internet_bill_label.setVisibility(View.GONE);
                }

                else {
                    internet_bill.setText(snapshot.child("internet_bill").getValue().toString());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void ApproveAd(String ad_id) {
        HashMap ad_approval = new HashMap();
        ad_approval.put("approved", 1);
        ad_approval.put("approved_by", admin_id);

        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("ad");

        dbRef.child(ad_id).updateChildren(ad_approval).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    Toast.makeText(AdminAdReview.this, "Ad Posted", Toast.LENGTH_LONG).show();

                    Intent intent_admin_ad_list = new Intent(AdminAdReview.this, AdminAdList.class);

                    startActivity(intent_admin_ad_list);
                    finish();
                }

                else {
                    Toast.makeText(AdminAdReview.this, "Something Went Wrong.Try Again Later.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    void DeleteAd(String ad_id, String address_id, String rent_id) {
        progressDialog.show();

        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("ad_images").child(ad_id);
        Query query = dbRef.orderByChild("imageURL");

        url = new ArrayList<>();

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    url.add(dataSnapshot.child("imageURL").getValue().toString());
                }

                total_images = url.size();

                for (String image: url) {
                    storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(image);

                    storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                            if (count == total_images) {
                                progressDialog.dismiss();

                                database = FirebaseDatabase.getInstance();
                                dbRef = database.getReference("ad");

                                dbRef.child(ad_id).removeValue();

                                database = FirebaseDatabase.getInstance();
                                dbRef = database.getReference("ad_images");

                                dbRef.child(ad_id).removeValue();

                                database = FirebaseDatabase.getInstance();
                                dbRef = database.getReference("address");

                                dbRef.child(address_id).removeValue();

                                database = FirebaseDatabase.getInstance();
                                dbRef = database.getReference("rent");

                                dbRef.child(rent_id).removeValue();

                                Toast.makeText(AdminAdReview.this, "Ad Deleted", Toast.LENGTH_LONG).show();

                                Intent intent_admin_ad_list = new Intent(AdminAdReview.this, AdminAdList.class);

                                startActivity(intent_admin_ad_list);
                                finish();
                            }

                            count++;

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}