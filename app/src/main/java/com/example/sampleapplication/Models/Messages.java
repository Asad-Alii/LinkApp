package com.example.sampleapplication.Models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Messages {

    //ArrayList<String> users = new ArrayList<>();
    public String senderId, message;

    @ServerTimestamp
    public Date time;

    public Messages(String message, String senderId)
    {
        this.message = message;
        this.senderId = senderId;
        //this.users = users;
    }

    /*public ArrayList<String> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<String> users) {
        this.users = users;
    }*/

    public String getSenderId() {
        return senderId;
    }

    public String getMessage() {
        return message;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
