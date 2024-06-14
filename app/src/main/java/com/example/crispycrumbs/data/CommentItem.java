package com.example.crispycrumbs.data;

public class CommentItem {
    int image;
    String username;
    String content;
    String Date;

    public CommentItem(int image, String username, String content, String date) {
        this.image = image;
        this.username = username;
        this.content = content;
        Date = date;
    }

    public int getImage() {
        return image;
    }

    public String getUsername() {
        return username;
    }

    public String getContent() {
        return content;
    }

    public String getDate() {
        return Date;
    }
}
