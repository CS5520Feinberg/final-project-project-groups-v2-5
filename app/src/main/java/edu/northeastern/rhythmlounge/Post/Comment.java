package edu.northeastern.rhythmlounge.Post;

import java.util.Date;

public class Comment {
    private String commentId;
    private String userId;
    private String postId;
    private String username;
    private String content;
    private Date timestamp;

    // Constructors, getters, setters

    public Comment() {
    }

    public Comment(String commentId, String userId, String username, String content) {
        this.commentId = commentId;
        this.userId = userId;
        this.username = username;
        this.content = content;
        this.timestamp = new Date();
        this.postId = postId;
    }
    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

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

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

}