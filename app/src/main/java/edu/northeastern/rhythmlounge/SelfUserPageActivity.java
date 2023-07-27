package edu.northeastern.rhythmlounge;

import android.os.Bundle;
import android.widget.TextView;

<<<<<<< Updated upstream
=======
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
>>>>>>> Stashed changes
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SelfUserPageActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;

    private TextView textViewOwnUsername, textViewOwnEmail, textViewOwnFollowers, textViewOwnFollowing;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
<<<<<<< Updated upstream
=======
    private FirebaseStorage fbStorage;
    private StorageReference storageReference;

    private ActivityResultLauncher<String> pickMedia;


>>>>>>> Stashed changes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self_user_page);

        initializeViewElements();
        initializeFirebaseElements();

        String currentUserId = getCurrentUserId();
        retrieveCurrentUser(currentUserId);
<<<<<<< Updated upstream
=======

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


>>>>>>> Stashed changes
    }

    /**
     * Initializes all the view elements of this activity.
     */
    private void initializeViewElements() {
        textViewOwnUsername = findViewById(R.id.textViewOwnUsername);
        textViewOwnEmail = findViewById(R.id.textViewOwnEmail);
        textViewOwnFollowers = findViewById(R.id.textViewOwnFollowers);
        textViewOwnFollowing = findViewById(R.id.textViewOwnFollowing);
    }

    /**
     * Initializes FirebaseAuth and FirebaseFirestore.
     */
    private void initializeFirebaseElements() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Gets the user ID of the current user.
     * @return The user ID of the current user.
     */
    private String getCurrentUserId() {
        return mAuth.getCurrentUser().getUid();
    }

    /**
     * Retrieves the current user from the Firebase database and populates the UI.
     * @param userId The user ID of the current user.
     */
    private void retrieveCurrentUser(String userId) {
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            User currentUser = documentSnapshot.toObject(User.class);
            populateUIWithCurrentUserDetails(currentUser);
        });
    }

    /**
     * Populates the UI with the details of the current user.
     * @param currentUser the current user object.
     */
    private void populateUIWithCurrentUserDetails(User currentUser) {
<<<<<<< Updated upstream
            textViewOwnUsername.setText(currentUser.getUsername());
            textViewOwnEmail.setText(currentUser.getEmail());
            textViewOwnFollowers.setText(String.valueOf(currentUser.getFollowers().size()));
            textViewOwnFollowing.setText(String.valueOf(currentUser.getFollowing().size()));
    }

=======
        textViewOwnUsername.setText(currentUser.getUsername());
        textViewOwnEmail.setText(currentUser.getEmail());
        textViewOwnFollowers.setText(String.valueOf(currentUser.getFollowers().size()));
        textViewOwnFollowing.setText(String.valueOf(currentUser.getFollowing().size()));

        if (currentUser.getProfilePictureUrl() != null && !currentUser.getProfilePictureUrl().isEmpty()) {
            // Load the image from the URL if it exists
            Glide.with(this).load(currentUser.getProfilePictureUrl()).into(imageViewProfilePic);
        } else {
            // Load a default image if the user has not set their profile picture
            Glide.with(this).load(R.drawable.defaultprofilepicture).into(imageViewProfilePic);
        }

    }



    //Profile Picture methods --------------------------------------------------------------------------------------------
    /**
     * Checks if the READ_EXTERNAL_STORAGE permission is granted. If not, it requests that the user grant it.
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
     * Opens an AlertDialog that asks the user if they want to update their profile picture.
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
     * Uploads a selected image to Firebase Storage and updates the user's profile picture URL in Firestore.
     * @param imageUri imageUri The Uri of the selected image.
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
            });
        })).addOnFailureListener(e -> {
            Toast.makeText(SelfUserPageActivity.this, "Failed to upload profile picture", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * This is bugged currently, if i leave the else, it will always state permission denied upon loading the activity.
     * @param requestCode The request code passed in requestPermissions(
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

                //} else {
                //Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Profile Picture methods --------------------------------------------------------------------------------------------
>>>>>>> Stashed changes
}

