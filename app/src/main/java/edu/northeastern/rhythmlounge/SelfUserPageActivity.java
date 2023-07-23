package edu.northeastern.rhythmlounge;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SelfUserPageActivity extends AppCompatActivity {

    private TextView textViewOwnUsername, textViewOwnEmail, textViewOwnFollowers, textViewOwnFollowing;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self_user_page);

        initializeViewElements();
        initializeFirebaseElements();

        String currentUserId = getCurrentUserId();
        retrieveCurrentUser(currentUserId);
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
            textViewOwnUsername.setText(currentUser.getUsername());
            textViewOwnEmail.setText(currentUser.getEmail());
            textViewOwnFollowers.setText(String.valueOf(currentUser.getFollowers().size()));
            textViewOwnFollowing.setText(String.valueOf(currentUser.getFollowing().size()));
    }

}
