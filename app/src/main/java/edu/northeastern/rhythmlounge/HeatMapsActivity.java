package edu.northeastern.rhythmlounge;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
public class HeatMapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "Activity____HeatMaps";
    private static final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = android.Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int ERROR_DIALOGUE_REQ = 9001;
    private static final int LOCATION_PERMISSION_REQ_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15;
    private Boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    //Widgets
    private EditText mSearchText;
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

        mSearchText = findViewById(R.id.search_input3);
        mGps = findViewById(R.id.ic_gps_icon);

        getLocationPermission();
        initMap();
        init();
        getDeviceLocation();


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
                hideSoftKeyboard();
            }
        });

    }

    /**
     * Method to implement GeoLocate
     * Searches the location using the input string provided in the search bar
     */
    private void geoLocate() {
        Log.d(TAG, "geoLocate: geoLocating");
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
            //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();
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
        mMap.addMarker(options);
        hideSoftKeyboard();
    }

    /**
     * Method to get current Device Location
     */
    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: Getting the Device's Current Location");
        ArrayList<LatLng> currentLatLon = new ArrayList<>();
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLocationPermissionGranted) {
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: Found Location");
                        Location currentLocation = (Location) task.getResult();
                        currentLatLon.add(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
                        moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM, "My Location");
                        mLists.put(getString(R.string.mycurrentlocation), new DataSet(currentLatLon));


                        //new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()) );
                    } else {
                        Log.d(TAG, "onComplete: Unable to get Current Location");
                    }
                });
            }

        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException" + e.getMessage());
        }
    }

    /**
     * Method to get Location Permission from the user
     */
    private void getLocationPermission() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.heatmap3);
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
                Log.d("IsRefresh", "Yes");
                if (fragment != null) {
                    // Reload Fragment
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.detach(fragment);
                    fragmentTransaction.attach(fragment);
                    fragmentTransaction.commit();
                }
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
                }
            } else {
                //permissionGranted = false;
                Toast.makeText(this, "Location permission not granted. Please try again.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
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

        // Set up the spinner/dropdown list
        Spinner spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.heatmaps_datasets_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new HeatMapsActivity.SpinnerActivity());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            mLists.put(getString(R.string.events), new HeatMapsActivity.DataSet(readItems(R.raw.events)));
            mLists.put(getString(R.string.friends), new HeatMapsActivity.DataSet(readItems(R.raw.friends)));
        } catch (JSONException e) {
            Toast.makeText(this, "Problem reading list of markers.", Toast.LENGTH_LONG).show();
        }
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

    /**
     * Class Spinner Activity that allows users to select HeapMaps or Friend's locations
     */
    public class SpinnerActivity implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            String dataset = parent.getItemAtPosition(pos).toString();
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            int padding = (int) (width * 0.15); // offset from edges of the map 15% of screen
            // Check if need to instantiate (avoid setData etc twice)
            if (mProvider == null) {
                mProvider = new HeatmapTileProvider.Builder().data(mLists.get(getString(R.string.events)).getData()).build();
                mOverlay = getMap().addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
                // Render links
                //attribution.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                // HeatMap of events
                if (parent.getSelectedItemPosition() == 1) {
                    mMap.clear();
                    mProvider.setData(mLists.get(dataset).getData());
                    for (int i = 0; i < mLists.get(dataset).getData().size(); i++) {
                        builder.include(mLists.get(dataset).getData().get(i));
                        LatLngBounds bounds = builder.build();
                        // to animate camera with some padding and bound -cover- all markers
                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
                        mMap.animateCamera(cu);
                    }
                    mOverlay = getMap().addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
                    mOverlay.setVisible(true);
                    mOverlay.clearTileCache();
                }
                // Current Location
                else if (parent.getSelectedItemPosition() == 0) {
                    getDeviceLocation();
                }
                // Friend's Location
                else {
                    mOverlay.clearTileCache();
                    mOverlay.setVisible(false);
                    // Method to mark multiple points on screen
                    for (int i = 0; i < dataset.length(); i++) {
                        mMap.addMarker(new MarkerOptions().position(Objects.requireNonNull(mLists.get(dataset)).getData().get(i)).title("Friends"));
                        builder.include(mLists.get(dataset).getData().get(i));
                        LatLngBounds bounds = builder.build();
                        // to animate camera with some padding and bound -cover- all markers
                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
                        mMap.animateCamera(cu);
                    }
                }
            }
        }

        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    private ArrayList<LatLng> readItems(int resource) throws JSONException {
        ArrayList<LatLng> list = new ArrayList<>();
        InputStream inputStream = getResources().openRawResource(resource);
        String json = new Scanner(inputStream).useDelimiter("\\A").next();
        JSONArray array = new JSONArray(json);
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            double lat = object.getDouble("lat");
            double lng = object.getDouble("lng");
            list.add(new LatLng(lat, lng));
        }
        return list;
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