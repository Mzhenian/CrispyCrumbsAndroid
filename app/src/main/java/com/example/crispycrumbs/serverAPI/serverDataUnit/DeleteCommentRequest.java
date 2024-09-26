package com.example.crispycrumbs.serverAPI.serverDataUnit;

public class DeleteCommentRequest {
    private String videoId;
    private String commentId; // Change to String
    private String userId;

    public DeleteCommentRequest(String videoId, String commentId, String userId) { // Accept commentId as String
        this.videoId = videoId;
        this.commentId = commentId;
        this.userId = userId;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getCommentId() {
        return commentId; // Use String for commentId
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
}



