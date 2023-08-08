package edu.northeastern.rhythmlounge.Playlist;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

import edu.northeastern.rhythmlounge.R;

/**
 * Adapter to bind song data to a RecyclerView for display.
 */
public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {
    private final Context context;
    private final List<DocumentSnapshot> songSnapshots;

    /**
     * Constructor for SongAdapter.
     * @param context       the activity or context invoking this adapter.
     * @param songSnapshots the list of DocumentSnapshots containing song data.
     */
    public SongAdapter(Context context, List<DocumentSnapshot> songSnapshots) {
        this.context = context;
        this.songSnapshots = songSnapshots;
    }

    /**
     * Called when RecyclerView needs a new ViewHolder.
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Called by the RecyclerView to display song data at the specified position.
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DocumentSnapshot songSnapshot = songSnapshots.get(position);
        Song song = songSnapshot.toObject(Song.class);
        Log.d("SongAdapter", "Binding song to holder: " + song.getTitle() + " by " + song.getArtist());
        holder.bind(song);
    }

    /**
     * Returns the total number of songs.
     * @return the number of songs available.
     */
    @Override
    public int getItemCount() {
        Log.d("SongAdapter", "Total number of songs: " + songSnapshots.size());
        return songSnapshots.size();
    }

    /**
     * ViewHolder class to hold the views for the song data/
     */
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView songTitleTextView;
        TextView songArtistTextView;

        ViewHolder(@NonNull View view) {
            super(view);
            songTitleTextView = itemView.findViewById(R.id.songTitleText);
            songArtistTextView = itemView.findViewById(R.id.songArtistText);
        }

        /**
         * Binds song data to the view elements.
         * @param song
         */
        void bind(Song song) {
            Log.d("SongAdapter", "Setting text views for song: " + song.getTitle() + " by " + song.getArtist());
            songTitleTextView.setText(song.getTitle());
            songArtistTextView.setText(song.getArtist());

            itemView.setOnClickListener(v -> {
                String url = song.getYoutubeLink();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                context.startActivity(intent);
            });
        }
    }
}
