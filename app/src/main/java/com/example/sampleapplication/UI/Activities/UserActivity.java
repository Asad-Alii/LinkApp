package com.example.sampleapplication.UI.Activities;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DecorContentParent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.sampleapplication.Adapter.UserListAdapter;
import com.example.sampleapplication.Models.UserList;
import com.example.sampleapplication.Utils.PreferenceUtils;
import com.example.sampleapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class UserActivity extends AppCompatActivity implements UserListAdapter.OnItemClickListener, SearchView.OnQueryTextListener {

    Button logoutBtn, editBtn;
    FirebaseFirestore db;
    RecyclerView recyclerView;
    LinearLayoutManager manager;
    UserListAdapter adapter;
    ArrayList<UserList> userArrayList;
    ArrayList<UserList> searchArrayList;
    private FirebaseAuth auth;
    ProgressDialog dialog;
    ProgressBar progressBar;
    String senderImage, senderName;
    SharedPreferences sharedMemory;
    Boolean isScrolling = false;
    Boolean isLastItemReached = false;
    int currentItems, totalItems, scrollOutItems;
    private DocumentSnapshot lastVisible;
    ArrayList<UserList> users;
    Query query;
    String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        setTitle("LinkApp");

        auth = FirebaseAuth.getInstance();
        dialog = new ProgressDialog(this);
        id = auth.getCurrentUser().getUid();
        sharedMemory = getSharedPreferences("testing", MODE_PRIVATE);
        progressBar = findViewById(R.id.progress_bar);
        users = new ArrayList<>();

        Task<InstanceIdResult> token = FirebaseInstanceId.getInstance().getInstanceId();

        userArrayList = new ArrayList<>();
        searchArrayList = new ArrayList<>();
        setUpRecyclerView();
        setUpFireBase();
        //loadFromFireBase();
        getDeviceToken();
        final SharedPreferences.Editor editor = sharedMemory.edit();

        //Toast.makeText(this, "OnCreate", Toast.LENGTH_SHORT).show();

        db.collection("users").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                DocumentSnapshot document = task.getResult();

                senderName = document.getString("username");
                senderImage = document.getString("imageUrl");
                editor.putString("senderName", senderName);
                editor.putString("senderImage", senderImage);
                editor.apply();

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(UserActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadFromFireBase();
        //Toast.makeText(this, "OnResume", Toast.LENGTH_SHORT).show();

    }

    public void getDeviceToken() {

        final String currentId = auth.getCurrentUser().getUid();

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {

                            Toast.makeText(UserActivity.this, "getInstanceId failed", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        Map<String, Object> map = new HashMap<>();
                        map.put("deviceToken", token);
                        db.collection("users")
                                .document(currentId)
                                .update(map);
                        //Toast.makeText(UserActivity.this, token, Toast.LENGTH_SHORT).show();

                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_option_btn, menu);

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(this);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_edit:

                Intent in = new Intent(UserActivity.this, EditProfileActivity.class);
                startActivity(in);
                finish();

                return true;

            case R.id.action_Logout:
                String email = null;
                String password = null;

                FirebaseAuth.getInstance().signOut();

                PreferenceUtils.saveEmail(email, UserActivity.this);
                PreferenceUtils.savePassword(password, UserActivity.this);

                db.collection("users")
                        .document(id)
                        .update("deviceToken", "");

                Intent intent = new Intent(UserActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                return true;

            case R.id.action_search:

                /*final AlertDialog.Builder dialog = new AlertDialog.Builder(UserActivity.this);
                LayoutInflater inflater = LayoutInflater.from(this);
                View searchUser = inflater.inflate(R.layout.activity_searchuser, null);
                dialog.setView(searchUser);
                dialog.show();*/

                /*final SearchView searchView = (SearchView) findViewById(R.id.action_search);
                SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
                searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));*/

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setUpRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view);
//        recyclerView.setHasFixedSize(true);
        manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);

    }

    private void setUpFireBase() {
        db = FirebaseFirestore.getInstance();
    }

    private void loadFromFireBase() {

        dialog.setMessage("Loading...");
        dialog.show();

        if (userArrayList.size() > 0)
            userArrayList.clear();
        //searchArrayList.clear();

        query = db.collection("users")
                .orderBy("lastMsgAt", Query.Direction.DESCENDING)
                .limit(10);

        query.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {

                            UserList ul = new UserList(documentSnapshot.getId(),
                                    documentSnapshot.getString("username"),
                                    documentSnapshot.getString("email"),
                                    documentSnapshot.getString("contact"),
                                    documentSnapshot.getString("password"),
                                    documentSnapshot.getString("imageUrl"),
                                    documentSnapshot.getString("deviceToken"));
                            userArrayList.add(ul);
                            searchArrayList.add(ul);
                        }

                        dialog.dismiss();

                        searchArrayList.clear();

                        adapter = new UserListAdapter(searchArrayList, UserActivity.this);
                        recyclerView.setAdapter(adapter);
                        filter(userInput);
                        lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                        adapter.setOnClickListener(UserActivity.this);

                        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                            @Override
                            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                                super.onScrollStateChanged(recyclerView, newState);
                                isScrolling = true;
                            }

                            @Override
                            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                                super.onScrolled(recyclerView, dx, dy);

                                int firstVisibleItem = manager.findFirstVisibleItemPosition();
                                int visibleItemCount = manager.getChildCount();
                                int totalItemCount = manager.getItemCount();

                                if (isScrolling && (firstVisibleItem + visibleItemCount == totalItemCount) && !isLastItemReached) {
                                    isScrolling = false;
                                    progressBar.setVisibility(View.VISIBLE);

                                    query = db.collection("users")
                                            .orderBy("lastMsgAt", Query.Direction.DESCENDING)
                                            .startAfter(lastVisible)
                                            .limit(10);

                                    query.get()
                                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                @Override
                                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {

                                                        UserList ul = new UserList(documentSnapshot.getId(),
                                                                documentSnapshot.getString("username"),
                                                                documentSnapshot.getString("email"),
                                                                documentSnapshot.getString("contact"),
                                                                documentSnapshot.getString("password"),
                                                                documentSnapshot.getString("imageUrl"),
                                                                documentSnapshot.getString("deviceToken"));
                                                        userArrayList.add(ul);
                                                        searchArrayList.add(ul);
                                                    }

                                                    if (queryDocumentSnapshots.size() > 0) {
                                                        //isLastItemReached = true;
                                                        adapter.notifyDataSetChanged();
                                                        lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                                                        //Toast.makeText(UserActivity.this, String.valueOf(searchArrayList.size()), Toast.LENGTH_SHORT).show();
                                                    }

                                                    /*if (queryDocumentSnapshots.size() > 0) {
                                                        //isLastItemReached = true;
                                                        adapter.notifyDataSetChanged();
                                                        lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                                                    }*/

                                                    progressBar.setVisibility(View.GONE);
                                                }
                                            });
                                }
                            }
                        });
                    }
                });

        /*Query first = db.collection("users")
                .orderBy("lastMsgAt", Query.Direction.DESCENDING)
                .limit(10);

        first.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(final QuerySnapshot documentSnapshots) {

                        for (DocumentSnapshot dc : documentSnapshots.getDocuments()) {

                            UserList ul = new UserList(dc.getId(),
                                    dc.getString("username"),
                                    dc.getString("email"),
                                    dc.getString("contact"),
                                    dc.getString("password"),
                                    dc.getString("imageUrl"),
                                    dc.getString("deviceToken"));
                            userArrayList.add(ul);
                            searchArrayList.add(ul);

                        }

                        dialog.dismiss();

                        searchArrayList.clear();

                        adapter = new UserListAdapter(searchArrayList, UserActivity.this);
                        filter(userInput);
                        recyclerView.setAdapter(adapter);
                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                        adapter.setOnClickListener(UserActivity.this);

                        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                            @Override
                            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                                super.onScrollStateChanged(recyclerView, newState);
                                isScrolling = true;
                            }

                            @Override
                            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                                super.onScrolled(recyclerView, dx, dy);

                                int firstVisibleItem = manager.findFirstVisibleItemPosition();
                                int visibleItemCount = manager.getChildCount();
                                int totalItemCount = manager.getItemCount();

                                if (isScrolling && (firstVisibleItem + visibleItemCount == totalItemCount) && !isLastItemReached) {
                                    isScrolling = false;
                                    progressBar.setVisibility(View.VISIBLE);

                                    final Query next = db.collection("users")
                                            .orderBy("lastMsgAt", Query.Direction.DESCENDING)
                                            .startAfter(lastVisible)
                                            .limit(10);

                                    next.get()
                                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                @Override
                                                public void onSuccess(QuerySnapshot documentSnapshots) {

                                                    for (DocumentSnapshot dc : documentSnapshots.getDocuments()) {

                                                        UserList ul = new UserList(dc.getId(),
                                                                dc.getString("username"),
                                                                dc.getString("email"),
                                                                dc.getString("contact"),
                                                                dc.getString("password"),
                                                                dc.getString("imageUrl"),
                                                                dc.getString("deviceToken"));
                                                        userArrayList.add(ul);
                                                        searchArrayList.add(ul);
                                                    }

                                                    adapter.notifyDataSetChanged();
                                                    lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);

                                                    if (documentSnapshots.getDocuments().size() < 10) {
                                                        isLastItemReached = true;
                                                    }

                                                    progressBar.setVisibility(View.GONE);

                                                }
                                            });
                                }
                                lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);

                            }
                        });

                    }
                });*/

        /*db.collection("users")
                .orderBy("lastMsgAt", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                UserList ul = new UserList(documentSnapshot.getId(),
                                        documentSnapshot.getString("username"),
                                        documentSnapshot.getString("email"),
                                        documentSnapshot.getString("contact"),
                                        documentSnapshot.getString("password"),
                                        documentSnapshot.getString("imageUrl"),
                                        documentSnapshot.getString("deviceToken"));
                                userArrayList.add(ul);
                                searchArrayList.add(ul);
                            }
                            dialog.dismiss();

                            searchArrayList.clear();

                            adapter = new UserListAdapter(searchArrayList, UserActivity.this);
                            filter(userInput);
                            recyclerView.setAdapter(adapter);
                            lastVisible = task.getResult().getDocuments().get(task.getResult().size() - 1);
                            adapter.setOnClickListener(UserActivity.this);
                            //Toast.makeText(UserActivity.this, "First page loaded!", Toast.LENGTH_SHORT).show();

                            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                                @Override
                                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                                    super.onScrollStateChanged(recyclerView, newState);
                                    if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                                        isScrolling = true;
                                    }
                                }

                                @Override
                                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                                    super.onScrolled(recyclerView, dx, dy);

                                    //progressBar.setVisibility(View.VISIBLE);
                                    int firstVisibleItem = manager.findFirstVisibleItemPosition();
                                    int visibleItemCount = manager.getChildCount();
                                    int totalItemCount = manager.getItemCount();

                                    if (isScrolling && (firstVisibleItem + visibleItemCount == totalItemCount) && !isLastItemReached) {
                                        isScrolling = false;
                                        progressBar.setVisibility(View.VISIBLE);

                                        db.collection("users")
                                                .orderBy("lastMsgAt", Query.Direction.DESCENDING)
                                                .startAfter(lastVisible)
                                                .limit(10)
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                                        for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                                            UserList ul = new UserList(documentSnapshot.getId(),
                                                                    documentSnapshot.getString("username"),
                                                                    documentSnapshot.getString("email"),
                                                                    documentSnapshot.getString("contact"),
                                                                    documentSnapshot.getString("password"),
                                                                    documentSnapshot.getString("imageUrl"),
                                                                    documentSnapshot.getString("deviceToken"));
                                                            userArrayList.add(ul);
                                                            searchArrayList.add(ul);
                                                        }

                                                        adapter.notifyDataSetChanged();
                                                        lastVisible = task.getResult().getDocuments().get(task.getResult().size() - 1);
                                                        //Toast.makeText(UserActivity.this, "Next page loaded!", Toast.LENGTH_SHORT).show();
                                                        //progressBar.setVisibility(View.GONE);

                                                        if (task.getResult().size() < 10) {
                                                            isLastItemReached = true;
                                                            //Toast.makeText(UserActivity.this, "End of List!", Toast.LENGTH_SHORT).show();
                                                            //progressBar.setVisibility(View.GONE);
                                                        }
                                                        progressBar.setVisibility(View.GONE);

                                                    }
                                                });
                                    }
                                }
                            });
                        }
                    }
                });*/


        /*db.collection("users").orderBy("lastMsgAt", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
//                searchArrayList.clear();
//                userArrayList.clear();
                //queryDocumentSnapshots.getDocumentChanges();
                for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                    //UserList ul = querySnapshot.toObject(UserList.class);
                    final DocumentSnapshot documentSnapshot = dc.getDocument();
                    *//*UserList userAdded = new UserList(documentSnapshot.getId(),
                            documentSnapshot.getString("username"),
                            documentSnapshot.getString("email"),
                            documentSnapshot.getString("contact"),
                            documentSnapshot.getString("password"),
                            documentSnapshot.getString("imageUrl"));
                    userArrayList.add(userAdded);*//*

                    switch (dc.getType()) {
                        case ADDED:
                            //Toast.makeText(UserActivity.this, documentSnapshot.getId(), Toast.LENGTH_LONG).show();
                            UserList userAdded = new UserList(documentSnapshot.getId(),
                                    documentSnapshot.getString("username"),
                                    documentSnapshot.getString("email"),
                                    documentSnapshot.getString("contact"),
                                    documentSnapshot.getString("password"),
                                    documentSnapshot.getString("imageUrl"),
                                    documentSnapshot.getString("deviceToken"));

                            if (documentSnapshot.get("lastMsgAt") != null) {

                                long seconds = ((Timestamp) (Object) documentSnapshot.get("lastMsgAt")).getSeconds();
                                long milliSeconds = seconds * 1000;
                                userAdded.time = new Date(milliSeconds);
                                userArrayList.add(userAdded);

                            }
                            //userArrayList.add(userAdded);
//                            searchArrayList.add(userAdded);
                            break;

                        case MODIFIED: {
                            //Toast.makeText(UserActivity.this, documentSnapshot.getId(), Toast.LENGTH_SHORT).show();
                            UserList userModified = new UserList(documentSnapshot.getId(),
                                    documentSnapshot.getString("username"),
                                    documentSnapshot.getString("email"),
                                    documentSnapshot.getString("contact"),
                                    documentSnapshot.getString("password"),
                                    documentSnapshot.getString("imageUrl"),
                                    documentSnapshot.getString("deviceToken"));
                            *//*userArrayList.add(userModified);
                            searchArrayList.add(userModified);*//*
//                            userArrayList.remove(dc.getOldIndex());
//                            userArrayList.add(dc.getNewIndex(), userModified);
                            int toUpdate = -1;
                            for (int i = 0; i < userArrayList.size(); i++) {
                                UserList userList = userArrayList.get(i);
                                if (userModified.getId().matches(userList.getId()))
                                    toUpdate = i;
                            }
                            if (toUpdate > -1)
                                userArrayList.remove(toUpdate);
                            userArrayList.add(0,userModified);
//                            searchArrayList.remove(dc.getOldIndex());
//                            searchArrayList.add(dc.getNewIndex(), userModified);
                            break;
                        }
                        case REMOVED:
                            //Toast.makeText(UserActivity.this, documentSnapshot.getId(), Toast.LENGTH_LONG).show();
                            UserList userRemoved = new UserList(documentSnapshot.getId(),
                                    documentSnapshot.getString("username"),
                                    documentSnapshot.getString("email"),
                                    documentSnapshot.getString("contact"),
                                    documentSnapshot.getString("password"),
                                    documentSnapshot.getString("imageUrl"),
                                    documentSnapshot.getString("deviceToken"));
                            //userArrayList.add(userRemoved);
//                            userArrayList.remove(dc.getOldIndex());
//                            searchArrayList.remove(dc.getOldIndex());
                            int toRemove = -1;
                            for (int i = 0; i < userArrayList.size(); i++) {
                                UserList userList = userArrayList.get(i);
                                if (userRemoved.getId().matches(userList.getId()))
                                    toRemove = i;
                            }
                            if (toRemove > -1)
                                userArrayList.remove(toRemove);
                            break;
                    }
                }

                dialog.dismiss();

                searchArrayList.clear();

                adapter = new UserListAdapter(searchArrayList, UserActivity.this);
                recyclerView.setAdapter(adapter);
                filter(userInput);
                adapter.setOnClickListener(UserActivity.this);

            }
        });*/

    }

    @Override
    public void onItemClick(int position) {

        final String senderId = auth.getCurrentUser().getUid();

        UserList ul = searchArrayList.get(position);
        final String receiverId = ul.getId();
        final String name = ul.getName();
        final String email = ul.getEmail();
        final String contact = ul.getContact();
        String password = ul.getPassword();
        final String imageUrl = ul.getImageUrl();
        final String deviceToken = ul.getDeviceToken();

        final Intent in = new Intent(getApplicationContext(), UserChatActivity.class);
        in.putExtra("senderId", senderId);
        in.putExtra("receiverId", receiverId);
        //in.putExtra("senderName", senderName);
        in.putExtra("name", name);
        in.putExtra("email", email);
        in.putExtra("contact", contact);
        in.putExtra("imageUrl", imageUrl);
        in.putExtra("deviceToken", deviceToken);
        startActivity(in);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void getCurrentUserName() {
        String id = auth.getCurrentUser().getUid();


        db.collection("users").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                DocumentSnapshot document = task.getResult();

                String name = document.getString("username");
                //Toast.makeText(UserActivity.this, name, Toast.LENGTH_SHORT).show();

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(UserActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    @Override
    public boolean onQueryTextSubmit(String s) {

        //Toast.makeText(this, "testing", Toast.LENGTH_SHORT).show();
        return false;
    }

    public void filter(String input) {
        searchArrayList.clear();
        for (UserList name : userArrayList) {
           /* if (name.getName().toLowerCase().contains(input)) {
                searchArrayList.add(name);
            }*/

            if (name.getName().toLowerCase().equals(input)) {
                searchArrayList.add(name);
            } else if (name.getName().toLowerCase().startsWith(input)) {
                searchArrayList.add(name);
            } else if (name.getName().toLowerCase().endsWith(input)) {
                searchArrayList.add(name);
            } else if (name.getName().toLowerCase().contains(input)) {
                searchArrayList.add(name);
            }
        }
       /* if (searchArrayList.size() == 0)
            searchArrayList.addAll(userArrayList);*/
//        userArrayList = newList;
        //adapter.updateList(searchArrayList);
        adapter.notifyDataSetChanged();
//        adapter = new UserListAdapter(searchArrayList, UserActivity.this);
//        recyclerView.setAdapter(adapter);
    }

    String userInput = "";

    @Override
    public boolean onQueryTextChange(String newText) {

        //searchArrayList.clear();
        userInput = newText.toLowerCase();
//        ArrayList<UserList> newList = new ArrayList<>();
        filter(userInput);

        //adapter.setOnClickListener(UserActivity.this);
        return true;
    }
}
