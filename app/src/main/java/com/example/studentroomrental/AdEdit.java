package com.example.studentroomrental;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RadioButton;
import android.widget.Spinner;
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

public class AdEdit extends AppCompatActivity {

    private Spinner location_editSpinner;
    private Spinner seat_editSpinner;
    private Spinner genderpref_editSpinner;

    String ad_id, address_id, rent_id;
    ArrayList<String> locations_edit;
    ArrayList<String> seat_edit;

    FirebaseDatabase database;
    DatabaseReference dbRef;
    FirebaseAuth firebaseAuth;
    StorageReference storageReference;

    ArrayList<String> url;
    int total_images = 0;
    int count = 1;

    private static final String TAG = "Notification";
    private static final int ERROR_DIALOG_REQUEST = 9001;

    private ProgressDialog progressDialog;

    EditText title, house_no, road_no, block_no, section_no, floor_no, rent, water_bill, gas_bill, electricity_bill, internet_bill;
    MultiAutoCompleteTextView description;
    RadioButton all_inclusive;

    String title_og, rent_og, water_bill_og, gas_bill_og, electricity_bill_og, internet_bill_og, description_og, location_og, seat_og, gender_pref_og, approved_og, approved_by_og;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_edit);

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deleting Ad");

        ad_id = getIntent().getStringExtra("key_ad_id");
        address_id = getIntent().getStringExtra("key_address_id");
        rent_id = getIntent().getStringExtra("key_rent_id");
        String user_id = firebaseAuth.getCurrentUser().getUid();

        location_editSpinner = findViewById(R.id.LocationEdit);
        locations_edit = new ArrayList<>();
        seat_editSpinner = findViewById(R.id.SeatEdit);
        seat_edit = new ArrayList<>();

        locations_edit.add("Mirpur - 1");
        locations_edit.add("Mirpur - 2");
        locations_edit.add("Dhanmondi - 27");
        locations_edit.add("Rupnagar");
        locations_edit.add("Rayer Bazar");

        ArrayAdapter<String> location_editAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                locations_edit
        );

        location_editSpinner.setAdapter(location_editAdapter);

        for(int i=1; i<=4; i++){
            seat_edit.add(String.valueOf(i));
        }

        ArrayAdapter<String> seat_editAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                seat_edit
        );

        seat_editSpinner.setAdapter(seat_editAdapter);

        genderpref_editSpinner = findViewById(R.id.GenderPrefEdit);
        ArrayList<String> genderpref_edit = new ArrayList<>();

        genderpref_edit.add("Female");
        genderpref_edit.add("Male");
        genderpref_edit.add("Either");

        ArrayAdapter<String> genderpref_editAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                genderpref_edit
        );

        genderpref_editSpinner.setAdapter(genderpref_editAdapter);

        title = findViewById(R.id.AdTitleEdit);
        house_no = findViewById(R.id.HouseNoEdit);
        road_no = findViewById(R.id.RoadNoEdit);
        block_no = findViewById(R.id.BlockNoEdit);
        section_no = findViewById(R.id.SectionNoEdit);
        floor_no = findViewById(R.id.FloorNoEdit);
        rent = findViewById(R.id.RentEdit);
        water_bill = findViewById(R.id.WaterBillEdit);
        gas_bill = findViewById(R.id.GasBillEdit);
        electricity_bill = findViewById(R.id.ElectricityBillEdit);
        internet_bill = findViewById(R.id.InternetBillEdit);
        description = findViewById(R.id.AdDescriptionEdit);
        all_inclusive = findViewById(R.id.InclusiveEdit);

        SetAdDetails(ad_id, address_id, rent_id);

        Button view_request = findViewById(R.id.ViewRentRequestButton);

        view_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_rent_request = new Intent(AdEdit.this, RentRequest.class);

                intent_rent_request.putExtra("key_ad_id", ad_id);

                startActivity(intent_rent_request);
            }
        });

        Button update_ad_details = findViewById(R.id.EditSave);

        update_ad_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (title.getText().toString().isEmpty()) {
                    Toast.makeText(AdEdit.this, "Ad Title Cannot Be Empty", Toast.LENGTH_LONG).show();
                }

                else if (rent.getText().toString().isEmpty()) {
                    Toast.makeText(AdEdit.this, "Rent Cannot Be Empty", Toast.LENGTH_LONG).show();
                }

                else if (house_no.getText().toString().isEmpty() && road_no.getText().toString().isEmpty() && block_no.getText().toString().isEmpty() && section_no.getText().toString().isEmpty() && floor_no.getText().toString().isEmpty()) {
                    Toast.makeText(AdEdit.this, "All Address Elements Cannot Be Empty", Toast.LENGTH_LONG).show();
                }

                else if (!all_inclusive.isChecked() && (water_bill.getText().toString().equals("0") &&
                        gas_bill.getText().toString().equals("0") &&
                        electricity_bill.getText().toString().equals("0") &&
                        internet_bill.getText().toString().equals("0"))) {

                    Toast.makeText(AdEdit.this, "If Rent Is Not All Inclusive, Please Fill Out The Bills.", Toast.LENGTH_LONG).show();
                }

                else if (all_inclusive.isChecked() && (!water_bill.getText().toString().equals("0") ||
                        !gas_bill.getText().toString().equals("0") ||
                        !electricity_bill.getText().toString().equals("0") ||
                        !internet_bill.getText().toString().equals("0"))) {

                    AlertDialog all_inclusive_conf = new AlertDialog.Builder(AdEdit.this).create();
                    all_inclusive_conf.setTitle("Is Rent Inclusive Of All Bills?");
                    all_inclusive_conf.setMessage("You have added bill costs and checked rent to be inclusive of all cost.\n Is rent inclusive of all bills?");
                    all_inclusive_conf.setButton(AlertDialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            water_bill.setText("0");
                            gas_bill.setText("0");
                            electricity_bill.setText("0");
                            internet_bill.setText("0");

                            UpdateAdDetails(ad_id, address_id, rent_id);
                        }
                    });

                    all_inclusive_conf.setButton(AlertDialog.BUTTON_NEGATIVE, "NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            all_inclusive.setChecked(false);

                            UpdateAdDetails(ad_id, address_id, rent_id);
                        }
                    });

                    all_inclusive_conf.show();
                }

                else {
                    UpdateAdDetails(ad_id, address_id, rent_id);
                }
            }
        });

        Button ad_delete = findViewById(R.id.UserAdDelete);

        ad_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog ad_delete_conf = new AlertDialog.Builder(AdEdit.this).create();
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

        Button change_images = findViewById(R.id.ImageChange);

        change_images.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_image_viewer = new Intent(AdEdit.this, ImageViewerEdit.class);

                intent_image_viewer.putExtra("key_ad_id", ad_id);

                startActivity(intent_image_viewer);
            }
        });

        Button map_post = findViewById(R.id.MapEdit);

        map_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "isServicesOk: checking google services version");

                int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(AdEdit.this);

                if (available == ConnectionResult.SUCCESS) {
                    Log.d(TAG, "isServicesOk: Google Play Services is working");

                    Intent intent_ad_map_set = new Intent(AdEdit.this, AdMap.class);

                    startActivity(intent_ad_map_set);
                }
                else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
                    Log.d(TAG, "isServicesOk: an error occured");
                    Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(AdEdit.this, available, ERROR_DIALOG_REQUEST);
                    dialog.show();
                }
                else {
                    Toast.makeText(AdEdit.this, "CANNOT MAKE MAP REQUESTS", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    void SetAdDetails(String ad_id, String address_id, String rent_id) {
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("ad").child(ad_id);

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                approved_og = snapshot.child("approved").getValue().toString();
                approved_by_og = snapshot.child("approved_by").getValue().toString();
                gender_pref_og = snapshot.child("gender_pref").getValue().toString();
                title_og = snapshot.child("heading").getValue().toString();
                location_og = snapshot.child("location").getValue().toString();
                seat_og = snapshot.child("seats").getValue().toString();

                if (!snapshot.child("description").getValue().toString().isEmpty()) {
                    description_og = snapshot.child("description").getValue().toString();
                }

                else {
                    description_og = "";
                }

                title.setText(title_og);
                description.setText(description_og);

                location_editSpinner.setSelection(locations_edit.indexOf(location_og));
                seat_editSpinner.setSelection(seat_edit.indexOf(seat_og));

                if (gender_pref_og.equals("Female")) {
                    genderpref_editSpinner.setSelection(0);
                }

                else if (gender_pref_og.equals("Male")) {
                    genderpref_editSpinner.setSelection(1);
                }

                else {
                    genderpref_editSpinner.setSelection(2);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("rent").child(rent_id);

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                rent_og = snapshot.child("rent").getValue().toString();

                if (!snapshot.child("electricity_bill").getValue().toString().isEmpty()) {
                    electricity_bill_og = snapshot.child("electricity_bill").getValue().toString();
                }

                else {
                    electricity_bill_og = "";
                }

                if (!snapshot.child("gas_bill").getValue().toString().isEmpty()) {
                    gas_bill_og = snapshot.child("gas_bill").getValue().toString();
                }

                else {
                    gas_bill_og = "";
                }

                if (!snapshot.child("water_bill").getValue().toString().isEmpty()) {
                    water_bill_og = snapshot.child("water_bill").getValue().toString();
                }

                else {
                    water_bill_og = "";
                }

                if (!snapshot.child("internet_bill").getValue().toString().isEmpty()) {
                    internet_bill_og = snapshot.child("internet_bill").getValue().toString();
                }

                else {
                    internet_bill_og = "";
                }

                rent.setText(rent_og);
                water_bill.setText(water_bill_og);
                gas_bill.setText(gas_bill_og);
                electricity_bill.setText(electricity_bill_og);
                internet_bill.setText(internet_bill_og);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("address").child(address_id);

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.child("block_no").getValue().toString().isEmpty()) {
                    block_no.setText(snapshot.child("block_no").getValue().toString());
                }

                if (!snapshot.child("road_no").getValue().toString().isEmpty()) {
                    road_no.setText(snapshot.child("road_no").getValue().toString());
                }

                if (!snapshot.child("house_no").getValue().toString().isEmpty()) {
                    house_no.setText(snapshot.child("house_no").getValue().toString());
                }

                if (!snapshot.child("floor_no").getValue().toString().isEmpty()) {
                    floor_no.setText(snapshot.child("floor_no").getValue().toString());
                }

                if (!snapshot.child("section").getValue().toString().isEmpty()) {
                    section_no.setText(snapshot.child("section").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void UpdateAdDetails(String ad_id, String address_id, String rent_id) {
        HashMap ad_detailsad = new HashMap();
        ad_detailsad.put("approved", 0);
        ad_detailsad.put("approved_by", "");
        ad_detailsad.put("gender_pref", genderpref_editSpinner.getSelectedItem().toString());
        ad_detailsad.put("heading", title.getText().toString());
        ad_detailsad.put("location", location_editSpinner.getSelectedItem().toString());
        ad_detailsad.put("seats", Integer.parseInt(seat_editSpinner.getSelectedItem().toString()));
        ad_detailsad.put("rent", Integer.parseInt(rent.getText().toString()));
        ad_detailsad.put("description", description.getText().toString());

        HashMap ad_detailsrent = new HashMap();
        ad_detailsrent.put("rent", Integer.parseInt(rent.getText().toString()));
        ad_detailsrent.put("electricity_bill", Integer.parseInt(electricity_bill.getText().toString()));
        ad_detailsrent.put("gas_bill", Integer.parseInt(gas_bill.getText().toString()));
        ad_detailsrent.put("internet_bill", Integer.parseInt(internet_bill.getText().toString()));
        ad_detailsrent.put("water_bill", Integer.parseInt(water_bill.getText().toString()));

        HashMap ad_detailsaddress = new HashMap();
        ad_detailsaddress.put("block_no", block_no.getText().toString());
        ad_detailsaddress.put("floor_no", floor_no.getText().toString());
        ad_detailsaddress.put("house_no", house_no.getText().toString());
        ad_detailsaddress.put("road_no", road_no.getText().toString());
        ad_detailsaddress.put("section", section_no.getText().toString());

        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("ad");

        dbRef.child(ad_id).updateChildren(ad_detailsad).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    database = FirebaseDatabase.getInstance();
                    dbRef = database.getReference("rent");

                    dbRef.child(rent_id).updateChildren(ad_detailsrent).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                database = FirebaseDatabase.getInstance();
                                dbRef = database.getReference("address");

                                dbRef.child(address_id).updateChildren(ad_detailsaddress).addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(AdEdit.this, "Changes Saved. Ad Will Be Made Public After Being Reviewed Again", Toast.LENGTH_LONG).show();

                                            Intent intent_adedit = new Intent(AdEdit.this, AdEdit.class);

                                            intent_adedit.putExtra("key_ad_id", ad_id);
                                            intent_adedit.putExtra("key_address_id", address_id);
                                            intent_adedit.putExtra("key_rent_id", rent_id);

                                            startActivity(intent_adedit);
                                            finish();
                                        }

                                        else {
                                            ad_detailsad.put("approved", Integer.parseInt(approved_og));
                                            ad_detailsad.put("approved_by", approved_by_og);
                                            ad_detailsad.put("gender_pref", gender_pref_og);
                                            ad_detailsad.put("heading", title_og);
                                            ad_detailsad.put("location", location_og);
                                            ad_detailsad.put("seats", Integer.parseInt(seat_og));
                                            ad_detailsad.put("rent", Integer.parseInt(rent_og));
                                            ad_detailsad.put("description", description_og);

                                            database = FirebaseDatabase.getInstance();
                                            dbRef = database.getReference("ad");

                                            dbRef.child(ad_id).updateChildren(ad_detailsad).addOnCompleteListener(new OnCompleteListener() {
                                                @Override
                                                public void onComplete(@NonNull Task task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(AdEdit.this, "Ad Changes Not Saved", Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });

                                            ad_detailsrent.put("rent", Integer.parseInt(rent_og));
                                            ad_detailsrent.put("water_bill", Integer.parseInt(water_bill_og));
                                            ad_detailsrent.put("gas_bill", Integer.parseInt(gas_bill_og));
                                            ad_detailsrent.put("electricity_bill", Integer.parseInt(electricity_bill_og));
                                            ad_detailsrent.put("internet_bill", Integer.parseInt(internet_bill_og));

                                            database = FirebaseDatabase.getInstance();
                                            dbRef = database.getReference("rent");

                                            dbRef.child(rent_id).updateChildren(ad_detailsrent).addOnCompleteListener(new OnCompleteListener() {
                                                @Override
                                                public void onComplete(@NonNull Task task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(AdEdit.this, "Ad Changes Not Saved", Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }

                            else {
                                ad_detailsad.put("approved", Integer.parseInt(approved_og));
                                ad_detailsad.put("approved_by", approved_by_og);
                                ad_detailsad.put("gender_pref", gender_pref_og);
                                ad_detailsad.put("heading", title_og);
                                ad_detailsad.put("location", location_og);
                                ad_detailsad.put("seats", Integer.parseInt(seat_og));
                                ad_detailsad.put("rent", Integer.parseInt(rent_og));
                                ad_detailsad.put("description", description_og);

                                database = FirebaseDatabase.getInstance();
                                dbRef = database.getReference("ad");

                                dbRef.child(ad_id).updateChildren(ad_detailsad).addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(AdEdit.this, "Ad Changes Not Saved", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                        }
                    });
                }

                else {
                    Toast.makeText(AdEdit.this, "Ad Changes Not Saved", Toast.LENGTH_LONG).show();
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
                    System.out.println(image);

                    storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                            if (count == total_images) {
                                progressDialog.dismiss();

                                database = FirebaseDatabase.getInstance();
                                dbRef = database.getReference("ad");

                                dbRef.child(ad_id).removeValue();

                                database = FirebaseDatabase.getInstance();
                                dbRef = database.getReference("address");

                                dbRef.child(address_id).removeValue();

                                database = FirebaseDatabase.getInstance();
                                dbRef = database.getReference("ad_images");

                                dbRef.child(ad_id).removeValue();

                                database = FirebaseDatabase.getInstance();
                                dbRef = database.getReference("rent");

                                dbRef.child(rent_id).removeValue();

                                Toast.makeText(AdEdit.this, "Ad Deleted", Toast.LENGTH_LONG).show();

                                Intent intent_user_ad = new Intent(AdEdit.this, UserAd.class);

                                startActivity(intent_user_ad);
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