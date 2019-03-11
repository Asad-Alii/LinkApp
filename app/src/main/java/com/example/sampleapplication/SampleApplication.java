package com.example.sampleapplication;

import android.app.Application;
import android.widget.Toast;

import com.example.sampleapplication.Utils.VolleyRequest;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class SampleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        VolleyRequest.init(this);
        //Toast.makeText(this, "Application Started!", Toast.LENGTH_SHORT).show();
    }
}
