package edu.northeastern.rhythmlounge;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.rhythmlounge.Events.Event;
import edu.northeastern.rhythmlounge.Events.EventDetailsActivity;
import edu.northeastern.rhythmlounge.Events.EventsAdapter;
import edu.northeastern.rhythmlounge.Playlists.OtherUserPlaylistAdapter;


/**
 * OtherUserPageActivity allows a user to view another user's profile, which includes:
 *      - username
 *      - # of followers
 *      - # of following
 *      - profile picture
 * <p>
 * Features:
 *      - Follow/Unfollow
 *
 * @author James Bebarski
 */
public class OtherUserPageActivity extends AppCompatActivity {

    private TextView textViewUsername, textViewFollowers, textViewFollowing;
    private ImageView imageViewProfilePic;
    private Button buttonFollowUnfollow;

    private RecyclerView otherUserPlaylistRecyclerView, otherUserAttendingRecyclerView, otherUserHostingRecyclerView;

    private EventsAdapter otherUserHostingEventsAdapter, otherUserAttendingEventsAdapter;

    private OtherUserPlaylistAdapter playlistAdapter;

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
        retrieveOtherUser(otherUserId);

        // If the current user is the same as the other user, navigate back to the homeactivity
        if (currentUserId.equals(otherUserId)) {
            navigateToSelfUserProfile();
            return;
        }

        initializeViewElements(otherUserId);

        // Handle the follow/unfollow button click
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

        initializePlaylistRecyclerView(otherUserId);
        initializeAttendingRecyclerView(otherUserId);
        initializeHostingRecyclerView(otherUserId);
        setupEventClickListeners();
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


    }

    /**
     * Initializes and sets up the RecyclerView for displaying another user's playlists.
     * @param otherUserId the ID of the other user whose playlists should be displayed.
     */
    private void initializePlaylistRecyclerView(String otherUserId) {
        otherUserPlaylistRecyclerView = findViewById(R.id.recyclerViewPlaylists);
        playlistAdapter = new OtherUserPlaylistAdapter(this, new ArrayList<>(), otherUserId);
        otherUserPlaylistRecyclerView.setAdapter(playlistAdapter);
        otherUserPlaylistRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * Initializes and sets up the RecyclerView for displaying events the other user is attending.
     * @param otherUserId the ID of the other user whose attending events.
     */
    private void initializeAttendingRecyclerView(String otherUserId) {
        otherUserAttendingRecyclerView = findViewById(R.id.otherAttendingRecyclerView);
        otherUserAttendingEventsAdapter = new EventsAdapter(new ArrayList<>());
        otherUserAttendingRecyclerView.setAdapter(otherUserAttendingEventsAdapter);
        otherUserAttendingRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        fetchUserEvents(otherUserId);
    }

    /**
     * Initializes and sets up the RecyclerView for displaying events the other user is hosting.
     * @param otherUserId the ID of the other user whose hosting events.
     */
    private void initializeHostingRecyclerView(String otherUserId) {
        otherUserHostingRecyclerView = findViewById(R.id.otherHostingRecyclerView);
        otherUserHostingEventsAdapter = new EventsAdapter(new ArrayList<>());
        otherUserHostingRecyclerView.setAdapter(otherUserHostingEventsAdapter);
        otherUserHostingRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        fetchUserEvents(otherUserId);
    }

    /**
     * Fetches the user's hosting and attending event's IDs and updates the Respective RecyclerView.
     * @param otherUserId the ID of the other user whose events need to be displayed.
     */
    private void fetchUserEvents(String otherUserId) {
        DocumentReference userDocRef = db.collection("users").document(otherUserId);

        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            List<String> hostingIds = (List<String>) documentSnapshot.get("hosting");
            List<String> rsvpIds = (List<String>) documentSnapshot.get("rsvpd");

            fetchEventsAndUpdateRecyclerView(hostingIds, otherUserHostingEventsAdapter);
            fetchEventsAndUpdateRecyclerView(rsvpIds, otherUserAttendingEventsAdapter);
        });
    }

    /**
     * Fetches the specific events based on given IDs and updates the RecyclerView using the provided adapter.
     * @param eventIds A list of event Ids that need to be fetched
     * @param adapter the adapter used to update the RecyclerView with fetched events.
     */
    private void fetchEventsAndUpdateRecyclerView(List<String> eventIds, EventsAdapter adapter) {
        if (eventIds == null || eventIds.isEmpty()) return;

        CollectionReference eventsRef = db.collection("events");
        List<Event> events = new ArrayList<>();

        for (String eventId : eventIds) {
            eventsRef.document(eventId).get().addOnSuccessListener(documentSnapshot -> {
                Event event = documentSnapshot.toObject(Event.class);
                if (event != null) {
                    event.setDocId(documentSnapshot.getId());
                    events.add(event);

                    if (events.size() == eventIds.size()) {
                        adapter.updateData(events);
                    }
                }
            }).addOnFailureListener(e -> Log.e(TAG, "Failed to fetch event: " + e.getMessage()));
        }
    }

    /**
     * Sets up click listeners for events in both the attending and hosting sections.
     */
    private void setupEventClickListeners() {
        Log.d(TAG, "Setting up event click listener");

        EventsAdapter.OnItemClickListener listener = event -> {
            Log.d(TAG, "Event clicked with ID: " + event.getDocId());
            Intent intent = new Intent(this, EventDetailsActivity.class);
            intent.putExtra("eventId", event.getDocId());
            intent.putExtra("event_name", event.getEventName());
            intent.putExtra("location", event.getLocation());
            intent.putExtra("venue", event.getVenue());
            intent.putExtra("description", event.getDescription());
            intent.putExtra("outside_link", event.getOutsideLink());
            intent.putExtra("date", event.getDate());
            intent.putExtra("time", event.getTime());
            intent.putExtra("imageURL", event.getImageURL());
            Log.d(TAG, "Passing eventId: " + event.getDocId());
            startActivity(intent);
        };

        otherUserHostingEventsAdapter.setOnItemClickListener(listener);
        otherUserAttendingEventsAdapter.setOnItemClickListener(listener);
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

    /**
     * Get's the user's playlist documents from firebase.
     * @param otherUserId the user page you are viewing
     */
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
     * <p>
     * Handles displaying a default profile picture if the other user has not set one,
     * and updates the follow/unfollow button based on the current user's following status.
     * @param otherUserId the userId of the other user.
     */
    @SuppressLint("SetTextI18n")
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

        if (!isFinishing() && currentUser != null && currentUser.getFollowing() != null) {
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

    /**
     * Helps navigate to a users followers.
     * @param otherUserId
     */
    private void handleFollowersClick(String otherUserId) {
        Intent intent = new Intent(OtherUserPageActivity.this, FollowersActivity.class);
        intent.putExtra("USER_ID", otherUserId);
        startActivity(intent);
    }

    /**
     * Helps navigate to the users following.
     * @param otherUserId
     */
    private void handleFollowingClick(String otherUserId) {
        Intent intent = new Intent(OtherUserPageActivity.this, FollowingActivity.class);
        intent.putExtra("USER_ID", otherUserId);
        startActivity(intent);
    }

    /**
     * Navigates the current user back to their homeactivity if they stumble to their own page.
     */
    private void navigateToSelfUserProfile() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
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