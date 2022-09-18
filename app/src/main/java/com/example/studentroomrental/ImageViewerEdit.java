package com.example.studentroomrental;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class ImageViewerEdit extends AppCompatActivity implements ImageDeleteListener {

    private static final int PICK_IMAGE = 1;

    RecyclerView image_viewer_editHolder;
    ImageViewerAdapter imageViewerAdapter;
    ArrayList<AdImages> image_viewer_editList;

    FirebaseDatabase database;
    DatabaseReference dbRef;
    StorageReference storageReference;

    private ProgressDialog progressDialog;

    String ad_id;

    ArrayList<Uri> ImageList = new ArrayList<Uri>();
    private Uri ImageUri;
    int total_upload_images = 0;
    int count = 1;
    int delete_count = 1;
    int total_ad_images = 0;

    ArrayList<AdImages> deleteList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer_edit);

        image_viewer_editHolder = findViewById(R.id.ImageViewerEditHolder);

        progressDialog = new ProgressDialog(this);

        Button delete_image = findViewById(R.id.DeleteImages);
        Button upload_more = findViewById(R.id.UploadMoreImages);

        image_viewer_editHolder.setHasFixedSize(true);
        image_viewer_editHolder.setLayoutManager(new LinearLayoutManager(this));

        ad_id = getIntent().getStringExtra("key_ad_id");

        GetAdImages(ad_id);

        upload_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent choose_image = new Intent(Intent.ACTION_GET_CONTENT);
                choose_image.setType("image/");
                choose_image.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(choose_image, PICK_IMAGE);
            }
        });

        delete_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog image_delete_conf = new AlertDialog.Builder(ImageViewerEdit.this).create();
                image_delete_conf.setTitle("Delete Images?");
                image_delete_conf.setMessage("Are You Sure You Want To Delete The Selected Images?");
                image_delete_conf.setButton(AlertDialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        progressDialog.setMessage("Deleting Images");
                        progressDialog.show();

                        for (AdImages image: deleteList) {
                            System.out.println(image.getImageURL());

                            storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(image.getImageURL().toString());

                            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {

                                    database = FirebaseDatabase.getInstance();
                                    dbRef = database.getReference("ad_images").child(ad_id);

                                    dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                                                if (dataSnapshot.child("imageURL").getValue().toString().equals(image.getImageURL().toString())) {

                                                    dbRef.child(dataSnapshot.getKey()).removeValue();
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                    if (delete_count == deleteList.size()) {
                                        Toast.makeText(ImageViewerEdit.this, "Images Deleted From Ad", Toast.LENGTH_LONG).show();

                                        Intent intent_imageviewer = new Intent(ImageViewerEdit.this, ImageViewerEdit.class);

                                        startActivity(intent_imageviewer);
                                        finish();
                                    }

                                    delete_count++;

                                }
                            });
                        }
                    }
                });

                image_delete_conf.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        for (AdImages x: deleteList) {
                            System.out.println(x.getImageURL());
                        }
                        image_delete_conf.dismiss();
                    }
                });

                image_delete_conf.show();
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

                    total_upload_images = ImageList.size();

                    AlertDialog new_upload_conf = new AlertDialog.Builder(ImageViewerEdit.this).create();
                    new_upload_conf.setTitle("Upload Images?");
                    new_upload_conf.setMessage("Are You Sure You Want To Add More Images To This Post?");
                    new_upload_conf.setButton(AlertDialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            UploadMoreImages();

                        }
                    });

                    new_upload_conf.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            new_upload_conf.dismiss();
                        }
                    });

                    new_upload_conf.show();
                }

                else {
                    Toast.makeText(ImageViewerEdit.this, "Please Add Images With Your Post", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public String GetFileExtension(Uri uri){
        ContentResolver contentResolver=getContentResolver();
        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();

        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    void GetAdImages(String ad_id) {
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("ad_images").child(ad_id);

        image_viewer_editList = new ArrayList<>();
        imageViewerAdapter = new ImageViewerAdapter(this, image_viewer_editList, this, 2);

        image_viewer_editHolder.setAdapter(imageViewerAdapter);

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                image_viewer_editList.clear();
                total_ad_images = Integer.parseInt(String.valueOf(snapshot.getChildrenCount()));
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    AdImages adImages = dataSnapshot.getValue(AdImages.class);

                    image_viewer_editList.add(adImages);
                }

                imageViewerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    void UploadMoreImages() {
        progressDialog.setMessage("Uploading Image");
        progressDialog.show();

        storageReference = FirebaseStorage.getInstance().getReference().child("Ad Images").child(ad_id);

        for (int i=0; i<total_upload_images; i++) {
            Uri images = ImageList.get(i);

            String rename_image = "Photo " + String.valueOf(total_ad_images + i + 1)+ "." + GetFileExtension(images);

            StorageReference img_title = storageReference.child(rename_image);

            img_title.putFile(images).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    img_title.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String img_url = String.valueOf(uri);

                            AddImageLink(img_url, ad_id);

                            if (count == total_upload_images) {
                                progressDialog.dismiss();
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

    @Override
    public void onImageDeleteListenerChange(ArrayList<AdImages> arrayList) {

        deleteList = arrayList;
        System.out.println(arrayList.size());
    }
}