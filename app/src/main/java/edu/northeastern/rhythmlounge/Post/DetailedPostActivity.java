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

public class DetailedPostActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private TextView usernameTextView;
    private TextView contentTextView;
    private Button deleteButton;
    private String postId;
    private ImageView postImageView;
    private TextView titleTextView;


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
                        postImageView.setOnClickListener(v -> showFullScreenImage(post.getImageUrl()));
                        Glide.with(this).load(post.getImageUrl()).into(postImageView);

                        usernameTextView.setText(post.getUsername());
                        contentTextView.setText(post.getContent());
                        titleTextView.setText(post.getTitle());

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

}
