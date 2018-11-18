package com.optionsquared.access;

import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
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
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    FirebaseDatabase database;
    DatabaseReference ref;
    SerialPlace selectedLoc = null;
    private GoogleMap mMap;
    PlaceAutocompleteFragment placeAutoComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.database = FirebaseDatabase.getInstance();
        ref = database.getReference();

        createDummyPlace();

        final TextView name = findViewById(R.id.name);
        final TextView alerts = findViewById(R.id.alerts);
        final LinearLayout results = findViewById(R.id.results);
        final LinearLayout scroll = findViewById(R.id.scroll);
        final ImageButton arrow = findViewById(R.id.arrow);
        final RatingBar rating = findViewById(R.id.rating);
        final Boolean[] extend = {false};
        arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout.MarginLayoutParams params = (LinearLayout.MarginLayoutParams) results.getLayoutParams();
                if (!extend[0]) {
                    results.animate().translationY(-1000);
                    arrow.setImageResource(android.R.drawable.arrow_down_float);
                    ArrayList<Review> issues = selectedLoc.issues;
                    ArrayList<Review> reviews = selectedLoc.reviews;
                    TextView reviewText = new TextView(v.getContext());
                    scroll.addView(reviewText);
                    reviewText.setText("Reviews:");
                    for (Review review : reviews) {
                        CardView newReview = new CardView(v.getContext());
                        LinearLayout reviewLayout = new LinearLayout(v.getContext());
                        reviewLayout.setOrientation(LinearLayout.VERTICAL);
                        scroll.addView(newReview);
                        newReview.addView(reviewLayout);
                        TextView username = new TextView(v.getContext());
                        username.setText(review.name);
                        reviewLayout.addView(username);
                        RatingBar stars = new RatingBar(v.getContext());
                        stars.setMax(5);
                        stars.setRating(review.rating);
                        reviewLayout.addView(stars);
                        TextView reviewContent = new TextView(v.getContext());
                        reviewContent.setText(review.text);
                        reviewLayout.addView(reviewContent);
                    }
                    extend[0] = true;
                } else {
                    results.animate().translationY(0);
                    arrow.setImageResource(android.R.drawable.arrow_up_float);
                    scroll.removeAllViews();
                    extend[0] = false;
                }
                results.setLayoutParams(params);
            }
        });

        getPlace("foo");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        placeAutoComplete = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete);
         placeAutoComplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                name.setText(place.getName());
                rating.setRating(selectedLoc.avgRating);
                ArrayList<Review> issues = selectedLoc.issues;
                if (issues.size() > 0) {
                    alerts.setText(issues.size() + " Alerts");
                } else {
                    alerts.setText("");
                }
                results.setVisibility(View.VISIBLE);
                addMarker(place);
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
        SerialPlace foo = new SerialPlace("foo");
        Review bar = new Review(3, "bad bad", (long) 4.20, "bar", true, 0);
        Review baz = new Review(1, "super bad", (long) 4.20, "baz", true, 0);
        foo.addReview(bar);
        foo.addReview(baz);

        ref.child("places").child("foo").setValue(foo);
    }

    /** Retrieves the Place information from the realtime database if it
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
