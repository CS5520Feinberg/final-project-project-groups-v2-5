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
 * This adapter populates items for displaying other user's playlists via RecyclerView.
 */
public class OtherUserPlaylistAdapter extends RecyclerView.Adapter<OtherUserPlaylistAdapter.PlaylistViewHolder> {

    private final List<DocumentSnapshot> playlistSnapshots;
    private final Context context;
    private final String otherUserId;

    /**
     * Constructor for OtherUserPlaylistAdapter.
     * @param context           The context where the adapter is used.
     * @param playlistSnapshots The list of playlists to be displayed.
     * @param otherUserId       The ID of the user whose playlists are being displayed.
     */
    public OtherUserPlaylistAdapter(Context context, List<DocumentSnapshot> playlistSnapshots, String otherUserId) {
        this.playlistSnapshots = playlistSnapshots;
        this.context = context;
        this.otherUserId = otherUserId;
    }

    /**
     * Inflates each playlist item.
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     * @return a new playlist viewholder
     */
    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("OtherPlaylistAdapter", "onCreateViewHolder is called");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_playlist, parent, false);
        return new PlaylistViewHolder(view);
    }

    /**
     * Binds the data to the views for each item.
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        Log.d("PlaylistAdapter", "onBindViewHolder is called for position " + position);

        DocumentSnapshot playlistSnapshot = playlistSnapshots.get(position);

        Playlist clickedPlaylist = playlistSnapshot.toObject(Playlist.class);

        holder.playlistNameTextView.setText(Objects.requireNonNull(clickedPlaylist).getName());

        holder.itemView.setOnClickListener(v -> {
            String playlistId = playlistSnapshot.getId();
            Intent intent = new Intent(context, OtherUserSongListActivity.class);
            intent.putExtra("playlistId", playlistId);
            intent.putExtra("otherUserId", otherUserId);
            context.startActivity(intent);
            Log.d("OtherPlaylistAdapter", "SongListActivity has been started with playlistId " + playlistId + intent);
        });
    }

    /**
     * Provides the total number of items.
     */
    @Override
    public int getItemCount() {
        Log.d("OtherPlaylistAdapter", "getItemCount is called. The count is: " + playlistSnapshots.size());
        return playlistSnapshots.size();
    }

    /**
     * Updates the data in the adapter with a fresh data set.
     * @param freshPlaylists the updated list of playlists
     */
    @SuppressLint("NotifyDataSetChanged")
    public void refreshData(List<DocumentSnapshot> freshPlaylists) {
        Log.d("OtherPlaylistAdapter", "refreshData is called. The freshPlaylists size is: " + freshPlaylists.size());
        playlistSnapshots.clear();
        playlistSnapshots.addAll(freshPlaylists);
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class that represents each playlist item.
     */
    public static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        final TextView playlistNameTextView;

        public PlaylistViewHolder(View view) {
            super(view);
            playlistNameTextView = view.findViewById(R.id.playlistNameText);
            Log.d("OtherPlaylistViewHolder", "PlaylistViewHolder has been initialized");
        }
    }
}
