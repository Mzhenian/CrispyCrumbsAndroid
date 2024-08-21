package com.example.crispycrumbs.dataUnit;

import com.example.crispycrumbs.localDB.LoggedInUser;
import com.example.crispycrumbs.view.MainPage;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;

public class PreviewVideoCard implements Serializable {
    @SerializedName("_id")
    private String videoId;
    // matching @SerializedName()
    private String title;
    // matching @SerializedName()
    private String thumbnail;
    // todo remove
//    @SerializedName()
//    private String profilePicture;
    // matching @SerializedName()
    private String videoFile;
    // matching @SerializedName()
    private String userId;
    // matching @SerializedName()
    private String description;
    // matching @SerializedName()
    private int views;
    // matching @SerializedName()
    private String uploadDate;
    // matching @SerializedName()
    private ArrayList<CommentItem> comments;
    // matching @SerializedName()
    private int likes;
    // matching @SerializedName()
    private int dislikes;
     @SerializedName("tags")
     private ArrayList<String> tags;
     @SerializedName("category")
    private String category;
     @SerializedName("likedBy")
    private ArrayList<String> likedBy;
     @SerializedName("dislikedBy")
    private ArrayList<String> dislikedBy;
//     @SerializedName("__v")


    public PreviewVideoCard(String videoId, String title, String thumbnail, String videoFile, String description) {
        this.videoId = videoId;
        this.title = title;
        this.thumbnail = thumbnail;
        this.videoFile = videoFile;
        this.description = description;

        this.userId = LoggedInUser.getUser().getUserId();
        this.comments = new ArrayList<>();
        this.views = 0;
        //todo remove
//        this.profilePicture = getDataManager().getUserById(userId).getProfilePhoto();
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
