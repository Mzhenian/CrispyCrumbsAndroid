package com.example.crispycrumbs.data;

import static com.example.crispycrumbs.ui.MainPage.getDataManager;

import com.example.crispycrumbs.ui.MainPage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;

public class PreviewVideoCard {
    private String videoId;
    private String title;
    private String thumbnail;
    private String profilePicture;
    private String videoFile;
    private String userId;
    private String description;
    private int views;
    private String uploadDate;
    private ArrayList<CommentItem> comments;

    public PreviewVideoCard(String videoId, String title, String thumbnail, String videoFile, String description) {
        this.videoId = videoId;
        this.title = title;
        this.thumbnail = thumbnail;
        this.videoFile = videoFile;
        this.description = description;

        this.userId = LoggedInUser.getUser().getUserId();
        this.comments = new ArrayList<>();
        this.views = 0;
        this.profilePicture = getDataManager().getUserById(userId).getProfilePhoto();
        this.uploadDate = LocalDate.now().toString();
    }

    //
    // Assuming that you will set the resource ID later in the code
    private transient UserItem uploader;

    private int likesCount; // Add this field
    private int dislikesCount; // Add this field


    public PreviewVideoCard() {}
    // Getters and setters for all fields

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getVideoFile() {
        return videoFile;
    }

    public void setVideoFile(String videoFile) {
        this.videoFile = videoFile;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getViews() {
        return views;
    }
    public void setViews(int views) {
        this.views = views;
    }
    public String getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
    }

    public ArrayList<CommentItem> getComments() {
        return comments;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public int getDislikesCount() {
        return dislikesCount;
    }

    public void setDislikesCount(int dislikesCount) {
        this.dislikesCount = dislikesCount;
    }

    public UserItem getUploader() {
        return uploader;
    }

    public void setUploader(UserItem uploader) {
        this.uploader = uploader;
    }
}
