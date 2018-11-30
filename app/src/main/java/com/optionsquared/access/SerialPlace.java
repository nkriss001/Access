package com.optionsquared.access;

import android.graphics.Bitmap;
import android.net.Uri;

import com.google.firebase.database.DataSnapshot;

import java.io.Serializable;
import java.util.ArrayList;

public class SerialPlace implements Serializable {
    public ArrayList<Review> reviews;
    public ArrayList<Review> issues;
    public ArrayList<Bitmap> images;
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

    public void addImage(Bitmap image) {
        this.images.add(image);
    }

}