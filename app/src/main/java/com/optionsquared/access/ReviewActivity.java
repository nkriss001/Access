package com.optionsquared.access;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
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
    private static final int PICK_FROM_GALLERY = 1;
    private ImageView imageView;
    private String image = null;
    private String picType = null;


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
                            if (checkSelfPermission(Manifest.permission.CAMERA)
                                    != PackageManager.PERMISSION_GRANTED) {
                                requestPermissions(new String[]{Manifest.permission.CAMERA},
                                        MY_CAMERA_PERMISSION_CODE);
                            }
                            else {
                                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                startActivityForResult(intent, CAMERA_REQUEST);
                            }
                        } else if (items[item].equals("Choose from Gallery")) {
                            if (ActivityCompat.checkSelfPermission(ReviewActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(ReviewActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_FROM_GALLERY);
                                //System.out.println("HERE IN REQUESTING IMAGES");
                            }
                            Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            // Start the Intent
                            startActivityForResult(galleryIntent, PHOTOS_REQUEST);
                        } else if (items[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            }
        });

        submitReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (name.getText().toString().isEmpty()) {
                    Toast.makeText(ReviewActivity.this, "Please include your name", Toast.LENGTH_LONG).show();
                } else if (reviewText.getText().toString().isEmpty()) {
                    Toast.makeText(ReviewActivity.this, "Please add text to your review", Toast.LENGTH_LONG).show();
                } else if (ratingBar.getRating() == 0) {
                    Toast.makeText(ReviewActivity.this, "Please rate the location", Toast.LENGTH_LONG).show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ReviewActivity.this, R.style.AlertDialog);
                    builder.setMessage("Ready to submit?");
                    builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            int rating = (int) ratingBar.getRating();
                            String text = reviewText.getText().toString();
                            // TODO : decide on time and username formats
                            // Calendar today = Calendar.getInstance();
                            // SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YY");
                            // String time = sdf.format(today.getTime());
                            long time = Calendar.getInstance().getTimeInMillis();
                            Review review = new Review(rating, text, time, name.getText().toString(), true, 0);
//                            SerialPlace location = (SerialPlace) getIntent().getSerializableExtra("location");
                            Intent intent = new Intent(ReviewActivity.this, MainActivity.class);
                            intent.putExtra("review", review);
                            System.out.println("SUBMIT REVIEW IMAGE GALLERY: " + image);
                            intent.putExtra("image", image);
                            intent.putExtra("type", picType);

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
                    //dialog.getWindow().setLayout(400, 200);
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
                        ratingText.setText("Awesome!");
                        break;
                    default:
                        ratingText.setText("");
                }
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new
                        Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }

        }
        else{
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //System.out.println("HERE IN ON REQUEST IMAGES");
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, PICK_FROM_GALLERY);
            } else {
                //do something like displaying a message that he didn`t allow the app to access gallery and you wont be able to let him select from gallery
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //FROM CAMERA
        if (requestCode == CAMERA_REQUEST) {
            if (resultCode == this.RESULT_OK) {
                Bitmap bp = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream byteStream =new  ByteArrayOutputStream();
                bp.compress(Bitmap.CompressFormat.PNG,100, byteStream);
                byte [] b=byteStream.toByteArray();
                image = Base64.encodeToString(b, Base64.DEFAULT);
                //System.out.println("IMAGE: " + image);
                imageView.setImageBitmap(bp);
                picType = "camera";

            }
        }

            //FROM GALLERY
        else {
            if (data != null) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();
                //System.out.println("PICTURE PATH: " + picturePath);
                imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                image = picturePath;
                picType = "gallery";
                }
            }
        }
    }

