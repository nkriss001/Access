package com.optionsquared.access;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.text.SimpleDateFormat;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewHolder> {

    ArrayList<Review> reviews;

    public static class ReviewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView name;
        public TextView review;
        public RatingBar ratingBar;
        public ImageView sideBarColor;
        public TextView score;
        public ImageButton upvote;
        public ImageButton downvote;
        public TextView date;

        public ReviewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.reviewerName);
            review = v.findViewById(R.id.reviewText);
            ratingBar = v.findViewById(R.id.ratingBar);
            sideBarColor = v.findViewById(R.id.sideBar);
            score = v.findViewById(R.id.score);
            upvote = v.findViewById(R.id.upvote);
            downvote = v.findViewById(R.id.downvote);
            date = v.findViewById(R.id.date);

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

        final Review curr = reviews.get(position);
        holder.name.setText(curr.name);
        holder.review.setText(curr.text);
        holder.ratingBar.setRating(curr.rating);
        Calendar currentDate = Calendar.getInstance();
        currentDate.setTimeInMillis(curr.time);
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a MM/dd/YYYY");
        holder.date.setText(sdf.format(curr.time));

        final TextView score = holder.score;
        displayVotes(score, curr);

        holder.upvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                curr.votes += 1;
                displayVotes(score, curr);
                if (!(curr.isReview)) {
                    curr.time = Calendar.getInstance().getTimeInMillis();
                }
            }
        });

        holder.downvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                curr.votes -= 1;
                displayVotes(score, curr);
            }
        });

        if(curr.isReview) {
            holder.sideBarColor.setBackgroundResource(R.drawable.ic_reviews_background);
        } else {
            holder.sideBarColor.setBackgroundResource(R.drawable.ic_issues_background);
            holder.ratingBar.setVisibility(View.GONE);
        }


    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return reviews.size();
    }

    // Display the number of votes for a review
    private void displayVotes(TextView score, Review curr) {
        if (curr.votes > 0) {
            score.setText("+" + Integer.toString(curr.votes));
            score.setTextColor(Color.GREEN);
        } else if (curr.votes < 0) {
            score.setText(Integer.toString(curr.votes));
            score.setTextColor(Color.RED);
        } else {
            score.setText(Integer.toString(curr.votes));
            score.setTextColor(Color.GRAY);
        }
    }
}