package com.example.crispycrumbs.data;

import java.util.ArrayList;

public class PreviewVideoCard {
    private String videoId;
    private String title;
    private String thumbnail;
    private String profilePicture;
    private String videoFile;
    private String userId;
    private int views;
    private String uploadDate;
    private ArrayList<CommentItem> comments;

    // Assuming that you will set the resource ID later in the code
    private transient int thumbnailResId, ProfilePicResId;

    // Getters and setters for all fields

    public String getVideoId() { return videoId; }
    public void setVideoId(String videoId) { this.videoId = videoId; }
    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }
    public String getThumbnail() { return thumbnail; }

    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }
    public String getVideoFile() { return videoFile; }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public void setVideoFile(String videoFile) { this.videoFile = videoFile; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getViews() { return views; }
    public void setViews(int views) { this.views = views; }

    public String getUploadDate() { return uploadDate; }
    public void setUploadDate(String uploadDate) { this.uploadDate = uploadDate; }

    public int getThumbnailResId() { return thumbnailResId; }
    public void setThumbnailResId(int thumbnailResId) { this.thumbnailResId = thumbnailResId; }



    public ArrayList<CommentItem> getComments() {
        return comments;
    }
}
