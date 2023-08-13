package edu.northeastern.rhythmlounge.Events;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
        editEventButton.setOnClickListener(view -> eventEditor());
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

    /**
     * Updates button edit button and delete button visibility
     */
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
        }).addOnFailureListener(e -> Log.d("EventDetailsActivity", "Something went wrong."));
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
        textViewOutsideLink.setText("Click here for more info");
        textViewOutsideLink.setPaintFlags(textViewOutsideLink.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
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

        // Handles the link view
        if (outsideLink != null && !outsideLink.trim().isEmpty()) {
            textViewOutsideLink.setText("Click here for more information");
            textViewOutsideLink.setTextColor(Color.BLUE);
            textViewOutsideLink.setPaintFlags(textViewOutsideLink.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            textViewOutsideLink.setOnClickListener(v -> openWebPage(outsideLink));
        } else {
            textViewOutsideLink.setText("");
        }
    }

    /**
     * Opens a web page in the default browser.
     * @param url The URL of the web page to open. If the URL does not start with "http or https, http is prepended.
     */
    private void openWebPage(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        try {
            Uri page = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, page);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Could not navigate", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Sets up event listeners for the rsvp checkbox
     */
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

    /**
     * Confirms the deletion of an event by prompting the user.
     */
    private void confirmDeleteEvent() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Delete Event");
        builder.setMessage("Are you sure you want to delete this event? This action cannot be reversed.");

        builder.setPositiveButton("Confirm", ((dialog, which) -> deleteEvent()));
        builder.setNegativeButton("Cancel", ((dialog, which) -> dialog.dismiss()));

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    /**
     * Deletes an event from Firestore and handles related data cleanup
     */
    private void deleteEvent() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference eventRef = db.collection("events").document(eventId);

        eventRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Map<String, Object> eventData = documentSnapshot.getData();

                if (eventData != null) {
                    db.collection("deleted_events").add(eventData).addOnSuccessListener(documentReference -> {

                        handleEventRemoval(eventData);

                        eventRef.delete().addOnSuccessListener(void1 -> {
                            Toast.makeText(EventDetailsActivity.this, "Event deleted successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        }).addOnFailureListener(e -> Toast.makeText(EventDetailsActivity.this, "Error deleting event: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    }).addOnFailureListener(e -> Toast.makeText(EventDetailsActivity.this, "Error moving event to deleted_events: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            }
        }).addOnFailureListener(e -> Toast.makeText(EventDetailsActivity.this, "Error fetching event data: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    /**
     * Handles the removal of an event by updating relavent fields in user documents.
     * @param eventData Data of the event that is being removed
     */
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
     * Launches a dialog to edit the event details.
     */
    private void eventEditor() {

        // Initializes the AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Event Details");

        // Inflate the custom view for the dialog
        LayoutInflater inflater = getLayoutInflater();
        View dialog = inflater.inflate(R.layout.dialog_edit_event_details, null);
        builder.setView(dialog);

        // Finds and set views from the dialog layout
        EditText editEventName = dialog.findViewById(R.id.dialogEditEventName);
        EditText editLocation = dialog.findViewById(R.id.dialogEditLocation);
        EditText editVenue = dialog.findViewById(R.id.dialogEditVenue);
        EditText editDescription = dialog.findViewById(R.id.dialogEditDescription);
        EditText editOutSideLink = dialog.findViewById(R.id.dialogEditOutsideLink);
        Button buttonSelectDate = dialog.findViewById(R.id.buttonSelectDate);
        Button buttonSelectTime = dialog.findViewById(R.id.buttonSelectTime);
        Button buttonCancel = dialog.findViewById(R.id.cancelEventChangesButton);

        // Create and display the dialog
        AlertDialog alertDialog = builder.create();

        // Set click listeners for the date and time buttons.
        buttonSelectDate.setOnClickListener(v -> showDatePickerDialog(buttonSelectDate));
        buttonSelectTime.setOnClickListener(v -> showTimePickerDialog(buttonSelectTime));
        buttonCancel.setOnClickListener(v -> alertDialog.dismiss());

        // Get save changes button.
        Button saveEventChangesButton = dialog.findViewById(R.id.saveEventChangesButton);

        // Set the data from the current views to the edit text fields
        editEventName.setText(textViewEventName.getText().toString());
        editLocation.setText(textViewLocation.getText().toString());
        editVenue.setText(textViewVenue.getText().toString());
        editDescription.setText(textViewDescription.getText().toString());
        editOutSideLink.setText(textViewOutsideLink.getText().toString());
        buttonSelectDate.setText(textViewDate.getText().toString());
        buttonSelectTime.setText(textViewTime.getText().toString());

        // Set click listener for the save changes button
        saveEventChangesButton.setOnClickListener(v -> {
            String updatedEventName = editEventName.getText().toString().trim();
            String updatedLocation = editLocation.getText().toString().trim();
            String updatedVenue = editVenue.getText().toString().trim();
            String updatedDescription = editDescription.getText().toString().trim();
            String updatedOutsideLink = editOutSideLink.getText().toString().trim();
            String updatedDate = buttonSelectDate.getText().toString().trim();
            String updatedTime = buttonSelectTime.getText().toString().trim();

            // Update event detials with the new data
            updateEvent(updatedEventName, updatedLocation, updatedVenue, updatedDescription, updatedOutsideLink, updatedDate, updatedTime);

            // Update the main views with the new data
            textViewEventName.setText(updatedEventName);
            textViewLocation.setText(updatedLocation);
            textViewVenue.setText(updatedVenue);
            textViewDescription.setText(updatedDescription);
            textViewOutsideLink.setText("Click here for more information");
            textViewDate.setText(updatedDate);
            textViewTime.setText(updatedTime);

            // Dismiss
            alertDialog.dismiss();
        });
        alertDialog.show();
    }

    /**
     * Updates an event's details in Firestore with provided data.
     * @param eventName   New name of the event
     * @param location    New location of the event
     * @param venue       New venue of the event
     * @param description New description of the event
     * @param date        New date of the event
     * @param time        New time for the event
     */
    private void updateEvent(String eventName, String location, String venue, String description, String outsideLink, String date, String time) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference eventRef = db.collection("events").document(eventId);

        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("eventName", eventName);
        updatedData.put("location", location);
        updatedData.put("venue", venue);
        updatedData.put("description", description);
        updatedData.put("outsideLink", outsideLink);
        updatedData.put("date", date);
        updatedData.put("time", time);

        eventRef.update(updatedData).addOnSuccessListener(void1 -> Toast.makeText(EventDetailsActivity.this, "Event Updated Successfully!", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(EventDetailsActivity.this, "Error updating the event: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Displays a TimePickerDialog to allow a user to select a time.
     * @param targetButton Button that will display the selected time.
     */
    private void showTimePickerDialog(Button targetButton) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) -> {
                    String selectedTime = formatTime(selectedHour, selectedMinute);
                    targetButton.setText(selectedTime);
                },
                hour, minute, false);

        timePickerDialog.show();
    }

    /**
     * Format the time in 12-hour format with AM/PM.
     * @param hourOfDay The hour of the day.
     * @param minute    The minute.
     * @return A formatted time string.
     */
    @SuppressLint("DefaultLocale")
    private String formatTime(int hourOfDay, int minute) {
        String amPm = (hourOfDay < 12) ? "AM" : "PM";
        int hour = (hourOfDay % 12 == 0) ? 12 : hourOfDay % 12;
        return String.format("%02d:%02d %s", hour, minute, amPm);
    }

    /**
     * Displays a TimePickerDialog to allow users to select a time.
     * @param targetButton the button that will display the selected time.
     */
    private void showDatePickerDialog(Button targetButton) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = formatDate(selectedYear, selectedMonth, selectedDay);
                    targetButton.setText(selectedDate);
                },
                year, month, day
        );
        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    /**
     * Formats the provided date into the format MM-dd-yyyy.
     * @param year  the year of the date
     * @param month the month of the date
     * @param day   the day of the date.
     * @return the formatted date string.
     */
    private String formatDate(int year, int month, int day) {
       Calendar calendar = Calendar.getInstance();
       calendar.set(year, month, day);
       SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
       return simpleDateFormat.format(calendar.getTime());
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



