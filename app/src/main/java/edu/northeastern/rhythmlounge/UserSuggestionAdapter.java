package edu.northeastern.rhythmlounge;

import android.content.Context;
import android.content.Intent;
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
 * UserSuggestionAdapter is a RecyclerView.Adapter that handles the display of user suggestions while searching.
 * It holds the user information and handles the population of the RecyclerView with user details.
 */
public class UserSuggestionAdapter extends RecyclerView.Adapter<UserSuggestionAdapter.ViewHolder> {
    private final List<User> users;
    private final List<String> userIds;
    private final Context context;

    /**
     * Constructs a new UserSuggestionAdapter.
     * @param context the context where the adapter is being used.
     * @param users the list of users to display
     * @param userIds the list of user IDs corresponding to the users
     */
    public UserSuggestionAdapter(Context context, List<User> users, List<String> userIds) {
        this.users = users;
        this.userIds = userIds;
        this.context = context;
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return the new ViewHolder
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.suggested_user_item, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
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
            Intent intent = new Intent(context, OtherUserPageActivity.class);
            intent.putExtra("USER_ID", userIds.get(position));
            context.startActivity(intent);
        });
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     * @return the total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return users.size();
    }

    /**
     * Viewholder class that holds the references to the views in each item.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView profileImage;
        final TextView usernameText;

        /**
         * Constructs a new ViewHolder.
         * @param view the item view.
         */
        public ViewHolder(View view) {
            super(view);
            profileImage = view.findViewById(R.id.userProfileImage);
            usernameText = view.findViewById(R.id.usernameText);
        }
    }
}
