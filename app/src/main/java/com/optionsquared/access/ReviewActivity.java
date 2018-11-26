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
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;

public class ReviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        final RatingBar ratingBar = findViewById(R.id.ratingBar);
        final EditText reviewText = findViewById(R.id.reviewText);
        final EditText name = findViewById(R.id.name);
        final TextView ratingText = findViewById(R.id.ratingText);
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
                            long currentTime = Calendar.getInstance().getTimeInMillis();
                            Review review = new Review((int)ratingBar.getRating(), reviewText.toString(), currentTime, name.toString(), true, 0);
                            SerialPlace location = (SerialPlace) getIntent().getSerializableExtra("location");
                            location.addReview(review);
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

//         TODO : add text upon rating change
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                ratingText.setText(String.valueOf(v));
                switch ((int)ratingBar.getRating()) {
                    case 1:
                        ratingText.setText("Very bad");
                        break;
                    case 2:
                        ratingText.setText("Needs improvement");
                        break;
                    case 3:
                        ratingText.setText("Good");
                        break;
                    case 4:
                        ratingText.setText("Great");
                        break;
                    case 5:
                        ratingText.setText("Awesome");
                        break;
                    default:
                        ratingText.setText("");
                }
            }
        });
    }
}
