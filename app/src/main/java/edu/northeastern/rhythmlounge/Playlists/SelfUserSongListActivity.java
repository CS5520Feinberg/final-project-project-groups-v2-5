package edu.northeastern.rhythmlounge.Playlists;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import edu.northeastern.rhythmlounge.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Activity to display and manage songs for the current user's playlist
 */
public class SelfUserSongListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SongAdapter songAdapter;

    private List<DocumentSnapshot> songSnapShots;

    private FloatingActionButton addButton;
    private String playlistId;
    private boolean isOwner;
    private FirebaseFirestore db;

    private String currentUserId;

    /**
     * Initializes the activity, sets up the various UI components, and fetches the data.
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self_user_song_list);

        addButton = findViewById(R.id.addSongButton);
        playlistId = getIntent().getStringExtra("playlistId");
        db = FirebaseFirestore.getInstance();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            currentUserId = currentUser.getUid();

            db.collection("users")
                    .document(currentUserId)
                    .collection("playlists")
                    .document(playlistId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        Log.d(TAG, "Fetching documents: Success");
                        if (documentSnapshot.exists()) {
                            isOwner = true;
                            initializeSongRecyclerView();
                        } else {
                            isOwner = false;
                            addButton.setVisibility(View.GONE);
                            initializeSongRecyclerView();
                        }
                    })
                    .addOnFailureListener(e -> Log.d(TAG, "There was an issue fetching the documents: ", e));

            addButton.setOnClickListener(v -> showAddSongDialog());
        } else {
            Toast.makeText(this, "You are not signed in. Please sign into continue.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Initializes the song RecyclerView and its related components
     */
    private void initializeSongRecyclerView() {

        Log.d(TAG, "initializeSongRecyclerView.");
        recyclerView = findViewById(R.id.songRecyclerView);
        getSongsFromPlaylist(playlistId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        if(isOwner){
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                    return false;
                }

                // If the user swipes left or right on the song item, it will remove the song.
                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    int position = viewHolder.getAdapterPosition();
                    DocumentSnapshot songSnapshot = songSnapShots.get(position);

                    String songTitle = songSnapshot.getString("title");
                    String artistName = songSnapshot.getString("artist");
                    String message = songTitle + " by " + artistName + " was deleted";

                    songSnapShots.remove(position);
                    songAdapter.notifyItemRemoved(position);

                    // Create a snackbar letting the user know the song was deleted
                    // This also provides an undo action if this was a mistake.
                    Snackbar.make(recyclerView, message, Snackbar.LENGTH_LONG)
                                    .setAction("Undo", v -> {
                                        songSnapShots.add(position, songSnapshot);
                                        songAdapter.notifyItemInserted(position);
                                    })
                                            .addCallback(new Snackbar.Callback() {
                                                @Override
                                                public void onDismissed(Snackbar snackbar, int event) {
                                                    if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                                                        deleteSong(songSnapshot.getId());
                                                    }
                                                }
                                            })
                                            .show();
                }
            });

            itemTouchHelper.attachToRecyclerView(recyclerView);
            addButton.setVisibility(View.VISIBLE);
        } else {
            addButton.setVisibility(View.GONE);
        }
    }

    /**
     * Fetches songs from a specific playlist for the current user
     * @param playListId the id of the playlist from which songs are to be fetched.
     */
    private void getSongsFromPlaylist(String playListId) {
        String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        db.collection("users")
                .document(currentUserId)
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

    /**
     * Deletes a song from the current playlist and updates the recyclerview.
     * @param songId the song id of the song to be deleted
     */
    private void deleteSong(String songId) {
        db.collection("users")
                .document(currentUserId)
                .collection("playlists")
                .document(playlistId)
                .collection("songs")
                .document(songId)
                .delete()
                .addOnSuccessListener(void1 -> {
                    Log.d(TAG, "Song was deleted!");
                    getSongsFromPlaylist(playlistId);
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error deleting song", e));

    }

    /**
     * Displays a dialog for the user to add a new song to to their playlist by entering its title and artist name.
     * This triggers a YouTube search for the song.
     */
    private void showAddSongDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Song");

        View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_add_song, null);
        EditText editTextSongTitle = dialogLayout.findViewById(R.id.editTextSongTitle);
        EditText editTextArtist = dialogLayout.findViewById(R.id.editTextArtist);

        builder.setView(dialogLayout);

        builder.setPositiveButton("Search", (dialog, which) -> {
            String songTitle = editTextSongTitle.getText().toString().trim();
            String artist = editTextArtist.getText().toString().trim();
            searchYouTubeForSong(songTitle, artist); // Begin search
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Searches for a song on YouTube using the provided title and artist.
     * @param songTitle the title of the song to search for
     * @param artist the name of the artist for the song.
     */
    private void searchYouTubeForSong(String songTitle, String artist) {
        String myApiKey = "AIzaSyC6LZAxWYKscXYvUsLep2dZ_FEzB1jMceI";
        String query = songTitle + " " + artist;
        String url = "https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&q=" + query + "&key=" + myApiKey;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                //....
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = Objects.requireNonNull(response.body()).string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        String videoId = jsonObject.getJSONArray("items").getJSONObject(0).getJSONObject("id").getString("videoId");
                        // save song details along with videoId
                        saveSongToFirestore(songTitle, artist, videoId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * Saves a new song to the Firebase database.
     * The song is linked to the YouTube video using its videoId
     * @param songTitle the title of the song
     * @param artistName the name of the artist for the song
     * @param videoId the youtube video ID for the song's videos.
     */
    private void saveSongToFirestore(String songTitle, String artistName, String videoId) {
        Song newSong = new Song(songTitle, artistName, "https://www.youtube.com/watch?v=" + videoId);

        db.collection("users")
                .document(currentUserId)
                .collection("playlists")
                .document(playlistId)
                .collection("songs")
                .add(newSong)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Song was added successfully.");
                    Toast.makeText(SelfUserSongListActivity.this, "Song was added successfully", Toast.LENGTH_SHORT).show();
                    getSongsFromPlaylist(playlistId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to add song: " + e.getMessage());
                    Toast.makeText(SelfUserSongListActivity.this, "Failed to add song.", Toast.LENGTH_SHORT).show();
                });
    }
}
