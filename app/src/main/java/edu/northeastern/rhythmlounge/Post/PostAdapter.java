package edu.northeastern.rhythmlounge.Post;

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

import java.util.List;
import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import edu.northeastern.rhythmlounge.R;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> posts;
    private Context context;

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
                            return false; // let Glide handle the error
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            holder.thumbnailImageView.setBackground(null); // Clear the background once image is loaded
                            return false; // let Glide handle the setting of the image resource
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
        TextView usernameTextView;
        //TextView contentTextView;
        ImageView thumbnailImageView;
        TextView titleTextView;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.tv_username);
            titleTextView = itemView.findViewById(R.id.tv_title);
            thumbnailImageView = itemView.findViewById(R.id.iv_thumbnail);

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

