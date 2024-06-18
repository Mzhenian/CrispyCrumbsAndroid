package com.example.crispycrumbs.data;

public class CommentItem {
    private int avatarResId;
    private String userId;
    private String comment;
    private String date;

    public CommentItem(int avatarResId, String userId, String comment, String date) {
        this.avatarResId = avatarResId;
        this.userId = userId;
        this.comment = comment;
        this.date = date;
    }

    public int getAvatarResId() {
        return avatarResId;
    }

    public String getUserId() {
        return userId;
    }

    public String getComment() {
        return comment;
    }

    public String getDate() {
        return date;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
