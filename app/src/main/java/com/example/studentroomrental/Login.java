package com.example.studentroomrental;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {

    EditText login_id, login_pass;
    Button login_user, user_pass_change;
    CheckBox login_showpass;
    
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        firebaseAuth = FirebaseAuth.getInstance();

        Button register = findViewById(R.id.NewRegistrationButton);

        register.setOnClickListener(view -> {
            Intent intent_register = new Intent(this, UserRegistration.class);

            startActivity(intent_register);
        });

        login_id = (EditText) findViewById(R.id.LoginID);
        login_pass = (EditText) findViewById(R.id.LoginPass);
        login_user = (Button) findViewById(R.id.LoginButton);
        user_pass_change = (Button) findViewById(R.id.PassChangeButton);
        login_showpass = (CheckBox) findViewById(R.id.LoginShowPass);

        login_showpass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    login_pass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }

                else {
                    login_pass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        login_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (login_id.getText().toString().isEmpty() && login_pass.getText().toString().isEmpty()) {
                    Toast.makeText(Login.this, "Please Type In Email Address And Password", Toast.LENGTH_LONG).show();
                }

                else if (login_id.getText().toString().isEmpty()) {
                    Toast.makeText(Login.this, "Please Type In Email Address", Toast.LENGTH_LONG).show();
                }

                else if (login_pass.getText().toString().isEmpty()) {
                    Toast.makeText(Login.this, "Please Type In Password", Toast.LENGTH_LONG).show();
                }

                else if (!Patterns.EMAIL_ADDRESS.matcher(login_id.getText().toString().trim()).matches()) {
                    Toast.makeText(Login.this, "Invalid Email", Toast.LENGTH_LONG).show();
                }

                else {
                    Login();
                }
            }
        });

        user_pass_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (login_id.getText().toString().isEmpty()) {
                    Toast.makeText(Login.this, "Please Type In Email Address", Toast.LENGTH_LONG).show();
                }

                else {
                    PassForget();
                }
            }
        });
    }

    void Login() {
        firebaseAuth.signInWithEmailAndPassword(login_id.getText().toString().trim(), login_pass.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Intent intent_login = new Intent(Login.this, Homepage.class);

                    startActivity(intent_login);
                    finish();
                }

                else {
                    Toast.makeText(Login.this, "Something Went Wrong. Please Try Again Later.", Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Login.this, "Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        }).addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                Toast.makeText(Login.this, "Something Went Wrong. Please Try Again Later.", Toast.LENGTH_LONG).show();
            }
        });
    }

    void PassForget() {
        firebaseAuth.sendPasswordResetEmail(login_id.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(Login.this, "Password Reset Link Sent To Email", Toast.LENGTH_LONG).show();
                }

                else {
                    Toast.makeText(Login.this, "Something Went Wrong. Please Try Again Later.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}