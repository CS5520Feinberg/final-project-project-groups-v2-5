package edu.northeastern.rhythmlounge;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

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
    private ImageView mGps;

    private static final int ALT_HEATMAP_RADIUS = 10;

    /**
     * Alternative opacity of heatmap overlay
     */
    private static final double ALT_HEATMAP_OPACITY = 0.4;

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
    private TileOverlay mOverlay;

    private boolean mDefaultGradient = true;
    private boolean mDefaultRadius = true;
    private boolean mDefaultOpacity = true;

    private boolean mIsRestore;
    /**
     * Maps name of data set to data (list of LatLngs)
     * Also maps to the URL of the data set for attribution
     */
    private final HashMap<String, HeatMapsActivity.DataSet> mLists = new HashMap<>();

    private FirebaseFirestore mDb;
    private UserLocation mUserLocation;

    private CollectionReference userLocationRef;

    List<User> followers = new ArrayList<>();
    private ArrayList<UserLocation> userArrayList = new ArrayList<>();
    private List<GeoPoint> geoPoints = new ArrayList<GeoPoint>();

    private LatLngBounds mMapBoundary;
    // Position of the authenticated user
    private UserLocation muserPosition;
    private ClusterManager mClusterManager;
    private MyClusterManagerRenderer mClusterManagerRenderer;
    private ArrayList<ClusterMarker> mClusterMarkers = new ArrayList<>();



    //-------------------------------------------- Map Initialization -----------------------------------------------------

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Toast.makeText(HeatMapsActivity.this, "Map is ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: Map is ready");
        mMap = googleMap;
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

            heatTileMethod(mIsRestore);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsRestore = savedInstanceState != null;
        setContentView(R.layout.activity_heat_maps);


        if (savedInstanceState != null) {
            CameraPosition cameraPosition = savedInstanceState.getParcelable("cameraPosition");
            if (cameraPosition != null) {
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        }

        mSearchText = findViewById(R.id.search_input3);
        mGps = findViewById(R.id.ic_gps_icon);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mAuth = FirebaseAuth.getInstance();

        mDb = FirebaseFirestore.getInstance();
        userLocationRef = mDb.collection("user_locations");

        getLocationPermission();
        initMap();
        init();
        getDeviceLocation();
        //setUserPosition();
        addMapMarkers();


    }

    /**
     * Method to initialize the custom widgets.
     */
    private void init() {
        Log.d(TAG, "init: Initializing Widgets");
        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        || event.getAction() == KeyEvent.KEYCODE_ENTER) {
                    //Execute method for searching
                    geoLocate();
                }
                return false;
            }
        });

        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Clicked GPS ICON");
                getDeviceLocation();

            }
        });
        hideSoftKeyboard();

    }

    /**
     * Method to implement GeoLocate
     * Searches the location using the input string provided in the search bar
     */
    private void geoLocate() {
        Log.d(TAG, "geoLocate: geoLocating");
        String searchString = mSearchText.getText().toString();
        Geocoder geocoder = new Geocoder(HeatMapsActivity.this);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString, 1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (list.size() > 0) {
            Address address = list.get(0);
            Log.d(TAG, "geoLocate: Found a location:" + address.toString());

            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM, address.getAddressLine(0));
        }
    }

    /**
     * Method to initialize Map fragment
     */
    private void initMap() {
        Log.d(TAG, "initMap: Initializing Map");
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.heatmap3);
        supportMapFragment.getMapAsync(HeatMapsActivity.this);

    }

    /**
     * Custom method to moveCamera or zoom in to a desired LatLng
     *
     * @param latLng The location to zoom into
     * @param zoom   Zoom parameter
     * @param title  Title of the location zoomed into
     */
    private void moveCamera(LatLng latLng, float zoom, String title) {
        Log.d(TAG, "moveCamera: Moving the camera to: Lat:" + latLng.latitude + "Lon: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title(title);
        hideSoftKeyboard();
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: Getting the Device's Current Location");
        ArrayList<LatLng> currentLatLon = new ArrayList<>();
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationTask = mFusedLocationProviderClient.getLastLocation();
                locationTask.addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Log.d(TAG, "onComplete: Found Location");
                        Location currentLocation = task.getResult();
                        currentLatLon.add(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
                        moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM, "My Location");
                    } else {
                        Log.d(TAG, "onComplete: Unable to get Current Location");
                    }
                    Log.d(TAG, "getDeviceLocation: Am i coming here 10 AUG?");
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException" + e.getMessage());
        }
    }


    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation: called ");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if(task.isSuccessful()){
                    Location location = task.getResult();
                    GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                    Log.d(TAG, "onComplete: Latitude: " +geoPoint.getLatitude());
                    Log.d(TAG, "onComplete: Longitude: " +geoPoint.getLongitude());

                    mUserLocation.setGeoPoint(geoPoint);
                    mUserLocation.setTimeStamp(null);
                    saveUserLocation();

                }
            }
        });
    }

    private void hideSoftKeyboard() {
        Log.d(TAG, "hideSoftKeyboard: Closing Keyboard Window");
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    //-------------------------------------------- Tile Implementation -----------------------------------------------------

    protected void heatTileMethod(boolean isRestore) {
        if (!isRestore) {
            getDeviceLocation();

        }

        Spinner spinner = findViewById(R.id.spinner2);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.heatmaps_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

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

        if (mProvider == null) {
            mProvider = new HeatmapTileProvider.Builder().data(dataPoints).build();
            mOverlay = getMap().addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
        } else {
            if(mOverlay.isVisible()){
                mOverlay.clearTileCache();
                mOverlay.setVisible(false);
            }
            if (parent.getItemAtPosition(position).toString().equals("myfriends")) {
                Toast.makeText(parent.getContext(), text + "HI FRIENDS", Toast.LENGTH_SHORT).show();
                getFollowingUserList();
                addMapMarkers();
            }


            else if (parent.getItemAtPosition(position).toString().equals("myevents")) {
                if (!mClusterMarkers.isEmpty()) {
                    mClusterManager.clearItems();
                    mClusterManager.cluster();
                }
                mProvider.setData(dataPoints);
                for (int i = 0; i < dataPoints.size(); i++) {
                    builder.include(dataPoints.get(i));
                    LatLngBounds bounds = builder.build();
                    // to animate camera with some padding and bound -cover- all markers
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
                    mMap.animateCamera(cu);
                }
                mOverlay = getMap().addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
                mOverlay.setVisible(true);
                mOverlay.clearTileCache();



                Toast.makeText(parent.getContext(), text + "HI EVENTS", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onItemSelected:USERARRAYLOIST " + userArrayList);
                Log.d(TAG, "onItemSelected:GEOPOINTS " + geoPoints);
            }
            else {
                Toast.makeText(parent.getContext(), text + "MY LOCATION", Toast.LENGTH_SHORT).show();
                getDeviceLocation();
            }

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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

    private boolean checkMapServices(){
        if(isServicesOK()){
            return isMapsEnabled();
        }
        return false;
    }
    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(HeatMapsActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occurred but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(HeatMapsActivity.this, available, ERROR_DIALOGUE_REQ);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public boolean isMapsEnabled(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if(mLocationPermissionGranted){
                    getDeviceLocation();
                    //getLastKnownLocation();
                    getUserDetails();
                }
                else{
                    getLocationPermission();
                }
            }
        }
    }

    /**
     * Method to get Location Permission from the user
     */
    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: Get Location Permissions");
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
                Log.d("IsRefresh", "Yes");
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
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                    initMap();
                    mLocationPermissionGranted = true;
                }
            } else {
                //permissionGranted = false;
                Toast.makeText(this, "Location permission not granted. Please try again.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }



    //-------------------------------------------- User Details and FireStore -----------------------------------------------------

    private void getUserDetails(){
        if(mUserLocation==null){
            mUserLocation = new UserLocation();
            String currentUserId = getIntent().getStringExtra("USER_ID");

            mDb.collection("users").document(currentUserId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                Log.d(TAG, "onComplete: Got the user details successfully." + currentUserId);
                                User user = task.getResult().toObject(User.class);
                                mUserLocation.setUser(user);
                                getLastKnownLocation();
                            }
                        }
                    });
        }
    }
    private String getCurrentUserId() {
        return Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
    }
    private void saveUserLocation(){
        String currentUserId = getCurrentUserId();
        if(mUserLocation != null){
            userLocationRef.document(currentUserId).set(mUserLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Log.d(TAG, "saveUserLocation: Inserted user location to DB:  "+
                                "\n latitude:" + mUserLocation.getGeoPoint().getLatitude() +
                                "\n longitude:" + mUserLocation.getGeoPoint().getLongitude());
                    }
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                                Log.d(TAG, "saveUserLocation: FAILED"+e);
                            }
                        }
                    );
        }
    }

    private void getFollowingUserList() {

        String currentUserId = getIntent().getStringExtra("USER_ID");
        Task<DocumentSnapshot> usersRef = mDb
                .collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    Log.d(TAG, "getFollowingUserList: Current User ID:"+currentUserId+getCurrentUserId());
                    // Get the list of user IDs that are following the current user
                    List<String> followerIds = (List<String>) documentSnapshot.get("following");
                    Log.d(TAG, "getFollowingUserList: followerIDS"+followerIds);
                    // For each follower, fetch their user location
                    for (String followerId : followerIds) {
                        getUserLocation(followerId);
                    }

                })
                .addOnFailureListener(e -> {
                    Log.w("FollowersActivity", "There was a problem getting the following list", e);
                });
    }

    private void getUserLocation(String user){
        DocumentReference locationRef = mDb.collection("user_locations")
                .document(user);

        locationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().exists()){
                        UserLocation userLocation = task.getResult().toObject(UserLocation.class);
                        // Process the user location or add it to your userArrayList
                        userArrayList.add(userLocation);

                        // You can access user's GeoPoint using userLocation.getGeoPoint()
                        GeoPoint geoPoint = userLocation.getGeoPoint();

                        geoPoints.add(geoPoint);
                        double latitude = geoPoint.getLatitude();
                        double longitude = geoPoint.getLongitude();

                        Log.d(TAG, "User Location: Lat: " + latitude + ", Lon: " + longitude);
                    }
                }
            }
        });
    }


    //-------------------------------------------- Custom Map Marker -----------------------------------------------------

    private void addMapMarkers(){

        if(mMap != null){
            Log.d(TAG, "addMapMarkers: I'm here");
            if(mClusterManager == null){
                Log.d(TAG, "addMapMarkers: I'm here2");
                mClusterManager = new ClusterManager<>(getApplicationContext(),mMap);
            }
            if(mClusterManagerRenderer == null){
                Log.d(TAG, "addMapMarkers: I'm here3");
                mClusterManagerRenderer = new MyClusterManagerRenderer(
                        getApplicationContext(),
                        mMap,
                        mClusterManager
                );
                mClusterManager.setRenderer(mClusterManagerRenderer);
            }

            for(UserLocation userLocation: userArrayList){
                Log.d(TAG, "addMapMarkers: I'm here4");
                Log.d(TAG, "addMapMarkers: location: " + userLocation.getGeoPoint().toString());
                try{
                    String snippet = "This is " + userLocation.getUser().getUsername();
                    String profilePictureUrl = userLocation.getUser().getProfilePictureUrl();


                    int avatar = R.drawable.avatar; // set the default avatar
                    try{
                        avatar = Integer.parseInt(userLocation.getUser().getProfilePictureUrl());
                    }catch (NumberFormatException e){
                        Log.d(TAG, "addMapMarkers: no avatar for " + userLocation.getUser().getUsername() + ", setting default.");
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

                }catch (NullPointerException e){
                    Log.e(TAG, "addMapMarkers: NullPointerException: " + e.getMessage() );
                }

            }
            mClusterManager.cluster();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMap != null) {
            CameraPosition cameraPosition = mMap.getCameraPosition();
            outState.putParcelable("cameraPosition", cameraPosition);
        }
    }

    protected void onPause() {
        Log.v(TAG, "onPause()");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.v(TAG, "onDestroy()");
        //stopLocationUpdates();
        //Log.v(TAG, "Stop FusedLocationClient Updates");
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        Log.v(TAG, "onResume()");
        super.onResume();
        if(checkMapServices()){
            if(mLocationPermissionGranted){
                Log.v(TAG, "onResume()"+mMap);
                if (mMap == null) {
                    initMap();
                }
                getUserDetails();
            }
            else {
                getLocationPermission();
            }
        }
    }
        @Override
    protected void onRestart() {
        Log.v(TAG, "onRestart()");
        super.onRestart();
    }

    @Override
    protected void onStart() {
        Log.v(TAG, "onStart()");
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.v(TAG, "onStop()");
        //stopLocationUpdates();
        super.onStop();
    }

}