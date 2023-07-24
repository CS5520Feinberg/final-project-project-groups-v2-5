package edu.northeastern.rhythmlounge;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;
import java.util.UUID;

public class SelfUserPageActivity extends AppCompatActivity {

    private TextView textViewOwnUsername, textViewOwnEmail, textViewOwnFollowers, textViewOwnFollowing;

    private ImageView imageViewProfilePic;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage fbStorage;
    private StorageReference storageReference;

    private static final int PICK_IMAGE_REQUEST = 101;

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 201;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self_user_page);

        initializeViewElements();
        initializeFirebaseElements();

        String currentUserId = getCurrentUserId();
        retrieveCurrentUser(currentUserId);

        imageViewProfilePic.setOnClickListener(v -> openImagePicker());


    }

    /**
     * Initializes all the view elements of this activity.
     */
    private void initializeViewElements() {
        textViewOwnUsername = findViewById(R.id.textViewOwnUsername);
        textViewOwnEmail = findViewById(R.id.textViewOwnEmail);
        textViewOwnFollowers = findViewById(R.id.textViewOwnFollowers);
        textViewOwnFollowing = findViewById(R.id.textViewOwnFollowing);
        imageViewProfilePic = findViewById(R.id.profile_pic);
    }

    /**
     * Initializes FirebaseAuth and FirebaseFirestore.
     */
    private void initializeFirebaseElements() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        fbStorage = FirebaseStorage.getInstance();
        storageReference = fbStorage.getReference();
    }

    /**
     * Gets the user ID of the current user.
     * @return The user ID of the current user.
     */
    private String getCurrentUserId() {
        return Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
    }

    /**
     * Retrieves the current user from the Firebase database and populates the UI.
     * @param userId The user ID of the current user.
     */
    private void retrieveCurrentUser(String userId) {
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            User currentUser = documentSnapshot.toObject(User.class);
            assert currentUser != null;
            populateUIWithCurrentUserDetails(currentUser);
        });
    }

    /**
     * Populates the UI with the details of the current user.
     * @param currentUser the current user object.
     */
    private void populateUIWithCurrentUserDetails(User currentUser) {
            textViewOwnUsername.setText(currentUser.getUsername());
            textViewOwnEmail.setText(currentUser.getEmail());
            textViewOwnFollowers.setText(String.valueOf(currentUser.getFollowers().size()));
            textViewOwnFollowing.setText(String.valueOf(currentUser.getFollowing().size()));

            String profilePicUrl= currentUser.getProfilePictureUrl();
            if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                Glide.with(this)
                        .load(currentUser.getProfilePictureUrl())
                        .into(imageViewProfilePic);
            }

    }

private void openImagePicker() {
        if (ContextCompat.checkSelfPermission(SelfUserPageActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                Toast.makeText(SelfUserPageActivity.this, "This permission is required to choose a profile picture.", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(SelfUserPageActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            } else {

                ActivityCompat.requestPermissions(SelfUserPageActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }
        } else {
            openImageSelector();
        }
    }

    private void openImageSelector() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            uploadImage(filePath);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openImageSelector();
                } else {
                    Toast.makeText(SelfUserPageActivity.this, "Permission to read your External storage was denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void uploadImage(Uri filePath) {
        if (filePath != null) {
            StorageReference reference = storageReference.child("profile_pictures/" + UUID.randomUUID().toString());
            reference.putFile(filePath)
                    .addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(uriTask -> {
                        if (uriTask.isSuccessful()) {
                            Uri downloadUrl = uriTask.getResult();

                            String profileImageUrl = downloadUrl.toString();
                            db.collection("users").document(getCurrentUserId())
                                    .update("profilePictureUrl", profileImageUrl);
                        } else {
                            Toast.makeText(SelfUserPageActivity.this, "Failed to get download URL", Toast.LENGTH_SHORT).show();
                        }
                    }))
                    .addOnFailureListener(e -> Toast.makeText(SelfUserPageActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show());

        }
    }

}
