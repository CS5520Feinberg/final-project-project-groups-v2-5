package edu.northeastern.rhythmlounge.Post;

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


    public Post() {
    }

    public Post(String userId, String username, String title, String content, String imageUrl) {
        this.userId = userId;
        this.username = username;
        this.content = content;
        this.timestamp = new Date();
        this.title = title;
        this.imageUrl = imageUrl;
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

}