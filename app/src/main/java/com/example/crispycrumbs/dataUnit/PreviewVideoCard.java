package com.example.crispycrumbs.dataUnit;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.crispycrumbs.converters.CommentItemTypeConverter;
import com.example.crispycrumbs.localDB.LoggedInUser;
import com.example.crispycrumbs.converters.StringListTypeConverter;
import com.example.crispycrumbs.view.MainPage;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;

@Entity(tableName = "videos")
public class PreviewVideoCard implements Serializable {
    @PrimaryKey
    @SerializedName("_id")
    @NonNull
    private String videoId;

    private String title;
    private String thumbnail;
    private String videoFile;
    private String userId;
    private String description;
    private int views;
    private String uploadDate;

    @TypeConverters(CommentItemTypeConverter.class)
    private ArrayList<CommentItem> comments;

    private int likes;
    private int dislikes;

    @TypeConverters(StringListTypeConverter.class)
    private ArrayList<String> tags;

    private String category;

    @TypeConverters(StringListTypeConverter.class)
    private ArrayList<String> likedBy;

    @TypeConverters(StringListTypeConverter.class)
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

    public ArrayList<String> getTags() {
        return tags;
    }

    public String getCategory() {
        return category;
    }

    public ArrayList<String> getLikedBy() {
        return likedBy;
    }

    public ArrayList<String> getDislikedBy() {
        return dislikedBy;
    }

    public void setComments(ArrayList<CommentItem> comments) {
        this.comments = comments;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setLikedBy(ArrayList<String> likedBy) {
        this.likedBy = likedBy;
    }

    public void setDislikedBy(ArrayList<String> dislikedBy) {
        this.dislikedBy = dislikedBy;
    }
}
