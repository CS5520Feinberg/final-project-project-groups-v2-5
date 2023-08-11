package edu.northeastern.rhythmlounge.Events;

import android.content.Intent;
import android.media.Image;
import android.media.metrics.Event;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.ktx.Firebase;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RecursiveAction;

import edu.northeastern.rhythmlounge.R;
import edu.northeastern.rhythmlounge.User;
import edu.northeastern.rhythmlounge.UserListItemAdapter;

/**
 * EventDetailsActivity provides a detailed view of a specific event.
 * It allows users to view the event details, check their RSVP status, and see other user's who've RSVPed.
 */
public class EventDetailsActivity extends AppCompatActivity {

    private TextView textViewEventName, textViewLocation, textViewVenue,
                     textViewDescription, textViewOutsideLink, textViewDate,
                     textViewTime;
    private ImageView imageViewEvent;
    private CheckBox checkBoxRSVP;

    private Button editEventButton, deleteEventButton;
    private RecyclerView rsvpRecyclerView;
    private UserListItemAdapter userListItemAdapter;
    private String currentUserId, eventId;

    private boolean isCurrentUserHost = false;

    private ListenerRegistration rsvpUsersListenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        // Get the current user's ID
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
        }

        initializeUIComponents();
        bindDataFromIntent();
        setupEventListeners();

        deleteEventButton.setOnClickListener(view -> confirmDeleteEvent());
    }

    /**
     * Initializes all the UI components.
     */
    private void initializeUIComponents() {
        textViewEventName = findViewById(R.id.textViewEventName);
        textViewLocation = findViewById(R.id.textViewLocation);
        textViewVenue = findViewById(R.id.textViewVenue);
        textViewDescription = findViewById(R.id.textViewDescription);
        textViewOutsideLink = findViewById(R.id.textViewOutsideLink);
        textViewDate = findViewById(R.id.textViewDate);
        textViewTime = findViewById(R.id.textViewTime);
        imageViewEvent = findViewById(R.id.imageViewEvent);
        checkBoxRSVP = findViewById(R.id.checkBoxRSVP);
        rsvpRecyclerView = findViewById(R.id.rsvpRecyclerView);

        // Buttons for editing and deleting the event.
        editEventButton = findViewById(R.id.editEventButton);
        deleteEventButton = findViewById(R.id.deleteEventButton);



    }

    private void updateHostButtonsVisibility() {
        if (isCurrentUserHost) {
            editEventButton.setVisibility(View.VISIBLE);
            deleteEventButton.setVisibility(View.VISIBLE);
        } else {
            editEventButton.setVisibility(View.GONE);
            deleteEventButton.setVisibility(View.GONE);
        }
    }

    /**
     * Checks if the current user viewing the event details activity is the host of the event.
     * Additionally, it will update the host buttons visibility (delete buttons and edit buttons).
     */
    private void checkIfCurrentUserIsHost() {
        Log.d("EventDetailsActivity", "Checking if current user is host.");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference eventRef = db.collection("events").document(eventId);

        eventRef.get().addOnSuccessListener(documentSnapshot -> {
           if (documentSnapshot.exists()) {
               String hostId = documentSnapshot.getString("eventCreator");
               isCurrentUserHost = currentUserId.equals(hostId);
               updateHostButtonsVisibility();
               if (isCurrentUserHost) {
                   Log.d("EventDetailsActivity", "Current user is the host");
               }
           }
        }).addOnFailureListener(e -> {
           Log.d("EventDetailsActivity", "Something went wrong.");
        });
    }

    /**
     * Binds the data passed through the Intent to the UI components.
     */
    private void bindDataFromIntent() {
        Intent intent = getIntent();
        if (intent == null) return;

        String eventName = intent.getStringExtra("event_name");
        String location = intent.getStringExtra("location");
        String venue = intent.getStringExtra("venue");
        String description = intent.getStringExtra("description");
        String outsideLink = intent.getStringExtra("outside_link");
        String date = intent.getStringExtra("date");
        String time = intent.getStringExtra("time");
        String imageURL = intent.getStringExtra("imageURL");
        eventId = getIntent().getStringExtra("eventId");
        Log.d("EventDetailsActivity", "Received eventId: " + eventId);

        updateUI(eventName, location, venue, description,
                outsideLink, date, time, imageURL);

        checkRSVPStatus();
        fetchRSVPUsers();
        checkIfCurrentUserIsHost();

    }

    /**
     * Updates all the UI components with the event data.
     * @param eventName   Name of the event.
     * @param location    Location of the event.
     * @param venue       Venue of the event.
     * @param description Description about the event.
     * @param outsideLink External link related to the event.
     * @param date        Date of the event.
     * @param time        Time of the event.
     * @param imageURL    URL for the event image.
     */
    private void updateUI(String eventName, String location, String venue, String description,
                          String outsideLink, String date, String time, String imageURL) {

        // Set all the text data
        textViewEventName.setText(eventName);
        textViewLocation.setText(location);
        textViewVenue.setText(venue);
        textViewDescription.setText(description);
        textViewOutsideLink.setText(outsideLink);
        textViewDate.setText(date);
        textViewTime.setText(time);

        // Use Glide to load the event image
        if (imageURL != null && !imageURL.isEmpty()) {
            Glide.with(this)
                    .load(imageURL)
                    .placeholder(R.drawable.concert)
                    .into(imageViewEvent);
        } else {
            // If no associated event image, use the default.
            imageViewEvent.setImageResource(R.drawable.concert);
        }
    }

    // Sets up event listeners for UI components (Checkbox).
    private void setupEventListeners() {

        // Listens for changes to the rsvp checkbox.
        checkBoxRSVP.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            if (isChecked) {
                addRSVP(currentUserId, eventId);
            } else {
                removeRSVP(currentUserId, eventId);
            }
        }));
    }

    private void confirmDeleteEvent() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Delete Event");
        builder.setMessage("Are you sure you want to delete this event? This action cannot be reversed.");

        builder.setPositiveButton("Confirm", ((dialog, which) -> deleteEvent()));
        builder.setNegativeButton("Cancel", ((dialog, which) -> dialog.dismiss()));

        AlertDialog dialog = builder.create();
        dialog.show();

    }
    private void deleteEvent() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference eventRef = db.collection("events").document(eventId);
        
        eventRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Map<String, Object> eventData = documentSnapshot.getData();

                db.collection("deleted_events").add(eventData).addOnSuccessListener(documentReference -> {

                    handleEventRemoval(eventData);

                    eventRef.delete().addOnSuccessListener(void1 -> {
                        Toast.makeText(EventDetailsActivity.this, "Event deleted successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }).addOnFailureListener(e -> Toast.makeText(EventDetailsActivity.this, "Error deleting event: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }).addOnFailureListener(e -> Toast.makeText(EventDetailsActivity.this, "Error moving event to deleted_events: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).addOnFailureListener(e -> Toast.makeText(EventDetailsActivity.this, "Error fetching event data: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void handleEventRemoval(Map<String, Object> eventData) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String hostId = (String) eventData.get("eventCreator");
        List<String> rsvps = (List<String>) eventData.get("rsvps");


        if (hostId != null && !hostId.isEmpty()) {
            DocumentReference hostRef = db.collection("users").document(hostId);
            hostRef.update("hosting", FieldValue.arrayRemove(eventId));
        }

        if (rsvps != null && !rsvps.isEmpty()) {
            for (String userId : rsvps) {
                DocumentReference userRef = db.collection("users").document(userId);
                userRef.update("rsvpd", FieldValue.arrayRemove(eventId));
            }
        }
    }

    /**
     * Adds the user's RSVP to the specified event.
     * @param userDocumentId  The user's document ID.
     * @param eventDocumentId The event's document ID.
     */
    private void addRSVP(String userDocumentId, String eventDocumentId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Adds the user to events rsvps
        DocumentReference eventRef = db.collection("events").document(eventDocumentId);
        eventRef.update("rsvps", FieldValue.arrayUnion(userDocumentId));
        // Adds the event to the users rsvpd
        DocumentReference userRef = db.collection("users").document(userDocumentId);
        userRef.update("rsvpd", FieldValue.arrayUnion(eventDocumentId));
    }

    /**
     * Removes the user's RSVP from the specified event.
     * @param userDocumentId  The user's document ID.
     * @param eventDocumentId The event's document ID.
     */
    private void removeRSVP(String userDocumentId, String eventDocumentId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Removes the user from events rsvps
        DocumentReference eventRef = db.collection("events").document(eventDocumentId);
        eventRef.update("rsvps", FieldValue.arrayRemove(userDocumentId));
        // Removes the event from users rspvd
        DocumentReference userRef = db.collection("users").document(userDocumentId);
        userRef.update("rsvpd", FieldValue.arrayRemove(eventDocumentId));
    }

    /**
     * Checks the user's RSVP status for the current event and updates the UI accordingly.
     */
    private void checkRSVPStatus() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference eventRef = db.collection("events").document();

        rsvpUsersListenerRegistration = eventRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                return;
            }
            if (documentSnapshot != null && documentSnapshot.exists()) {
                List<String> rsvps = (List<String>) documentSnapshot.get("rsvps");
                checkBoxRSVP.setChecked(rsvps != null && rsvps.contains(currentUserId));
            }
        });
    }

    /**
     * Fetches a list of users who've RSVPed for the event.
     */
    private void fetchRSVPUsers() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference eventRef = db.collection("events").document(eventId);

        rsvpUsersListenerRegistration = eventRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                return;
            }
            if (snapshot != null && snapshot.exists()) {
                List<String> rsvps = (List<String>) snapshot.get("rsvps");
                if (rsvps != null) {
                    fetchUsersDetails(rsvps);
                }
            }
        });
    }

    /**
     * Fetches a list of users who've RSVPed for the event.
     * @param userIds List of user IDs to fetch details for.
     */
    private void fetchUsersDetails(List<String> userIds) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("users");

        // List to hold asynchronous tasks for fetching each user's details
        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (String userId : userIds) {
            // add each user's fetch task to the list.
            tasks.add(usersRef.document(userId).get());
        }

        // Execute all fetch tasks in parallel and wait for all of them to succeed
        Tasks.whenAllSuccess(tasks).addOnSuccessListener(objects -> {

            List<User> rsvpUsers = new ArrayList<>(); // List to hold the fetched user's details
            for (Object object : objects) {
                DocumentSnapshot documentSnapshot = (DocumentSnapshot) object; // Convert each returned object to a documentsnapshot
                User user = documentSnapshot.toObject(User.class); // Convert the documentsnapshot to a user object
                rsvpUsers.add(user); // Add the user object to the list.
            }
            setupRSVPRecyclerView(rsvpUsers, userIds);
        });
    }

    /**
     * Sets up the RecyclerView to display a list of users who've RSVPed.
     * @param users   List of user details.
     * @param userIds List of user IDs.
     */
    private void setupRSVPRecyclerView(List<User> users, List<String> userIds) {
        userListItemAdapter = new UserListItemAdapter(this, users, userIds);
        rsvpRecyclerView.setAdapter(userListItemAdapter);
        rsvpRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}



