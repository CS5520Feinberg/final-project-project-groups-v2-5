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

public class UserSuggestionAdapter extends RecyclerView.Adapter<UserSuggestionAdapter.ViewHolder> {
    private final List<User> users;
    private final Context context;

    public UserSuggestionAdapter(Context context, List<User> users) {
        this.users = users;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.suggested_user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        holder.usernameText.setText(user.getUsername());
        Glide.with(context).load(user.getProfilePictureUrl()).into(holder.profileImage);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, OtherUserPageActivity.class);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

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
