package com.optionsquared.access;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;

public class ReviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        final RatingBar ratingBar = findViewById(R.id.ratingBar);
        final EditText reviewText = findViewById(R.id.reviewText);
        Button submitReviewButton = findViewById(R.id.submitReview);

        submitReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (reviewText.getText().toString().isEmpty()) {

                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ReviewActivity.this);
                    builder.setMessage("Ready to submit?");
                    builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // submit the review
                            Review review = new Review(ratingBar.getNumStars(), reviewText.toString());
                            Intent intent = new Intent(ReviewActivity.this, MainActivity.class);
                            intent.putExtra("review", review);
                            setResult(RESULT_OK, intent);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });

        /* TODO : add text upon rating change
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {

            }
        });
        */
    }
}
