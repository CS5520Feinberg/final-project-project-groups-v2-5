package edu.northeastern.rhythmlounge.Posts;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.northeastern.rhythmlounge.R;

public class PostActivity extends AppCompatActivity {

    private List<Post> posts = new ArrayList<>();
    private PostAdapter postAdapter;
    RecyclerView rvPosts;
    FloatingActionButton fabCreatePost;
    SwipeRefreshLayout swipeRefreshLayout;
    TextView tvEmptyState;
    private Date latestTimestamp = null;
    private FirebaseFirestore db;
    private static final int PICK_IMAGE_REQUEST = 1;
    public static final int REQUEST_CODE_DETAILED_POST_ACTIVITY = 100;  // Defining the request code
    private Uri imageUri;
    private final StorageReference storageRef = FirebaseStorage.getInstance().getReference("uploads");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        rvPosts = findViewById(R.id.rv_posts);
        fabCreatePost = findViewById(R.id.fab_create_post);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        tvEmptyState = findViewById(R.id.tv_empty_state);

        posts = new ArrayList<>();
        postAdapter = new PostAdapter(posts, this);

        rvPosts.setLayoutManager(new LinearLayoutManager(this));
        rvPosts.setAdapter(postAdapter);

        updateEmptyStateVisibility();

        // Refresh the page
        swipeRefreshLayout.setOnRefreshListener(this::fetchPosts);

        fabCreatePost.setOnClickListener(v -> {
            Intent intent = new Intent(PostActivity.this, CreatePostActivity.class);
            startActivity(intent);
        });

        db = FirebaseFirestore.getInstance();
        fetchPosts();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchPosts() {
        posts.clear(); // Clear the existing list first.
        latestTimestamp = null; // Reset the timestamp.
        postAdapter.notifyDataSetChanged(); // Notify the adapter about the cleared list.

        Query query = db.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING) // Order by most recent
                .limit(50);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Post> newPosts = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Post post = document.toObject(Post.class);
                    post.setPostId(document.getId());
                    post.setThumbnailUrl(document.getString("thumbnailUrl"));

                    // Check if this post is not already in the list
                    boolean isDuplicate = false;
                    for (Post existingPost : posts) {
                        if (existingPost.getPostId().equals(post.getPostId())) {
                            isDuplicate = true;
                            break;
                        }
                    }

                    if (!isDuplicate) {
                        newPosts.add(post);
                    }

                    // Update the latestTimestamp
                    Date postTimestamp = post.getTimestamp();
                    if (latestTimestamp == null || postTimestamp.after(latestTimestamp)) {
                        latestTimestamp = postTimestamp;
                    }
                }
                posts.addAll(0, newPosts);  // Add the new posts to the beginning of the list
                postAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);

                updateEmptyStateVisibility();

            } else {
                Toast.makeText(PostActivity.this, "Failed to fetch posts. Please try again later.", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if the result comes from DetailedPostActivity and indicates a successful deletion
        if (requestCode == REQUEST_CODE_DETAILED_POST_ACTIVITY && resultCode == RESULT_OK) {
            fetchPosts();
        }
    }

    private void updateEmptyStateVisibility() {
        if (posts.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
        }
    }

}