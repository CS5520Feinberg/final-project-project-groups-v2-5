package edu.northeastern.rhythmlounge.Playlist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;
import java.util.Objects;

import edu.northeastern.rhythmlounge.R;

/**
 * Adapter to bind a user's own playlists to a RecyclerView.
 */
public class SelfUserPlaylistAdapter extends RecyclerView.Adapter<SelfUserPlaylistAdapter.PlaylistViewHolder> {

    private final List<DocumentSnapshot> playlistSnapshots;
    private final Context context;

    /**
     * Constructor for SelfUserPlaylistAdapter.
     * @param context           the activity or context invoking this adapter.
     * @param playlistSnapshots the list of DocumentSnapshots containing playlist data
     */
    public SelfUserPlaylistAdapter(Context context, List<DocumentSnapshot> playlistSnapshots) {
        this.playlistSnapshots = playlistSnapshots;
        this.context = context;
    }

    /**
     * Called when RecyclerView needs a ViewHolder
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     */
    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("SelfUserPlaylistAdapter", "onCreateViewHolder is called");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_playlist, parent, false);
        return new PlaylistViewHolder(view);
    }

    /**
     * Called by RecyclerView to display playlist data at the specified position.
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        Log.d("SelfUserPlaylistAdapter", "onBindViewHolder is called for position " + position);

        DocumentSnapshot playlistSnapshot = playlistSnapshots.get(position);

        Playlist clickedPlaylist = playlistSnapshot.toObject(Playlist.class);

        holder.playlistNameTextView.setText(Objects.requireNonNull(clickedPlaylist).getName());

        holder.itemView.setOnClickListener(v -> {
            String playlistId = playlistSnapshot.getId();
            Intent intent = new Intent(context, SelfUserSongListActivity.class);
            intent.putExtra("playlistId", playlistId);
            context.startActivity(intent);
            Log.d("SelfUserPlaylistAdapter", "SongListActivity has been started with playlistId " + playlistId + intent);
        });
    }

    /**
     * Returns the total number of playlists
     * @return the number of playlists available.
     */
    @Override
    public int getItemCount() {
        Log.d("SelfUserPlaylistAdapter", "getItemCount is called. The count is: " + playlistSnapshots.size());
        return playlistSnapshots.size();
    }

    /**
     * Refresh the data in the adapter with a new list of DocumentSnapshots.
     * @param freshPlaylists the new list of DocumentSnapshots.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void refreshData(List<DocumentSnapshot> freshPlaylists) {
        Log.d("SelfUserPlaylistAdapter", "refreshData is called. The freshPlaylists size is: " + freshPlaylists.size());
        playlistSnapshots.clear();
        playlistSnapshots.addAll(freshPlaylists);
        notifyDataSetChanged();
    }

    /**
     * Viewholder to hold the view items for playlists.
     */
    public static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        final TextView playlistNameTextView;

        public PlaylistViewHolder(View view) {
            super(view);
            playlistNameTextView = view.findViewById(R.id.playlistNameText);
            Log.d("SelfUserPlaylistViewHolder", "PlaylistViewHolder has been initialized");
        }
    }
}
