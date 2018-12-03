package com.optionsquared.access;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.text.SimpleDateFormat;

public class ReviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<Object> reviews;
    SerialPlace selectedLoc;
    DatabaseReference ref;
    static final boolean issueHeader = false;
    static final boolean reviewHeader = false;
    static final int TYPE_HEADER = 0;
    static final int TYPE_ITEM = 1;

    public static class ReviewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView name;
        public TextView review;
        public RatingBar ratingBar;
        public ImageView sideBarColor;
        public TextView score;
        public ImageButton upvote;
        public ImageButton downvote;
        public LinearLayout votingLayout;
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
            votingLayout = v.findViewById(R.id.votingLayout);
            date = v.findViewById(R.id.date);

        }
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder{
        public TextView headerTitle;
        public HeaderViewHolder(View itemView) {
            super(itemView);
            headerTitle = (TextView)itemView.findViewById(R.id.header_id);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ReviewAdapter(ArrayList<Object> reviews, SerialPlace selectedLoc,
                         DatabaseReference ref) {
        this.selectedLoc = selectedLoc;
        this.ref = ref;
        this.reviews = reviews;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.header, parent, false);
            return new HeaderViewHolder(v);
        } else if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.review_item, parent, false);
            return new ReviewHolder(v);
        }
        throw new RuntimeException("No match for " + viewType + ".");
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        final Object curr = reviews.get(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).headerTitle.setText((String) curr);
        } else if (holder instanceof ReviewHolder) {
            ReviewHolder reviewHolder = (ReviewHolder) holder;
            final Review review = (Review) curr;
            reviewHolder.name.setText(review.name);
            reviewHolder.review.setText(review.text);
            reviewHolder.ratingBar.setRating(review.rating);
            Calendar currentDate = Calendar.getInstance();
            currentDate.setTimeInMillis(review.time);
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a MM/dd/yyyy");
            reviewHolder.date.setText(sdf.format(review.time));

            final TextView score = reviewHolder.score;
            displayVotes(score, review);

            reviewHolder.upvote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    review.votes += 1;
                    displayVotes(score, review);
                    if (!(review.isReview)) {
                        review.time = Calendar.getInstance().getTimeInMillis();
                    }
                    ref.child("places").child(selectedLoc.key).setValue(selectedLoc);
                }
            });

            reviewHolder.downvote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    review.votes -= 1;
                    displayVotes(score, review);
                    ref.child("places").child(selectedLoc.key).setValue(selectedLoc);
                }
            });

            if(review.isReview) {
                reviewHolder.sideBarColor.setBackgroundResource(R.drawable.ic_reviews_background);
                reviewHolder.votingLayout.setVisibility(View.GONE);
            } else {
                reviewHolder.sideBarColor.setBackgroundResource(R.drawable.ic_issues_background);
                reviewHolder.ratingBar.setVisibility(View.GONE);
            }
        }




    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return reviews.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (reviews.get(position) instanceof Review) {
            return TYPE_ITEM;
        }
        return TYPE_HEADER;
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