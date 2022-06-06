package com.example.studentroomrental;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class UserProfileEdit extends AppCompatActivity {

    private static final int PICK_FILE = 1;
    private Spinner user_gender_editSpinner;

    EditText user_name, user_phone, district, institute;
    TextView user_email;
    ImageView profile_image;
    String user_id;

    FirebaseAuth firebaseAuth;
    StorageReference storageReference;
    FirebaseStorage firebaseStorage;

    private ProgressDialog progressDialog;

    public Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_edit);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading Image");

        user_gender_editSpinner = findViewById(R.id.UserGenderEdit);
        ArrayList<String> user_gender_edit = new ArrayList<>();

        user_gender_edit.add("Female");
        user_gender_edit.add("Male");

        ArrayAdapter<String> user_gender_editAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                user_gender_edit
        );

        user_gender_editSpinner.setAdapter(user_gender_editAdapter);

        user_id = firebaseAuth.getCurrentUser().getUid();

        profile_image = findViewById(R.id.EditUserProfileImage);

        storageReference = FirebaseStorage.getInstance().getReference().child("User Profile Images/" + user_id + ".jpg");

        try {
            final File userProfileImage = File.createTempFile("profile_image", ".jpg");
            storageReference.getFile(userProfileImage).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap = BitmapFactory.decodeFile(userProfileImage.getAbsolutePath());
                    profile_image.setImageBitmap(bitmap);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //Toast.makeText(UserProfile.this, "Failed To Load Profile Image", Toast.LENGTH_LONG).show();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        setUserInfo(user_id);

        Button save_userinfo = findViewById(R.id.UserInfoEditSave);

        save_userinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateUserInfo(user_id);
            }
        });

        Button change_profile_image = findViewById(R.id.ProfileImageChange);

        change_profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChangeProfileImage();
            }
        });
    }

    void setUserInfo(String user_id) {
        user_name = findViewById(R.id.UserNameEdit);
        user_phone = findViewById(R.id.UserPhoneEdit);
        user_email = findViewById(R.id.UserEmailEdit);
        district = findViewById(R.id.UserDistrictEdit);
        institute = findViewById(R.id.UserInstituteEdit);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = database.getReference("user").child(user_id);

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user_name.setText(snapshot.child("user_name").getValue().toString());
                user_phone.setText(snapshot.child("user_phone").getValue().toString());
                user_email.setText(snapshot.child("user_email").getValue().toString());
                district.setText(snapshot.child("district").getValue().toString());

                if (snapshot.child("gender").getValue().toString().equals("Female")) {
                    user_gender_editSpinner.setSelection(0);
                }
                else {
                    user_gender_editSpinner.setSelection(1);
                }

                if (!snapshot.child("institute").getValue().toString().isEmpty()) {
                    institute.setText(snapshot.child("institute").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void UpdateUserInfo(String user_id) {
        user_name = findViewById(R.id.UserNameEdit);
        user_phone = findViewById(R.id.UserPhoneEdit);
        district = findViewById(R.id.UserDistrictEdit);
        institute = findViewById(R.id.UserInstituteEdit);
        user_gender_editSpinner = findViewById(R.id.UserGenderEdit);

        HashMap user_info_save = new HashMap();
        user_info_save.put("user_name", user_name.getText().toString());
        user_info_save.put("user_phone", user_phone.getText().toString());
        user_info_save.put("district", district.getText().toString());
        user_info_save.put("institute", institute.getText().toString());
        user_info_save.put("gender", user_gender_editSpinner.getSelectedItem().toString());

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = database.getReference("user");

        dbRef.child(user_id).updateChildren(user_info_save).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    Toast.makeText(UserProfileEdit.this, "Profile Updated", Toast.LENGTH_LONG).show();

                    Intent intent_userprofile = new Intent(UserProfileEdit.this, UserProfileEdit.class);

                    startActivity(intent_userprofile);
                    finish();
                }

                else {
                    Toast.makeText(UserProfileEdit.this, "Profile Update Unsuccessful", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uri = data.getData();
            profile_image.setImageURI(uri);
            uploadNewProfileImage();
        }
    }

    void ChangeProfileImage() {
        Intent intent_change_image = new Intent(Intent.ACTION_GET_CONTENT);

        intent_change_image.setType("image/");
        startActivityForResult(intent_change_image, PICK_FILE);
    }

    void uploadNewProfileImage() {
        AlertDialog change_image_conf = new AlertDialog.Builder(UserProfileEdit.this).create();
        change_image_conf.setTitle("Change Profile Image?");
        change_image_conf.setMessage("Are You Sure You Want To Change Profile Image?");
        change_image_conf.setButton(AlertDialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                progressDialog.show();

                storageReference = firebaseStorage.getReference().child("User Profile Images");

                StorageReference filename = storageReference.child(user_id + "." + GetFileExtension(uri));

                filename.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        Toast.makeText(UserProfileEdit.this, "Profile Image Updated", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        change_image_conf.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent_userprofile = new Intent(UserProfileEdit.this, UserProfileEdit.class);

                startActivity(intent_userprofile);
                finish();
            }
        });

        change_image_conf.show();
    }

    public String GetFileExtension(Uri uri){
        ContentResolver contentResolver=getContentResolver();
        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();

        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
}