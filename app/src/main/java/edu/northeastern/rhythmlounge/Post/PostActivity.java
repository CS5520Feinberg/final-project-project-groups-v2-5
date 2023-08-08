package edu.northeastern.rhythmlounge.Post;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    private EditText etTitle;
    private ImageView ivDialogPreview;
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

        if (posts.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
        }

        swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchPosts();
            swipeRefreshLayout.setRefreshing(false);
        });


        fabCreatePost.setOnClickListener(v -> showCreatePostDialog());

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
                        if (existingPost.getUserId().equals(post.getUserId())) {
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

                if (posts.isEmpty()) {
                    tvEmptyState.setVisibility(View.VISIBLE);
                } else {
                    tvEmptyState.setVisibility(View.GONE);
                }

            } else {
                Toast.makeText(PostActivity.this, "Error fetching posts.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void createPost(String title, String content, String imageUrl) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;
        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String username = documentSnapshot.getString("username");
                    Post newPost = new Post(currentUser.getUid(), username, title, content, imageUrl);
                    newPost.setThumbnailUrl(imageUrl);

                    db.collection("posts")
                            .add(newPost)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(PostActivity.this, "Post created!", Toast.LENGTH_SHORT).show();
                                newPost.setPostId(documentReference.getId());  // Set the post ID here
                                posts.add(0, newPost);
                                postAdapter.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> Toast.makeText(PostActivity.this, "Error creating post.", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(PostActivity.this, "Error fetching username.", Toast.LENGTH_SHORT).show());
    }

    @SuppressLint("SetTextI18n")
    private void showCreatePostDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Post");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(10, 10, 10, 10); // Padding for aesthetic appeal

        etTitle = new EditText(this);  // Use the class level etTitle
        etTitle.setHint("Post Title");
        layout.addView(etTitle);

        EditText etContent = new EditText(this);
        etContent.setHint("Write your post here...");
        layout.addView(etContent);

        Button btnUploadImage = new Button(this);
        btnUploadImage.setText("Upload Image");
        layout.addView(btnUploadImage);

        ivDialogPreview = new ImageView(this); // Use the class level ivDialogPreview
        ivDialogPreview.setVisibility(View.GONE); // Initially, it should be hidden
        layout.addView(ivDialogPreview);

        builder.setView(layout);

        btnUploadImage.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        builder.setPositiveButton("Post", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();
            if (!title.isEmpty() && !content.isEmpty()) {
                uploadImageAndCreatePost(title, content);
            } else {
                Toast.makeText(PostActivity.this, "Post title and content cannot be empty.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(d -> {
            imageUri = null;
            ivDialogPreview.setVisibility(View.GONE);
        });
        dialog.show();
    }


    private void uploadImageAndCreatePost(String title, String content) {
        if (imageUri != null) {
            StorageReference fileReference = storageRef.child(System.currentTimeMillis() + ".jpg");

            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Once the image is uploaded, retrieve its download URL
                        fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            createPost(title, content, imageUrl);
                        });
                    })
                    .addOnFailureListener(e -> Toast.makeText(PostActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            createPost(title, content, null); // If there's no image, create the post without an image
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if the result comes from DetailedPostActivity and indicates a successful deletion
        if (requestCode == REQUEST_CODE_DETAILED_POST_ACTIVITY && resultCode == RESULT_OK) {
            fetchPosts();
        }

        // Check if the result comes from picking an image
        else if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            if (etTitle != null && etTitle.isShown()) {
                Glide.with(this).load(imageUri).into(ivDialogPreview);
                ivDialogPreview.setVisibility(View.VISIBLE);
            }
        }
    }
}