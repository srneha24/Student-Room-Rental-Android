package com.example.studentroomrental;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Random;

public class AdView extends AppCompatActivity{

    Button ad_publisherButton, view_image,rent_request;

    FirebaseAuth firebaseAuth;
    DatabaseReference dbRef;
    FirebaseDatabase database;

    private static final String TAG = "Notification";
    private static final int ERROR_DIALOG_REQUEST = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // key_adloc == 1 --> Homepage Ads / Search Resutls
        // key_adloc == 2 --> User Rental
        // key_adloc == 3 --> User Archive
        firebaseAuth = FirebaseAuth.getInstance();
        String user_id = firebaseAuth.getCurrentUser().getUid();

        AdCheck(getIntent().getStringExtra("key_ad_id"), getIntent().getIntExtra("key_adloc", 1), getIntent().getStringExtra("key_userid"));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_view);

        String ad_id = getIntent().getStringExtra("key_ad_id");
        String address_id = getIntent().getStringExtra("key_address_id");
        String rent_id = getIntent().getStringExtra("key_rent_id");

        AdDetails(ad_id, address_id, rent_id);

        ImageButton user_profile = findViewById(R.id.UserProfile);
        user_profile.setOnClickListener(view -> {
            GoToProfile();
        });

        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("ad").child(ad_id);

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (user_id.equals(snapshot.child("user_id").getValue().toString())) {
                    rent_request.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ad_publisherButton = findViewById(R.id.AdPublisher);

        ad_publisherButton.setOnClickListener(view -> {
            database = FirebaseDatabase.getInstance();
            dbRef = database.getReference("ad").child(ad_id);

            dbRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Intent intent_ad_publisher = new Intent(AdView.this, AdPublisher.class);

                    intent_ad_publisher.putExtra("key_ad_publisher_id", snapshot.child("user_id").getValue().toString());

                    startActivity(intent_ad_publisher);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        });

        view_image = findViewById(R.id.AdViewImages);

        view_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_image_viewer = new Intent(AdView.this, ImageViewer.class);

                intent_image_viewer.putExtra("key_ad_id", ad_id);
                intent_image_viewer.putExtra("key_loc", 0);

                startActivity(intent_image_viewer);
            }
        });

        ImageButton map_post = findViewById(R.id.AdMapButton);

        map_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "isServicesOk: checking google services version");

                int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(AdView.this);

                if (available == ConnectionResult.SUCCESS) {
                    Log.d(TAG, "isServicesOk: Google Play Services is working");

                    Intent intent_ad_map_set = new Intent(AdView.this, AdMap.class);

                    startActivity(intent_ad_map_set);
                }
                else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
                    Log.d(TAG, "isServicesOk: an error occured");
                    Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(AdView.this, available, ERROR_DIALOG_REQUEST);
                    dialog.show();
                }
                else {
                    Toast.makeText(AdView.this, "CANNOT MAKE MAP REQUESTS", Toast.LENGTH_LONG).show();
                }
            }
        });

        rent_request = findViewById(R.id.RentRequestButton);

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("user").child(user_id);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() == null) {
                    Intent intent_user_profile = new Intent(AdView.this, AdminProfile.class);

                    rent_request.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (getIntent().getIntExtra("key_adloc", 1) != 1) {
            rent_request.setVisibility(View.GONE);
        }

        rent_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database = FirebaseDatabase.getInstance();
                dbRef = database.getReference("ad").child(ad_id);

                dbRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        SendRentRequest(snapshot.child("user_id").getValue().toString(), user_id, ad_id);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        Button download_ad = findViewById(R.id.DownloadAdViewPDF);

        download_ad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DownloadAd(ad_id);
            }
        });
    }

    void AdCheck(String ad_id, int ad_loc, String user_id) {
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("ad").child(ad_id);

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (ad_loc == 1 && (Integer.parseInt(snapshot.child("approved").getValue().toString()) == 0 || Integer.parseInt(snapshot.child("rented").getValue().toString()) == 1)) {
                    Toast.makeText(AdView.this, "Rental No Longer Available", Toast.LENGTH_LONG).show();

                    Intent intent_homepagereturn = new Intent(AdView.this, Homepage.class);

                    startActivity(intent_homepagereturn);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void AdDetails(String ad_id, String address_id, String rent_id) {
        TextView title = findViewById(R.id.AdTitle);
        ad_publisherButton = findViewById(R.id.AdPublisher);
        TextView location = findViewById(R.id.AdLocation);
        TextView seat = findViewById(R.id.AdSeats);
        TextView gender_pref = findViewById(R.id.GenderPref);
        TextView house_no = findViewById(R.id.AdHouseNo);
        TextView road_no = findViewById(R.id.AdRoadNo);
        TextView block_no = findViewById(R.id.AdBlockNo);
        TextView section_no = findViewById(R.id.AdSectionNo);
        TextView floor_no = findViewById(R.id.AdFloorNo);
        TextView rent = findViewById(R.id.AdRent);
        TextView water_bill = findViewById(R.id.AdWaterBill);
        TextView gas_bill = findViewById(R.id.AdGasBill);
        TextView electricity_bill = findViewById(R.id.AdElectricityBill);
        TextView internet_bill = findViewById(R.id.AdInternetBill);
        TextView description = findViewById(R.id.AdDescription);

        TextView house_no_label = findViewById(R.id.HouseNoLabel);
        TextView road_no_label = findViewById(R.id.RoadNoLabel);
        TextView block_no_label = findViewById(R.id.BlockNoLabel);
        TextView section_no_label = findViewById(R.id.SectionNoLabel);
        TextView floor_no_label = findViewById(R.id.FloorNoLabel);
        TextView rent_label = findViewById(R.id.RentLabel);
        TextView water_bill_label = findViewById(R.id.WaterBillLabel);
        TextView gas_bill_label = findViewById(R.id.GasBillLabel);
        TextView electricity_bill_label = findViewById(R.id.ElectricityBillLabel);
        TextView internet_bill_label = findViewById(R.id.InternetBillLabel);
        TextView description_label = findViewById(R.id.DescriptionLabel);

        database = FirebaseDatabase.getInstance();


        dbRef = database.getReference("ad").child(ad_id);
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                title.setText(snapshot.child("heading").getValue().toString());
                ad_publisherButton.setText(snapshot.child("user_name").getValue().toString());
                location.setText(snapshot.child("location").getValue().toString());
                seat.setText(snapshot.child("seats").getValue().toString());
                gender_pref.setText(snapshot.child("gender_pref").getValue().toString());

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

    void SendRentRequest(String ad_publisher_id, String user_id, String ad_id) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMMM HH:mm");
        LocalDateTime now = LocalDateTime.now();
        String time = dtf.format(now);

        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("rent-request").child(ad_id);

        HashMap req = new HashMap();
        req.put("user_id", user_id);
        req.put("time", time);

        dbRef.child(getRandomKey()).updateChildren(req).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                Toast.makeText(AdView.this, "Rent Request Sent", Toast.LENGTH_LONG).show();
            }
        });
    }

    String getRandomKey() {
        Random random = new Random();
        // Generates random integers 0 to 49
        int x = random.nextInt(25);

        if(x == 0) {
            x = 8;
        }

        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < x) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();

        return saltStr;
    }

    void DownloadAd(String ad_id) {
        TextView title = findViewById(R.id.AdTitle);
        ad_publisherButton = findViewById(R.id.AdPublisher);
        TextView location = findViewById(R.id.AdLocation);
        TextView seat = findViewById(R.id.AdSeats);
        TextView gender_pref = findViewById(R.id.GenderPref);
        TextView house_no = findViewById(R.id.AdHouseNo);
        TextView road_no = findViewById(R.id.AdRoadNo);
        TextView block_no = findViewById(R.id.AdBlockNo);
        TextView section_no = findViewById(R.id.AdSectionNo);
        TextView floor_no = findViewById(R.id.AdFloorNo);
        TextView rent = findViewById(R.id.AdRent);
        TextView water_bill = findViewById(R.id.AdWaterBill);
        TextView gas_bill = findViewById(R.id.AdGasBill);
        TextView electricity_bill = findViewById(R.id.AdElectricityBill);
        TextView internet_bill = findViewById(R.id.AdInternetBill);
        TextView description = findViewById(R.id.AdDescription);

        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(250, 550, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        paint.setColor(Color.rgb(0, 0, 0));

        int count = 0;

        char ch[] = new char[title.getText().toString().length()];

        for (int i = 0; i<title.getText().toString().length(); i++) {
            ch[i] = title.getText().toString().charAt(i);

            if (Character.isWhitespace(ch[i])) {
                count++;

                if (count == 4) {
                    ch[i] = '\n';
                    count = 0;
                }
            }
        }

        String title_pdf = String.valueOf(ch);
        int y = 50;

        for (String line:title_pdf.split("\n")){
            paint.setTextSize(15.5f);
            paint.setFakeBoldText(true);
            canvas.drawText(line, 20, y, paint);

            y+=paint.descent()-paint.ascent();
        }

        paint.setTextSize(10.0f);
        paint.setFakeBoldText(true);
        canvas.drawText("Ad By: " + ad_publisherButton.getText().toString(), 20, 110, paint);

        paint.setFakeBoldText(false);

        paint.setTextSize(10.0f);
        canvas.drawText("Location: " + location.getText().toString(), 20, 130, paint);

        paint.setTextSize(10.0f);
        canvas.drawText("No. Of Seats: " + seat.getText().toString(), 20, 150, paint);

        paint.setTextSize(10.0f);
        canvas.drawText("Gender Preference: " + gender_pref.getText().toString(), 20, 170, paint);

        paint.setTextSize(13.0f);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);
        canvas.drawText("Address", 120, 230, paint);

        paint.setFakeBoldText(false);

        paint.setTextSize(10.0f);
        canvas.drawText("House No.: " + house_no.getText().toString() + " Road No.: " + road_no.getText().toString() + " Block No.: " + block_no.getText().toString(), 120, 250, paint);

        paint.setTextSize(10.0f);
        canvas.drawText("Section: " + section_no.getText().toString() + " Floor No.: " + floor_no.getText().toString(), 120, 270, paint);

        paint.setTextSize(13.0f);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);
        canvas.drawText("Rent", 120, 330, paint);

        paint.setFakeBoldText(false);

        paint.setTextSize(10.0f);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Rent: " + rent.getText().toString(), 120, 350, paint);

        paint.setTextSize(10.0f);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Water Bill: " + water_bill.getText().toString(), 120, 370, paint);

        paint.setTextSize(10.0f);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Electricity Bill: " + electricity_bill.getText().toString(), 120, 390, paint);

        paint.setTextSize(10.0f);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Gas Bill: " + gas_bill.getText().toString(), 120, 420, paint);

        paint.setTextSize(10.0f);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Internet Bill: " + internet_bill.getText().toString(), 120, 450, paint);

        paint.setTextSize(10.0f);
        canvas.drawText("Description: ", 120, 480, paint);

        count = 0;

        char ch_desc[] = new char[description.getText().toString().length()];

        for (int i = 0; i<description.getText().toString().length(); i++) {
            ch_desc[i] = description.getText().toString().charAt(i);

            if (Character.isWhitespace(ch_desc[i])) {
                count++;

                if (count == 4) {
                    ch_desc[i] = '\n';
                    count = 0;
                }
            }
        }

        String description_pdf = String.valueOf(ch_desc);
        y = 510;

        for (String line:description_pdf.split("\n")){
            paint.setTextSize(10.0f);
            canvas.drawText(line, 120, y, paint);

            y+=paint.descent()-paint.ascent();
        }

        pdfDocument.finishPage(page);

        File file = new File(this.getExternalFilesDir("/Address/"), "Address - " + ad_id + ".pdf");

        try {
            pdfDocument.writeTo(new FileOutputStream(file));

            Toast.makeText(AdView.this, "Address PDF Download Successful", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }

        pdfDocument.close();

    }

    void GoToProfile() {
        String user_id = firebaseAuth.getCurrentUser().getUid();

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("user").child(user_id);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() == null) {
                    Intent intent_user_profile = new Intent(AdView.this, AdminProfile.class);

                    startActivity(intent_user_profile);
                }
                else {
                    Intent intent_user_profile = new Intent(AdView.this, UserProfile.class);

                    startActivity(intent_user_profile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
