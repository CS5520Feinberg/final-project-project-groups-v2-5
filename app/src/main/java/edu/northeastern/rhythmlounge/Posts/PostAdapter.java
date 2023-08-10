package edu.northeastern.rhythmlounge.Posts;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.firestore.FirebaseFirestore;

import edu.northeastern.rhythmlounge.R;
import edu.northeastern.rhythmlounge.User;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private final List<Post> posts;
    private final Context context;

    public PostAdapter(List<Post> posts, Context context) {
        this.posts = posts;
        this.context = context;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.post_item, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.usernameTextView.setText(post.getUsername());
        holder.titleTextView.setText(post.getTitle());
        holder.likeCountTextView.setText(String.valueOf(post.getLikeCount()));
        holder.commentCountTextView.setText(String.valueOf(post.getCommentCount()));

        // Format the date
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
        holder.dateTextView.setText(sdf.format(post.getTimestamp()));
        holder.likeCountTextView.setText(String.valueOf(post.getLikeCount()));

        // Fetch the profile picture URL for this post's user
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(post.getUserId()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    assert user != null;
                    String profilePicUrl = user.getProfilePictureUrl();
                    if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                        Glide.with(context)
                                .load(profilePicUrl)
                                .into(holder.userProfilePicImageView);
                    } else {
                        // Load a default placeholder image if the user hasn't uploaded a profile picture
                        holder.userProfilePicImageView.setImageResource(R.drawable.avatar);
                    }
                })
                .addOnFailureListener(e -> {
                });


        String imageUrlToShow = post.getThumbnailUrl();
        if (imageUrlToShow == null || imageUrlToShow.isEmpty()) {
            imageUrlToShow = post.getImageUrl(); // Use imageUrl as a fallback
        }

        if (imageUrlToShow != null && !imageUrlToShow.isEmpty()) {
            // Set gray background when the image is loading
            holder.thumbnailImageView.setBackgroundColor(ContextCompat.getColor(context, R.color.Background));

            Glide.with(context)
                    .load(imageUrlToShow) // Use the determined URL
                    .placeholder(R.drawable.logo)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Log.e("GlideError", "Load failed", e);
                            holder.thumbnailImageView.setBackground(null); // Clear the background on error
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            holder.thumbnailImageView.setBackground(null); // Clear the background once image is loaded
                            return false;
                        }
                    })
                    .into(holder.thumbnailImageView);

            holder.thumbnailImageView.setVisibility(View.VISIBLE);
        } else {
            holder.thumbnailImageView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView userProfilePicImageView;
        TextView usernameTextView;
        ImageView thumbnailImageView;
        TextView titleTextView;
        ImageView likeIconImageView;
        TextView likeCountTextView;
        ImageView commentIconImageView;
        TextView commentCountTextView;
        TextView dateTextView;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.tv_username);
            titleTextView = itemView.findViewById(R.id.tv_title);
            thumbnailImageView = itemView.findViewById(R.id.iv_thumbnail);
            userProfilePicImageView = itemView.findViewById(R.id.iv_user_profile_picture);
            likeIconImageView = itemView.findViewById(R.id.iv_liked_icon);
            likeCountTextView = itemView.findViewById(R.id.tv_liked_count);
            commentIconImageView = itemView.findViewById(R.id.iv_comment_icon);
            commentCountTextView = itemView.findViewById(R.id.tv_comment_count);
            dateTextView = itemView.findViewById(R.id.tv_date);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Post clickedPost = posts.get(position);
                    Intent intent = new Intent(context, DetailedPostActivity.class);
                    intent.putExtra("POST_ID", clickedPost.getPostId());
                    ((Activity) context).startActivityForResult(intent, PostActivity.REQUEST_CODE_DETAILED_POST_ACTIVITY);
                }
            });
        }
    }
}

