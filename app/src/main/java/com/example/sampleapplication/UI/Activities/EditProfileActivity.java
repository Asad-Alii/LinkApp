package com.example.sampleapplication.UI.Activities;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.sampleapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IAMGE_REQUEST = 1;
    ImageView profileImage;
    EditText name, emaill, contactt, passwordd, imageurl;
    Button editprofileBtn;
    private FirebaseAuth auth;
    private FirebaseFirestore FirebaseApp;
    private StorageReference mStorageRef;
    ProgressDialog dialog;
    String imageUrl;
    private Uri mImageUri;
    SharedPreferences sharedMemory;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editprofile);

        FirebaseApp = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference("ProfileImages");
        dialog = new ProgressDialog(this);
        sharedMemory = getSharedPreferences("testing", MODE_PRIVATE);

        profileImage = findViewById(R.id.current_image);
        name = findViewById(R.id.name_profile);
        emaill = findViewById(R.id.email_profile);
        contactt = findViewById(R.id.contact_profile);
        passwordd = findViewById(R.id.password_profile);
        imageurl = findViewById(R.id.imageurl_profile);
        editprofileBtn = findViewById(R.id.editProfile_btn);
        final String senderId = auth.getCurrentUser().getUid();

        dialog.setMessage("Loading...");
        dialog.show();

        loadSenderProfile(senderId);

        editprofileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                uploadFile();
                updateSenderProfile(senderId);

            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });
    }

    public void loadSenderProfile(String senderId){

        FirebaseApp.collection("users").document(senderId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        DocumentSnapshot document = task.getResult();

                        String id = document.getId();
                        String username = document.getString("username");
                        String email= document.getString("email");
                        String contact= document.getString("contact");
                        String password= document.getString("password");
                        imageUrl= document.getString("imageUrl");

                        dialog.dismiss();
                        name.setText(username);
                        emaill.setText(email);
                        contactt.setText(contact);
                        passwordd.setText(password);
                        imageurl.setText(imageUrl);
                        Glide.with(getApplicationContext()).load(imageUrl).into(profileImage);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditProfileActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void updateSenderProfile(String senderId){

        String name1 = name.getText().toString();
        String email = emaill.getText().toString();
        String mobileno = contactt.getText().toString();
        final String password = passwordd.getText().toString();
        String image = imageurl.getText().toString();

        final AuthCredential credential = EmailAuthProvider
                .getCredential(email, password);

        final Map<String, String> user = new HashMap<>();
        user.put("username", name1);
        user.put("email", email);
        user.put("contact", mobileno);
        user.put("password", password);
        user.put("imageUrl", image);

        FirebaseApp.collection("users").document(senderId)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void avoid) {

                        Toast.makeText(EditProfileActivity.this, "Successfull", Toast.LENGTH_SHORT).show();
                        Intent in = new Intent(EditProfileActivity.this, UserActivity.class);
                        startActivity(in);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Log.w(TAG, "Error adding document", e);
                        Toast.makeText(EditProfileActivity.this, "Unsuccessfull", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openFileChooser() {

        Intent in = new Intent();
        in.setType("image/*");
        in.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(in, PICK_IAMGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IAMGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            mImageUri = data.getData();
            Glide.with(EditProfileActivity.this).load(mImageUri.toString()).into(profileImage);
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    private void uploadFile() {

        final String currentuser = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (mImageUri != null) {
            final StorageReference fileRef = mStorageRef.child(currentuser + "." + getFileExtension(mImageUri));

            fileRef.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    final String currentuser = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            Map<String, Object> map = new HashMap<>();
                            map.put("imageUrl", uri.toString());
                            //Toast.makeText(SignupActivity.this, uri.toString(), Toast.LENGTH_SHORT).show();
                            FirebaseApp.collection("users").document(currentuser).update(map);
                            //nameTxt.setText(uri.toString());
                            //Picasso.with(SignupActivity.this).load(uri.toString()).into(profileImage);
                            //Picasso.with(SignupActivity.this).load(uri.toString()).error(R.drawable.logo).placeholder(R.drawable.ic_send).into(profileImage);
                            //Glide.with(SignupActivity.this).load(uri.toString()).into(profileImage);

                        }
                    });

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //mProgressBar.setProgress(0);
                        }
                    }, 300);
                    Toast.makeText(EditProfileActivity.this, "Upload Successful!", Toast.LENGTH_SHORT).show();
                    //ProfileImage img = new ProfileImage("ProfileImage" + fileRef.getDownloadUrl().toString());
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            //mProgressBar.setProgress((int) progress);
                        }
                    });
        } else {
            Toast.makeText(this, "No file selected!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent in = new Intent(EditProfileActivity.this, UserActivity.class);
        startActivity(in);
    }
}
