package edu.northeastern.rhythmlounge;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines a RhythmLounge user, which has a username, email address,
 * list of followers, and accounts that they follow.
 */
public class User {
    private String username;
    private String email;
    private List<String> followers;
    private List<String> following;

    private String profilePictureUrl;

    private String bio;

    private List<String> hosting;

    public User() {
    }

    /**
     * Constructs a new user object using just a registered username and email address.
     * @param username
     * @param email
     */
    public User(String username, String email) {
        this.username = username;
        this.email = email;
        this.followers = new ArrayList<>();
        this.following = new ArrayList<>();
        this.bio = "";
        this.hosting = new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getFollowers() {
        return followers;
    }

    public void setFollowers(List<String> followers) {
        this.followers = followers;
    }

    public List<String> getFollowing() {
        return following;
    }

    public void setFollowing(List<String> following) {
        this.following = following;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }
    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getBio() { return bio; }

    public void setBio(String bio) { this.bio = bio; }

    public List<String> getHosting() { return hosting; }

    public void setHosting(List<String> hosting) { this.hosting = hosting; }

    /**
     * Method that allows a User A to add User B to their following list.
     * Additionally, this method adds User A to User B's list of followers.
     * @param userIdToFollow - The user id of the user that will be followed.
     */
    public void followUser(String userIdToFollow) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Adds a user to the following list of the current user.
        db.collection("users").document(currentUserId).update("following", FieldValue.arrayUnion(userIdToFollow));

        // Add the current user to the followers list of the user to follow.
        db.collection("users").document(userIdToFollow).update("followers", FieldValue.arrayUnion(currentUserId));
    }

    /**
     * Method that allows a User A to remove User B from their following list,
     * Additionally, this removes User A from User B's followers list.
     * @author James Bebarski
     * @param userIdToUnfollow - The user id of the user that will be unfollowed.
     */
    public void unfollowUser(String userIdToUnfollow) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Removes the user from the following list of the current user.
        db.collection("users").document(currentUserId).update("following", FieldValue.arrayRemove(userIdToUnfollow));

        // Removes the current user from the followers list of the current user to unfollow.
        db.collection("users").document(userIdToUnfollow).update("followers", FieldValue.arrayRemove(currentUserId));
    }

}
