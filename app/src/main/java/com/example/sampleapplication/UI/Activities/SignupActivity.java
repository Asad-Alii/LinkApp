package com.example.sampleapplication.UI.Activities;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.sampleapplication.Models.UserList;
import com.example.sampleapplication.Utils.PreferenceUtils;
import com.example.sampleapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SignupActivity extends AppCompatActivity {

    int Image_Request_Code = 7;

    EditText nameTxt, emailTxt, mobilenoTxt, passwordTxt, confirmpasswordTxt;
    Button signup, uploadBtn;
    //ImageView profileImage;
    private ProgressBar mProgressBar;
    private Uri mImageUri;

    FirebaseFirestore FirebaseApp;
    private FirebaseAuth auth;
    private StorageReference mStorageRef;
    ProgressDialog dialog;
    ImageView profileImage;

    //private static final String TAG = "SignupActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        FirebaseApp = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference("ProfileImages");
        dialog = new ProgressDialog(this);

        nameTxt = findViewById(R.id.name_txt);
        emailTxt = findViewById(R.id.email_txt);
        mobilenoTxt = findViewById(R.id.mobile_txt);
        passwordTxt = findViewById(R.id.password_txt);
        confirmpasswordTxt = findViewById(R.id.confirmpassword_txt);
        signup = findViewById(R.id.signup_btn);
        //profileImage = findViewById(R.id.profile_image);
//        mProgressBar = findViewById(R.id.progress_bar);
        uploadBtn = findViewById(R.id.upload_btn);
        profileImage = (ImageView) findViewById(R.id.profile_image);

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //openFileChooser();


                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, "Please Select Image"), Image_Request_Code);


            }
        });


        /*profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });*/

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = nameTxt.getText().toString();
                final String email = emailTxt.getText().toString();
                String mobileno = mobilenoTxt.getText().toString();
                final String password = passwordTxt.getText().toString();
                String confirmpassword = confirmpasswordTxt.getText().toString();

                dialog.setMessage("Signing up...");
                //dialog.show();
                //uploadFile();

                if (TextUtils.isEmpty(name)){
                    nameTxt.setError("Name is required!");
                    return;
                }

                if (TextUtils.isEmpty(email)){
                    emailTxt.setError("Email is required!");
                    return;
                }

                if (TextUtils.isEmpty(mobileno)){
                    mobilenoTxt.setError("Mobile No. is required!");
                    return;
                }

                if (TextUtils.isEmpty(password)){
                    passwordTxt.setError("Password is required!");
                    return;
                }

                if (TextUtils.isEmpty(confirmpassword)){
                    confirmpasswordTxt.setError("Confirm password!");
                    return;
                }

                if (!password.equals(confirmpassword)){
                    confirmpasswordTxt.setError("Password does not match!");
                    return;
                }
                else {

                    dialog.show();

                    final Map<String, Object> user = new HashMap<>();
                    user.put("username", name);
                    user.put("email", email);
                    user.put("contact", mobileno);
                    user.put("password", password);
                    user.put("lastMsgAt", FieldValue.serverTimestamp());

                    auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {

                                        //Toast.makeText(SignupActivity.this, "Successful", Toast.LENGTH_SHORT).show();

                                        final String currentuser = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                        //Toast.makeText(SignupActivity.this, currentuser, Toast.LENGTH_SHORT).show();
                                        uploadFile();

                                        FirebaseApp.collection("users").document(currentuser)
                                                .set(user)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void avoid) {

                                                        dialog.dismiss();
                                                        PreferenceUtils.saveEmail(email, SignupActivity.this);
                                                        PreferenceUtils.savePassword(password, SignupActivity.this);
                                                        //Toast.makeText(SignupActivity.this, "Successfull", Toast.LENGTH_SHORT).show();
                                                        Intent in = new Intent(SignupActivity.this, UserActivity.class);
                                                        startActivity(in);
                                                        finish();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        //Log.w(TAG, "Error adding document", e);
                                                        Toast.makeText(SignupActivity.this, "Unsuccessfull", Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                    } else {
                                        dialog.dismiss();
                                        Toast.makeText(SignupActivity.this, "Unsuccessful", Toast.LENGTH_SHORT).show();

                                    }
                                }
                            });



                }

                /*if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(mobileno) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(confirmpassword)) {

                    if (password.equals(confirmpassword)) {

                        //registerProgressBar.setVisibility(View.VISIBLE);

                        final Map<String, Object> user = new HashMap<>();
                        user.put("username", name);
                        user.put("email", email);
                        user.put("contact", mobileno);
                        user.put("password", password);
                        user.put("lastMsgAt", FieldValue.serverTimestamp());

                        auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {

                                            //Toast.makeText(SignupActivity.this, "Successful", Toast.LENGTH_SHORT).show();

                                            final String currentuser = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                            //Toast.makeText(SignupActivity.this, currentuser, Toast.LENGTH_SHORT).show();
                                            uploadFile();

                                            FirebaseApp.collection("users").document(currentuser)
                                                    .set(user)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void avoid) {

                                                            dialog.dismiss();
                                                            PreferenceUtils.saveEmail(email, SignupActivity.this);
                                                            PreferenceUtils.savePassword(password, SignupActivity.this);
                                                            //Toast.makeText(SignupActivity.this, "Successfull", Toast.LENGTH_SHORT).show();
                                                            Intent in = new Intent(SignupActivity.this, UserActivity.class);
                                                            startActivity(in);
                                                            finish();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            //Log.w(TAG, "Error adding document", e);
                                                            Toast.makeText(SignupActivity.this, "Unsuccessfull", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });

                                        } else {
                                            dialog.dismiss();
                                            Toast.makeText(SignupActivity.this, "Unsuccessful", Toast.LENGTH_SHORT).show();

                                        }
                                    }
                                });


                    } else {

                        Toast.makeText(SignupActivity.this, "Passwords dont match!", Toast.LENGTH_SHORT).show();

                    }

                }*/


            }
        });
    }

    private void openFileChooser() {

        /*Intent in = new Intent();
        in.setType("image/*");
        in.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(in, PICK_IAMGE_REQUEST);*/

        /*Intent in = new Intent();
        in.setType("image/*");
        in.setAction(Intent.ACTION_GET_CONTENT);*/
        Intent i = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(i,Image_Request_Code);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Image_Request_Code  && resultCode == RESULT_OK && data != null && data.getData() != null ) {

            mImageUri = data.getData();
            Picasso.with(SignupActivity.this).load(mImageUri).centerCrop().resize(500,500).into(profileImage);
            //Glide.with(SignupActivity.this).load(mImageUri).into(profileImage);
            //profileImage.setImageURI(mImageUri);

//                try {
//                    Bitmap bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
//
//                    profileImage.setImageBitmap(bmp);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }



            //selectImage.setImageBitmap(bmp);

            /*InputStream imageStream = null;
            try {
                imageStream = getContentResolver().openInputStream(
                        data.getData());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            Bitmap bmp = BitmapFactory.decodeStream(imageStream);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            try {
                stream.close();
                stream = null;
            } catch (IOException e) {

                e.printStackTrace();
            }*/
            //Glide.with(SignupActivity.this).load(byteArray).into(profileImage);
            //profileImage.setImageBitmap(bmp);
            //profileImage.setImageBitmap(bmp);
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

            /*Uri file = Uri.fromFile(new File(String.valueOf(mImageUri)));
            StorageReference riversRef = mStorageRef.child("images/"+file.getLastPathSegment());
            riversRef.putFile(file);

// Register observers to listen for when the download is done or if it fails
            riversRef.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...
                }
            });*/

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
//                            mProgressBar.setProgress(0);
                        }
                    }, 300);
                    Toast.makeText(SignupActivity.this, "Upload Successful!", Toast.LENGTH_SHORT).show();
                    //ProfileImage img = new ProfileImage("ProfileImage" + fileRef.getDownloadUrl().toString());
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SignupActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
//                            mProgressBar.setProgress((int) progress);
                        }
                    });
        } else {
            Toast.makeText(this, "No file selected!", Toast.LENGTH_SHORT).show();
        }
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
