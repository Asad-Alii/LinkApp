package com.example.sampleapplication.Models;

public class ProfileImage {

    private String imageurl;

    public ProfileImage(){

    }

    public ProfileImage(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }
}
