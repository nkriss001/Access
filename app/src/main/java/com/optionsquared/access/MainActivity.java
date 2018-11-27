package com.optionsquared.access;

import android.content.Intent;
import android.graphics.Point;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    FirebaseDatabase database;
    DatabaseReference ref;
    SerialPlace selectedLoc = null;
    private GoogleMap mMap;
    PlaceAutocompleteFragment placeAutoComplete;
    private final String APIKEY = "AIzaSyBbkrnKO95otvPVdAYWwNGCa2Sxx6Vcxik";
    static int REVIEW_REQUEST = 1;
    static int ISSUE_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.database = FirebaseDatabase.getInstance();
        ref = database.getReference();

        Button reviewButton = findViewById(R.id.reviewButton);
        Button issueButton = findViewById(R.id.issueButton);

        createDummyPlace();

        getPlace("foo");
        initMap();

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
        final LinearLayoutManager manager =
                new LinearLayoutManager(
                        this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REVIEW_REQUEST) {
            if (resultCode == RESULT_OK) {
                Review review = (Review) data.getSerializableExtra("review");
                selectedLoc.addReview(review);
                ref.child("places").child(selectedLoc.key).setValue(selectedLoc);
                getPlace(selectedLoc.key);
            }
        } else if (requestCode == ISSUE_REQUEST) {
            if (resultCode == RESULT_OK) {
                Review issue = (Review) data.getSerializableExtra("issue");
                selectedLoc.addIssue(issue);
                ref.child("places").child(selectedLoc.key).setValue(selectedLoc);
                getPlace(selectedLoc.key);
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
                addMarker(place);
                getPlace((String) place.getName());
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

    void createDummyPlace() {
        SerialPlace foo = new SerialPlace("Dwinelle Hall");

        Review bar = new Review(3, "Dwinelle is unpredictable in terms of elevators working.", (long) 4.20, "Oski", true, 0);
        Review baz = new Review(1, "The elevator is always broken and the layout is confusing.", (long) 4.20, "Dirks", true, 0);
        Review b3 = new Review(3, "Dwinelle is unpredictable in terms of elevators working.", (long) 4.20, "Oski", true, 0);
        Review b4 = new Review(1, "The elevator is always broken and the layout is confusing.", (long) 4.20, "Dirks", true, 0);

        Review bork = new Review(1, "The elevator is down!", (long) 4.20, "Carol Christ", false, 0);

        foo.addReview(b3);
        foo.addReview(b4);
        foo.addReview(bar);
        foo.addReview(baz);
        foo.addIssue(bork);

        ref.child("places").child("Dwinelle Hall").setValue(foo);

        SerialPlace soda = new SerialPlace("Soda Hall");

        Review b5 = new Review(5, "Soda's super easy to navigate.", (long) 4.20, "Alice", true, 0);
        Review b6 = new Review(4, "The elevator is always available", (long) 4.20, "Bob", true, 0);

        //Review bark = new Review(1, "The elevator is down!", (long) 4.20, "Carol Christ", false, 0);

        soda.addReview(b5);
        soda.addReview(b6);
        //foo.addIssue(bark);

        ref.child("places").child("Soda Hall").setValue(soda);

        selectedLoc = soda;
    }

    /** Retrieves the SerialPlace information from the realtime database if it
     * exists, saves that info in selectedLoc or null if it doesn't exist.
     * @param key
     */
    void getPlace(String key) {
        DatabaseReference placeRef = ref.child("places").child(key);

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
                    ArrayList<Review> outputs = new ArrayList<>();
                    issues = selectedLoc.issues;
                    ArrayList<Review> reviews = selectedLoc.reviews;
                    outputs.addAll(issues);
                    outputs.addAll(reviews);
                    ReviewAdapter r = new ReviewAdapter(outputs);
                    recyclerView.setAdapter(r);
                    recyclerView.setLayoutManager(manager);

                    mLayout.setScrollableView(recyclerView);
                } else {
                    selectedLoc = null;
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





