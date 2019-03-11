package com.example.sampleapplication.Models;

import android.print.PrinterId;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class UserList {

    @ServerTimestamp
    public Date time;

    private String Id;
    private String Username;
    private String Email;
    private String Contact;
    private String Password;
    private String imageUrl;
    private String deviceToken;

    public UserList(String id, String username, String email, String contact, String password, String imageUrl, String deviceToken) {

        Id = id;
        Username = username;
        Email = email;
        Contact = contact;
        Password = password;
        this.imageUrl = imageUrl;
        this.deviceToken = deviceToken;

    }

    public String getId()
    {
        return Id;
    }

    public String getName()
    {
        return Username;
    }

    public String getEmail()
    {
        return Email;
    }

    public String getContact()
    {
        return Contact;
    }

    public String getPassword()
    {
        return Password;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
