package edu.northeastern.rhythmlounge;

import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.CameraPosition;

public class MapViewModel extends ViewModel {
    private CameraPosition cameraPosition;

    public void setCameraPosition(CameraPosition position) {
        cameraPosition = position;
    }

    public CameraPosition getCameraPosition() {
        return cameraPosition;
    }
}
