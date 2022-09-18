package com.example.studentroomrental;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Report extends AppCompatActivity {

    private Spinner report_monthSpinner;
    private Spinner report_yearSpinner;

    String admin_id, admin_name, highest_posted;
    int count_posted_ads = 0;
    int count_rented_ads = 0;
    int total_users;
    int new_users = 0;

    ArrayList<String> posted_users;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        admin_id = getIntent().getStringExtra("key_admin_id");
        admin_name = getIntent().getStringExtra("key_admin_name");

        report_monthSpinner = findViewById(R.id.ReportMonth);
        ArrayList<String> report_month = new ArrayList<>();
        report_yearSpinner = findViewById(R.id.ReportYear);
        ArrayList<String> report_year = new ArrayList<>();

        report_month.add("Select Month");
        report_month.add("January");
        report_month.add("February");
        report_month.add("March");
        report_month.add("April");
        report_month.add("May");
        report_month.add("June");
        report_month.add("July");
        report_month.add("August");
        report_month.add("September");
        report_month.add("October");
        report_month.add("November");
        report_month.add("December");

        ArrayAdapter<String> report_monthAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                report_month
        );

        report_monthSpinner.setAdapter(report_monthAdapter);

        report_year.add("Select Year");
        report_year.add("2022");
        report_year.add("2021");
        report_year.add("2020");

        ArrayAdapter<String> report_yearAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                report_year
        );

        report_yearSpinner.setAdapter(report_yearAdapter);

        Button monthly_report = findViewById(R.id.GenerateReportButton);

        monthly_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GenerateReport(report_monthSpinner.getSelectedItem().toString(), report_yearSpinner.getSelectedItem().toString());
            }
        });
    }

    void GenerateReport(String month, String year) {
        String report_date = month + " " + year;

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("user");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                total_users = Integer.parseInt(String.valueOf(snapshot.getChildrenCount()));
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    String rent_date = dataSnapshot.child("date_joined").getValue().toString();
                    SimpleDateFormat old_format_rented = new SimpleDateFormat("yyyy-MM-dd");
                    Date new_format_rented = null;
                    try {
                        new_format_rented = old_format_rented.parse(rent_date);
                        old_format_rented.applyPattern("MMMM yyyy");
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    String new_date_rented = old_format_rented.format(new_format_rented);

                    if(new_date_rented.equals(report_date)) {
                        new_users++;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = database.getReference("ad");

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                        String post_date = dataSnapshot.child("post_date").getValue().toString();
                        SimpleDateFormat old_format_posted = new SimpleDateFormat("yyyy-MM-dd");
                        Date new_format_posted = null;
                        try {
                            new_format_posted = old_format_posted.parse(post_date);
                            old_format_posted.applyPattern("MMMM yyyy");
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        String new_date_posted = old_format_posted.format(new_format_posted);

                        if(new_date_posted.equals(report_date)) {
                            count_posted_ads++;

                            posted_users.add(dataSnapshot.child("user_name").getValue().toString());
                        }

                        String rent_date = dataSnapshot.child("rent_date").getValue().toString();

                        if (rent_date.equals(" ")) {
                            SimpleDateFormat old_format_rented = new SimpleDateFormat("yyyy-MM-dd");
                            Date new_format_rented = null;
                            try {
                                new_format_rented = old_format_rented.parse(rent_date);
                                old_format_rented.applyPattern("MMMM yyyy");
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            String new_date_rented = old_format_rented.format(new_format_rented);

                            if(new_date_rented.equals(report_date)) {
                                count_rented_ads++;
                            }
                        }
                    }

                    if (count_posted_ads > 0) {
                        HashMap<String, Integer> hashMap = new HashMap<>();

                        for(String user: posted_users) {
                            hashMap.put(user, Collections.frequency(posted_users, user));
                        }

                        Map.Entry<String, Integer> maxEntry = null;

                        for (Map.Entry<String, Integer> entry: hashMap.entrySet()) {
                            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
                            {
                                maxEntry = entry;
                            }
                        }

                        highest_posted = maxEntry.getKey();
                    }

                    //GENERATE PDF
                    ActivityCompat.requestPermissions(Report.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

                    PdfDocument pdfDocument = new PdfDocument();
                    Paint paint = new Paint();
                    PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(200, 280, 1).create();
                    PdfDocument.Page page = pdfDocument.startPage(pageInfo);
                    Canvas canvas = page.getCanvas();

                    paint.setColor(Color.rgb(0, 0, 0));

                    paint.setTextSize(10.0f);
                    paint.setFakeBoldText(true);
                    canvas.drawText("STUDENT ROOM RENTAL", 30, 50, paint);

                    paint.setTextSize(8.0f);
                    canvas.drawText("Report For " + month + " "  + year, 30, 80, paint);

                    paint.setTextSize(8.0f);
                    canvas.drawText("Admin ID: " + admin_id, 30, 100, paint);

                    paint.setTextSize(8.0f);
                    canvas.drawText("Admin Name: " + admin_name, 30, 120, paint);

                    paint.setFakeBoldText(false);

                    if (count_posted_ads == 0) {
                        paint.setTextSize(6.0f);
                        canvas.drawText("Total Ads Posted: No Ads Were Posted", 10, 150, paint);
                    }

                    else {
                        paint.setTextSize(6.0f);
                        canvas.drawText("Total Ads Posted: " + String.valueOf(count_posted_ads), 10, 150, paint);
                    }

                    if (count_rented_ads == 0) {
                        paint.setTextSize(6.0f);
                        canvas.drawText("Total Rentals: No Rents Were Made", 10, 170, paint);
                    }

                    else {
                        paint.setTextSize(6.0f);
                        canvas.drawText("Total Rentals: " + String.valueOf(count_rented_ads), 10, 170, paint);
                    }

                    paint.setTextSize(6.0f);
                    canvas.drawText("Total Users: " + String.valueOf(total_users), 10, 200, paint);

                    if (new_users == 0) {
                        paint.setTextSize(6.0f);
                        canvas.drawText("Number Of New Users: No New Users", 10, 220, paint);
                    }

                    else {
                        paint.setTextSize(6.0f);
                        canvas.drawText("Number Of New Users: " + String.valueOf(new_users), 10, 220, paint);
                    }

                    if (count_posted_ads == 0) {
                        paint.setTextSize(6.0f);
                        canvas.drawText("Most Ads Posted By User: N/A", 10, 240, paint);
                    }

                    else {
                        paint.setTextSize(6.0f);
                        canvas.drawText("Most Ads Posted By User: " + highest_posted, 10, 240, paint);
                    }

                    pdfDocument.finishPage(page);

                    File file = new File(Report.this.getExternalFilesDir("/Monthly Report/"), month + " " + year + ".pdf");

                    try {
                        pdfDocument.writeTo(new FileOutputStream(file));

                        Toast.makeText(Report.this, "Monthly Report PDF Download Successful", Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    pdfDocument.close();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}