package com.example.studentroomrental;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

public class PasswordSet extends AppCompatActivity {

    Button reguser;
    EditText reg_pass, reg_passconf;
    CheckBox reg_showpass;

    String reg_username, reg_phone, reg_email, reg_institute, reg_district, reg_gender, reg_password;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_set);

        firebaseAuth = FirebaseAuth.getInstance();

        reguser = (Button) findViewById(R.id.SignUp);
        reg_pass = (EditText) findViewById(R.id.RegPass);
        reg_passconf = (EditText) findViewById(R.id.RegPassConf);
        reg_showpass = (CheckBox) findViewById(R.id.RegShowPass);

        reg_showpass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    reg_pass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    reg_passconf.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }

                else {
                    reg_pass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    reg_passconf.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        reg_username = getIntent().getStringExtra("key_regusername").trim();
        reg_phone = getIntent().getStringExtra("key_regphone");
        reg_email = getIntent().getStringExtra("key_regemail").trim();
        reg_institute = getIntent().getStringExtra("key_reginstitute").trim();
        reg_district = getIntent().getStringExtra("key_regdistrict").trim();
        reg_gender = getIntent().getStringExtra("key_reggender").trim();

        reguser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (reg_pass.getText().toString().isEmpty()) {
                    Toast.makeText(PasswordSet.this, "Please Type In The Password", Toast.LENGTH_LONG).show();
                }

                else if (reg_passconf.getText().toString().isEmpty()) {
                    Toast.makeText(PasswordSet.this, "Please Confirm The Password", Toast.LENGTH_LONG).show();
                }

                else if (reg_pass.getText().toString().length() < 6) {
                    Toast.makeText(PasswordSet.this, "Password Length Must Be Greater Than 6 Characters", Toast.LENGTH_LONG).show();
                }

                else if (!reg_pass.getText().toString().equals(reg_passconf.getText().toString())) {
                    Toast.makeText(PasswordSet.this, "Password And Password Confirmation Do Not Match", Toast.LENGTH_LONG).show();
                }

                else {
                    reg_password = reg_pass.getText().toString();
                    RegisterUser();
                }
            }
        });
    }

    void RegisterUser(){
        firebaseAuth.createUserWithEmailAndPassword(reg_email, reg_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String user_id = firebaseAuth.getCurrentUser().getUid();
                    User user = new User(reg_username, reg_phone, reg_institute, reg_district, reg_gender, reg_email);

                    DatabaseReference mDatabase;
                    mDatabase = FirebaseDatabase.getInstance().getReference().child("user").child(user_id);
                    mDatabase.setValue(user);

                    Toast.makeText(PasswordSet.this, "Registration Successful", Toast.LENGTH_LONG).show();

                    Intent intent_regtohome = new Intent(PasswordSet.this, Homepage.class);

                    startActivity(intent_regtohome);
                    finish();
                }

                else {
                    Toast.makeText(PasswordSet.this, "Something Went Wrong. Please Try Again Later.", Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PasswordSet.this, "Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        }).addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                Toast.makeText(PasswordSet.this, "Something Went Wrong. Please Try Again Later.", Toast.LENGTH_LONG).show();
            }
        });
    }
}