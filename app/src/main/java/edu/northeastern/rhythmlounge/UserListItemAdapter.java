package edu.northeastern.rhythmlounge;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * This Adapter helps populate a RecyclerView with user items.
 * Each item displays a user's profile image and username, and clicking an item opens the user's profile page.
 */
public class UserListItemAdapter extends RecyclerView.Adapter<UserListItemAdapter.ViewHolder> {
    private final List<User> users;
    private final List<String> userIds;
    private final Context context;

    /**
     * Constructs a new UserListItemAdapter with the given context, list of users, and list of user IDs.
     *
     * @param context the context in which the adapter is operating.
     * @param users the list of User objects.
     * @param userIds the list of user IDs.
     */
    public UserListItemAdapter(Context context, List<User> users, List<String> userIds) {
        this.users = users;
        this.userIds = userIds;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds the User object at the current position to the ViewHolder, displaying the username and profile image.
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String userId = userIds.get(position);
        Log.d("UserListItemAdapter", "Position: " + position + ", UserID: " + userId);

        User user = users.get(position);
        holder.usernameText.setText(user.getUsername());

        // Check if a profile picture URL is available and use a default picture if not.
        if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
            Glide.with(context)
                    .load(user.getProfilePictureUrl())
                    .into(holder.profileImage);
        } else {
            Glide.with(context)
                    .load(R.drawable.defaultprofilepicture)
                    .into(holder.profileImage);
        }

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                Intent intent = new Intent(context, OtherUserPageActivity.class);
                intent.putExtra("USER_ID", userIds.get(pos));
                context.startActivity(intent);
            }
        });
    }

    /**
     * Returns the total number of items in the list of users.
     * @return
     */
    @Override
    public int getItemCount() {
        return users.size();
    }

    /**
     * ViewHolder class to hold the views for the user profile image and username.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView profileImage;
        final TextView usernameText;

        public ViewHolder(View view) {
            super(view);
            profileImage = view.findViewById(R.id.userProfileImage);
            usernameText = view.findViewById(R.id.usernameText);
        }
    }
}
