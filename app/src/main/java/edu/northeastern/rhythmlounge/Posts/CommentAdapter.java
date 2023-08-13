package edu.northeastern.rhythmlounge.Posts;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import edu.northeastern.rhythmlounge.OtherUserPageActivity;
import edu.northeastern.rhythmlounge.R;
import edu.northeastern.rhythmlounge.User;
import edu.northeastern.rhythmlounge.UserProfileActivity;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private final List<Comment> comments;
    private final Context context;
    private final Map<String, User> userCache = new HashMap<>();

    public CommentAdapter(List<Comment> comments, Context context) {
        this.comments = comments;
        this.context = context;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(view, this);
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

        User cachedUser = userCache.get(comment.getUserId());
        if (cachedUser != null) {
            holder.usernameTextView.setText(cachedUser.getUsername());
            Glide.with(holder.itemView.getContext())
                    .load(cachedUser.getProfilePictureUrl())
                    .placeholder(R.drawable.avatar)
                    .into(holder.profileImageView);
        } else {
            // Fetch the user's details from Firestore.
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(comment.getUserId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            assert user != null;
                            holder.usernameTextView.setText(user.getUsername());

                            if (comment.getUserId().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())) {
                                holder.deleteButton.setVisibility(View.VISIBLE);
                                holder.deleteButton.setOnClickListener(v -> deleteComment(comment.getPostId(), comment.getCommentId()));
                            }

                            // Load the profile image.
                            Glide.with(holder.itemView.getContext())
                                    .load(user.getProfilePictureUrl())
                                    .placeholder(R.drawable.avatar)
                                    .into(holder.profileImageView);
                        }
                        User user = documentSnapshot.toObject(User.class);
                        userCache.put(comment.getUserId(), user);

                        holder.profileImageView.setOnClickListener(v -> goToUserProfile(comment.getUserId()));

                        holder.usernameTextView.setOnClickListener(v -> goToUserProfile(comment.getUserId()));

                        Log.d("CommentAdapter", "CommentID: " + comment.getCommentId() + ", PostID: " + comment.getPostId());
                    })
                    .addOnFailureListener(e -> Log.e("CommentAdapter", "Error fetching user details: " + e.getMessage()));
            }
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

        public CommentViewHolder(@NonNull View itemView, final CommentAdapter adapter) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.comment_username);
            commentTextView = itemView.findViewById(R.id.comment_content);
            profileImageView = itemView.findViewById(R.id.comment_profile_image);
            timeAgoTextView = itemView.findViewById(R.id.comment_time_ago);
            deleteButton = itemView.findViewById(R.id.delete_button);

            deleteButton.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Comment currentComment = adapter.comments.get(position);
                    adapter.deleteComment(currentComment.getPostId(), currentComment.getCommentId());
                }
            });

            profileImageView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Comment currentComment = adapter.comments.get(position);
                    adapter.goToUserProfile(currentComment.getUserId());
                }
            });

            usernameTextView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Comment currentComment = adapter.comments.get(position);
                    adapter.goToUserProfile(currentComment.getUserId());
                }
            });
        }
    }

    private static class CommentDiffCallback extends DiffUtil.Callback {
        private final List<Comment> oldComments;
        private final List<Comment> newComments;

        public CommentDiffCallback(List<Comment> oldComments, List<Comment> newComments) {
            this.oldComments = oldComments;
            this.newComments = newComments;
        }

        @Override
        public int getOldListSize() {
            return oldComments.size();
        }

        @Override
        public int getNewListSize() {
            return newComments.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldComments.get(oldItemPosition).getCommentId().equals(newComments.get(newItemPosition).getCommentId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldComments.get(oldItemPosition).equals(newComments.get(newItemPosition));
        }
    }

    public void setComments(List<Comment> newComments) {
        CommentDiffCallback diffCallback = new CommentDiffCallback(this.comments, newComments);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        this.comments.clear();
        this.comments.addAll(newComments);
        diffResult.dispatchUpdatesTo(this);
    }


    private String getTimeAgo(Date date) {
        if (date == null) return "";

        long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - date.getTime();

        if (timeDifference < 60 * 1000) {
            return "just now";
        } else if (timeDifference < 60 * 60 * 1000) {
            int minutes = (int) (timeDifference / (60 * 1000));
            return minutes + "m ago";
        } else if (timeDifference < 24 * 60 * 60 * 1000) {
            int hours = (int) (timeDifference / (60 * 60 * 1000));
            return hours + "h ago";
        } else {
            int days = (int) (timeDifference / (24 * 60 * 60 * 1000));
            return days + "d ago";
        }
    }

    private void deleteComment(String postId, String commentId) {
        // Create an AlertDialog
        new AlertDialog.Builder(context)
                .setTitle("Delete Comment")
                .setMessage("Are you sure you want to delete this comment?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // The actual delete logic
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
                                decrementPostCommentCount(postId);
                            })
                            .addOnFailureListener(e -> Toast.makeText(context, "Error deleting comment: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("No", null)  // No action for "No"
                .show();
    }

    private void decrementPostCommentCount(String postId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference postRef = db.collection("posts").document(postId);

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot snapshot = transaction.get(postRef);

            // Get the current comment count
                    Long count = snapshot.getLong("commentCount");
                    long currentCount = (count != null) ? count : 0;
                    if (currentCount > 0) {
                transaction.update(postRef, "commentCount", currentCount - 1);
            }
            return null;
        })
                .addOnSuccessListener(aVoid -> Log.d("CommentAdapter", "Comment count decremented!"))
                .addOnFailureListener(e -> Log.e("CommentAdapter", "Error decrementing comment count", e));
    }

    private void goToUserProfile(String userId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && userId.equals(currentUser.getUid())) {
            // Navigate to SelfUserPageFragment through UserProfileActivity
            Intent intent = new Intent(context, UserProfileActivity.class);
            context.startActivity(intent);
        } else {
            // Navigate to other user's page
            Intent intent = new Intent(context, OtherUserPageActivity.class);
            intent.putExtra("USER_ID", userId);
            context.startActivity(intent);
        }
    }
}