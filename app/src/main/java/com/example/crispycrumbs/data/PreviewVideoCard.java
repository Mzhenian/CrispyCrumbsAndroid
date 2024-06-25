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
    private int likes, dislikes;

//    private TextView title;
//    private TextView videoDate;
//    private TextView videoViews;
//    private TextView videoLikes;
//    private  int likescount = 0;
//
//    public VideoCardItem(TextView title, TextView videoDate, TextView videoViews, TextView videoLikes) {
//        this.title = title;
//        this.videoDate = videoDate;
//        this.videoViews = videoViews;
//        this.videoLikes = videoLikes;
//    }
//
//    public TextView getTitle() {
//        return title;
//    }
//
//    public TextView getVideoDate() {
//        return videoDate;
//    }
//
//    public TextView getVideoViews() {
//        return videoViews;
//    }
//
//    public TextView getVideoLikes() {
//        return videoLikes;
//    }
//
//    public void setVideoViews(TextView videoViews) {
//        this.videoViews = videoViews;
//    }
//
//    public void setVideoLikes(TextView videoLikes) {
//        this.videoLikes = videoLikes;
//    }
//
//    public int getLikesCount() {
//        return likescount;
//    }
//
//    public void setLikesCount(int likesCount) {
//        this.likescount = likesCount;
//    }

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

    public int getLikes() {
        return likes;
    }


    public PreviewVideoCard() {}
    // Getters and setters for all fields

    public String getVideoId() { return videoId; }
    public void setVideoId(String videoId) { this.videoId = videoId; }

    public String getTitle() { return title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

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

//    public int getThumbnailResId() { return thumbnailResId; }
//    public void setThumbnailResId(int thumbnailResId) { this.thumbnailResId = thumbnailResId; }



    public ArrayList<CommentItem> getComments() {
        return comments;
    }
}
