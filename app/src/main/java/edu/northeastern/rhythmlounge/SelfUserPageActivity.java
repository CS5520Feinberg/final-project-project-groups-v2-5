package edu.northeastern.rhythmlounge;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;


import androidx.activity.result.ActivityResultLauncher;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

/**
 * Represents an activity where users can view and modify their own user page.
 */
public class SelfUserPageActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;

    private TextView textViewOwnUsername, textViewOwnEmail, textViewOwnFollowers, textViewOwnFollowing;

    private ImageView imageViewProfilePic;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage fbStorage;
    private StorageReference storageReference;

    private ActivityResultLauncher<String> pickMedia;


    /**
     * Initializes activity and it's components
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self_user_page);

        initializeViewElements();
        initializeFirebaseElements();

        String currentUserId = getCurrentUserId();
        retrieveCurrentUser(currentUserId);

        checkPermission();

        pickMedia = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: " + uri);

                Glide.with(this).load(uri).into(imageViewProfilePic);
                uploadImageToFirebaseStorage(uri);
            } else {
                Log.d("PhotoPicker", "No media selected");
            }
        });


        imageViewProfilePic.setOnClickListener(v -> openImageDialog());


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
        // Populates username
        textViewOwnUsername.setText(currentUser.getUsername());

        // Populates Email -
        // NOTE: We might want to remove this later down the road, I added it simply to see if we could properly retrieve this type of information from firebase.
        textViewOwnEmail.setText(currentUser.getEmail());

        // Populate # of followers to UI
        int followerCount = (currentUser.getFollowers() != null) ? currentUser.getFollowers().size() : 0;
        textViewOwnFollowers.setText(String.valueOf(followerCount));

        // Populate # of following to UI
        int followingCount = (currentUser.getFollowing() != null) ? currentUser.getFollowing().size() : 0;
        textViewOwnFollowing.setText(String.valueOf(followingCount));

        // Populates the profile picture.
        if (currentUser.getProfilePictureUrl() != null && !currentUser.getProfilePictureUrl().isEmpty()) {
            // Load the image from the URL if it exists
            Glide.with(this).load(currentUser.getProfilePictureUrl()).into(imageViewProfilePic);
        } else {
            // Load a default image if the user has not set their profile picture
            Glide.with(this).load(R.drawable.defaultprofilepicture).into(imageViewProfilePic);
        }

    }

    /**
     * Checks whether the necessary permission to read external storage is granted.
     * If not, this will request the user for permission.
     */
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(this)
                        .setTitle("Permission needed")
                        .setMessage("This permission is needed to access your media.")
                        .setPositiveButton("OK", (dialog, which) -> ActivityCompat.requestPermissions(SelfUserPageActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE))
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .create().show();
            } else {
                ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, PERMISSION_REQUEST_CODE);
            }
        }

    }

    /**
     * Opens a dialog prompting the user to select a new profile picture.
     */
    private void openImageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Profile Picture")
                .setMessage("Would you like to upload a new profile picture?")
                .setPositiveButton("Yes", (dialog, which) -> pickMedia.launch("image/*"))
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    /**
     * Uploads the selected image to Firebase storage and updates the profile picture URL in the database.
     * @param imageUri the URI of the selected image.
     */
    private void uploadImageToFirebaseStorage(Uri imageUri) {
        String userId = getCurrentUserId();
        StorageReference profilePicRef = storageReference.child("profile_pics/" + userId);

        profilePicRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> profilePicRef.getDownloadUrl().addOnSuccessListener(uri -> {
            String imageUrl = uri.toString();

            db.collection("users").document(userId).update("profilePictureUrl", imageUrl).addOnSuccessListener(aVoid -> {
                Toast.makeText(SelfUserPageActivity.this, "Profile picture updated", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(SelfUserPageActivity.this, "Failed to update profile picture", Toast.LENGTH_SHORT).show();
                Log.d("FirebaseError", "Firestore update failure: " + e.getMessage());
            });
        })).addOnFailureListener(e -> {
            Toast.makeText(SelfUserPageActivity.this, "Failed to upload profile picture", Toast.LENGTH_SHORT).show();
            Log.d("FirebaseError", "Firebase Storage upload failure: " + e.getMessage());
        });
    }

    /**
     * This is bugged currently, if i leave the else, it will always state permission denied upon loading the activity.
     * @param requestCode The request code passed in {@link -requestPermissions(
     * android.app.Activity, String[], int)}
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either {@link android.content.pm.PackageManager#PERMISSION_GRANTED}
     *     or {@link android.content.pm.PackageManager#PERMISSION_DENIED}. Never null.
     *
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();

                /// BUG: If i leave this in it will always say Permission denied when starting the activity.
                //} else {
                //Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

