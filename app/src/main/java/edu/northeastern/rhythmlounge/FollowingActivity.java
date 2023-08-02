package edu.northeastern.rhythmlounge;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity to display the list of users that the current user is following.
 */
public class FollowingActivity extends AppCompatActivity {

    private RecyclerView followingRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following);

        // Initialize the RecyclerView
        followingRecyclerView = findViewById(R.id.followingRecyclerView);
        followingRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Add divider lines between items in the RecyclerView
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(followingRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        followingRecyclerView.addItemDecoration(dividerItemDecoration);

        // List to hold the users that the current user is following
        List<User> following = new ArrayList<>();

        // get the current user's ID from the intent
        String currentUserId = getIntent().getStringExtra("USER_ID");

        // Connect to Firestore database
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Fetch the document for the current user
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    // Get the list of user IDs that the current user is following
                    List<String> followingIds = (List<String>) documentSnapshot.get("following");

                    if (followingIds != null) {
                        // Initialize the following list with null values to set its size
                        for (int i = 0; i < followingIds.size(); i++) {
                            following.add(null);
                        }

                        // Iterate over the following IDs to fetch each corresponding user
                        for (int i = 0; i < followingIds.size(); i++) {
                            String followingId = followingIds.get(i);
                            int index = i; // Capture the index for use inside the inner callback

                            // Fetch the document for a following user
                            db.collection("users").document(followingId)
                                    .get()
                                    .addOnSuccessListener(followingDocument -> {
                                        User user = followingDocument.toObject(User.class);
                                        following.set(index, user); // Set the user at the correct position in the list

                                        // Check if all users have been added
                                        if (!following.contains(null)) {
                                            UserListItemAdapter userListItemAdapter = new UserListItemAdapter(FollowingActivity.this, following, followingIds);
                                            followingRecyclerView.setAdapter(userListItemAdapter);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.w("FollowingActivity", "There was a problem getting a following user", e);
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w("FollowingActivity", "There was a problem getting the following list", e);
                });
    }
}
