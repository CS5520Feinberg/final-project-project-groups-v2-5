package edu.northeastern.rhythmlounge;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A fragment class that allows user's to create a new event.
 */
public class CreateEventFragment extends Fragment {

    private CircleImageView imageViewUploadedImage;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    private Uri selectedImageUri;

    private EditText editTextEventName, editTextCity, editTextState, editTextVenue, editTextDescription, editTextDate, editTextTime, editTextOutsideLink;

    private CollectionReference eventsRef;

    private StorageReference storageReference;

    /**
     * This is invoked when the fragment creates it's object hierarchy.
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create_event, container, false);

        // Initializes the Firebase FireStore Database and get a reference to the events collection
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");

        // Initializes the Firebase Storage and gets a reference to event_pics in storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference("event_pics");

        // Assign all the EditText Views.
        editTextEventName = rootView.findViewById(R.id.editTextEventName);
        editTextCity = rootView.findViewById(R.id.editTextCity);
        editTextState = rootView.findViewById(R.id.editTextState);
        editTextVenue = rootView.findViewById(R.id.editTextVenue);
        editTextDescription = rootView.findViewById(R.id.editTextDescription);
        editTextOutsideLink = rootView.findViewById(R.id.editTextOutsideLink);
        editTextDate = rootView.findViewById(R.id.editTextDate);
        editTextTime = rootView.findViewById(R.id.editTextTime);

        // Set click listeners to show Date and Time picker dialogs.
        editTextDate.setOnClickListener(v -> showDatePickerDialog());
        editTextTime.setOnClickListener(v -> showTimePickerDialog());

        // Find and assign ImageView for uploaded image and Button for uploading image
        imageViewUploadedImage = rootView.findViewById(R.id.imageViewUploadedImage);
        Button buttonUploadImage = rootView.findViewById(R.id.buttonUploadImage);
        buttonUploadImage.setOnClickListener(v -> pickImageFromGallery());

        // Initializes the image picker launcher.
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    selectedImageUri = data.getData();
                    imageViewUploadedImage.setImageURI(selectedImageUri);
                }
            }
        });

        buttonUploadImage.setOnClickListener(v -> pickImageFromGallery());

        Button buttonCreateEvent = rootView.findViewById(R.id.buttonCreateEvent);
        buttonCreateEvent.setOnClickListener(v -> saveEvent());

        ToggleButton toggleButtonConcert = rootView.findViewById(R.id.toggleButtonConcert);
        ToggleButton toggleButtonEvent = rootView.findViewById(R.id.toggleButtonEvent);

        toggleButtonConcert.setOnClickListener(v -> {
            if (toggleButtonConcert.isChecked()) {
                toggleButtonEvent.setChecked(false);
            }
        });

        // Set OnClickListener for Event button
        toggleButtonEvent.setOnClickListener(v -> {
            if (toggleButtonEvent.isChecked()) {
                toggleButtonConcert.setChecked(false);
            }
        });

        return rootView;
    }

    /**
     * Helper method to pick an image from gallery
     */
    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    /**
     * Update the imageURL field of a specific event in Firestore with a given URL.
     * @param documentId The id of the document to update.
     * @param imageUrl   The URL of the image to be updated.
     */
    private void updateEventWithImageUrl(String documentId, String imageUrl) {
        DocumentReference eventRef = eventsRef.document(documentId);
        eventRef.update("imageURL", imageUrl)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Image URL successfully updated!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error updating image URL", e));
    }

    /**
     * Upload the selected image to Firebase storage.
     * @param documentId The id of the event document.
     */
    private void uploadImage(String documentId) {
        if (selectedImageUri != null) {
            final StorageReference imageRef = storageReference.child(documentId + ".jpg");
            imageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String imageUrl = uri.toString();
                                updateEventWithImageUrl(documentId, imageUrl);
                                Log.d(TAG, "Event image uploaded successfully.");
                            })
                            .addOnFailureListener(e -> Log.d(TAG, "Failed to get image URL.")))
                    .addOnFailureListener(e -> Log.d(TAG, "Failed to upload image."));
        }
    }

    /**
     * Display date picker dialog
     */
    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                dateSetListener,
                year, month, day);
        datePickerDialog.show();
    }

    /**
     * Listener for date picker dialog
     */
    private final DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

            SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(Calendar.YEAR, year);
            selectedDate.set(Calendar.MONTH, month);
            selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            String formattedDate = dateFormat.format(selectedDate.getTime());
            editTextDate.setText(formattedDate);
        }
    };

    /**
     * Display time picker dialog
     */
    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, selectedHour, selectedMinute) -> {
                    String selectedTime = formatTime(selectedHour, selectedMinute);
                    editTextTime.setText(selectedTime);
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
     * Save event to Firebase Firestore.
     */
    private void saveEvent() {

        String eventName = editTextEventName.getText().toString().trim();
        String location = editTextCity.getText().toString().trim() + ", " + editTextState.getText().toString().trim();
        String venue = editTextVenue.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String outsideLink = editTextOutsideLink.getText().toString().trim();
        String date = editTextDate.getText().toString().trim();
        String time = editTextTime.getText().toString().trim();

        String eventCreator = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        // Validate the input before saving the event (only eventName, location, description, date, time are required)
        if (!isValidInput(eventName, location, description, date, time)) {
            return;
        }

        // Assemble the event
        Map<String, Object> event = new HashMap<>();
        event.put("eventName", eventName);
        event.put("location", location);
        event.put("venue", venue);
        event.put("description", description);
        event.put("date", date);
        event.put("time", time);
        event.put("eventCreator", eventCreator);
        event.put("rsvps", new ArrayList<>());
        event.put("outsideLink", outsideLink);

        // Add the event to the database.
        eventsRef.add(event)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(requireContext(), "Event created successfully!", Toast.LENGTH_SHORT).show();
                    clearFields();
                    uploadImage(documentReference.getId());
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to create event.", Toast.LENGTH_SHORT).show());
    }

    /**
     * Check if the mandatory inputs are valid before saving the event.
     */
    private boolean isValidInput(String eventName, String location, String venue, String date, String time) {
        if (eventName.isEmpty()) {
            Toast.makeText(requireContext(), "Event name is required.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (location.isEmpty()) {
            Toast.makeText(requireContext(), "Location is required.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (venue.isEmpty()) {
            Toast.makeText(requireContext(), "Venue is required.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (date.isEmpty()) {
            Toast.makeText(requireContext(), "Date is required.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (time.isEmpty()) {
            Toast.makeText(requireContext(), "Time is required.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Clear all fields after successfully saving an event.
     * */
    private void clearFields() {
        editTextEventName.setText("");
        editTextCity.setText("");
        editTextState.setText("");
        editTextVenue.setText("");
        editTextDescription.setText("");
        editTextOutsideLink.setText("");
        editTextDate.setText("");
        editTextTime.setText("");
        imageViewUploadedImage.setImageResource(R.drawable.concert);
        selectedImageUri = null;
    }

}









