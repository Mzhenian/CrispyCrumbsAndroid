package com.example.crispycrumbs.data;

public class CommentItem {
    private int avatarResId;
    private String userId;
    private String userName;
    private String comment;
    private String date;

    public CommentItem(int avatarResId, String userId, String userName, String comment, String date) {
        this.avatarResId = avatarResId;
        this.userName = userName;
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

    public String getUserName() {
        return userName;
    }
}
