package com.example.sampleapplication.MessageService;

import android.support.annotation.NonNull;

import com.example.sampleapplication.Listeners.CallBackListener;
import com.example.sampleapplication.Models.Messages;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.firestore.SetOptions;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatQueries {

    static FirebaseFirestore fireBaseApp = FirebaseFirestore.getInstance();
    @ServerTimestamp
    Date time;
    //fireBaseApp = FirebaseFirestore.getInstance();

    public static void manageChannel(final String msg, final String senderId, final String receiverId, final CallBackListener<String, String> callback) {


        //String timeStamp = String.valueOf(FieldValue.serverTimestamp());
        //List<Messages> list = new ArrayList<>();
        final Messages m = new Messages(msg, senderId);
        //Ids id = new Ids();
        //list.add(m);

        final Map<String, Object> messages = new HashMap<>();
        final Map<String, String> ids = new HashMap<>();
        ids.put("id1", senderId);
        ids.put("id2", receiverId);
        //messages.put("messages", msg);
        //messages.put("users", Arrays.asList(senderId, receiverId));
        //String[] arr = {senderId, receiverId};
        messages.put(senderId, true);
        messages.put(receiverId, true);
        //messages.put("messages", m);
        //messages.put("senderId", senderId);
        //messages.put("receiverId", receiverId);
        final HashMap<String, Messages> map = new HashMap<>();
        map.put(String.valueOf(System.currentTimeMillis()), m);
        messages.put("messages", map);

        fireBaseApp.collection("chat")
                .whereEqualTo(senderId, true)
                .whereEqualTo(receiverId, true)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        String docid = null;

                        if (task.isSuccessful())
                        {
                            for (QueryDocumentSnapshot doc : task.getResult())
                            {
                                docid = doc.getId();
                                //id = doc.getString("receiverId");
                            }
                            //callback.success(docid);
                            if (docid == null)
                            {
                                //callback.error("Chat not available!");
                                createChannel(msg, senderId, receiverId, callback);
                            }
                            else
                            {
                                //callback.success("Chat available!");
                                /*DocumentReference docRef = fireBaseApp.collection("chat").document(docid);
                                docRef.update("messages", map);*/

                                DocumentReference docRef = fireBaseApp.collection("chat").document(docid);
                                docRef.set(messages, SetOptions.merge());
                            }
                        }

                    }
                });

    }

    public static void createChannel(String msg, final String senderId, final String receiverId, final CallBackListener<String, String> callback)
    {
        final Messages m = new Messages(msg, senderId);
        final Map<String, Object> messages = new HashMap<>();
        final Map<String, String> ids = new HashMap<>();
        ids.put("id1", senderId);
        ids.put("id2", receiverId);
        messages.put(senderId, true);
        messages.put(receiverId, true);
        HashMap<String, Messages> map = new HashMap<>();
        map.put(String.valueOf(System.currentTimeMillis()), m);
        messages.put("messages", map);

        fireBaseApp.collection("chat")
                .document(senderId+receiverId)
                .set(messages)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void avoid) {
                        callback.success("Message Sent!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.error(e.getMessage());
                    }
                });
    }

    public void findChannel(String senderId, String receiverId, final CallBackListener<String, String> callback) {
        fireBaseApp = FirebaseFirestore.getInstance();

        /*CollectionReference cr = fireBaseApp.collection("chat");
        cr.whereArrayContains("users", "degdtkFQEmNRmESX1b5cxAZdBX52").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                if (queryDocumentSnapshots.getDocuments().size() > 0)
                {
                    //callback.success(queryDocumentSnapshots.getDocuments().get(0).toObject(Chat.class));
                    callback.success((queryDocumentSnapshots.getDocuments().get(0).toObject(Chat.class)));

                }

            }
        });*/

        fireBaseApp.collection("chat")
                .whereEqualTo(senderId, true)
                .whereEqualTo(receiverId, true)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                if (queryDocumentSnapshots.getDocuments().size() > 0)
                {
                    callback.success(queryDocumentSnapshots.getDocuments().get(0).toString());
                }

            }
        });

    }
}
