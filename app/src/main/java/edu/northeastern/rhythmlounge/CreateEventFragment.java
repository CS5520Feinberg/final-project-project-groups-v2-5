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
import android.widget.TimePicker;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateEventFragment extends Fragment {

    private CircleImageView imageViewUploadedImage;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private static final int REQUEST_IMAGE_PICK = 1;
    private EditText editTextDate;
    private EditText editTextTime;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create_event, container, false);

        editTextDate = rootView.findViewById(R.id.editTextDate);
        editTextTime = rootView.findViewById(R.id.editTextTime);

        editTextDate.setOnClickListener(v -> showDatePickerDialog());
        editTextTime.setOnClickListener(v -> showTimePickerDialog());

        imageViewUploadedImage = rootView.findViewById(R.id.imageViewUploadedImage);
        Button buttonUploadImage = rootView.findViewById(R.id.buttonUploadImage);
        buttonUploadImage.setOnClickListener(v -> pickImageFromGallery());

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Uri selectedImageUri = data.getData();
                        imageViewUploadedImage.setImageURI(selectedImageUri);
                    }
                }
            }
        });
        buttonUploadImage.setOnClickListener(v -> pickImageFromGallery());

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
            // Update the "Select Date" EditText with the selected date
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
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

        // Show the TimePickerDialog
        timePickerDialog.show();
    }

    @SuppressLint("DefaultLocale")
    private String formatTime(int hourOfDay, int minute) {
        String amPm = (hourOfDay < 12) ? "AM" : "PM";
        int hour = (hourOfDay % 12 == 0) ? 12 : hourOfDay % 12;
        return String.format("%02d:%02d %s", hour, minute, amPm);
    }

}







