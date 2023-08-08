package edu.northeastern.rhythmlounge.Post;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import edu.northeastern.rhythmlounge.R;
import edu.northeastern.rhythmlounge.User;

public class DetailedPostActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private TextView usernameTextView;
    private TextView contentTextView;
    private Button deleteButton;
    private String postId;
    private ImageView postImageView;
    private TextView titleTextView;
    private ImageView userProfileImageView;
    private ImageButton likeButton;
    private TextView likeCountTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_post);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        usernameTextView = findViewById(R.id.tv_username);
        titleTextView = findViewById(R.id.tv_title);
        contentTextView = findViewById(R.id.tv_content);
        deleteButton = findViewById(R.id.btn_delete);
        userProfileImageView = findViewById(R.id.iv_user_profile_image);
        likeButton = findViewById(R.id.btn_like);
        likeCountTextView = findViewById(R.id.tv_like_count);

        // Get post ID from intent
        postId = getIntent().getStringExtra("POST_ID");
        //preview image
        postImageView = findViewById(R.id.iv_post_image);

        if (postId != null) {
            fetchPostDetails();
        } else {
            Toast.makeText(this, "Error loading post details", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchPostDetails() {
        db.collection("posts").document(postId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Post post = documentSnapshot.toObject(Post.class);

                    if (post != null) {
                        post.setPostId(documentSnapshot.getId());
                        postImageView.setOnClickListener(v -> showFullScreenImage(post.getImageUrl()));
                        Glide.with(this).load(post.getImageUrl()).into(postImageView);

                        usernameTextView.setText(post.getUsername());
                        contentTextView.setText(post.getContent());
                        titleTextView.setText(post.getTitle());

                        // Fetch the profile picture for this post's user
                        db.collection("users").document(post.getUserId()).get()
                                .addOnSuccessListener(userDocument -> {
                                    User user = userDocument.toObject(User.class);
                                    assert user != null;
                                    String profilePicUrl = user.getProfilePictureUrl();
                                    if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                                        Glide.with(this)
                                                .load(profilePicUrl)
                                                .into(userProfileImageView);
                                    } else {
                                        // Load a default placeholder image if the user hasn't uploaded a profile picture
                                        userProfileImageView.setImageResource(R.drawable.avatar);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                });

                        // Setting the initial state of the like button and like count
                        if(post.getLikedByUsers() != null && post.getLikedByUsers().contains(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())) {
                            likeButton.setImageResource(R.drawable.ic_liked);
                        } else {
                            likeButton.setImageResource(R.drawable.ic_like);
                        }
                        likeCountTextView.setText(String.valueOf(post.getLikeCount()));

                        likeButton.setOnClickListener(v -> {
                            if(post.getLikedByUsers().contains(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())) {
                                post.removeLike(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                likeButton.setImageResource(R.drawable.ic_like);
                            } else {
                                post.addLike(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                likeButton.setImageResource(R.drawable.ic_liked);
                            }
                            likeCountTextView.setText(String.valueOf(post.getLikeCount()));
                            // Update post in Firestore
                            updatePostInFirestore(post);
                        });


                        // Check if post has an image URL and set visibility
                        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
                            Glide.with(this).load(post.getImageUrl()).into(postImageView);
                            postImageView.setVisibility(View.VISIBLE);
                        } else {
                            postImageView.setVisibility(View.GONE);
                        }

                        // Allow post creators to delete their posts
                        if (post.getUserId().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())) {
                            deleteButton.setVisibility(View.VISIBLE);
                            deleteButton.setOnClickListener(v -> deletePost(postId));
                        }
                    } else {
                        Toast.makeText(this, "Error loading post details", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading post details", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void deletePost(String postId) {
        db.collection("posts").document(postId).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(DetailedPostActivity.this, "Post deleted successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK); // Indicate that the deletion was successful
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(DetailedPostActivity.this, "Error deleting post", Toast.LENGTH_SHORT).show());
    }

    private void showFullScreenImage(String imageUrl) {
        Dialog fullScreenDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        fullScreenDialog.setContentView(R.layout.dialog_fullscreen_image);

        ImageView fullscreenImageView = fullScreenDialog.findViewById(R.id.fullscreen_image);
        ImageButton closeButton = fullScreenDialog.findViewById(R.id.close_button);

        Glide.with(this)
                .load(imageUrl)
                .into(fullscreenImageView);

        closeButton.setOnClickListener(v -> fullScreenDialog.dismiss());

        fullScreenDialog.show();
    }

    private void updatePostInFirestore(Post post) {
        if (post.getPostId() == null) {
            Toast.makeText(this, "Error: Post ID is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("posts").document(post.getPostId())
                .update(
                        "likeCount", post.getLikeCount(),
                        "likedByUsers", post.getLikedByUsers()
                )
                .addOnSuccessListener(aVoid -> {
                    // Post was successfully updated in Firestore
                    Toast.makeText(this, "Liked the post!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error liking the post", Toast.LENGTH_SHORT).show());
    }
}
