package com.example.crispycrumbs;

public class PreviewVideoCard {
    private String title;
    private String thumbnail;
    private String videoFile;
    private String userId;
    private int views;
    private String uploadDate;

    // Assuming that you will set the resource ID later in the code
    private transient int thumbnailResId;

    // Getters and setters for all fields
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }

    public String getVideoFile() { return videoFile; }
    public void setVideoFile(String videoFile) { this.videoFile = videoFile; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getViews() { return views; }
    public void setViews(int views) { this.views = views; }

    public String getUploadDate() { return uploadDate; }
    public void setUploadDate(String uploadDate) { this.uploadDate = uploadDate; }

    public int getThumbnailResId() { return thumbnailResId; }
    public void setThumbnailResId(int thumbnailResId) { this.thumbnailResId = thumbnailResId; }
}
