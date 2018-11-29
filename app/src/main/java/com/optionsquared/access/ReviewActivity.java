package com.optionsquared.access;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.text.SimpleDateFormat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class ReviewActivity extends AppCompatActivity {
    FirebaseDatabase database;
    DatabaseReference ref;
    private static final int CAMERA_REQUEST = 1888;
    private static final int PHOTOS_REQUEST = 2000;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        this.database = FirebaseDatabase.getInstance();
        ref = database.getReference();

        final RatingBar ratingBar = findViewById(R.id.ratingBar);
        final EditText reviewText = findViewById(R.id.reviewText);
        final EditText name = findViewById(R.id.reviewerName);
        final TextView ratingText = findViewById(R.id.ratingText);
        imageView = (ImageView)this.findViewById(R.id.imageView);
        ImageButton photoSubmitButton = findViewById(R.id.imageButton);
        Button submitReviewButton = findViewById(R.id.submitReview);

        Toolbar toolbar = findViewById(R.id.reviewToolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        photoSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence[] items = { "Take Photo", "Choose from Gallery", "Cancel" };
                AlertDialog.Builder builder = new AlertDialog.Builder(ReviewActivity.this, R.style.AlertDialog);
                builder.setTitle("Add Photo!");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (items[item].equals("Take Photo")) {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent, CAMERA_REQUEST);
                        } else if (items[item].equals("Choose from Gallery")) {
                            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            // Start the Intent
                            startActivityForResult(galleryIntent, PHOTOS_REQUEST);
                        } else if (items[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
//                Intent galleryintent = new Intent(Intent.ACTION_PICK,
//                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                galleryintent.setType("image/*");
//
//                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//
//                Intent chooser = new Intent(Intent.ACTION_CHOOSER);
//                chooser.putExtra(Intent.EXTRA_INTENT, galleryintent);
//                chooser.putExtra(Intent.EXTRA_TITLE, "title");
//
//                Intent[] intentArray =  {cameraIntent};
//                chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
//                startActivityForResult(chooser, CAMERA_REQUEST);
            }
        });

        submitReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (reviewText.getText().toString().isEmpty()) {
                    Toast.makeText(ReviewActivity.this, "Please add text to your review", Toast.LENGTH_LONG).show();
                } else if (name.getText().toString().isEmpty()) {
                    Toast.makeText(ReviewActivity.this, "Please include your name", Toast.LENGTH_LONG).show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ReviewActivity.this, R.style.AlertDialog);
                    builder.setMessage("Ready to submit?");
                    builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            int rating = (int) ratingBar.getRating();
                            String text = reviewText.getText().toString();
                            // TODO : decide on time and username formats
//                             alternatively: long time = Calendar.getInstance().getTimeInMillis();
                            // Calendar today = Calendar.getInstance();
                            // SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YY");
                            // String time = sdf.format(today.getTime());
                            long time = Calendar.getInstance().HOUR;
                            Review review = new Review(rating, text, time, name.getText().toString(), true, 0);
//                            SerialPlace location = (SerialPlace) getIntent().getSerializableExtra("location");
                            Intent intent = new Intent(ReviewActivity.this, MainActivity.class);
                            intent.putExtra("review", review);
                            setResult(RESULT_OK, intent);
                            finish();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //FROM CAMERA
        if (requestCode == CAMERA_REQUEST) {
            if (resultCode == this.RESULT_OK) {
                Bitmap bp = (Bitmap) data.getExtras().get("data");
                imageView.setImageBitmap(bp);
                File fileDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Documents/Images/");


            }
        }

            //FROM GALLERY
        else{
            File fileDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Documents/TWINE/");
            Uri selectedImage = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
            imageView.setImageBitmap(bitmap);

            }
    }
}

