package edu.northeastern.rhythmlounge.Posts;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.northeastern.rhythmlounge.R;

public class PostActivity extends AppCompatActivity {

    private List<Post> posts = new ArrayList<>();
    private PostAdapter postAdapter;
    RecyclerView rvPosts;
    FloatingActionButton fabCreatePost;
    FloatingActionButton fabRefresh;
    SwipeRefreshLayout swipeRefreshLayout;
    TextView tvEmptyState;
    private Date latestTimestamp = null;
    private FirebaseFirestore db;
    public static final int REQUEST_CODE_DETAILED_POST_ACTIVITY = 100;  // Defining the request code
    private static final int PAGE_SIZE = 10; // Amount of posts loaded in a single batch
    private DocumentSnapshot lastVisiblePost; // Keeps track of the last post we fetched
    private boolean isLastPage = false; // Helps to know if there are no more posts to fetch
    private boolean isLoading = false; // Helps to ensure multiple fetch requests aren't happening at the same time


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        rvPosts = findViewById(R.id.rv_posts);
        fabCreatePost = findViewById(R.id.fab_create_post);
        fabRefresh = findViewById(R.id.fab_refresh);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        tvEmptyState = findViewById(R.id.tv_empty_state);

        posts = new ArrayList<>();
        postAdapter = new PostAdapter(posts, this);

        rvPosts.setLayoutManager(new LinearLayoutManager(this));
        rvPosts.setAdapter(postAdapter);

        updateEmptyStateVisibility();

        // Refresh the page
        swipeRefreshLayout.setOnRefreshListener(() -> {
            resetPagination();
            fetchPosts();
        });

        fabCreatePost.setOnClickListener(v -> {
            Intent intent = new Intent(PostActivity.this, CreatePostActivity.class);
            startActivity(intent);
        });

        fabRefresh.setOnClickListener(v -> {
            // Reset pagination and fetch posts from the beginning
            resetPagination();
            fetchPosts();
        });

        db = FirebaseFirestore.getInstance();
        setupScrollListener();
        fetchPosts();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchPosts() {
        if (!isLoading) { // Only fetch new posts if it's not currently loading
            isLoading = true;

            Query query = db.collection("posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(PAGE_SIZE);

            if (lastVisiblePost != null) {
                query = query.startAfter(lastVisiblePost);
            }

            query.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<Post> newPosts = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Post post = document.toObject(Post.class);
                        post.setPostId(document.getId());
                        newPosts.add(post);
                    }

                    // Check for last item and set the lastVisiblePost
                    if (newPosts.size() > 0) {
                        lastVisiblePost = task.getResult().getDocuments().get(task.getResult().size() - 1);
                        posts.addAll(newPosts);
                    } else {
                        isLastPage = true;
                    }

                    postAdapter.notifyDataSetChanged();

                } else {
                    Toast.makeText(PostActivity.this, "Failed to fetch posts. Please try again later.", Toast.LENGTH_SHORT).show();
                }

                swipeRefreshLayout.setRefreshing(false);
                isLoading = false;
                updateEmptyStateVisibility();
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if the result comes from DetailedPostActivity and indicates a successful deletion
        if (requestCode == REQUEST_CODE_DETAILED_POST_ACTIVITY && resultCode == RESULT_OK) {
            resetPagination();
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

    private void setupScrollListener() {
        RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int totalItemCount = 0;
                if (layoutManager != null) {
                    totalItemCount = layoutManager.getItemCount();
                }
                int lastVisibleItem = 0;
                if (layoutManager != null) {
                    lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                }

                if (!isLoading && !isLastPage) {
                    if (lastVisibleItem + 1 >= totalItemCount) {
                        // End has been reached, fetch more posts
                        fetchPosts();
                    }
                }
            }
        };
        rvPosts.addOnScrollListener(onScrollListener);
    }

    private void resetPagination() {
        lastVisiblePost = null;
        isLastPage = false;
        isLoading = false;
        posts.clear();
    }
}