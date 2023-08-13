package edu.northeastern.rhythmlounge.Posts;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import edu.northeastern.rhythmlounge.R;

public class CreatePostActivity extends AppCompatActivity {

    private EditText etTitle, etContent;
    private ImageView ivDialogPreview;
    private Uri imageUri;
    private final StorageReference storageRef = FirebaseStorage.getInstance().getReference("uploads");
    private FirebaseFirestore db;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        ivDialogPreview = findViewById(R.id.ivDialogPreview);
        Button btnUploadImage = findViewById(R.id.btnUploadImage);
        Button btnPost = findViewById(R.id.btnPost);
        db = FirebaseFirestore.getInstance();

        btnUploadImage.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        btnPost.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();
            if (!title.isEmpty() && !content.isEmpty()) {
                uploadImageAndCreatePost(title, content);
            } else {
                Toast.makeText(CreatePostActivity.this, "Post title and content cannot be empty.", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(CreatePostActivity.this, "Post created!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(CreatePostActivity.this, "Error creating post.", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(CreatePostActivity.this, "Error fetching username.", Toast.LENGTH_SHORT).show());
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
                    .addOnFailureListener(e -> Toast.makeText(CreatePostActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            createPost(title, content, null); // If there's no image, create the post without an image
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if the result comes from picking an image
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(ivDialogPreview);
            ivDialogPreview.setVisibility(View.VISIBLE);
        }
    }
}
