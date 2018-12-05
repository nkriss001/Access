package com.optionsquared.access;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;

import java.io.Serializable;

public class Review implements Serializable, Comparable<Review> {

    public int rating;
    public String text;
    public long time;
    public String name;
    public boolean isReview;


    public int votes;
    // TODO: add way of storing photo, possibly just string id of location in firebase database

    Review(int rating, String text, long time, String name, boolean isReview, int votes) {
        this.rating = rating;
        this.text = text;
        this.time = time;
        this.name = name;
        this.isReview = isReview;
        this.votes = votes;
    }

    Review(DataSnapshot review) {
        this.rating = ((Long) review.child("rating").getValue()).intValue();
        this.text = (String) review.child("text").getValue();
        this.time = (Long) review.child("time").getValue();
        this.name = (String) review.child("name").getValue();
        this.isReview = (Boolean) review.child("isReview").getValue();
        this.votes = ((Long) review.child("votes").getValue()).intValue();
    }


    //TODO: add getters and setters

    public int getRating() {
        return this.rating;
    }

    @Override
    public int compareTo(@NonNull Review review) {
        return (int)(this.time - review.time);
    }
}