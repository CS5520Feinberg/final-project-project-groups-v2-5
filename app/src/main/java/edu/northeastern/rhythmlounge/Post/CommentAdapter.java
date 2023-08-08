package edu.northeastern.rhythmlounge.Post;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import edu.northeastern.rhythmlounge.R;
import edu.northeastern.rhythmlounge.User;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> comments;
    private final Context context;

    public CommentAdapter(List<Comment> comments, Context context) {
        this.comments = comments;
        this.context = context;
    }


    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.commentTextView.setText(comment.getContent());

        // Format the date to "time ago" format
        String timeAgo = getTimeAgo(comment.getTimestamp());
        holder.timeAgoTextView.setText(timeAgo);

        // By default, hide the delete button
        holder.deleteButton.setVisibility(View.GONE);

        // Fetch the user's details from Firestore.
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(comment.getUserId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        assert user != null;
                        holder.usernameTextView.setText(user.getUsername());

                        // Show the delete button if the comment belongs to the currently logged-in user
                        // (assuming you have the logged-in user's ID stored somewhere, e.g., in a variable called currentUserId)
                        if (comment.getUserId().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())) {
                            holder.deleteButton.setVisibility(View.VISIBLE);
                            holder.deleteButton.setOnClickListener(v -> {
                                // Implement the delete action
                                deleteComment(comment.getPostId(), comment.getCommentId());
                            });
                        }

                        // Load the profile image.
                        Glide.with(holder.itemView.getContext())
                                .load(user.getProfilePictureUrl())
                                .placeholder(R.drawable.avatar)
                                .into(holder.profileImageView);
                    }
                    Log.d("CommentAdapter", "CommentID: " + comment.getCommentId() + ", PostID: " + comment.getPostId());
                })
                .addOnFailureListener(e -> Log.e("CommentAdapter", "Error fetching user details: " + e.getMessage()));
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView;
        TextView commentTextView;
        TextView timeAgoTextView;
        ImageView profileImageView;
        ImageButton deleteButton;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.comment_username);
            commentTextView = itemView.findViewById(R.id.comment_content);
            profileImageView = itemView.findViewById(R.id.comment_profile_image);
            timeAgoTextView = itemView.findViewById(R.id.comment_time_ago);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }

    public void setComments(List<Comment> newComments) {
        this.comments = newComments;
    }

    private String getTimeAgo(Date date) {
        if (date == null) return "";

        long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - date.getTime();

        if (timeDifference < 60 * 1000) {
            return "just now";
        } else if (timeDifference < 60 * 60 * 1000) {
            int mins = (int) (timeDifference / (60 * 1000));
            return mins + "m ago";
        } else if (timeDifference < 24 * 60 * 60 * 1000) {
            int hours = (int) (timeDifference / (60 * 60 * 1000));
            return hours + "h ago";
        } else {
            int days = (int) (timeDifference / (24 * 60 * 60 * 1000));
            return days + "d ago";
        }
    }

    private void deleteComment(String postId, String commentId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (postId == null || commentId == null) {
            Toast.makeText(context, "Error: Post ID or Comment ID is null.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("posts").document(postId).collection("comments").document(commentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Comment deleted successfully
                    int indexToRemove = -1;
                    for (int i = 0; i < comments.size(); i++) {
                        if (comments.get(i).getCommentId().equals(commentId)) {
                            indexToRemove = i;
                            break;
                        }
                    }
                    if (indexToRemove != -1) {
                        comments.remove(indexToRemove);
                        notifyItemRemoved(indexToRemove);
                    }
                    Toast.makeText(context, "Comment deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Handle the error
                    Toast.makeText(context, "Error deleting comment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}