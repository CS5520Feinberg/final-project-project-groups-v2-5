package edu.northeastern.rhythmlounge;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;


public class HeatMapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "Activity____HeatMaps";
    private static final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = android.Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int ERROR_DIALOGUE_REQ = 9001;
    private static final int LOCATION_PERMISSION_REQ_CODE = 1234;
    private Boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Toast.makeText(HeatMapsActivity.this,"Map is ready",Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: Map is ready");
        mMap = googleMap;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heat_maps);


        getLocationPermission();


    }

    private void initMap(){
        Log.d(TAG, "initMap: Initializing Map");
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.heatmap2);
        supportMapFragment.getMapAsync(HeatMapsActivity.this);

    }

    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: Get Location Permissions");
        String[] permissions = {
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission
                (this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if (ContextCompat.checkSelfPermission
                    (this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
            }
            else{
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQ_CODE);
            }
        } else{
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQ_CODE);
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        Log.d(TAG, "onRequestPermissionsResult: Called onRequestPermission");
//        mLocationPermissionGranted = false;
//
//        switch(requestCode){
//            case LOCATION_PERMISSION_REQ_CODE:{
//                if(grantResults.length > 0) {
//                    for(int i =0; i<grantResults.length;i++)
//                    {
//                        if(grantResults[i] == PackageManager.PERMISSION_GRANTED) {
//                            mLocationPermissionGranted = false;
//                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
//                            return;
//                    }
//                }
//                    mLocationPermissionGranted = true;
//                    Log.d(TAG, "onRequestPermissionsResult: permissions granted");
//                    //Initialise our map if permissions are granted
//                    initMap();
//                }
//            }
//        }
//    }

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
            }
            else {
                //permissionGranted = false;
                Toast.makeText(this, "Location permission not granted. Please try again.", Toast.LENGTH_SHORT).show();
                finish();
            }
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