package com.example.crispycrumbs.serverAPI.serverDataUnit;

public class EditCommentRequest {
    private String videoId;
    private String commentId;
    private String userId;
    private String comment;
    private String date;

    public EditCommentRequest(String videoId, String commentId, String userId, String commentText, String date) {
        this.videoId = videoId;
        this.commentId = commentId;
        this.userId = userId;
        this.comment = commentText;
        this.date = date;
    }

    public String getVideoId() {
        return videoId;
    }

    public String getCommentId() {
        return commentId;
    }

    public String getUserId() {
        return userId;
    }

    public String getCommentText() {
        return comment;
    }

    public String getDate() {
        return date;
    }
}
