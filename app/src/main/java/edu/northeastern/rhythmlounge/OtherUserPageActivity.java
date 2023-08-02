package edu.northeastern.rhythmlounge;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * OtherUserPageActivity allows a user to view another user's profile, which includes:
 *      - username
 *      - email (WARNING: we might not want to display this later)
 *      - # of followers
 *      - # of following
 *      - profile picture
 *
 * Features:
 *      - Follow/Unfollow
 *
 * @author James Bebarski
 */
public class OtherUserPageActivity extends AppCompatActivity {

    // Current UI elements for displaying information
    private TextView textViewUsername, textViewEmail, textViewFollowers, textViewFollowing;
    private ImageView imageViewProfilePic;
    private Button buttonFollowUnfollow, buttonFollowers, buttonFollowing;

    // The current user using the application and the other user they are viewing
    private User currentUser, otherUser;

    private FirebaseAuth mAuth; // Firebase authentication instance
    private FirebaseFirestore db; // Firebase Firestore instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user_page);

        // Initializes the view and the Firebase elements
        initializeViewElements();
        initializeFirebaseElements();

        // Get the current user's ID and retrieve their details
        String currentUserId = getCurrentUserId();
        retrieveCurrentUser(currentUserId);

        // Get the other user's ID from the intent and retrieve their details
        Intent intent = getIntent();
        String otherUserId = intent.getStringExtra("USER_ID");
        Log.d("OtherUserPageActivity", "Other User ID: " + otherUserId);

        if (currentUserId.equals(otherUserId)) {
            navigateToSelfUserProfile();
            return;
        }

        // Handle the follow/unfollow button click
        retrieveCurrentUser(currentUserId);
        retrieveOtherUser(otherUserId);
        handleFollowUnfollowButtonClick(otherUserId);
        handleFollowingButtonClick(otherUserId);
        handleFollowersButtonClick(otherUserId);


    }

    /**
     * Initializes all the view elements of this activity.
     */
    private void initializeViewElements() {
        textViewUsername = findViewById(R.id.textViewUsername);
        textViewEmail = findViewById(R.id.textViewEmail);
        textViewFollowers = findViewById(R.id.textViewFollowers);
        imageViewProfilePic = findViewById(R.id.other_user_profile_picture);
        buttonFollowUnfollow = findViewById(R.id.buttonFollowUnfollow);
        buttonFollowers = findViewById(R.id.buttonFollowing);
        buttonFollowing = findViewById(R.id.buttonFollowers);
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
     * Retrieves the current user's data from Firebase.
     * If the other user's data is already available, populates the UI with their details.
     * @param userId the user ID of the current user.
     */
    private void retrieveCurrentUser(String userId) {
    db.collection("users").document(userId)
            .addSnapshotListener((snapshot, e) -> {
                if (e != null || snapshot == null) return;
                currentUser = snapshot.toObject(User.class);
                if (otherUser != null) {
                    populateUIWithOtherUserDetails(userId);
                }
            });
}

    /**
     * Retrieves the other user's data from Firebase and populates the UI.
     * @param otherUserId the user ID of the other user.
     */
    private void retrieveOtherUser(String otherUserId) {
        db.collection("users").document(otherUserId)
            .addSnapshotListener((snapshot, e) -> {
                if (e != null || snapshot == null) return;

                otherUser = snapshot.toObject(User.class);
                populateUIWithOtherUserDetails(otherUserId);
            });
    }

    /**
     * Populates the UI with the other user's details including:
     *      - username
     *      - email (WARNING: we might not want to display this later)
     *      - # of followers
     *      - # of following
     *      - profile picture
     *
     * Handles displaying a default profile picture if the other user has not set one,
     * and updates the follow/unfollow button based on the current user's following status.
     * @param otherUserId the userId of the other user.
     */
    private void populateUIWithOtherUserDetails(String otherUserId) {

    if (otherUser != null) {

        // Set the username
        textViewUsername.setText(otherUser.getUsername());

        // Set the email
        textViewEmail.setText(otherUser.getEmail());

        // Determine and set the follower count of the other user
        int followerCount = (otherUser.getFollowers() != null) ? otherUser.getFollowers().size() : 0;

        // Determine and set the following count of the other user
        int followingCount = (otherUser.getFollowing() != null) ? otherUser.getFollowing().size() : 0;

        // Display the followers • following text
        String followersFollowingText = followingCount + " Following • " + followerCount + " Followers";
        textViewFollowers.setText(followersFollowingText);

        // Load the profile picture if one is available, otherwise use the built in default
        if (otherUser.getProfilePictureUrl() != null && !otherUser.getProfilePictureUrl().isEmpty()) {
            Glide.with(this).load(otherUser.getProfilePictureUrl()).into(imageViewProfilePic);
        } else {
            Glide.with(this).load(R.drawable.defaultprofilepicture).into(imageViewProfilePic);
        }

    } else {
        // If something went wrong display an error message.
        Toast.makeText(this, "Failed to load the user information.", Toast.LENGTH_LONG).show();
        finish();
    }

    if (currentUser != null && currentUser.getFollowing() != null) {
        // Update follow/unfollow button based on current user's following status
        if (currentUser.getFollowing().contains(otherUserId)) {
            buttonFollowUnfollow.setText("Unfollow");
        } else {
            buttonFollowUnfollow.setText("Follow");
        }
    } else {
        // Set default text for follow/unfollow button
        buttonFollowUnfollow.setText("Follow");
    }
}

    /**
     * Handles the click event for the follow/unfollow button.
     * Updates the following status of the current user and changes the button text accordingly.
     * @param otherUserId the user ID of the other user.
     */
    private void handleFollowUnfollowButtonClick(String otherUserId) {
        buttonFollowUnfollow.setOnClickListener(v -> {
            if (currentUser.getFollowing().contains(otherUserId)) {
                currentUser.unfollowUser(otherUserId);
                buttonFollowUnfollow.setText("Follow");
            } else {
                currentUser.followUser(otherUserId);
                buttonFollowUnfollow.setText("Unfollow");
            }
        });
    }

    private void handleFollowersButtonClick(String otherUserId) {
        buttonFollowers.setOnClickListener(v -> {
            Intent intent = new Intent(OtherUserPageActivity.this, FollowersActivity.class);
            intent.putExtra("USER_ID", otherUserId);
            startActivity(intent);
        });
    }

    private void handleFollowingButtonClick(String otherUserId) {
        buttonFollowing.setOnClickListener(v -> {
            Intent intent = new Intent(OtherUserPageActivity.this, FollowingActivity.class);
            intent.putExtra("USER_ID", otherUserId);
            startActivity(intent);
        });
    }

    private void navigateToSelfUserProfile() {
        Intent intent = new Intent(this, SelfUserPageFragment.class);
        startActivity(intent);
        finish();
    }

    public void onFollowersClicked(View view) {
    }

    public void onFollowingClicked(View view) {
    }

}