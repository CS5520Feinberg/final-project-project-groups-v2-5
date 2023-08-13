package edu.northeastern.rhythmlounge.Posts;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Post {
    private String userId;
    private String username;
    private String content;
    private Date timestamp;
    private String title;
    private String thumbnailUrl;
    private String imageUrl;
    private String postId;
    private String profilePictureUrl;
    private int likeCount = 0;
    private int commentCount = 0;
    private List<String> likedByUsers = new ArrayList<>();

    public Post() {
    }

    public Post(String userId, String username, String title, String content, String imageUrl) {
        this.userId = userId;
        this.username = username;
        this.content = content;
        this.timestamp = new Date();
        this.title = title;
        this.imageUrl = imageUrl;
        this.likeCount = 0;
        this.commentCount = 0;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public List<String> getLikedByUsers() {
        return likedByUsers;
    }

    public void setLikedByUsers(List<String> likedByUsers) {
        this.likedByUsers = likedByUsers;
    }

    public void addLike(String userId) {
        // Increment like count
        this.likeCount++;
        // Add user ID to the list
        this.likedByUsers.add(userId);
    }

    public void removeLike(String userId) {
        // Decrement like count
        this.likeCount--;
        // Remove user ID from the list
        this.likedByUsers.remove(userId);
    }

    public void addComment() {
        this.commentCount++;
    }

    public void removeComment() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }

}