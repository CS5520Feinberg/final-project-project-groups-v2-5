package edu.northeastern.rhythmlounge;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import edu.northeastern.rhythmlounge.Events.Event;
import edu.northeastern.rhythmlounge.Events.EventDetailsActivity;
import edu.northeastern.rhythmlounge.Events.EventsAdapter;
import edu.northeastern.rhythmlounge.HeatMapAdapters.HeatMapSpinnerAdapter;
import edu.northeastern.rhythmlounge.HeatMapAdapters.HeatMapSpinnerEventAdapter;
import edu.northeastern.rhythmlounge.HeatMapAdapters.HeatMapSpinnerUserAdapter;
import edu.northeastern.rhythmlounge.HeatMapSpinnerInventory.SpinnerData;

/**
 * Class to implement HeatMaps using Google MAPs SDK
 * The first part of the code is to initialise the map and get current devices' location
 * The second part of the code is to overlay HeatMap tile
 */
public class HeatMapsActivity extends AppCompatActivity implements OnMapReadyCallback, AdapterView.OnItemSelectedListener {

    private static final String TAG = "Activity____HeatMaps";
    private static final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = android.Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int ERROR_DIALOGUE_REQ = 9001;
    private static final int LOCATION_PERMISSION_REQ_CODE = 1234;
    private static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9002;
    private static final float DEFAULT_ZOOM = 15;
    private Boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    //Widgets
    private EditText mSearchText;
    private FirebaseAuth mAuth;
    private ImageView mGps, mSearchmaps;
    private boolean isSpinnerTouched_events, isSpinnerTouched_users = false;
    private static final int ALT_HEATMAP_RADIUS = 10;

    /**
     * Alternative opacity of heatmap overlay
     */
    private static final double ALT_HEATMAP_OPACITY = 0.7;

    /**
     * Alternative heatmap gradient (blue -> red)
     * Copied from Javascript version
     */
    private static final int[] ALT_HEATMAP_GRADIENT_COLORS = {
            Color.argb(0, 0, 255, 255),// transparent
            Color.argb(255 / 3 * 2, 0, 255, 255),
            Color.rgb(0, 191, 255),
            Color.rgb(0, 0, 127),
            Color.rgb(255, 0, 0)
    };

    public static final float[] ALT_HEATMAP_GRADIENT_START_POINTS = {
            0.0f, 0.10f, 0.20f, 0.60f, 1.0f
    };

    public static final Gradient ALT_HEATMAP_GRADIENT = new Gradient(ALT_HEATMAP_GRADIENT_COLORS,
            ALT_HEATMAP_GRADIENT_START_POINTS);

    private HeatmapTileProvider mProvider;

    private boolean isMapReady = false;
    private TileOverlay mOverlay;

    private boolean mDefaultGradient = true;
    private boolean mDefaultRadius = true;
    private boolean mDefaultOpacity = true;

    private boolean mIsRestore;
    private boolean flag_isopen, flag_isopen2, flag_isopen3, flag_isopen4 = false;
    private CameraPosition savedCameraPosition;
    private FirebaseFirestore mDb;
    private UserLocation mUserLocation;
    private SupportMapFragment supportMapFragment;

    private CollectionReference userLocationRef;

    private ArrayList<UserLocation> userArrayList = new ArrayList<>();
    private List<GeoPoint> geoPoints = new ArrayList<GeoPoint>();

    private List<LatLng> event_dataPoints = new ArrayList<>();

    List<HashMap<String, Object>> eventList = new ArrayList<>();

    private EventsAdapter myEventsAdapter;
    private List<Event> myEventsList;

    private RelativeLayout searchbarMap;
    private ClusterManager mClusterManager, mClusterManager2;
    private MyClusterManagerRenderer mClusterManagerRenderer, mClusterManagerRenderer2;
    private ArrayList<ClusterMarker> mClusterMarkers = new ArrayList<>();
    private ArrayList<ClusterMarker> mClusterMarkers2 = new ArrayList<>();

    private Button showdetailsButton;
    private ProgressBar loadingIndicator;
    private Spinner spinner_heatmap, spinner_events, spinner_users;

    private HeatMapSpinnerAdapter mAdapter;
    private HeatMapSpinnerEventAdapter mAdapter_Events;

    private HeatMapSpinnerUserAdapter mAdapter_Users;
    private MapViewModel mapViewModel;
    private List<DocumentSnapshot> spinnerOptions_Events = new ArrayList<>();
    private List<DocumentSnapshot> spinnerOptions_Users = new ArrayList<>();
    private boolean isAutomatiallySelected = true;
    private boolean onRestartFlag = false;
    private boolean atAllEvents, atMyFollowers, atMyLocation = false;

    //-------------------------------------------- Map Initialization -----------------------------------------------------

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        if (savedCameraPosition != null) {
            runOnUiThread(() -> mMap.moveCamera(CameraUpdateFactory.newCameraPosition(savedCameraPosition)));
        }
        if (mLocationPermissionGranted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            // Setting the default Location button to false
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            // If permission is granted, initialise the widgets such as CurrentLocation Icon
            init();
            getCurrentUserId();
            heatTileMethod(mIsRestore);
            isMapReady = true;
            loadingIndicator.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mIsRestore = savedInstanceState != null;
        atMyLocation = true;

        setContentView(R.layout.activity_heat_maps);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        loadingIndicator.setVisibility(View.VISIBLE);

        if (!mClusterMarkers.isEmpty()) {
            mClusterManager.clearItems();
            mClusterManager.cluster();
        }

        mapViewModel = new ViewModelProvider(this).get(MapViewModel.class);

        searchbarMap = findViewById(R.id.searchBarLayout3);
        mSearchText = findViewById(R.id.search_input3);
        mGps = findViewById(R.id.ic_gps_icon);
        mSearchmaps = findViewById(R.id.search_maps);

        showdetailsButton = findViewById(R.id.buttonShowDetails);


        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mAuth = FirebaseAuth.getInstance();

        mDb = FirebaseFirestore.getInstance();
        userLocationRef = mDb.collection("user_locations");

        showdetailsButton.setVisibility(View.VISIBLE);
        myEventsList = new ArrayList<>();
        myEventsAdapter = new EventsAdapter(myEventsList);


        showdetailsButton.setOnClickListener(v -> {
            if (!flag_isopen) {
                addMapMarkersEvents();
                flag_isopen = true;
            } else {
                if (!mClusterMarkers2.isEmpty()) {
                    mClusterManager2.clearItems();
                    mClusterManager2.cluster();
                    flag_isopen = false;
                }
            }

        });

        mSearchmaps.setOnClickListener(v -> {
            if (atMyLocation) {
                spinner_events.setVisibility(View.GONE);
                spinner_users.setVisibility(View.GONE);
                if (!flag_isopen2) {
                    searchbarMap.setVisibility(View.VISIBLE);
                    flag_isopen2 = true;
                } else {
                    searchbarMap.setVisibility(View.GONE);
                    flag_isopen2 = false;
                }

            } else if (atMyFollowers) {
                spinner_events.setVisibility(View.GONE);
                searchbarMap.setVisibility(View.GONE);
                if (!flag_isopen4) {
                    spinner_users.setVisibility(View.VISIBLE);
                    flag_isopen4 = true;
                } else {
                    spinner_users.setVisibility(View.GONE);
                    flag_isopen4 = false;
                }

            }
            // All EVENTS
            else if (atAllEvents) {
                spinner_users.setVisibility(View.GONE);
                searchbarMap.setVisibility(View.GONE);
                if (!flag_isopen3) {
                    spinner_events.setVisibility(View.VISIBLE);
                    flag_isopen3 = true;
                } else {
                    spinner_events.setVisibility(View.GONE);
                    flag_isopen3 = false;
                }
            }
        });

        getLocationPermission();

        if (savedInstanceState != null) {
            savedCameraPosition = savedInstanceState.getParcelable("camera_position");
        }

        runOnUiThread(() -> {
            initMap();
            init();
        });


        if (supportMapFragment == null) {
            supportMapFragment = SupportMapFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.heatmap3, supportMapFragment)
                    .commit();
        }
        runOnUiThread(() -> {
            getDeviceLocation();
            getFollowingUserList();
            getEventsWLocation();
        });


        spinner_events = findViewById(R.id.customspinner_events);
        mAdapter_Events = new HeatMapSpinnerEventAdapter(HeatMapsActivity.this, spinnerOptions_Events);
        spinner_events.setAdapter(mAdapter_Events);

        spinner_events.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                isSpinnerTouched_events = true;
                return false;
            }
        });

        spinner_users = findViewById(R.id.customspinner_users);
        mAdapter_Users = new HeatMapSpinnerUserAdapter(HeatMapsActivity.this, spinnerOptions_Users);
        spinner_users.setAdapter(mAdapter_Users);

        spinner_users.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                isSpinnerTouched_users = true;
                return false;
            }
        });


        spinner_users.setOnItemSelectedListener(this);
        spinner_events.setOnItemSelectedListener(this);
        spinner_heatmap.setOnItemSelectedListener(this);
    }

    /**
     * Method to initialize the custom widgets.
     */
    private void init() {
        getCurrentUserId();
        heatTileMethod(mIsRestore);
        isMapReady = true;
        Log.d(TAG, "init: Initializing Widgets");
        mSearchText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || event.getAction() == KeyEvent.ACTION_DOWN
                    || event.getAction() == KeyEvent.KEYCODE_ENTER
                    || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                Log.d(TAG, "onEditorAction: " + event.getAction() + event.getKeyCode());
                //Execute method for searching
                geoLocate();
                return true;

            }
            return false;
        });

        mGps.setOnClickListener(v -> {
            atAllEvents = false;
            atMyFollowers = false;
            atMyLocation = true;
            showdetailsButton.setVisibility(View.VISIBLE);
            spinner_events.setVisibility(View.GONE);
            spinner_users.setVisibility(View.GONE);
            spinner_heatmap.setSelection(0);
            getDeviceLocation();

        });
        hideSoftKeyboard();

    }

    /**
     * Method to implement GeoLocate
     * Searches the location using the input string provided in the search bar
     */
    private void geoLocate() {
        String searchString = mSearchText.getText().toString();
        Geocoder geocoder = new Geocoder(HeatMapsActivity.this);
        List<Address> list;
        try {
            list = geocoder.getFromLocationName(searchString, 1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (list.size() > 0) {
            Address address = list.get(0);
            runOnUiThread(() -> moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), 10, address.getAddressLine(0)));
        }
    }

    private GeoPoint geoLocator(String addressInput) {
        double event_lat = 0, event_lon = 0;
        String searchString = addressInput;
        Geocoder geocoder = new Geocoder(HeatMapsActivity.this);
        List<Address> list;
        try {
            list = geocoder.getFromLocationName(searchString, 1);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        if (list.size() > 0) {
            Address address = list.get(0);
            event_lat = address.getLatitude();
            event_lon = address.getLongitude();
        }
        return new GeoPoint(event_lat, event_lon);
    }

    /**
     * Method to initialize Map fragment
     */
    private void initMap() {
        Log.d(TAG, "initMap: Initializing Map");
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.heatmap3);
        supportMapFragment.getMapAsync(googleMap -> {
            loadingIndicator.setVisibility(View.VISIBLE);
            mMap = googleMap;
            runOnUiThread(() -> {
                CameraPosition cameraPosition = mapViewModel.getCameraPosition();
                if (cameraPosition != null) {
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            });

            if (mLocationPermissionGranted) {
                if (ActivityCompat.checkSelfPermission(HeatMapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(HeatMapsActivity.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mMap.setMyLocationEnabled(true);
                // Setting the default Location button to false
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                // If permission is granted, initialise the widgets such as CurrentLocation Icon
                init();


            }
            new Handler().postDelayed(() -> runOnUiThread(() -> {
                // Hide the progress bar
                loadingIndicator.setVisibility(View.GONE);
            }), 2000);

        });

    }

    /**
     * Custom method to moveCamera or zoom in to a desired LatLng
     *
     * @param latLng The location to zoom into
     * @param zoom   Zoom parameter
     * @param title  Title of the location zoomed into
     */
    private void moveCamera(LatLng latLng, float zoom, String title) {
        loadingIndicator.setVisibility(View.VISIBLE);
        runOnUiThread(() -> {
            if (mMap != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
                loadingIndicator.setVisibility(View.GONE);
            }
        });
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title(title);
        hideSoftKeyboard();
    }

    private void getDeviceLocation() {
        if (!mClusterMarkers2.isEmpty()) {
            mClusterManager2.clearItems();
            mClusterManager2.cluster();
        }
        if (!mClusterMarkers.isEmpty()) {
            mClusterManager.clearItems();
            mClusterManager.cluster();
        }
        ArrayList<LatLng> currentLatLon = new ArrayList<>();
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationTask = mFusedLocationProviderClient.getLastLocation();
                locationTask.addOnCompleteListener(this, task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Location currentLocation = task.getResult();
                        currentLatLon.add(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
                        runOnUiThread(() -> moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM, "My Location"));
                    } else {
                    }
                });
            }
        } catch (SecurityException e) {
        }
    }


    private void getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Location location = task.getResult();
                GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                mUserLocation.setGeoPoint(geoPoint);
                mUserLocation.setTimeStamp(null);
                saveUserLocation();

            }
        });
    }

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    //-------------------------------------------- Tile Implementation -----------------------------------------------------

    protected void heatTileMethod(boolean isRestore) {
        if (!isRestore) {
            getDeviceLocation();

        }
        spinner_heatmap = findViewById(R.id.customspinner);
        runOnUiThread(() -> {
            mAdapter = new HeatMapSpinnerAdapter(HeatMapsActivity.this, SpinnerData.getSpinnerOptions());
            spinner_heatmap.setAdapter(mAdapter);
        });


    }

    public void changeRadius(View view) {
        if (mDefaultRadius) {
            mProvider.setRadius(ALT_HEATMAP_RADIUS);
        } else {
            mProvider.setRadius(HeatmapTileProvider.DEFAULT_RADIUS);
        }
        mOverlay.clearTileCache();
        mDefaultRadius = !mDefaultRadius;
    }

    public void changeGradient(View view) {
        if (mDefaultGradient) {
            mProvider.setGradient(ALT_HEATMAP_GRADIENT);
        } else {
            mProvider.setGradient(HeatmapTileProvider.DEFAULT_GRADIENT);
        }
        mOverlay.clearTileCache();
        mDefaultGradient = !mDefaultGradient;
    }

    public void changeOpacity(View view) {
        if (mDefaultOpacity) {
            mProvider.setOpacity(ALT_HEATMAP_OPACITY);
        } else {
            mProvider.setOpacity(HeatmapTileProvider.DEFAULT_OPACITY);
        }
        mOverlay.clearTileCache();
        mDefaultOpacity = !mDefaultOpacity;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if (parent.getId() == findViewById(R.id.customspinner).getId()) {
            handleHeatmapSpinnerItemSelected(parent, position);
        } else if (parent.getId() == findViewById(R.id.customspinner_events).getId() && isSpinnerTouched_events) {
            handleEventsSpinnerItemSelected(parent, position);
        } else if (parent.getId() == findViewById(R.id.customspinner_users).getId() && isSpinnerTouched_users) {
            handleUsersSpinnerItemSelected(parent, position);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void handleHeatmapSpinnerItemSelected(AdapterView<?> parent, int position) {
        String text = parent.getItemAtPosition(position).toString();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.15);

        List<LatLng> dataPoints = new ArrayList<>();
        dataPoints.add(new LatLng(37.7749, -122.4194));


        for (GeoPoint geoPoint : geoPoints) {
            double latitude = geoPoint.getLatitude();
            double longitude = geoPoint.getLongitude();
            LatLng latLng = new LatLng(latitude, longitude);
            dataPoints.add(latLng);
        }

        event_dataPoints.add(new LatLng(37.7749, -122.4194));

        if (mMap != null) {
            if (mProvider == null) {
                mProvider = new HeatmapTileProvider.Builder().data(event_dataPoints).build();
                mOverlay = getMap().addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
            } else {
                if (mOverlay.isVisible()) {
                    mOverlay.clearTileCache();
                    mOverlay.setVisible(false);
                }

                // ------------------------------------ SHOW FRIENDS/FOLLOWERS -------------------------------------------------
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (parent.getItemAtPosition(position).toString().equals("2")) {
                            atAllEvents = false;
                            atMyFollowers = true;
                            atMyLocation = false;
                            showdetailsButton.setVisibility(View.GONE);
                            searchbarMap.setVisibility(View.GONE);
                            spinner_events.setVisibility(View.GONE);
                            if (!mClusterMarkers2.isEmpty()) {
                                mClusterManager2.clearItems();
                                mClusterManager2.cluster();
                            }


                            //Toast.makeText(parent.getContext(), text + "HI FRIENDS", Toast.LENGTH_SHORT).show();
                            for (int i = 0; i < dataPoints.size(); i++) {
                                builder.include(dataPoints.get(i));
                                LatLngBounds bounds = builder.build();
                                // to animate camera with some padding and bound -cover- all markers
                                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
                                mMap.animateCamera(cu);
                            }
                            getFollowingUserList();
                            addMapMarkers();
                        }

                        // ------------------------------------ SHOW ALL EVENTS ----------------------------------------------------------

                        else if (parent.getItemAtPosition(position).toString().equals("1")) {
                            atAllEvents = true;
                            atMyFollowers = false;
                            atMyLocation = false;
                            showdetailsButton.setVisibility(View.VISIBLE);
                            searchbarMap.setVisibility(View.GONE);
                            spinner_users.setVisibility(View.GONE);
                            int initialposition = spinner_events.getSelectedItemPosition();
                            spinner_events.setSelection(initialposition, false);

                            if (!mClusterMarkers.isEmpty()) {
                                mClusterManager.clearItems();
                                mClusterManager.cluster();
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    getEventsWLocation();
                                }
                            });

                            mProvider.setData(event_dataPoints);
                            for (int i = 0; i < event_dataPoints.size(); i++) {

                                builder.include(event_dataPoints.get(i));
                                LatLngBounds bounds = builder.build();
                                // to animate camera with some padding and bound -cover- all markers
                                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
                                mMap.animateCamera(cu);
                            }
                            mOverlay = getMap().addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
                            mOverlay.setVisible(true);
                            mOverlay.clearTileCache();
                        }

                        // ------------------------------------ SHOW MY LOCATION ----------------------------------------------------------

                        else {
                            atMyLocation = true;
                            atAllEvents = false;
                            atMyFollowers = false;
                            showdetailsButton.setVisibility(View.VISIBLE);
                            spinner_events.setVisibility(View.GONE);
                            spinner_users.setVisibility(View.GONE);

                            if (!flag_isopen2) {
                                searchbarMap.setVisibility(View.VISIBLE);
                                flag_isopen2 = true;
                            } else {
                                searchbarMap.setVisibility(View.GONE);
                                flag_isopen2 = false;
                            }

                            mOverlay = getMap().addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
                            mOverlay.setVisible(true);
                            mOverlay.clearTileCache();

                            if (!mClusterMarkers.isEmpty()) {
                                mClusterManager.clearItems();
                                mClusterManager.cluster();
                            }
                            //Toast.makeText(parent.getContext(), text + "MY LOCATION", Toast.LENGTH_SHORT).show();
                            getDeviceLocation();
                        }
                    }
                });

            }
        }
    }

    private void handleEventsSpinnerItemSelected(AdapterView<?> parent, int position) {

        runOnUiThread(() -> {
            if (!isAutomatiallySelected) {
                DocumentSnapshot selectedSnapshot = (DocumentSnapshot) parent.getItemAtPosition(position);

//            selectedPosition = position;

                // Extract data from the DocumentSnapshot
                String s_eventId = selectedSnapshot.getId();
                String s_eventName = selectedSnapshot.getString("eventName");
                String s_location = selectedSnapshot.getString("location");
                String s_venue = selectedSnapshot.getString("venue");
                String s_description = selectedSnapshot.getString("description");
                String s_outside_link = selectedSnapshot.getString("outside_link");
                String s_date = selectedSnapshot.getString("date");
                String s_time = selectedSnapshot.getString("time");
                String s_imageURL = selectedSnapshot.getString("imageURL");


                Intent intent = new Intent(HeatMapsActivity.this, EventDetailsActivity.class);
                intent.putExtra("eventId", s_eventId); // Pass eventId to the next activity if needed
                intent.putExtra("event_name", s_eventName);
                intent.putExtra("location", s_location);
                intent.putExtra("venue", s_venue);
                intent.putExtra("description", s_description);
                intent.putExtra("outside_link", s_outside_link);
                intent.putExtra("date", s_date);
                intent.putExtra("time", s_time);
                intent.putExtra("imageURL", s_imageURL);
                startActivity(intent);
            }
            isAutomatiallySelected = false;
        });

    }

    private void handleUsersSpinnerItemSelected(AdapterView<?> parent, int position) {

        runOnUiThread(() -> {
            DocumentSnapshot selectedSnapshot = (DocumentSnapshot) parent.getItemAtPosition(position);
            String user_id = selectedSnapshot.getId();
            Intent intent = new Intent(HeatMapsActivity.this, OtherUserPageActivity.class);
            intent.putExtra("USER_ID", user_id);
            startActivity(intent);
        });

    }


    private static class DataSet {
        private final ArrayList<LatLng> mDataset;

        public DataSet(ArrayList<LatLng> dataSet) {
            this.mDataset = dataSet;
        }

        public ArrayList<LatLng> getData() {
            return mDataset;
        }

    }

    protected GoogleMap getMap() {
        return mMap;
    }

    //-------------------------------------------- Google Services, Location and GPS Permissions -----------------------------------------------------

    private boolean checkMapServices() {
        if (isServicesOK()) {
            return isMapsEnabled();
        }
        return false;
    }

    public boolean isServicesOK() {
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(HeatMapsActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(HeatMapsActivity.this, available, ERROR_DIALOGUE_REQ);
            dialog.show();
        }
        return false;
    }

    public boolean isMapsEnabled() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> {
                    Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if (mLocationPermissionGranted) {
                    getDeviceLocation();
                    getUserDetails();
                } else {
                    getLocationPermission();
                }
            }
        }
    }

    /**
     * Method to get Location Permission from the user
     */
    private void getLocationPermission() {
        String[] permissions = {
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission
                (this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission
                    (this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                initMap();
                getUserDetails();
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQ_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQ_CODE);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 44) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //permissionGranted = true;
                if (ContextCompat.checkSelfPermission(HeatMapsActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    initMap();
                    mLocationPermissionGranted = true;
                }
            } else {
                finish();
            }
        }
    }


    //-------------------------------------------- User Details and FireStore -----------------------------------------------------

    private void getUserDetails() {
        if (mUserLocation == null) {
            mUserLocation = new UserLocation();
            String currentUserId = getIntent().getStringExtra("USER_ID");

            mDb.collection("users").document(currentUserId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            User user = task.getResult().toObject(User.class);
                            mUserLocation.setUser(user);
                            getLastKnownLocation();
                        }
                    });
        }
    }

    private String getCurrentUserId() {
        return Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
    }

    private void saveUserLocation() {
        String currentUserId = getCurrentUserId();
        if (mUserLocation != null) {
            userLocationRef.document(currentUserId).set(mUserLocation).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "saveUserLocation: Inserted user location to DB:  " +
                                    "\n latitude:" + mUserLocation.getGeoPoint().getLatitude() +
                                    "\n longitude:" + mUserLocation.getGeoPoint().getLongitude());
                        }
                    })
                    .addOnFailureListener(e -> Log.d(TAG, "saveUserLocation: FAILED" + e)
                    );
        }
    }

    private void getFollowingUserList() {
        spinnerOptions_Users.clear();

        String currentUserId = getIntent().getStringExtra("USER_ID");
        Task<DocumentSnapshot> usersRef = mDb
                .collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    List<String> followerIds = (List<String>) documentSnapshot.get("following");
                    for (String followerId : followerIds) {
                        getUserLocation(followerId);

                        mDb.collection("users").document(followerId).get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot documentSnapshot1 = task.getResult();
                                        if (documentSnapshot1.exists()) {
                                            spinnerOptions_Users.add(documentSnapshot1);
                                            mAdapter_Users.notifyDataSetChanged();
                                        }

                                    }

                                });

                    }
                })
                .addOnFailureListener(e -> {
                    Log.w("FollowersActivity", "There was a problem getting the following list", e);
                });
    }

    private void getEventsWLocation() {
        spinnerOptions_Events.clear();
        String currentUserId = getIntent().getStringExtra("USER_ID");
        loadingIndicator.setVisibility(View.VISIBLE);
        mDb.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        spinnerOptions_Events.add(document);
                        mAdapter_Events.notifyDataSetChanged();
                        Event event = document.toObject(Event.class);
                        String name = event.getEventName();
                        String location = event.getLocation();
                        String venue = event.getVenue();
                        String address = location + " " + venue;

                        GeoPoint geoPoint = geoLocator(address);
                        // Create a HashMap and add name and location
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("name", name);
                        hashMap.put("geopoint", geoPoint);

                        double latitude = geoPoint.getLatitude();
                        double longitude = geoPoint.getLongitude();
                        LatLng latLng = new LatLng(latitude, longitude);
                        event_dataPoints.add(latLng);
                        eventList.add(hashMap);


                    }
                })
                .addOnFailureListener(e -> {
                    Log.w("FollowersActivity", "There was a problem getting the following list", e);
                });

        loadingIndicator.setVisibility(View.GONE);
    }

    private void getUserLocation(String user) {
        DocumentReference locationRef = mDb.collection("user_locations")
                .document(user);

        locationRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    UserLocation userLocation = task.getResult().toObject(UserLocation.class);
                    // Process the user location or add it to your userArrayList
                    userArrayList.add(userLocation);
                    // You can access user's GeoPoint using userLocation.getGeoPoint()
                    GeoPoint geoPoint = userLocation.getGeoPoint();
                    geoPoints.add(geoPoint);
                }
            }
        });
    }


    //-------------------------------------------- Custom Map Marker -----------------------------------------------------

    private void addMapMarkers() {

        if (mMap != null) {
            if (mClusterManager == null) {
                mClusterManager = new ClusterManager<>(getApplicationContext(), mMap);
            }
            if (mClusterManagerRenderer == null) {
                mClusterManagerRenderer = new MyClusterManagerRenderer(
                        getApplicationContext(),
                        mMap,
                        mClusterManager
                );
                mClusterManager.setRenderer(mClusterManagerRenderer);
            }

            for (UserLocation userLocation : userArrayList) {
                try {
                    String snippet = "This is " + userLocation.getUser().getUsername();
                    String profilePictureUrl = userLocation.getUser().getProfilePictureUrl();

                    int avatar = R.drawable.avatar; // set the default avatar
                    try {
                        avatar = Integer.parseInt(userLocation.getUser().getProfilePictureUrl());
                    } catch (NumberFormatException ignored) {
                    }

                    ClusterMarker newClusterMarker = new ClusterMarker(
                            new LatLng(userLocation.getGeoPoint().getLatitude(), userLocation.getGeoPoint().getLongitude()),
                            userLocation.getUser().getUsername(),
                            snippet,
                            avatar,
                            userLocation.getUser()
                    );
                    mClusterManager.addItem(newClusterMarker);
                    mClusterMarkers.add(newClusterMarker);

                } catch (NullPointerException e) {
                    Log.e(TAG, "addMapMarkers: NullPointerException: " + e.getMessage());
                }

            }
            mClusterManager.cluster();
        }
    }


    private void addMapMarkersEvents() {

        if (mMap != null) {
            if (mClusterManager2 == null) {
                mClusterManager2 = new ClusterManager<>(getApplicationContext(), mMap);
            }
            if (mClusterManagerRenderer2 == null) {
                mClusterManagerRenderer2 = new MyClusterManagerRenderer(
                        getApplicationContext(),
                        mMap,
                        mClusterManager2
                );
                mClusterManager2.setRenderer(mClusterManagerRenderer2);
            }

            for (HashMap<String, Object> event : eventList) {
                String locationName = (String) event.get("name");
                GeoPoint geopoint = (GeoPoint) event.get("geopoint");
                try {
                    String snippet = "Event " + event.get("name");
                    int avatar = R.drawable.defaulteventpicture; // set the default avatar

                    ClusterMarker newClusterMarker = new ClusterMarker(
                            new LatLng(geopoint.getLatitude(), geopoint.getLongitude()),
                            locationName,
                            snippet,
                            avatar
                    );
                    mClusterManager2.addItem(newClusterMarker);
                    mClusterMarkers2.add(newClusterMarker);
                } catch (NullPointerException e) {
                    Log.e(TAG, "addMapMarkersEvent: NullPointerException: " + e.getMessage());
                    e.printStackTrace();
                }

            }
            mClusterManager2.cluster();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMap != null) {
            mapViewModel.setCameraPosition(mMap.getCameraPosition());
            outState.putParcelable("camera_position", mMap.getCameraPosition());

        }
    }

    protected void onPause() {
        Log.v(TAG, "onPause()");
        if (supportMapFragment != null) {
            supportMapFragment.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.v(TAG, "onDestroy()");

        super.onDestroy();
        if (mMap != null) {
            mMap.clear();
        }
    }

    @Override
    protected void onResume() {
        Log.v(TAG, "onResume()");
        loadingIndicator.setVisibility(View.VISIBLE);
        super.onResume();


        if (supportMapFragment != null) {
            supportMapFragment.onResume();
        }

        if (checkMapServices()) {
            if (mLocationPermissionGranted) {
                if (mMap == null) {
                    initMap();
                    getUserDetails();

                }
            }
        }
        if (onRestartFlag) {
            loadingIndicator.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onRestart() {
        Log.v(TAG, "onRestart()");
        super.onRestart();
        onRestartFlag = true;
    }

    @Override
    protected void onStart() {
        Log.v(TAG, "onStart()");
        super.onStart();
        loadingIndicator.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStop() {
        Log.v(TAG, "onStop()");
        super.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (supportMapFragment != null) {
            supportMapFragment.onLowMemory();
        }
    }
}