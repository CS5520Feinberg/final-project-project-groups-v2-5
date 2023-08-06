package edu.northeastern.rhythmlounge;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;


public class UserLocation {

    private GeoPoint geoPoint;
    private @ServerTimestamp Date timeStamp;
    private User user;

    public UserLocation() {
    }

    public UserLocation(GeoPoint geoPoint, Date timeStamp, User user) {
        this.geoPoint = geoPoint;
        this.timeStamp = timeStamp;
        this.user = user;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public User getUser() {
        return user;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "UserLocation{" +
                "geoPoint=" + geoPoint +
                ", timeStamp=" + timeStamp +
                ", user=" + user +
                '}';
    }
}

