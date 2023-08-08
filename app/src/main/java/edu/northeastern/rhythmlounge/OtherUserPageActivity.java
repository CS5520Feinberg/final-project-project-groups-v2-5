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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.rhythmlounge.Playlist.OtherUserPlaylistAdapter;


/**
 * OtherUserPageActivity allows a user to view another user's profile, which includes:
 *      - username
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
    private TextView textViewUsername, textViewFollowers, textViewFollowing;
    private ImageView imageViewProfilePic;
    private Button buttonFollowUnfollow;

    private RecyclerView otherUserRecyclerView;

    private OtherUserPlaylistAdapter playlistAdapter;

    // The current user using the application and the other user they are viewing
    private User currentUser, otherUser;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ListenerRegistration currentUserListenerRegistration;
    private ListenerRegistration otherUserListenerRegistration;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user_page);

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

        initializeViewElements(otherUserId);

        // Handle the follow/unfollow button click
        retrieveCurrentUser(currentUserId);
        retrieveOtherUser(otherUserId);
        handleFollowUnfollowButtonClick(otherUserId);

        // Fetch the user data from Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(otherUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    otherUser = documentSnapshot.toObject(User.class);
                    // Call the method to populate the UI here, after the data fetch is completed
                    populateUIWithOtherUserDetails(otherUserId);
                })
                .addOnFailureListener(e -> {
                    // Handle any errors here
                    Toast.makeText(this, "Failed to load the user information.", Toast.LENGTH_LONG).show();
                    finish();
                });
    }


    /**
     * Initializes all the view elements of this activity.
     */
    private void initializeViewElements(String otherUserId) {
        textViewUsername = findViewById(R.id.textViewUsername);
        textViewFollowers = findViewById(R.id.textViewFollowers);
        textViewFollowing = findViewById(R.id.textViewFollowing);
        imageViewProfilePic = findViewById(R.id.other_user_profile_picture);
        buttonFollowUnfollow = findViewById(R.id.buttonFollowUnfollow);

        textViewFollowing.setOnClickListener(v -> handleFollowingClick(otherUserId));
        textViewFollowers.setOnClickListener(v -> handleFollowersClick(otherUserId));

        otherUserRecyclerView = findViewById(R.id.recyclerViewPlaylists);
        playlistAdapter = new OtherUserPlaylistAdapter(this, new ArrayList<>(), otherUserId);
        otherUserRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        otherUserRecyclerView.setAdapter(playlistAdapter);
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
        currentUserListenerRegistration = db.collection("users").document(userId)
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
        otherUserListenerRegistration = db.collection("users").document(otherUserId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null) return;
                    otherUser = snapshot.toObject(User.class);
                    getOtherUserPlaylists(otherUserId);
                    populateUIWithOtherUserDetails(otherUserId);
                });
    }

    private void getOtherUserPlaylists(String otherUserId) {
        db.collection("users")
                .document(otherUserId)
                .collection("playlists")
                .get()
                .addOnCompleteListener(task -> {
                   if (task.isSuccessful()) {
                       List<DocumentSnapshot> playlistSnapshots = task.getResult().getDocuments();
                       playlistAdapter.refreshData(playlistSnapshots);
                       Log.d("OtherUserPageActivity", "Successfully retrieved playlists: " + playlistSnapshots.size());
                   } else {
                       Log.d("OtherUserPageActivity", "Error getting documents: ", task.getException());
                   }
                });
    }

    /**
     * Populates the UI with the other user's details including:
     *      - username
     *      - # of followers
     *      - # of following
     *      - profile picture
     *
     * Handles displaying a default profile picture if the other user has not set one,
     * and updates the follow/unfollow button based on the current user's following status.
     * @param otherUserId the userId of the other user.
     */
    private void populateUIWithOtherUserDetails(String otherUserId) {
        if (!isFinishing() && otherUser != null) {  // Added isFinishing() check

            // Set the username
            textViewUsername.setText(otherUser.getUsername());

            // Determine and set the follower count of the other user
            int followerCount = (otherUser.getFollowers() != null) ? otherUser.getFollowers().size() : 0;

            // Determine and set the following count of the other user
            int followingCount = (otherUser.getFollowing() != null) ? otherUser.getFollowing().size() : 0;

            // Set the follower count display.
            textViewFollowers.setText("Followers: " + followerCount);
            textViewFollowing.setText("Following: " + followingCount);

            // Load the profile picture if one is available, otherwise use the built in default
            if (otherUser.getProfilePictureUrl() != null && !otherUser.getProfilePictureUrl().isEmpty()) {
                Glide.with(this).load(otherUser.getProfilePictureUrl()).into(imageViewProfilePic);
            } else {
                Glide.with(this).load(R.drawable.defaultprofilepicture).into(imageViewProfilePic);
            }

        } else {
            // If something went wrong display an error message.
            //Toast.makeText(this, "Failed to load the user information.", Toast.LENGTH_LONG).show();
            finish();
        }

        if (!isFinishing() && currentUser != null && currentUser.getFollowing() != null) {  // Added isFinishing() check
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
            if (currentUser.getFollowing() != null && currentUser.getFollowing().contains(otherUserId)) {
                currentUser.unfollowUser(otherUserId);
                buttonFollowUnfollow.setText("Follow");
            } else {
                currentUser.followUser(otherUserId);
                buttonFollowUnfollow.setText("Unfollow");
            }
        });
    }

    private void handleFollowersClick(String otherUserId) {
        Intent intent = new Intent(OtherUserPageActivity.this, FollowersActivity.class);
        intent.putExtra("USER_ID", otherUserId);
        startActivity(intent);
    }

    private void handleFollowingClick(String otherUserId) {
        Intent intent = new Intent(OtherUserPageActivity.this, FollowingActivity.class);
        intent.putExtra("USER_ID", otherUserId);
        startActivity(intent);
    }


    private void navigateToSelfUserProfile() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    public void onFollowersClicked(View view, String otherUserId) {

    }

    public void onFollowingClicked(View view, String otherUserId) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentUserListenerRegistration != null) {
            currentUserListenerRegistration.remove();
        }
        if (otherUserListenerRegistration != null) {
            otherUserListenerRegistration.remove();
        }
    }

}