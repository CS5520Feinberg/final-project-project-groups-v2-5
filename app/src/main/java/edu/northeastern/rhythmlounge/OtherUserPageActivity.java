package edu.northeastern.rhythmlounge;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * This is the activity for when one view is viewing another user's profile.
 */
public class OtherUserPageActivity extends AppCompatActivity {

    private TextView textViewUsername, textViewEmail, textViewFollowers, textViewFollowing;
    private Button buttonFollowUnfollow;

    private User currentUser, otherUser;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user_page);

        initializeViewElements();
        initializeFirebaseElements();

        String currentUserId = getCurrentUserId();
        retrieveCurrentUser(currentUserId);

        Intent intent = getIntent();
        String otherUserId = intent.getStringExtra("USER_ID");

        retrieveOtherUser(otherUserId);
        handleFollowUnfollowButtonClick(otherUserId);
    }

    /**
     * Initializes all the view elements of this activity.
     */
    private void initializeViewElements() {
        textViewUsername = findViewById(R.id.textViewUsername);
        textViewEmail = findViewById(R.id.textViewEmail);
        textViewFollowers = findViewById(R.id.textViewFollowers);
        textViewFollowing = findViewById(R.id.textViewFollowing);
        buttonFollowUnfollow = findViewById(R.id.buttonFollowUnfollow);
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
     * Retrieves the current user from the Firebase Firestore database.
     * @param userId The user ID of the current user.
     */
    private void retrieveCurrentUser(String userId) {
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> currentUser = documentSnapshot.toObject(User.class));
    }

    /**
     * Retrieves the other user's data from Firebase Firestore database and populates the UI elements.
     * @param otherUserId The user ID of the other user.
     */
    private void retrieveOtherUser(String otherUserId) {
        db.collection("users").document(otherUserId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                otherUser = documentSnapshot.toObject(User.class);
                populateUIWithOtherUserDetails(otherUserId);
            }
        });
    }

    /**
     * Populates the UI with the details of the other user.
     * @param otherUserId The user ID of the other user.
     */
    private void populateUIWithOtherUserDetails(String otherUserId) {
        textViewUsername.setText(otherUser.getUsername());
        textViewEmail.setText(otherUser.getEmail());
        textViewFollowers.setText(String.valueOf(otherUser.getFollowers().size()));
        textViewFollowing.setText(String.valueOf(otherUser.getFollowing().size()));

        if (currentUser.getFollowing().contains(otherUserId)) {
            buttonFollowUnfollow.setText("Unfollow");
        } else {
            buttonFollowUnfollow.setText("Follow");
        }
    }

    /**
     * Handles the click event for the follow/unfollow button.
     * @param otherUserId The user ID of the other user.
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
}