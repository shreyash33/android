package com.example.awesome.thanxdude;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import id.zelory.compressor.Compressor;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class NewPostActivity extends AppCompatActivity {

    private Toolbar newPostToolbar;

    private ImageView newPostImage;
    private EditText newPostDesc;
    private Button newPostBtn;
    private Compressor compressedImageFile;

    private ProgressBar newpostprogress;

    private Uri postImageURI = null;
    private StorageReference  mStorageReference;
    private FirebaseFirestore mFirebaseFirestore;
    private FirebaseAuth mAuth;

    private String current_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        current_user_id = mAuth.getCurrentUser().getUid();

        newPostToolbar = findViewById(R.id.new_post_toolbar);
        setSupportActionBar(newPostToolbar);
        getSupportActionBar().setTitle("Add New Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        newPostImage = findViewById(R.id.new_post_image);
        newPostDesc = findViewById(R.id.newpost_description);
        newPostBtn = findViewById(R.id.post_Btn);
        newpostprogress = findViewById(R.id.newpostprogress);


        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512,512)
                        .setAspectRatio(1,1)
                        .start(NewPostActivity.this);

            }
        });

        newPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String desc = newPostDesc.getText().toString();

                if(!TextUtils.isEmpty(desc) && postImageURI != null)
                {


                    String randomName = UUID.randomUUID().toString();

                    final StorageReference filePath = mStorageReference.child("post_images").child(randomName +".jpg");
                    filePath.putFile(postImageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful())
                            {
                                newpostprogress.setVisibility(View.VISIBLE);
                                File newImageFile = new File(postImageURI.getPath());

                               //  compressedImageFile = new Compressor(NewPostActivity.this).compressToFile(newImageFile);


                                 filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {

                                        String download_uri = uri.toString();

                                        Map<String, Object> postMap = new HashMap<>();
                                        postMap.put("image_url",download_uri);
                                        postMap.put("desc",desc);
                                        postMap.put("user_id", current_user_id);
                                        postMap.put("timestamp",FieldValue.serverTimestamp());

                                        mFirebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {

                                                if(task.isSuccessful())
                                                {
                                                    Toast.makeText(NewPostActivity.this,"Post is added", Toast.LENGTH_LONG).show();
                                                    Intent mainintent = new Intent(NewPostActivity.this,MainActivity.class);
                                                    startActivity(mainintent);
                                                    finish();

                                                }
                                                else
                                                {
                                                    newpostprogress.setVisibility(View.INVISIBLE);
                                                    String error = task.getException().getMessage().toString();
                                                    Toast.makeText(NewPostActivity.this,"Error:"+ error, Toast.LENGTH_LONG).show();
                                                }

                                                newpostprogress.setVisibility(View.INVISIBLE);

                                            }
                                        });

                                    }
                                });




                            }
                            else
                            {
                                newpostprogress.setVisibility(View.INVISIBLE);
                                String error = task.getException().getMessage().toString();
                                Toast.makeText(NewPostActivity.this,"Error:"+ error, Toast.LENGTH_LONG).show();

                            }
                        }
                    });

                }

            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK)
            {
                postImageURI = result.getUri();
                newPostImage.setImageURI(postImageURI);
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Exception error = result.getError();
            }
        }
    }


}
