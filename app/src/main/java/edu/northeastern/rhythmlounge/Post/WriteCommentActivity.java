package edu.northeastern.rhythmlounge.Post;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Date;
import java.util.Objects;

import edu.northeastern.rhythmlounge.R;

public class WriteCommentActivity extends AppCompatActivity {

    private EditText commentEditText;
    private String postId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_comment);

        commentEditText = findViewById(R.id.comment_edit_text);
        Button postButton = findViewById(R.id.post_button);

        postId = getIntent().getStringExtra("POST_ID");
        Log.d("DEBUG", "Post ID in WriteCommentActivity: " + postId);
        db = FirebaseFirestore.getInstance();

        postButton.setOnClickListener(v -> postComment());
    }

    private void postComment() {
        String content = commentEditText.getText().toString().trim();

        if (content.isEmpty()) {
            Toast.makeText(this, "Please write a comment", Toast.LENGTH_SHORT).show();
            return;
        }

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setTimestamp(new Date());
        comment.setUserId(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

        db.collection("posts").document(postId).collection("comments")
                .add(comment)
                .addOnSuccessListener(documentReference -> {
                    comment.setCommentId(documentReference.getId());
                    Toast.makeText(this, "Comment posted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error posting comment", Toast.LENGTH_SHORT).show());
    }
}
