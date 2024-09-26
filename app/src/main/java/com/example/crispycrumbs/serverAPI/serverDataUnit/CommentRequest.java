package com.example.crispycrumbs.serverAPI.serverDataUnit;

public class CommentRequest {
    private String videoId;
    private String commentText;
    private String date;

    public CommentRequest(String videoId, String commentText, String date) {
        this.videoId = videoId;
        this.commentText = commentText;
        this.date = date;
    }

    // Getters and setters
    public String getVideoId() { return videoId; }
    public String getCommentText() { return commentText; }
    public String getDate() { return date; }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
