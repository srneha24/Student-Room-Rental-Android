package com.example.studentroomrental;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

public class AdPost extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private Spinner location_postSpinner;
    private Spinner seat_postSpinner;
    private Spinner genderpref_postSpinner;

    private ProgressDialog progressDialog;

    private static final String TAG = "Notification";
    private static final int ERROR_DIALOG_REQUEST = 9001;

    EditText title, house_no, road_no, block_no, section_no, floor_no, rent, water_bill, gas_bill, electricity_bill, internet_bill;
    TextView chosen_images;
    MultiAutoCompleteTextView description;
    RadioButton all_inclusive_post;
    Button image_post;

    FirebaseAuth firebaseAuth;
    FirebaseDatabase database;
    DatabaseReference dbRef;
    StorageReference storageReference;

    ArrayList<Uri> ImageList = new ArrayList<Uri>();
    private Uri ImageUri;
    int total_images = 0;
    int count = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_post);

        firebaseAuth = FirebaseAuth.getInstance();
        String user_id = firebaseAuth.getCurrentUser().getUid();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading Image");

        location_postSpinner = findViewById(R.id.LocationPost);
        ArrayList<String> locations_post = new ArrayList<>();
        seat_postSpinner = findViewById(R.id.SeatPost);
        ArrayList<String> seat_post = new ArrayList<>();

        locations_post.add("Location");
        locations_post.add("Mirpur - 1");
        locations_post.add("Mirpur - 2");
        locations_post.add("Dhanmondi - 27");
        locations_post.add("Rupnagar");
        locations_post.add("Rayer Bazar");

        ArrayAdapter<String> locations_postAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                locations_post
        );

        location_postSpinner.setAdapter(locations_postAdapter);

        seat_post.add("No. of Seats");
        for(int i=1; i<=4; i++){
            seat_post.add(String.valueOf(i));
        }

        ArrayAdapter<String> seat_postAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                seat_post
        );

        seat_postSpinner.setAdapter(seat_postAdapter);

        genderpref_postSpinner = findViewById(R.id.GenderPrefPost);
        ArrayList<String> genderpref_post = new ArrayList<>();

        genderpref_post.add("Gender Preference");
        genderpref_post.add("Female");
        genderpref_post.add("Male");
        genderpref_post.add("Either");

        ArrayAdapter<String> genderpref_postAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                genderpref_post
        );

        genderpref_postSpinner.setAdapter(genderpref_postAdapter);

        title = findViewById(R.id.AdTitlePost);
        house_no = findViewById(R.id.HouseNoPost);
        road_no = findViewById(R.id.RoadNoPost);
        block_no = findViewById(R.id.BlockNoPost);
        section_no = findViewById(R.id.SectionNoPost);
        floor_no = findViewById(R.id.FloorNoPost);
        rent = findViewById(R.id.RentPost);
        water_bill = findViewById(R.id.WaterBillPost);
        gas_bill = findViewById(R.id.GasBillPost);
        electricity_bill = findViewById(R.id.ElectricityBillPost);
        internet_bill = findViewById(R.id.InternetBillPost);
        description = findViewById(R.id.AdDescriptionPost);
        all_inclusive_post = findViewById(R.id.InclusivePost);
        chosen_images = findViewById(R.id.ImagesChosen);
        chosen_images.setVisibility(View.GONE);

        image_post = findViewById(R.id.ImageUpload);

        image_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent choose_image = new Intent(Intent.ACTION_GET_CONTENT);
                choose_image.setType("image/");
                choose_image.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(choose_image, PICK_IMAGE);
            }
        });

        Button post_ad = findViewById(R.id.PostAdButton);

        post_ad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database = FirebaseDatabase.getInstance();
                dbRef = database.getReference("user").child(user_id);

                dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String user_name = snapshot.child("user_name").getValue().toString();

                        if (title.getText().toString().isEmpty()) {
                            Toast.makeText(AdPost.this, "Ad Title Cannot Be Empty", Toast.LENGTH_LONG).show();
                        }

                        else if (rent.getText().toString().isEmpty()) {
                            Toast.makeText(AdPost.this, "Rent Cannot Be Empty", Toast.LENGTH_LONG).show();
                        }

                        else if (location_postSpinner.getSelectedItem().toString().equals("Location")) {
                            Toast.makeText(AdPost.this, "Please Choose A Location", Toast.LENGTH_LONG).show();
                        }

                        else if (seat_postSpinner.getSelectedItem().toString().equals("No. of Seats")) {
                            Toast.makeText(AdPost.this, "No. Of Seats Is Required", Toast.LENGTH_LONG).show();
                        }

                        else if (genderpref_postSpinner.getSelectedItem().toString().equals("Gender Preference")) {
                            Toast.makeText(AdPost.this, "Gender Preference Is Required", Toast.LENGTH_LONG).show();
                        }

                        else if (house_no.getText().toString().isEmpty() &&
                                road_no.getText().toString().isEmpty() &&
                                block_no.getText().toString().isEmpty() &&
                                section_no.getText().toString().isEmpty() &&
                                floor_no.getText().toString().isEmpty()) {

                            Toast.makeText(AdPost.this, "All Address Details Cannot Be Empty", Toast.LENGTH_LONG).show();
                        }

                        else if (!all_inclusive_post.isChecked() &&
                                (water_bill.getText().toString().isEmpty() &&
                                        gas_bill.getText().toString().isEmpty() &&
                                        electricity_bill.getText().toString().isEmpty() &&
                                        internet_bill.getText().toString().isEmpty())) {

                            Toast.makeText(AdPost.this, "If Rent Is Not All Inclusive, Please Fill Out The Bills.", Toast.LENGTH_LONG).show();

                        }

                        else if (total_images < 1) {
                            Toast.makeText(AdPost.this, "Please Add Images To Your Post", Toast.LENGTH_LONG).show();
                        }

                        else if (all_inclusive_post.isChecked() &&
                                (!water_bill.getText().toString().isEmpty() ||
                                        !gas_bill.getText().toString().isEmpty() ||
                                        !electricity_bill.getText().toString().isEmpty() ||
                                        !internet_bill.getText().toString().isEmpty())) {

                            AlertDialog all_inclusive_conf = new AlertDialog.Builder(AdPost.this).create();
                            all_inclusive_conf.setTitle("Is Rent Inclusive Of All Bills?");
                            all_inclusive_conf.setMessage("You have added bill costs and checked rent to be inclusive of all cost.\n Is rent inclusive of all bills?");
                            all_inclusive_conf.setButton(AlertDialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    water_bill.setText("0");
                                    gas_bill.setText("0");
                                    electricity_bill.setText("0");
                                    internet_bill.setText("0");

                                    PostAd(user_id, user_name);
                                }
                            });

                            all_inclusive_conf.setButton(AlertDialog.BUTTON_NEGATIVE, "NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    all_inclusive_post.setChecked(false);

                                    PostAd(user_id, user_name);
                                }
                            });

                            all_inclusive_conf.show();
                        }

                        else {
                            PostAd(user_id, user_name);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        Button map_post = findViewById(R.id.MapPost);

        map_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "isServicesOk: checking google services version");

                int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(AdPost.this);

                if (available == ConnectionResult.SUCCESS) {
                    Log.d(TAG, "isServicesOk: Google Play Services is working");

                    Intent intent_ad_map_set = new Intent(AdPost.this, AdMap.class);

                    startActivity(intent_ad_map_set);
                }
                else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
                    Log.d(TAG, "isServicesOk: an error occured");
                    Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(AdPost.this, available, ERROR_DIALOG_REQUEST);
                    dialog.show();
                }
                else {
                    Toast.makeText(AdPost.this, "CANNOT MAKE MAP REQUESTS", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE) {
            if (resultCode == RESULT_OK) {
                if (data.getClipData() != null) {
                    int countClipData = data.getClipData().getItemCount();
                    int currentImageSelected = 0;

                    while (currentImageSelected < countClipData) {
                        ImageUri = data.getClipData().getItemAt(currentImageSelected).getUri();
                        ImageList.add(ImageUri);

                        currentImageSelected++;
                    }

                    total_images = ImageList.size();
                    image_post.setVisibility(View.GONE);
                    chosen_images.setText(String.valueOf(ImageList.size()) + " Images Selected");
                    chosen_images.setVisibility(View.VISIBLE);
                }

                else {
                    Toast.makeText(AdPost.this, "Please Add Images With Your Post", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public String GetFileExtension(Uri uri){
        ContentResolver contentResolver=getContentResolver();
        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();

        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    void PostAd(String user_id, String user_name) {
        progressDialog.show();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        String ad_id = dtf.format(now) + "_" + user_id;
        String address_id = "A-" + ad_id;
        String rent_id = "R-" + ad_id;

        dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String post_date = dtf.format(now);

        int approved = 0;
        String approved_by = "";
        int rented = 0;
        String rented_by = "";
        String description_text = "";
        String rent_date = "";
        String block_no_text = "";
        String floor_no_text = "";
        String house_no_text = "";
        String road_no_text = "";
        String section_no_text = "";
        int electricity_bill_text = 0;
        int gas_bill_text = 0;
        int internet_bill_text = 0;
        int water_bill_text = 0;

        if (!description.getText().toString().isEmpty()) {
            description_text = description.getText().toString();
        }

        if (!block_no.getText().toString().isEmpty()) {
            block_no_text = block_no.getText().toString();
        }

        if (!floor_no.getText().toString().isEmpty()) {
            floor_no_text = floor_no.getText().toString();
        }

        if (!house_no.getText().toString().isEmpty()) {
            house_no_text = house_no.getText().toString();
        }

        if (!road_no.getText().toString().isEmpty()) {
            road_no_text = road_no.getText().toString();
        }

        if (!section_no.getText().toString().isEmpty()) {
            section_no_text = section_no.getText().toString();
        }

        if (!electricity_bill.getText().toString().isEmpty()) {
            electricity_bill_text = Integer.parseInt(electricity_bill.getText().toString());
        }

        if (!gas_bill.getText().toString().isEmpty()) {
            gas_bill_text = Integer.parseInt(gas_bill.getText().toString());
        }

        if (!internet_bill.getText().toString().isEmpty()) {
            internet_bill_text = Integer.parseInt(internet_bill.getText().toString());
        }

        if (!water_bill.getText().toString().isEmpty()) {
            water_bill_text = Integer.parseInt(water_bill.getText().toString());
        }

        Ad new_ad = new Ad(address_id, approved, approved_by, description_text, genderpref_postSpinner.getSelectedItem().toString(), title.getText().toString(), location_postSpinner.getSelectedItem().toString(), post_date, Integer.parseInt(rent.getText().toString()), rent_date, rent_id, rented, rented_by, Integer.parseInt(seat_postSpinner.getSelectedItem().toString()), user_id, user_name);
        Address new_address = new Address(block_no_text, floor_no_text, house_no_text, road_no_text, section_no_text);
        Rent new_rent = new Rent(electricity_bill_text, gas_bill_text, internet_bill_text, Integer.parseInt(rent.getText().toString()), water_bill_text);

        storageReference = FirebaseStorage.getInstance().getReference().child("Ad Images").child(ad_id);

        for (int i=0; i<total_images; i++) {
            Uri images = ImageList.get(i);

            String rename_image = "Photo " + String.valueOf(i + 1)+ "." + GetFileExtension(images);

            StorageReference img_title = storageReference.child(rename_image);

            img_title.putFile(images).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    img_title.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String img_url = String.valueOf(uri);

                            AddImageLink(img_url, ad_id);

                            if (count == total_images) {
                                progressDialog.dismiss();

                                dbRef = FirebaseDatabase.getInstance().getReference().child("ad").child(ad_id);
                                dbRef.setValue(new_ad);

                                dbRef = FirebaseDatabase.getInstance().getReference().child("address").child(address_id);
                                dbRef.setValue(new_address);

                                dbRef = FirebaseDatabase.getInstance().getReference().child("rent").child(rent_id);
                                dbRef.setValue(new_rent);

                                Toast.makeText(AdPost.this, "Ad Posted. Will Be Made Public After Verification.", Toast.LENGTH_LONG).show();

                                Intent intent_adposttohome = new Intent(AdPost.this, Homepage.class);

                                startActivity(intent_adposttohome);
                                finish();
                            }

                            count++;


                        }
                    });
                }
            });
        }

    }

    private void AddImageLink(String url, String ad_id) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ad_images").child(ad_id);

        HashMap<String, String> ImgMap = new HashMap<>();
        ImgMap.put("imageURL", url);

        databaseReference.push().setValue(ImgMap);
    }
}