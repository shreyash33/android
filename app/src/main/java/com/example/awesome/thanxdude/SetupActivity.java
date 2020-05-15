package com.example.awesome.thanxdude;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import de.hdodenhof.circleimageview.CircleImageView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class SetupActivity extends AppCompatActivity {

    private CircleImageView setupImage;
    private EditText name;
    private EditText mob_no;
    private Button setupbtn;
    private ProgressBar setupprogress;
    private Uri mainImageURI = null;


    private String userName;
    private String mobileNumber;
    private String user_id;

    private Boolean isChanged = false;

    public FirebaseAuth mAuth;
    public  StorageReference mStorageReference;
    public  FirebaseFirestore mFirebaseFirestore;
    public StorageReference image_path;
    public Uri download_uri= null;
    StorageReference ref;
    Uri testuri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        setupImage = findViewById(R.id.setup_profile_image);
        name = findViewById(R.id.setup_name);
        mob_no = findViewById(R.id.setup_Mobile);
        setupbtn = findViewById(R.id.setupBtn);
        setupprogress = findViewById(R.id.setup_progress);
        mAuth = FirebaseAuth.getInstance();
        user_id = mAuth.getCurrentUser().getUid();
        mStorageReference = FirebaseStorage.getInstance().getReference();

        mFirebaseFirestore = FirebaseFirestore.getInstance();

        setupprogress.setVisibility(View.VISIBLE);
        setupbtn.setEnabled(false);

        mFirebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful())
                {

                    if(task.getResult().exists())
                    {
                        String Dname = task.getResult().getString("UserName");
                        String Dmobile = task.getResult().getString("MobileNumber");
                        String Dimage = task.getResult().getString("ProfileImage");

                       mainImageURI = Uri.parse(Dimage);

                        name.setText(Dname);
                        mob_no.setText(Dmobile);
                        RequestOptions placeholderrequest = new RequestOptions();
                        placeholderrequest.placeholder(R.drawable.default_image);

                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placeholderrequest).load(Dimage).into(setupImage);


                    }




                }
                else
                {
                    String error = task.getException().getMessage().toString();
                    Toast.makeText(SetupActivity.this,"Firestore Retrieve  Error:"+ error, Toast.LENGTH_LONG).show();


                }
                setupprogress.setVisibility(View.INVISIBLE);
                setupbtn.setEnabled(true);

            }
        });

        setupbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                userName = name.getText().toString();
                mobileNumber = mob_no.getText().toString();
                setupprogress.setVisibility(View.VISIBLE);

                if(( !TextUtils.isEmpty(userName) && !TextUtils.isEmpty(mobileNumber)) && mainImageURI != null )
                {
                    if(isChanged)
                    {

                        user_id = mAuth.getCurrentUser().getUid();

                              image_path = mStorageReference.child("profile_images").child(user_id + ".jpeg");
                              image_path.putFile(mainImageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                if(task.isSuccessful())
                                {
                                       image_path.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                           @Override
                                           public void onComplete(@NonNull Task<Uri> task) {
                                               testuri = task.getResult();
                                           }
                                       });
                                    storeFirestore(task,userName,mobileNumber,user_id,mainImageURI,testuri);

                                }
                                else
                                {
                                    String error = task.getException().getMessage().toString();
                                    Toast.makeText(SetupActivity.this,"Error:"+ error, Toast.LENGTH_LONG).show();
                                    setupprogress.setVisibility(View.INVISIBLE);

                                }

                                setupprogress.setVisibility(View.INVISIBLE);
                            }
                        });

                    }
                    else
                    {
                        storeFirestore(null,userName,mobileNumber, user_id, mainImageURI, mainImageURI);
                    }
                }


            }
        });

        setupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                {
                    if(ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                    {
                        ActivityCompat.requestPermissions(SetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                        Toast.makeText(SetupActivity.this,"You have permission",Toast.LENGTH_LONG).show();

                    }
                    else
                    {
                        // start picker to get image for cropping and then use the image in cropping activity
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1,1)
                                .start(SetupActivity.this);
                    }
                }
            }
        });
    }

    private void storeFirestore(Task<UploadTask.TaskSnapshot> task, final String userName, final String mobileNumber, final String user_id, final Uri imageURI, Uri mainImageURI) {


        // download_uri= null;
        if (task != null) {
            Log.i("userid", user_id);
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            storageReference.child("profile_images/").child(user_id + ".jpeg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    download_uri = uri;


            Map<String, String> usermap = new HashMap<>();
            usermap.put("UserName", userName);
            usermap.put("MobileNumber", mobileNumber);
            usermap.put("ProfileImage", download_uri.toString());

            mFirebaseFirestore.collection("Users").document(user_id).set(usermap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful()) {
                        Toast.makeText(SetupActivity.this, "User settings has been updated ", Toast.LENGTH_LONG).show();
                        Intent mainintent = new Intent(SetupActivity.this, MainActivity.class);
                        startActivity(mainintent);
                        finish();
                    } else {
                        String error = task.getException().getMessage().toString();
                        Toast.makeText(SetupActivity.this, "Firestore Error:" + error, Toast.LENGTH_LONG).show();

                    }

                }

            });
            setupprogress.setVisibility(View.INVISIBLE);
                }
            });

        } else {
            Log.i("else ", "me hu");
            download_uri = imageURI;


            Log.i("download uri is",download_uri.toString());

            Map<String, String> usermap = new HashMap<>();
            usermap.put("UserName", userName);
            usermap.put("MobileNumber", mobileNumber);
            usermap.put("ProfileImage", download_uri.toString());

            mFirebaseFirestore.collection("Users").document(this.user_id).set(usermap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful()) {
                        Toast.makeText(SetupActivity.this, "User settings has been updated ", Toast.LENGTH_LONG).show();
                        Intent mainintent = new Intent(SetupActivity.this, MainActivity.class);
                        startActivity(mainintent);
                        finish();
                    } else {
                        String error = task.getException().getMessage().toString();
                        Toast.makeText(SetupActivity.this, "Firestore Error:" + error, Toast.LENGTH_LONG).show();

                    }

                }

            });
            setupprogress.setVisibility(View.INVISIBLE);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK)
            {
                mainImageURI = result.getUri();
                setupImage.setImageURI(mainImageURI);

                isChanged = true;
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Exception error = result.getError();
            }
        }
    }
}
