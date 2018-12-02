package com.optionsquared.access;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import com.google.firebase.database.DataSnapshot;

import java.io.Serializable;
import java.util.ArrayList;

public class SerialPlace implements Serializable {
    public ArrayList<Review> reviews;
    public ArrayList<Review> issues;
    public ArrayList<String> images;
    public long avgRating;
    public String key;
    public String addr;

    SerialPlace(String key, String addr) {
        this.addr = addr;
        this.key = key;
        reviews = new ArrayList<>();
        issues = new ArrayList<>();
        images = new ArrayList<>();
        setRating();
    }

    SerialPlace(DataSnapshot dataSnapshot) {
        this.key = dataSnapshot.getKey();
        this.addr = (String) dataSnapshot.child("addr").getValue();
        this.avgRating = (long) dataSnapshot.child("avgRating").getValue();
        this.images = new ArrayList<>();

        reviews = new ArrayList<>();
        issues = new ArrayList<>();


        DataSnapshot reviews = dataSnapshot.child("reviews");
        Review temp;
        for (DataSnapshot review : reviews.getChildren()) {
            temp = new Review(review);
            addReview(temp);
        }

        DataSnapshot issues = dataSnapshot.child("issues");
        for (DataSnapshot issue : issues.getChildren()) {
            temp = new Review(issue);
            addIssue(temp);
        }

        DataSnapshot imageBitmaps = dataSnapshot.child("images");
        for (DataSnapshot image : imageBitmaps.getChildren()) {
            String imgTemp = image.toString();
            addImage(imgTemp);
        }
    }


    private Bitmap stringToBitMap(String encodedString){
        try {
            byte [] encodeByte=Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap=BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }

    public void addReview(Review r) {
        if (r != null) {
            reviews.add(r);
            // recompute the average rating
            setRating();
        }
    }

    public void addIssue(Review r) {
        issues.add(r);
    }


    public void setRating() {
        if (reviews.size() == 0) {
            return;
        } else {
            int sum = 0;
            for (Review r : this.reviews) {
                sum += r.getRating();
            }
            this.avgRating = sum / reviews.size();
        }
    }

    public void addImage(String image) {
        this.images.add(image);
    }
    public void addImageFirst(String image){
        this.images.add(0, image);
    }

    public ArrayList<Bitmap> getImageBitmaps() {
        ArrayList<Bitmap> bms = new ArrayList<>();
        for (String i : this.images) {
            bms.add(stringToBitMap(i));
        }
        return bms;
    }
    public Bitmap getFirstImageBitmap(){
        if (images.size() > 0) {
            return stringToBitMap(images.get(0));
        }
        return null;
    }

}