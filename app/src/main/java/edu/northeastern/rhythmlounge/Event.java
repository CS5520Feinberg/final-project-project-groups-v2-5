package edu.northeastern.rhythmlounge;

import android.location.Address;
import android.location.Location;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents an event with a eventID, eventName, eventDate,
 * description, eventPictureUrl, host, location, and a list of attendees.
 */
public class Event {

    private String eventId;
    private String eventName;

    private Date eventDate;

    private String description;

    private String eventPictureUrl;

    private User host;

    private Address location;

    private List<User> attendees;

    /**
     * Resource ID for the default event picture. This is built into the app and used until a custom is uploaded.
     */
    private static final int DEFAULT_PICTURE_RES_ID = R.drawable.defaulteventpicture;

    public Event() {
    }

    /**
     * Constructor to initialize an event with the specified details.
     * @param eventId     Unique identifier for the event
     * @param eventName   Name of the event
     * @param eventDate   Date of the event
     * @param description Description of the event
     * @param host        Host of the event
     * @param location    Location of the event.
     */
    public Event(String eventId, String eventName, Date eventDate, String description, User host, Address location) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.description = description;
        this.host = host;
        this.location = location;
        this.eventPictureUrl = null;
        this.attendees = new ArrayList<>();
    }

    /**
     * Creates a new event in the Firestore database.
     * @param event The event object to be created.
     */
    public void createEvent(Event event) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events")
                .add(event)
                .addOnSuccessListener(documentReference -> event.setEventId(documentReference.getId()));
    }

    /**
     * Adds an attendee to the event.
     * @param userId the user ID of the attendee to add.
     */
    public void addAttendee(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(eventId).update("attendees", FieldValue.arrayUnion(userId));
    }

    /**
     * Removes an attendee from the event.
     * @param userId the user ID of the attendee to remove.
     */
    public void removeAttendee(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(eventId).update("attendees", FieldValue.arrayRemove(userId));
    }


    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDefaultPictureResId() {
        return DEFAULT_PICTURE_RES_ID;
    }

    public String getEventPictureUrl() {
        return eventPictureUrl;
    }

    public void setEventPictureUrl(String eventPictureUrl) {
        this.eventPictureUrl = eventPictureUrl;
    }

    public User getHost() {
        return host;
    }

    public void setHost(User host) {
        this.host = host;
    }

    public Address getLocation() {
        return location;
    }

    public void setLocation() {
        this.location = location;
    }

    public List<User> getAttendees() {
        return attendees;
    }

    public void setAttendees(List<User> attendees) {
        this.attendees = attendees;
    }



}
