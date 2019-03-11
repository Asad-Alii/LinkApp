package com.example.sampleapplication.UI.Activities;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.example.sampleapplication.Adapter.MessagesListAdapter;
import com.example.sampleapplication.MessageService.ChatQueries;
import com.example.sampleapplication.Listeners.CallBackListener;
import com.example.sampleapplication.Models.MessageList;
import com.example.sampleapplication.Models.Messages;
import com.example.sampleapplication.Utils.PreferenceUtils;
import com.example.sampleapplication.R;
import com.example.sampleapplication.Utils.VolleyRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class UserChatActivity extends AppCompatActivity implements MessagesListAdapter.OnItemClickListener {

    EditText sendmsgtxt;
    ImageView sendmsgBtn, attachmentBtn;
    TextView chatUsername;
    ImageView chatprofileImage;

    private Bitmap mImageBitmap;
    private String mCurrentPhotoPath;
    private ImageView mImageView;

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_CAMERA_REQUEST = 2;
    private static final int PICK_FILE_REQUEST = 3;
    private StorageReference imageStorageRef;
    private StorageReference cameraStorageRef;
    private StorageReference fileStorageRef;

    FirebaseFirestore fireBaseApp;
    ChatQueries cq;
    ArrayList<MessageList> messageArrayList;
    ArrayList<String> arrayList;
    ArrayList<Messages> testarrayList;
    ArrayList<HashMap<String, String>> messageList;
    HashMap<String, HashMap<String, String>> testing;
    ArrayList<HashMap<String, String>> senderList;
    RecyclerView recyclerView;
    SwipeRefreshLayout swipeRefresh;
    LinearLayoutManager linearLayoutManager;
    MessagesListAdapter adapter;
    Parcelable recyclerViewState;
    String senderName, senderImage;
    Uri imageUri = null;
    Uri cameraUri = null;
    Uri fileUri = null;
    int i = 0;
    private long downloadID;
    ProgressDialog pDialog;
    SharedPreferences sharedMemory;

    public String senderId, receiverId, name, email, contact, imageUrl, deviceToken, docId;

    public static final int PAGE_START = 1;
    private int currentPage = PAGE_START;
    private boolean isLastPage = false;
    private int totalPage = 10;
    private boolean isLoading = false;
    int itemCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userchat);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        setTitle("");

        sharedMemory = getSharedPreferences("testing", MODE_PRIVATE);
        fireBaseApp = FirebaseFirestore.getInstance();
        cq = new ChatQueries();
        messageArrayList = new ArrayList<MessageList>();
        arrayList = new ArrayList<String>();
        testarrayList = new ArrayList<Messages>();
        messageList = new ArrayList<>();
        testing = new HashMap<String, HashMap<String, String>>();
        senderList = new ArrayList<HashMap<String, String>>();
        pDialog = new ProgressDialog(this);
        setUpRecyclerView();

        senderId = getIntent().getStringExtra("senderId");
        receiverId = getIntent().getStringExtra("receiverId");
        name = getIntent().getStringExtra("name");
        email = getIntent().getStringExtra("email");
        contact = getIntent().getStringExtra("contact");
        imageUrl = getIntent().getStringExtra("imageUrl");
        deviceToken = getIntent().getStringExtra("deviceToken");
        docId = getIntent().getStringExtra("docId");
        //senderName = getIntent().getStringExtra("senderName");
        imageStorageRef = FirebaseStorage.getInstance().getReference("LinkApp/" + senderId + "/Images");
        cameraStorageRef = FirebaseStorage.getInstance().getReference("LinkApp/" + senderId + "/Images");
        fileStorageRef = FirebaseStorage.getInstance().getReference("LinkApp/" + senderId + "/Files");

        sendmsgtxt = findViewById(R.id.sendmsg_editTxt);
        sendmsgBtn = findViewById(R.id.sendmsg_btn);
        chatUsername = findViewById(R.id.chat_username);
        chatprofileImage = findViewById(R.id.chat_profileimage);
        attachmentBtn = findViewById(R.id.attachment_btn);
        mImageView = findViewById(R.id.image_viewer);

        chatUsername.setText(name);
        Glide.with(UserChatActivity.this).load(imageUrl).into(chatprofileImage);

        getChat(senderId, receiverId);
        //loadMore();
        final SharedPreferences.Editor editor = sharedMemory.edit();
        editor.putString("senderId", senderId);
        editor.putString("receiverImage", imageUrl);
        editor.apply();

        registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        sendmsgtxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() == 0) {
                    //sendmsgtxt.setError("Required");
                    sendmsgBtn.setEnabled(false);
                    sendmsgBtn.setImageResource(R.drawable.ic_send_disable);
                } else {
                    //sendmsgtxt.setError(null);
                    sendmsgBtn.setImageResource(R.drawable.ic_send);
                    sendmsgBtn.setEnabled(true);
                }

            }
        });

        sendmsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String msg = sendmsgtxt.getText().toString().trim();

                if (TextUtils.isEmpty(sendmsgtxt.getText().toString())) {


                } else {

                    cq.manageChannel(msg, senderId, receiverId, new CallBackListener<String, String>() {
                        @Override
                        public void success(String success) {

                            Toast.makeText(UserChatActivity.this, success, Toast.LENGTH_LONG).show();

                        }

                        @Override
                        public void error(String error) {

                            Toast.makeText(UserChatActivity.this, error, Toast.LENGTH_SHORT).show();

                        }
                    });
                }

                if (!TextUtils.isEmpty(sendmsgtxt.getText().toString())) {

                    fireBaseApp.collection("users").document(senderId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            DocumentSnapshot document = task.getResult();

                            senderName = document.getString("username");
                            sendMessageNotification(UserChatActivity.this, deviceToken, senderName, msg, receiverId, senderId, name, imageUrl);

                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Toast.makeText(UserChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            });

                }

                linearLayoutManager.setStackFromEnd(true);
                sendmsgtxt.setText("");
                Map<String, Object> map = new HashMap<>();
                map.put("lastMsgAt", FieldValue.serverTimestamp());
                fireBaseApp.collection("users")
                        .document(receiverId)
                        .update(map);
                fireBaseApp.collection("users")
                        .document(senderId)
                        .update(map);

            }
        });

        attachmentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final AlertDialog.Builder dialog = new AlertDialog.Builder(UserChatActivity.this);
                final AlertDialog alert = dialog.create();
                LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                View attachFileLayout = inflater.inflate(R.layout.file_chooser, null);
//                attachFileLayout.setBackgroundResource(R.drawable.sendmsg_background);
                alert.setView(attachFileLayout);

                Button pickImageBtn = attachFileLayout.findViewById(R.id.pickImage_btn);
                Button pickCameraBtn = attachFileLayout.findViewById(R.id.pickCamera_btn);
                Button pickFileBtn = attachFileLayout.findViewById(R.id.pickFile_btn);

                alert.show();

                pickImageBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openImage();
                        alert.cancel();
                    }
                });

                pickCameraBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //openCamera();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                openCamera();
                                alert.cancel();

                            } else {
                                // let's request permission.
                                String[] permissionRequest = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                                requestPermissions(permissionRequest, PICK_CAMERA_REQUEST);
                            }
                        }

                    }
                });

                pickFileBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openFile();
                        alert.cancel();
                    }
                });
            }
        });

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                loadMore();
                swipeRefresh.setRefreshing(false);
                //linearLayoutManager.setStackFromEnd(false);
            }
        });

    }

    private void openFile() {

        Intent in = new Intent();
        in.setType("application/*");
        in.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(in, PICK_FILE_REQUEST);
    }

    private void openCamera() {

        String fileName = "Camera_Example.jpg";

        // Create parameters for Intent with filename
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, fileName);
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image capture by camera");

        // imageUri is the current activity attribute, define and save it for later usage
        cameraUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        // Standard Intent action that can be sent to have the camera
        // application capture an image and return it.

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivityForResult(intent, PICK_CAMERA_REQUEST);

    }

    private void openImage() {

       /* Intent in = new Intent();
        in.setType("image/*");
        in.setAction(Intent.ACTION_GET_CONTENT);
        //startActivityForResult(in, PICK_IMAGE_REQUEST);
        startActivityForResult(Intent.createChooser(in,"Select Picture"), PICK_IMAGE_REQUEST);*/

        //Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        i.setType("image/* video/* audio/*");
        startActivityForResult(i, PICK_IMAGE_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            final AlertDialog.Builder dialog = new AlertDialog.Builder(UserChatActivity.this);
            LayoutInflater inflater = LayoutInflater.from(getApplicationContext());

            //Toast.makeText(this, imageUri.toString(), Toast.LENGTH_SHORT).show();

            if (imageUri.toString().contains("images")) {

                View imageViewerLayout = inflater.inflate(R.layout.image_viewer, null);
                dialog.setView(imageViewerLayout);

                ImageView imageViewer = imageViewerLayout.findViewById(R.id.image_viewer);
                imageViewer.setImageURI(imageUri);

                dialog.setPositiveButton("SEND", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        pDialog.setMessage("Sending picture...");
                        pDialog.show();
                        uploadImage();
                    }
                });

                dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {

                    @Override

                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }

                });
            } else if (imageUri.toString().contains("video")) {

                View imageViewerLayout = inflater.inflate(R.layout.video_viewer, null);
                dialog.setView(imageViewerLayout);

                VideoView videoViewer = imageViewerLayout.findViewById(R.id.video_viewer);
                videoViewer.setVideoURI(imageUri);
                videoViewer.start();

                dialog.setPositiveButton("SEND", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        pDialog.setMessage("Sending video...");
                        pDialog.show();
                        uploadImage();
                    }
                });

                dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {

                    @Override

                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }

                });
            }

            dialog.show();
        }

        if (requestCode == PICK_CAMERA_REQUEST && resultCode == RESULT_OK) {

            final AlertDialog.Builder dialog = new AlertDialog.Builder(UserChatActivity.this);
            LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
            View imageViewerLayout = inflater.inflate(R.layout.image_viewer, null);
            dialog.setView(imageViewerLayout);

            ImageView imageViewer = imageViewerLayout.findViewById(R.id.image_viewer);
            imageViewer.setImageURI(cameraUri);

            dialog.setPositiveButton("SEND", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    pDialog.setMessage("Sending...");
                    pDialog.show();
                    uploadCamera();
                }
            });

            dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {

                @Override

                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }

            });
            dialog.show();

        }

        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            fileUri = data.getData();
            final AlertDialog.Builder dialog = new AlertDialog.Builder(UserChatActivity.this);
            LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
            View imageViewerLayout = inflater.inflate(R.layout.image_viewer, null);
            dialog.setView(imageViewerLayout);

            if (getFileExtension(fileUri).equals("pdf")) {

                ImageView imageViewer = imageViewerLayout.findViewById(R.id.image_viewer);
                imageViewer.setImageResource(R.drawable.pdf);
            }

            if (getFileExtension(fileUri).equals("docx")) {

                ImageView imageViewer = imageViewerLayout.findViewById(R.id.image_viewer);
                imageViewer.setImageResource(R.drawable.docx);
            }

            if (getFileExtension(fileUri).equals("txt")) {

                ImageView imageViewer = imageViewerLayout.findViewById(R.id.image_viewer);
                imageViewer.setImageResource(R.drawable.txt);
            }

            if (getFileExtension(fileUri).equals("xlsx")) {

                ImageView imageViewer = imageViewerLayout.findViewById(R.id.image_viewer);
                imageViewer.setImageResource(R.drawable.xlsx);
            }

            dialog.setPositiveButton("SEND", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    pDialog.setMessage("Sending...");
                    pDialog.show();
                    uploadFile();
                }
            });

            dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {

                @Override

                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }

            });
            dialog.show();

        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    /*private void uploadFile() {

        final String currentuser = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (imageUri != null) {
            final StorageReference fileRef = imageStorageRef.child("image_" + i + "." + getFileExtension(imageUri));
            i++;

            fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    final String currentuser = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            Map<String, Object> map = new HashMap<>();
                            map.put("imageUrl", uri.toString());

                            cq.manageChannel(uri.toString(), senderId, receiverId, new CallBackListener<String, String>() {
                                @Override
                                public void success(String success) {
                                    Toast.makeText(UserChatActivity.this, "Image Sent!", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void error(String error) {

                                    Toast.makeText(UserChatActivity.this, "Image Not Sent!", Toast.LENGTH_SHORT).show();
                                }

                            });
                        }
                    });
                    pDialog.dismiss();
                    Toast.makeText(UserChatActivity.this, "Upload Successful!", Toast.LENGTH_SHORT).show();
                    sendMessageNotification(UserChatActivity.this, deviceToken, sharedMemory.getString("senderName", ""), "sent you a file...", receiverId, senderId, name, imageUrl);
                    //ProfileImage img = new ProfileImage("ProfileImage" + fileRef.getDownloadUrl().toString());
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(UserChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        } else {
            //Toast.makeText(this, "No file selected!", Toast.LENGTH_SHORT).show();
            final StorageReference fileRef = imageStorageRef.child("file_" + i + "." + getFileExtension(cameraUri));
            i++;

            fileRef.putFile(cameraUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    final String currentuser = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            Map<String, Object> map = new HashMap<>();
                            map.put("imageUrl", uri.toString());

                            cq.manageChannel(uri.toString(), senderId, receiverId, new CallBackListener<String, String>() {
                                @Override
                                public void success(String success) {
                                    Toast.makeText(UserChatActivity.this, "Image Sent!", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void error(String error) {

                                    Toast.makeText(UserChatActivity.this, "Image Not Sent!", Toast.LENGTH_SHORT).show();
                                }

                            });
                        }
                    });
                    pDialog.dismiss();
                    Toast.makeText(UserChatActivity.this, "Upload Successful!", Toast.LENGTH_SHORT).show();
                    sendMessageNotification(UserChatActivity.this, deviceToken, senderName, "sent you a file...", receiverId, senderId, name, imageUrl);
                    //ProfileImage img = new ProfileImage("ProfileImage" + fileRef.getDownloadUrl().toString());
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(UserChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "No file selected!", Toast.LENGTH_SHORT).show();
        }
    }*/

    private void uploadImage() {

        if (imageUri != null) {
            final StorageReference fileRef = imageStorageRef.child("image_" + i + "." + getFileExtension(imageUri));
            i++;

            fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            Map<String, Object> map = new HashMap<>();
                            map.put("imageUrl", uri.toString());

                            cq.manageChannel(uri.toString(), senderId, receiverId, new CallBackListener<String, String>() {
                                @Override
                                public void success(String success) {
                                    Toast.makeText(UserChatActivity.this, "Image Sent!", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void error(String error) {

                                    Toast.makeText(UserChatActivity.this, "Image Not Sent!", Toast.LENGTH_SHORT).show();
                                }

                            });
                        }
                    });
                    pDialog.dismiss();
                    Toast.makeText(UserChatActivity.this, "Sent Successful!", Toast.LENGTH_SHORT).show();
                    sendMessageNotification(UserChatActivity.this, deviceToken, sharedMemory.getString("senderName", ""), "sent you a file...", receiverId, senderId, name, imageUrl);
                    //ProfileImage img = new ProfileImage("ProfileImage" + fileRef.getDownloadUrl().toString());
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(UserChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

            return;
        }
        else {
            Toast.makeText(this, "No file selected!", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadCamera() {

        if (cameraUri != null) {
            //Toast.makeText(this, "No file selected!", Toast.LENGTH_SHORT).show();
            final StorageReference fileRef = cameraStorageRef.child("camera_image_" + i + "." + getFileExtension(cameraUri));
            i++;

            fileRef.putFile(cameraUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            Map<String, Object> map = new HashMap<>();
                            map.put("imageUrl", uri.toString());

                            cq.manageChannel(uri.toString(), senderId, receiverId, new CallBackListener<String, String>() {
                                @Override
                                public void success(String success) {
                                    Toast.makeText(UserChatActivity.this, "Image Sent!", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void error(String error) {

                                    Toast.makeText(UserChatActivity.this, "Image Not Sent!", Toast.LENGTH_SHORT).show();
                                }

                            });
                        }
                    });
                    pDialog.dismiss();
                    Toast.makeText(UserChatActivity.this, "Sent Successful!", Toast.LENGTH_SHORT).show();
                    sendMessageNotification(UserChatActivity.this, deviceToken, senderName, "sent you a file...", receiverId, senderId, name, imageUrl);
                    //ProfileImage img = new ProfileImage("ProfileImage" + fileRef.getDownloadUrl().toString());
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(UserChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

            return;
        }
        else {
            Toast.makeText(this, "No file selected!", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadFile() {

        if (fileUri != null) {
            final StorageReference fileRef = fileStorageRef.child("file_" + i + "." + getFileExtension(fileUri));
            i++;

            fileRef.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            Map<String, Object> map = new HashMap<>();
                            map.put("imageUrl", uri.toString());

                            cq.manageChannel(uri.toString(), senderId, receiverId, new CallBackListener<String, String>() {
                                @Override
                                public void success(String success) {
                                    Toast.makeText(UserChatActivity.this, "Image Sent!", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void error(String error) {

                                    Toast.makeText(UserChatActivity.this, "Image Not Sent!", Toast.LENGTH_SHORT).show();
                                }

                            });
                        }
                    });
                    pDialog.dismiss();
                    Toast.makeText(UserChatActivity.this, "Sent Successful!", Toast.LENGTH_SHORT).show();
                    sendMessageNotification(UserChatActivity.this, deviceToken, sharedMemory.getString("senderName", ""), "sent you a file...", receiverId, senderId, name, imageUrl);
                    //ProfileImage img = new ProfileImage("ProfileImage" + fileRef.getDownloadUrl().toString());
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(UserChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

            return;
        }
        else {
            Toast.makeText(this, "No file selected!", Toast.LENGTH_SHORT).show();
        }
    }

    public static void sendMessageNotification(final Context activity, final String instanceIdToken, String senderName, String senderMsg, final String senderId, final String receiverId, String name, String imageUrl) {

        SharedPreferences sharedPreferences = activity.getSharedPreferences("testing", MODE_PRIVATE);

        final JSONObject jsonData = new JSONObject();
        try {
            jsonData.put("title", senderName);
            jsonData.put("body", senderMsg);
            jsonData.put("senderId", receiverId);
            jsonData.put("receiverId", senderId);
            jsonData.put("senderName", sharedPreferences.getString("senderName", ""));
            jsonData.put("imageUrl", sharedPreferences.getString("senderImage", ""));
        } catch (Exception e) {
        }

        final String url = "https://fcm.googleapis.com/fcm/send";
        StringRequest myReq = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Toast.makeText(activity, "Successful", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Toast.makeText(activity, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {

            @Override
            public byte[] getBody() throws com.android.volley.AuthFailureError {
                Map<String, Object> rawParameters = new HashMap<>();
                rawParameters.put("data", jsonData);
                rawParameters.put("to", instanceIdToken);
                return new JSONObject(rawParameters).toString().getBytes();
            }

            ;

            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "key=" + "AIzaSyCF8QRuVaQI5hVtlChgkxQw80D55n_ybY4");
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };

        VolleyRequest.addRequest(myReq);
    }

    private void setUpRecyclerView() {
        recyclerView = findViewById(R.id.message_recyclerview);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        //linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

    }

    //HashMap<String, HashMap<String, String>> test = new HashMap<>();
    HashMap<String, HashMap<String, String>> test = new HashMap<>();
    ArrayList<Messages> msgs = new ArrayList<>();
    Set<String> keys;
    public boolean runOnce = false;

    private void getChat(final String senderId, final String receiverId) {

        fireBaseApp.collection("chat")
                .whereEqualTo(senderId, true)
                .whereEqualTo(receiverId, true)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {

                        msgs.clear();
                        for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                            final DocumentSnapshot documentSnapshot = dc.getDocument();

                            //test = (HashMap<String, HashMap<String, String>>) documentSnapshot.get("messages");
                            test = (HashMap<String, HashMap<String, String>>) documentSnapshot.get("messages");
                            keys = test.keySet();
                            for (HashMap<String, String> value : test.values()) {

                                Messages m = new Messages(value.get("message"),
                                        value.get("senderId"));

                                if (value.get("time") != null) {

                                    long seconds = ((Timestamp) (Object) value.get("time")).getSeconds();
                                    long milliSeconds = seconds * 1000;
                                    m.time = new Date(milliSeconds);
                                    msgs.add(m);
                                }
                            }

                        }

                        if (msgs != null) {

                            Comparator<Messages> c = new Comparator<Messages>() {

                                @Override
                                public int compare(Messages a, Messages b) {
                                    return Long.compare(a.getTime().getTime(), b.getTime().getTime());
                                }
                            };
                            Collections.sort(msgs, c);

                            if (!runOnce){
                                loadMore();
                                runOnce = true;
                            }
                            else {
                                loadMoreAgain();
                            }

                            linearLayoutManager.setStackFromEnd(true);

                        }
                    }
                });

    }

    ArrayList<Messages> sublist;
    int diff = 0;

    public void loadMore(){

        diff = diff + 11;
        int startIndex = (msgs.size()-1) - diff;
        int endIndex = msgs.size();

        if (startIndex > 11){

            sublist = new ArrayList<Messages>(msgs.subList(startIndex, endIndex));
        }
        else {

            //int a = endIndex - startIndex;
            //diff = a;

            sublist = new ArrayList<Messages>(msgs.subList(0, endIndex));
            //Toast.makeText(this, "No more messages!", Toast.LENGTH_SHORT).show();
            swipeRefresh.setEnabled(false);
        }
            adapter = new MessagesListAdapter(sublist, UserChatActivity.this);
            recyclerView.setAdapter(adapter);
            recyclerView.getRecycledViewPool().setMaxRecycledViews(0, 0);
            adapter.setOnClickListener(UserChatActivity.this);
    }

    public void loadMoreAgain(){

        int startIndex = (msgs.size()-1) - diff;
        int endIndex = msgs.size();

        if (startIndex > 11){

            sublist = new ArrayList<Messages>(msgs.subList(startIndex, endIndex));
        }
        else {

            //int a = endIndex - startIndex;
            //diff = a;

            sublist = new ArrayList<Messages>(msgs.subList(0, endIndex));
            //Toast.makeText(this, "No more messages!", Toast.LENGTH_SHORT).show();
            swipeRefresh.setEnabled(false);
        }
        adapter = new MessagesListAdapter(sublist, UserChatActivity.this);
        recyclerView.setAdapter(adapter);
        recyclerView.getRecycledViewPool().setMaxRecycledViews(0, 0);
        adapter.setOnClickListener(UserChatActivity.this);


    }

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getReceiverImage() {
        return imageUrl;
    }

    private Menu menu;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_option_btn, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.chat_action_details:

                final AlertDialog.Builder dialog = new AlertDialog.Builder(UserChatActivity.this);
                LayoutInflater inflater = LayoutInflater.from(this);
                View receiverProfileLayout = inflater.inflate(R.layout.activity_receiverprofile, null);
                dialog.setView(receiverProfileLayout);

                final ImageView receiverProfileImage = receiverProfileLayout.findViewById(R.id.receiverProfileImage);
                final TextView receiverProfileName = receiverProfileLayout.findViewById(R.id.receiverProfileName);
                final TextView receiverProfileEmail = receiverProfileLayout.findViewById(R.id.receiverProfileEmail);
                final TextView receiverProfileContact = receiverProfileLayout.findViewById(R.id.receiverProfileContact);

                receiverProfileName.setText(name);
                receiverProfileEmail.setText(email);
                receiverProfileContact.setText(contact);
                Glide.with(getApplicationContext()).load(imageUrl).into(receiverProfileImage);
                dialog.show();
                return true;

            case R.id.chat_action_Logout:
                String email = null;
                String password = null;

                FirebaseAuth.getInstance().signOut();

                PreferenceUtils.saveEmail(email, UserChatActivity.this);
                PreferenceUtils.savePassword(password, UserChatActivity.this);

                Intent intent = new Intent(UserChatActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                return true;

            case R.id.chat_action_delete:

                Toast.makeText(this, "Deleted!", Toast.LENGTH_SHORT).show();

                if (arr.get(0).equals(senderId)){

                    value.put("message", "This message is deleted!");
                    value.put("senderId", senderId);
                    value.put("time", arr.get(1));

                    test.put(keyArray.get(msgs.indexOf(sublist.get(index))), value);

                    fireBaseApp.collection("chat")
                            .whereEqualTo(senderId, true)
                            .whereEqualTo(receiverId, true)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                    String docid = null;

                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot doc : task.getResult()) {
                                            docid = doc.getId();
                                            //id = doc.getString("receiverId");
                                        }

                                        if (docid == null) {

                                        } else {
                                            fireBaseApp.collection("chat")
                                                    .document(docid)
                                                    .update("messages", test);

                                        }
                                    }

                                }
                            });
                }

                if (arr.get(0).equals(receiverId)){

                    value.put("message", "This message is deleted!");
                    value.put("senderId", receiverId);
                    value.put("time", arr.get(1));

                    test.put(keyArray.get(msgs.indexOf(sublist.get(index))), value);

                    fireBaseApp.collection("chat")
                            .whereEqualTo(senderId, true)
                            .whereEqualTo(receiverId, true)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                    String docid = null;

                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot doc : task.getResult()) {
                                            docid = doc.getId();
                                            //id = doc.getString("receiverId");
                                        }

                                        if (docid == null) {

                                        } else {
                                            fireBaseApp.collection("chat")
                                                    .document(docid)
                                                    .update("messages", test);

                                        }
                                    }

                                }
                            });
                }

                return true;


            case R.id.chat_action_edit: {

                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(UserChatActivity.this);
                final AlertDialog alert = dialogBuilder.create();
                LayoutInflater inflaterLayout = LayoutInflater.from(this);
                View editOrDeleteChatLayout = inflaterLayout.inflate(R.layout.edit_or_delete_chat, null);
                alert.setView(editOrDeleteChatLayout);
                final EditText editMessageTxt = editOrDeleteChatLayout.findViewById(R.id.edit_editText);
                final Button okayEditMessageBtn = editOrDeleteChatLayout.findViewById(R.id.okayEditMessage_btn);

                Toast.makeText(UserChatActivity.this, String.valueOf(arr.get(0)), Toast.LENGTH_SHORT).show();

                if (arr.get(0).equals(senderId)){

                    okayEditMessageBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            String msg = editMessageTxt.getText().toString();
                            value.put("message", msg);
                            value.put("senderId", senderId);
                            value.put("time", arr.get(1));

                            test.put(keyArray.get(msgs.indexOf(sublist.get(index))), value);

                            fireBaseApp.collection("chat")
                                    .whereEqualTo(senderId, true)
                                    .whereEqualTo(receiverId, true)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                            String docid = null;

                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot doc : task.getResult()) {
                                                    docid = doc.getId();
                                                    //id = doc.getString("receiverId");
                                                }

                                                if (docid == null) {

                                                } else {
                                                    fireBaseApp.collection("chat")
                                                            .document(docid)
                                                            .update("messages", test);

                                                }
                                            }

                                        }
                                    });
                            editMessageTxt.setText("");
                            alert.cancel();

                        }
                    });
                }

                if (arr.get(0).equals(receiverId)){

                    okayEditMessageBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            String msg = editMessageTxt.getText().toString();
                            value.put("message", msg);
                            value.put("senderId", receiverId);
                            value.put("time", arr.get(1));

                            test.put(keyArray.get(msgs.indexOf(sublist.get(index))), value);

                            fireBaseApp.collection("chat")
                                    .whereEqualTo(senderId, true)
                                    .whereEqualTo(receiverId, true)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                            String docid = null;

                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot doc : task.getResult()) {
                                                    docid = doc.getId();
                                                    //id = doc.getString("receiverId");
                                                }

                                                if (docid == null) {

                                                } else {
                                                    fireBaseApp.collection("chat")
                                                            .document(docid)
                                                            .update("messages", test);

                                                }
                                            }

                                        }
                                    });
                            editMessageTxt.setText("");
                            alert.cancel();

                        }
                    });
                }

                alert.show();

                return true;
            }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemClick(int position) {

        //final String senderId = auth.getCurrentUser().getUid();
        MenuItem deleteItem = menu.findItem(R.id.chat_action_delete);
        MenuItem editItem = menu.findItem(R.id.chat_action_edit);
        index = 0;

        if (deleteItem.isVisible() && editItem.isVisible())
        {
            deleteItem.setVisible(false);
            editItem.setVisible(false);
        }

        if (sublist.get(position).getMessage().startsWith("https:")) {

            if (sublist.get(position).getMessage().contains(".jpg") || sublist.get(position).getMessage().contains(".png")) {

                if (senderId.equals(sublist.get(position).getSenderId())) {

                    Messages m = sublist.get(position);
                    final String msg = m.getMessage();

                    final AlertDialog.Builder dialog = new AlertDialog.Builder(UserChatActivity.this);
                    LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                    View imageViewerLayout = inflater.inflate(R.layout.image_viewer, null);
                    dialog.setView(imageViewerLayout);

                    ImageView imageViewer = imageViewerLayout.findViewById(R.id.image_viewer);
                    //imageViewer.setImageURI(imageUri);

                    Glide.with(getApplicationContext()).load(msg).into(imageViewer);
                    dialog.show();
                } else {

                    Messages m = sublist.get(position);
                    final String msg = m.getMessage();

                    final AlertDialog.Builder dialog = new AlertDialog.Builder(UserChatActivity.this);
                    LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                    View imageViewerLayout = inflater.inflate(R.layout.image_viewer, null);
                    dialog.setView(imageViewerLayout);

                    ImageView imageViewer = imageViewerLayout.findViewById(R.id.image_viewer);
                    //imageViewer.setImageURI(imageUri);

                    Glide.with(getApplicationContext()).load(msg).into(imageViewer);

                    dialog.setPositiveButton("DOWNLOAD", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Picasso.with(UserChatActivity.this)
                                    .load(msg)
                                    .into(new Target() {
                                              @Override
                                              public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                                  try {
                                                      String root = Environment.getExternalStorageDirectory().toString();
                                                      File myDir = new File(root + "/LinkApp/Images");

                                                      if (!myDir.exists()) {
                                                          myDir.mkdirs();
                                                      }

                                                      String name = new Date().toString() + ".jpg";
                                                      myDir = new File(myDir, name);
                                                      FileOutputStream out = new FileOutputStream(myDir);
                                                      bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);

                                                      out.flush();
                                                      out.close();
                                                  } catch (Exception e) {
                                                      // some action
                                                  }

                                                  Toast.makeText(UserChatActivity.this, "Image saved on device!", Toast.LENGTH_SHORT).show();
                                              }

                                              @Override
                                              public void onBitmapFailed(Drawable errorDrawable) {
                                              }

                                              @Override
                                              public void onPrepareLoad(Drawable placeHolderDrawable) {
                                              }
                                          }
                                    );
                        }
                    });

                    dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {

                        @Override

                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }

                    });
                    dialog.show();
                }
            }
            if (sublist.get(position).getMessage().contains(".mp4")) {

                if (senderId.equals(sublist.get(position).getSenderId())) {

                    Messages m = sublist.get(position);
                    final String msg = m.getMessage();

                    Uri video = Uri.parse(msg);

                    final AlertDialog.Builder dialog = new AlertDialog.Builder(UserChatActivity.this);
                    LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                    View imageViewerLayout = inflater.inflate(R.layout.video_viewer, null);
                    dialog.setView(imageViewerLayout);

                    VideoView videoViewer = imageViewerLayout.findViewById(R.id.video_viewer);
                    videoViewer.setVideoURI(video);
                    videoViewer.start();
                    dialog.show();
                }

            } else {

                beginDownload(sublist.get(position).getMessage());
            }


        }

    }

    final ArrayList<String> keyArray = new ArrayList<>();
    final ArrayList<String> arr = new ArrayList<>();
    final HashMap<String, String> value = new HashMap<>();
    int index = 0;

    @Override
    public void onItemLongClick(final int position) {

        keyArray.clear();
        arr.clear();
        value.clear();

        MenuItem deleteItem = menu.findItem(R.id.chat_action_delete);
        MenuItem editItem = menu.findItem(R.id.chat_action_edit);
        index = 0;
        index = position;
//        final ArrayList<String> keyArray = new ArrayList<>();
//        final ArrayList<String> arr = new ArrayList<>();
//        final HashMap<String, String> value = new HashMap<>();
        //Toast.makeText(this, String.valueOf(index), Toast.LENGTH_SHORT).show();

        keyArray.addAll(keys);
        Collections.sort(keyArray);
        arr.addAll(test.get(keyArray.get(msgs.indexOf(sublist.get(position)))).values());

        //Toast.makeText(UserChatActivity.this, String.valueOf(keyArray.get(msgs.indexOf(sublist.get(position)))), Toast.LENGTH_SHORT).show();

        if (!arr.get(2).equals("This message is deleted!")) {

            deleteItem.setVisible(true);
            editItem.setVisible(true);

            final AlertDialog.Builder dialog = new AlertDialog.Builder(UserChatActivity.this);
            final AlertDialog alert = dialog.create();
            LayoutInflater inflater = LayoutInflater.from(this);
            View editOrDeleteChatLayout = inflater.inflate(R.layout.edit_or_delete_chat, null);
            alert.setView(editOrDeleteChatLayout);
            final EditText editMessageTxt = editOrDeleteChatLayout.findViewById(R.id.edit_editText);
            final Button editMessageBtn = editOrDeleteChatLayout.findViewById(R.id.editMessage_btn);
            final Button okayEditMessageBtn = editOrDeleteChatLayout.findViewById(R.id.okayEditMessage_btn);
            final Button deleteMessageBtn = editOrDeleteChatLayout.findViewById(R.id.deleteMessage_btn);

            deleteMessageBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    value.put("message", "This message is deleted!");
                    value.put("senderId", senderId);
                    value.put("time", arr.get(1));

                    test.put(keyArray.get(msgs.indexOf(sublist.get(position))), value);

                    fireBaseApp.collection("chat")
                            .whereEqualTo(senderId, true)
                            .whereEqualTo(receiverId, true)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                    String docid = null;

                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot doc : task.getResult()) {
                                            docid = doc.getId();
                                            //id = doc.getString("receiverId");
                                        }

                                        if (docid == null) {

                                        } else {
                                            fireBaseApp.collection("chat")
                                                    .document(docid)
                                                    .update("messages", test);

                                        }
                                    }

                                }
                            });

                    editMessageTxt.setText("");
                    alert.cancel();
                }
            });

            editMessageBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    editMessageTxt.setVisibility(View.VISIBLE);
                    deleteMessageBtn.setVisibility(View.GONE);
                    editMessageBtn.setVisibility(View.GONE);
                    okayEditMessageBtn.setVisibility(View.VISIBLE);


                }
            });

            okayEditMessageBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String msg = editMessageTxt.getText().toString();
                    value.put("message", msg);
                    value.put("senderId", senderId);
                    value.put("time", arr.get(1));

                    test.put(keyArray.get(msgs.indexOf(sublist.get(position))), value);

                    fireBaseApp.collection("chat")
                            .whereEqualTo(senderId, true)
                            .whereEqualTo(receiverId, true)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                    String docid = null;

                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot doc : task.getResult()) {
                                            docid = doc.getId();
                                            //id = doc.getString("receiverId");
                                        }

                                        if (docid == null) {

                                        } else {
                                            fireBaseApp.collection("chat")
                                                    .document(docid)
                                                    .update("messages", test);

                                        }
                                    }

                                }
                            });
                    //adapter.notifyDataSetChanged();
                    //recyclerView.scrollToPosition(position);
                    //adapter.notifyItemChanged(position);
                    /*recyclerView.scrollToPosition(position);
                    adapter.notifyItemChanged(position);
                    adapter.notifyDataSetChanged();*/
                    //recyclerView.scrollTo(firstVisibleItem, lastVisibleItem);
                    editMessageTxt.setText("");
                    alert.cancel();

                }
            });

            //alert.show();

        } else {

            Toast.makeText(this, "This message cannot be edited or deleted!", Toast.LENGTH_LONG).show();
            deleteItem.setVisible(false);
            editItem.setVisible(false);
        }
    }

    private void beginDownload(String str) {

        Uri downloadUrl = Uri.parse(str);

        if (str.contains(".pdf")) {

            File file = new File(getExternalFilesDir(null), "Demo.pdf");

            DownloadManager.Request request = new DownloadManager.Request(downloadUrl)
                    .setTitle("Demo File")
                    .setDescription("Downloading")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationUri(Uri.fromFile(file))
                    .setAllowedOverMetered(true);

            DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            downloadID = downloadManager.enqueue(request);
        }

        if (str.contains(".docx")) {

            File file = new File(getExternalFilesDir(null), "Demo.docx");

            DownloadManager.Request request = new DownloadManager.Request(downloadUrl)
                    .setTitle("Demo File")
                    .setDescription("Downloading")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationUri(Uri.fromFile(file))
                    .setAllowedOverMetered(true);

            DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            downloadID = downloadManager.enqueue(request);
        }

        if (str.contains(".txt")) {

            File file = new File(getExternalFilesDir(null), "Demo.txt");

            DownloadManager.Request request = new DownloadManager.Request(downloadUrl)
                    .setTitle("Demo File")
                    .setDescription("Downloading")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationUri(Uri.fromFile(file))
                    .setAllowedOverMetered(true);

            DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            downloadID = downloadManager.enqueue(request);
        }

    }

    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //Getting download id
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            //
            if (downloadID == id) {
                Toast.makeText(UserChatActivity.this, "Download Complete!", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onDownloadComplete);
    }

}
