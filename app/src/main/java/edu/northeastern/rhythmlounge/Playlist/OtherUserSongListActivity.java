package edu.northeastern.rhythmlounge.Playlist;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import edu.northeastern.rhythmlounge.R;
/**
 * Fetches and displays the song list for a given playlist.
 */
public class OtherUserSongListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SongAdapter songAdapter;

    private List<DocumentSnapshot> songSnapShots;

    private String playlistId;
    private FirebaseFirestore db;

    private String otherUserId;

    /**
     * Inititializes the activity fetching the song list for each given playlist
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user_song_list);

        playlistId = getIntent().getStringExtra("playlistId");
        otherUserId = getIntent().getStringExtra("otherUserId");

        db = FirebaseFirestore.getInstance();

        db.collection("users")
                    .document(otherUserId)
                    .collection("playlists")
                    .document(playlistId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        Log.d(TAG, "Fetching documents: Success");
                        if (documentSnapshot.exists()) {
                            initializeSongRecyclerView();
                        }
                    })
                    .addOnFailureListener(e -> Log.d(TAG, "There was an issue fetching the documents: ", e));
    }

    /**
     * Initializes the RecyclerView to display the songs and applies line decorations.
     */
    private void initializeSongRecyclerView() {

        Log.d(TAG, "initializeOtherUserSongRecyclerView.");
        recyclerView = findViewById(R.id.songRecyclerView);
        getSongsFromPlaylist(playlistId, otherUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    /**
     * Fetches songs from the given playlist of the specified user.
     * @param playListId the ID of the playlist from which songs need to be fetched
     * @param otherUserId the ID of the other user whose playlist is being accessed
     */
    private void getSongsFromPlaylist(String playListId, String otherUserId) {
        db.collection("users")
                .document(otherUserId)
                .collection("playlists")
                .document(playListId)
                .collection("songs")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Fetching songs: Success");
                        songSnapShots = task.getResult().getDocuments();
                        songAdapter = new SongAdapter(this, songSnapShots);

                        recyclerView.setAdapter(songAdapter);
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }
}
