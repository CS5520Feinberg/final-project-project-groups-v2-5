package edu.northeastern.rhythmlounge;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TimePicker;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateEventFragment extends Fragment {

    private CircleImageView imageViewUploadedImage;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    private EditText editTextEventName, editTextCity, editTextState, editTextDescription, editTextDate, editTextTime;
    private DatabaseReference eventsRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create_event, container, false);

        eventsRef = FirebaseDatabase.getInstance().getReference().child("events");

        editTextEventName = rootView.findViewById(R.id.editTextEventName);
        editTextCity = rootView.findViewById(R.id.editTextCity);
        editTextState = rootView.findViewById(R.id.editTextState);
        editTextDescription = rootView.findViewById(R.id.editTextDescription);
        editTextDate = rootView.findViewById(R.id.editTextDate);
        editTextTime = rootView.findViewById(R.id.editTextTime);

        editTextDate.setOnClickListener(v -> showDatePickerDialog());
        editTextTime.setOnClickListener(v -> showTimePickerDialog());

        imageViewUploadedImage = rootView.findViewById(R.id.imageViewUploadedImage);
        Button buttonUploadImage = rootView.findViewById(R.id.buttonUploadImage);
        buttonUploadImage.setOnClickListener(v -> pickImageFromGallery());

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    Uri selectedImageUri = data.getData();
                    imageViewUploadedImage.setImageURI(selectedImageUri);
                }
            }
        });
        buttonUploadImage.setOnClickListener(v -> pickImageFromGallery());

        Button buttonCreateEvent = rootView.findViewById(R.id.buttonCreateEvent);
        buttonCreateEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                saveEvent();
            }
        });

        return rootView;
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

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

    @SuppressLint("DefaultLocale")
    private String formatTime(int hourOfDay, int minute) {
        String amPm = (hourOfDay < 12) ? "AM" : "PM";
        int hour = (hourOfDay % 12 == 0) ? 12 : hourOfDay % 12;
        return String.format("%02d:%02d %s", hour, minute, amPm);
    }

    private void saveEvent() {

        String eventName = editTextEventName.getText().toString().trim();
        String location = editTextCity.getText().toString().trim() + ", " + editTextState.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String date = editTextDate.getText().toString().trim();
        String time = editTextTime.getText().toString().trim();

        if (eventName.isEmpty() || location.isEmpty() || description.isEmpty() || date.isEmpty() || time.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference newEventRef = eventsRef.push();
        String eventId = newEventRef.getKey();

        HashMap<String, Object> eventMap = new HashMap<>();
        eventMap.put("eventName", eventName);
        eventMap.put("location", location);
        eventMap.put("description", description);
        eventMap.put("date", date);
        eventMap.put("time", time);


        newEventRef.setValue(eventMap)
                .addOnSuccessListener(aVoid -> {

                    Toast.makeText(requireContext(), "Event created successfully!", Toast.LENGTH_SHORT).show();

                    editTextEventName.setText("");
                    editTextCity.setText("");
                    editTextState.setText("");
                    editTextDescription.setText("");
                    editTextDate.setText("");
                    editTextTime.setText("");
                    imageViewUploadedImage.setImageResource(R.drawable.concert);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to create event.", Toast.LENGTH_SHORT).show();
                });
    }
}









