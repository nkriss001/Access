package com.optionsquared.access;

import android.app.ActionBar;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Typeface;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.Gravity;
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

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    FirebaseDatabase database;
    DatabaseReference ref;
    SerialPlace selectedLoc = null;
    private GoogleMap mMap;
    PlaceAutocompleteFragment placeAutoComplete;
    private final String APIKEY = "AIzaSyBbkrnKO95otvPVdAYWwNGCa2Sxx6Vcxik";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.database = FirebaseDatabase.getInstance();
        ref = database.getReference();

        createDummyPlace();
        initSlidingPanel();
        getPlace("foo");
        initMap();

    }

    public int pxToDp(int px) {

        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    private void initSlidingPanel() {
        final ImageButton arrow = findViewById(R.id.arrow);

        arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final LinearLayout results = findViewById(R.id.results);
                final ImageButton arrow = findViewById(R.id.arrow);
                final RecyclerView recyclerView = findViewById(R.id.recycler);
                final ConstraintLayout card = findViewById(R.id.constraintLayout);



                final Boolean[] extend = {false};
                arrow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LinearLayout.MarginLayoutParams params = (LinearLayout.MarginLayoutParams) results.getLayoutParams();
                        if (!extend[0]) {
                            Point size = new Point();
                            getWindowManager().getDefaultDisplay().getSize(size);
                            int height = size.y - 950;
                            ViewGroup.LayoutParams lp = card.getLayoutParams();
                            lp.height = -3;
                            card.setLayoutParams(lp);
                            results.animate().translationY(-height);

                            LinearLayout.LayoutParams lp2 =
                                    new LinearLayout.LayoutParams(
                                            RelativeLayout.LayoutParams.MATCH_PARENT,
                                            size.y - results.getHeight() + 200);
                            recyclerView.setLayoutParams(lp2);

                            arrow.setImageResource(android.R.drawable.arrow_down_float);

                            final LinearLayoutManager manager =
                                    new LinearLayoutManager(
                                            v.getContext(),
                                            LinearLayoutManager.VERTICAL,
                                            false);

                            ArrayList<Review> issues = new ArrayList(selectedLoc.issues);
                            ArrayList<Review> reviews =  new ArrayList(selectedLoc.reviews);
                            issues.addAll(reviews);
                            ReviewAdapter r = new ReviewAdapter(issues);
                            recyclerView.setAdapter(r);
                            recyclerView.setLayoutManager(manager);
                            extend[0] = true;
                        } else {
                            results.animate().translationY(0);
                            arrow.setImageResource(android.R.drawable.arrow_up_float);
                            Point size = new Point();
                            getWindowManager().getDefaultDisplay().getSize(size);
                            ViewGroup.LayoutParams lp = card.getLayoutParams();
                            lp.height = -1;
                            card.setLayoutParams(lp);

                            LinearLayout.LayoutParams lp2 =
                                    new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 300);
                            recyclerView.setLayoutParams(lp2);
                            ReviewAdapter r = new ReviewAdapter(new ArrayList());
                            recyclerView.setAdapter(r);
                            extend[0] = false;
                        }
                        results.setLayoutParams(params);
                    }
                });
            }
        });
    }

    private void initMap() {
        final TextView name = findViewById(R.id.name);
        final TextView alerts = findViewById(R.id.alerts);
        final RatingBar rating = findViewById(R.id.rating);
        final LinearLayout results = findViewById(R.id.results);
        final ImageView imageView = findViewById(R.id.locationImage);

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
                imageView.setImageResource(R.drawable.dwinelle);
                results.setVisibility(View.VISIBLE);
                name.setText(selectedLoc.key);
                rating.setRating(selectedLoc.avgRating);
                ArrayList<Review> issues = selectedLoc.issues;
                if (issues.size() > 0) {
                    alerts.setText(issues.size() + " Alerts!");
                } else {
                    alerts.setText("No Alerts");
                }
                results.setVisibility(View.VISIBLE);
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

