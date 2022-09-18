package com.example.studentroomrental;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UserRegistration extends AppCompatActivity {

    private Spinner reg_genderSpinner;
    EditText reg_username, reg_phone, reg_email, reg_institute, reg_district;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);

        reg_genderSpinner = findViewById(R.id.RegGender);
        ArrayList<String> reg_gender = new ArrayList<>();

        reg_gender.add("Gender");
        reg_gender.add("Female");
        reg_gender.add("Male");

        ArrayAdapter<String> reg_genderAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                reg_gender
        );

        reg_genderSpinner.setAdapter(reg_genderAdapter);

        reg_username = (EditText) findViewById(R.id.RegUserName);
        reg_phone = (EditText) findViewById(R.id.RegPhone);
        reg_email = (EditText) findViewById(R.id.RegUserEmail);
        reg_institute = (EditText) findViewById(R.id.RegUserInstitute);
        reg_district = (EditText) findViewById(R.id.RegUserDistrict);

        Button reg_cont = findViewById(R.id.RegCont);

        reg_cont.setOnClickListener(view -> {
            if (reg_username.getText().toString().isEmpty()
                    || reg_phone.getText().toString().isEmpty()
                    || reg_email.getText().toString().isEmpty()
                    || reg_district.getText().toString().isEmpty()
                    || reg_genderSpinner.getSelectedItem().toString().equals("Gender")) {

                Toast.makeText(UserRegistration.this, "Please Provide All Information", Toast.LENGTH_LONG).show();
            }

            else if (!Patterns.EMAIL_ADDRESS.matcher(reg_email.getText().toString()).matches()) {
                Toast.makeText(UserRegistration.this, "Invalid Email", Toast.LENGTH_LONG).show();
            }

            else {
                String reg_ins = "";
                if (!reg_institute.getText().toString().isEmpty()) {
                    reg_ins = reg_institute.getText().toString();
                }

                Intent intent_reg_cont = new Intent(this, PasswordSet.class);

                intent_reg_cont.putExtra("key_regusername", reg_username.getText().toString());
                intent_reg_cont.putExtra("key_regphone", reg_phone.getText().toString());
                intent_reg_cont.putExtra("key_regemail", reg_email.getText().toString());
                intent_reg_cont.putExtra("key_reginstitute", reg_ins);
                intent_reg_cont.putExtra("key_regdistrict", reg_district.getText().toString());
                intent_reg_cont.putExtra("key_reggender", reg_genderSpinner.getSelectedItem().toString());

                startActivity(intent_reg_cont);
            }

        });
    }
}