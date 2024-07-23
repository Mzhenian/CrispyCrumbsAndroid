package com.example.crispycrumbs.data;

import static com.example.crispycrumbs.ui.MainPage.getDataManager;

import com.example.crispycrumbs.ui.MainPage;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;

public class PreviewVideoCard implements Serializable {
    private String videoId; // todo migrate to _id
    private String _id;
    private String title;
    private String thumbnail;
    private String profilePicture;
    private String videoFile;
    private String userId;
    private String description;
    private int views;
    private String uploadDate;
    private ArrayList<CommentItem> comments;
    private int likes;
    private int dislikes;

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

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getDislikes() {
        return dislikes;
    }

    public void setDislikes(int dislikes) {
        this.dislikes = dislikes;
    }

    public UserItem getUploader() {
        return MainPage.getDataManager().getUserById(userId);
    }

    public void setUploader(UserItem uploader) {
        this.userId = uploader.getUserId();
    }
}
