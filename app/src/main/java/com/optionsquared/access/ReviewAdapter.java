package com.optionsquared.access;

import android.media.Rating;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewHolder> {

    ArrayList<Review> reviews;

    public static class ReviewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView name;
        public TextView review;
        public RatingBar ratingBar;
        public ImageView sideBarColor;

        public ReviewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.name);
            review = v.findViewById(R.id.reviewText);
            ratingBar = v.findViewById(R.id.ratingBar);
            sideBarColor = v.findViewById(R.id.sideBar);

        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ReviewAdapter(ArrayList<Review> reviews) {
        this.reviews = reviews;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ReviewAdapter.ReviewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.review_item, parent, false);

        ReviewHolder vh = new ReviewHolder(v);

        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ReviewHolder holder, int position) {

        Review curr = reviews.get(position);
        holder.name.setText(curr.name);
        holder.review.setText(curr.text);
        holder.ratingBar.setRating(curr.rating);

        if(curr.isReview) {
            holder.sideBarColor.setBackgroundResource(R.drawable.ic_reviews_background);
        } else {
            holder.sideBarColor.setBackgroundResource(R.drawable.ic_issues_background);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return reviews.size();
    }
}