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
 * Activity to display the list of users that are following the current user.
 */
public class FollowersActivity extends AppCompatActivity {

    private RecyclerView followersRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);

        // Initialize RecyclerView
        followersRecyclerView = findViewById(R.id.followersRecyclerView);
        followersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Adds a basic divider line between each item
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(followersRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        followersRecyclerView.addItemDecoration(dividerItemDecoration);

        // List to hold the users that are following the current user
        List<User> followers = new ArrayList<>();

        // Get the current user's ID from the intent
        String currentUserId = getIntent().getStringExtra("USER_ID");

        // Connect to the Firestore database
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Fetch the document for the current user
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    // Get the list of user IDs that are following the current user
                    List<String> followerIds = (List<String>) documentSnapshot.get("followers");

                    if (followerIds != null) {
                        // Initialize the followers list with null values to set its size
                        for (int i = 0; i < followerIds.size(); i++) {
                            followers.add(null);
                        }

                        // Iterate over the follower IDs to fetch each corresponding user
                        for (int i = 0; i < followerIds.size(); i++) {
                            String followerId = followerIds.get(i);
                            int index = i; // Capture the index for use inside the inner callback

                            // Fetch the document for a follower user
                            db.collection("users").document(followerId)
                                    .get()
                                    .addOnSuccessListener(followerDocument -> {
                                        User user = followerDocument.toObject(User.class);
                                        followers.set(index, user);

                                        // Check if all users have been added, and set the RecyclerView adapter
                                        if (!followers.contains(null)) {
                                            UserListItemAdapter userListItemAdapter = new UserListItemAdapter(FollowersActivity.this, followers, followerIds);
                                            followersRecyclerView.setAdapter(userListItemAdapter);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.w("FollowersActivity", "There was a problem getting a follower", e);
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w("FollowersActivity", "There was a problem getting the followers list", e);
                });
    }
}
