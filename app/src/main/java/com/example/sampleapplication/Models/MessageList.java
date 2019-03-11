package com.example.sampleapplication.Models;

public class MessageList {

    public String message;
    public String senderId;

    public MessageList(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

   /* public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }*/
}
