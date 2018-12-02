package com.optionsquared.access;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.firebase.database.core.Repo;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    FirebaseDatabase database;
    FirebaseStorage storage;
    StorageReference storageReference;
    DatabaseReference ref;
    SerialPlace selectedLoc = null;
    private GoogleMap mMap;
    PlaceAutocompleteFragment placeAutoComplete;
    private final String APIKEY = "AIzaSyBbkrnKO95otvPVdAYWwNGCa2Sxx6Vcxik";
    static int REVIEW_REQUEST = 1;
    static int ISSUE_REQUEST = 2;
    private Context c;
    LinearLayout card;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(true);
        ref = database.getReference();
        card = findViewById(R.id.card);

        Button reviewButton = findViewById(R.id.reviewButton);
        Button issueButton = findViewById(R.id.issueButton);

        initMap();
        c = getApplicationContext();

        reviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, ReviewActivity.class);
                i.putExtra("location", selectedLoc);
                startActivityForResult(i, REVIEW_REQUEST);
            }
        });

        issueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, ReportIssueActivity.class);
                i.putExtra("location", selectedLoc);
                startActivityForResult(i, ISSUE_REQUEST);
            }
        });

        SlidingUpPanelLayout mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        if (mLayout != null) {
            mLayout.setAnchorPoint(0.9f);
        }

        final RecyclerView recyclerView = findViewById(R.id.recycler);
        final LinearLayoutManager manager = new LinearLayoutManager(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REVIEW_REQUEST) {
            if (resultCode == RESULT_OK) {
                Review review = (Review) data.getSerializableExtra("review");
                String image = (String) data.getStringExtra("image");
                if (image != null){
                    selectedLoc.addImage(image);
                }
                selectedLoc.addReview(review);
                ref.child("places").child(selectedLoc.key).setValue(selectedLoc);
                getPlace(selectedLoc.key, selectedLoc.addr);
            }
        } else if (requestCode == ISSUE_REQUEST) {
            if (resultCode == RESULT_OK) {
                Review issue = (Review) data.getSerializableExtra("issue");
                selectedLoc.addIssue(issue);
                ref.child("places").child(selectedLoc.key).setValue(selectedLoc);
                getPlace(selectedLoc.key, selectedLoc.addr);
            }
        }
    }

    private void initMap() {

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        placeAutoComplete = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete);
        placeAutoComplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                SlidingUpPanelLayout mLayout = findViewById(R.id.sliding_layout);
                mLayout.setPanelHeight(400);
                addMarker(place);
                getPlace((String) place.getName(), place.getAddress().toString());
            }

            @Override
            public void onError(Status status) {
                Log.d("Maps", "An error occurred: " + status);
            }
        });

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    public void addMarker(Place p){
        mMap.clear();
        MarkerOptions markerOptions = new MarkerOptions();

        markerOptions.position(p.getLatLng());
        markerOptions.title(p.getName()+"");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

        mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(p.getLatLng()));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));

    }

    /** Retrieves the SerialPlace information from the realtime database if it
     * exists, saves that info in selectedLoc or null if it doesn't exist.
     * @param key
     */
    void getPlace(String key, final String addr) {
        final String keyString = key.replace(".", "");
        DatabaseReference placeRef = ref.child("places").child(keyString);

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    selectedLoc = new SerialPlace(dataSnapshot);

                    final TextView name = findViewById(R.id.name);
                    final TextView alerts = findViewById(R.id.alerts);
                    final RatingBar rating = findViewById(R.id.rating);
                    final ImageView imageView = findViewById(R.id.locationImage);
                    SlidingUpPanelLayout mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
                    Bitmap imageDisplay = null;
                    if (selectedLoc.images.size() > 0){
                        imageDisplay = selectedLoc.getFirstImageBitmap();
                        imageView.setImageBitmap(imageDisplay);
                    }
                    else {
                        imageView.setImageResource(R.drawable.dwinelle);
                    }
                    imageView.setImageResource(R.drawable.dwinelle);
                    name.setText(selectedLoc.key);
                    rating.setRating(selectedLoc.avgRating);
                    ArrayList<Review> issues = selectedLoc.issues;
                    ArrayList<Review> removed = new ArrayList<>();
                    long time = Calendar.getInstance().getTimeInMillis();
                    for (Review issue : issues) {
                        if (time - issue.time > (long) 8.64e+7) {
                            removed.add(issue);
                        }
                    }
                    issues.removeAll(removed);
                    if (issues.size() == 1) {
                        alerts.setText(issues.size() + " Issue!");
                    } else if (issues.size() > 1) {
                        alerts.setText(issues.size() + " Issues!");
                    } else {
                        alerts.setText("No Issues");
                        alerts.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
                    }

                    final RecyclerView recyclerView = findViewById(R.id.recycler);
                    final LinearLayoutManager manager =
                            new LinearLayoutManager(getApplicationContext());
                    ArrayList<Object> outputs = new ArrayList<>();
                    issues = selectedLoc.issues;
                    ArrayList<Review> reviews = selectedLoc.reviews;
                    if (!issues.isEmpty()) {
                        outputs.add("Issues");
                    }
                    outputs.addAll(issues);
                    if (!reviews.isEmpty()) {
                        outputs.add("Reviews");
                    }
                    outputs.addAll(reviews);
                    ReviewAdapter r = new ReviewAdapter(outputs, selectedLoc, ref);
                    recyclerView.setAdapter(r);
                    recyclerView.setLayoutManager(manager);

                    mLayout.setScrollableView(recyclerView);
                } else {
                    System.out.println("HERE");
                    final SerialPlace newPlace = new SerialPlace(keyString, addr);
                    final ImageView imageView = findViewById(R.id.locationImage);
                    ref.child("places").child(keyString).setValue(newPlace);
                    selectedLoc = newPlace;

                    final TextView name = findViewById(R.id.name);
                    final TextView alerts = findViewById(R.id.alerts);
                    final RatingBar rating = findViewById(R.id.rating);
                    SlidingUpPanelLayout mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);

                    imageView.setImageResource(R.drawable.dwinelle);
                    name.setText(selectedLoc.key);
                    rating.setRating(selectedLoc.avgRating);
                    ArrayList<Review> issues = selectedLoc.issues;
                    if (issues.size() > 0) {
                        alerts.setText(issues.size() + " Alerts!");
                    } else {
                        alerts.setText("No Alerts");
                    }

                    final RecyclerView recyclerView = findViewById(R.id.recycler);
                    final LinearLayoutManager manager =
                            new LinearLayoutManager(
                                    getApplicationContext());

                    ArrayList<Object> outputs = new ArrayList<>();
                    issues = selectedLoc.issues;
                    ArrayList<Review> reviews = selectedLoc.reviews;
                    if (!issues.isEmpty()) {
                        outputs.add("Issues");
                    }
                    outputs.addAll(issues);
                    if (!reviews.isEmpty()) {
                        outputs.add("Reviews");
                    }
                    outputs.addAll(reviews);
                    ReviewAdapter r = new ReviewAdapter(outputs, selectedLoc, ref);
                    recyclerView.setAdapter(r);
                    recyclerView.setLayoutManager(manager);

                    mLayout.setScrollableView(recyclerView);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                selectedLoc = null;
            }
        };

        placeRef.addListenerForSingleValueEvent(valueEventListener);
    }
}





