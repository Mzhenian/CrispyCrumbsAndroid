package com.example.crispycrumbs;

import android.widget.TextView;

public class VideoCardItem {

    private TextView title;
    private TextView videoDate;
    private TextView videoViews;
    private TextView videoLikes;
    private  int likescount = 0;

    public VideoCardItem(TextView title, TextView videoDate, TextView videoViews, TextView videoLikes) {
        this.title = title;
        this.videoDate = videoDate;
        this.videoViews = videoViews;
        this.videoLikes = videoLikes;
    }

    public TextView getTitle() {
        return title;
    }

    public TextView getVideoDate() {
        return videoDate;
    }

    public TextView getVideoViews() {
        return videoViews;
    }

    public TextView getVideoLikes() {
        return videoLikes;
    }

    public void setVideoViews(TextView videoViews) {
        this.videoViews = videoViews;
    }

    public void setVideoLikes(TextView videoLikes) {
        this.videoLikes = videoLikes;
    }

    public int getLikesCount() {
        return likescount;
    }

    public void setLikesCount(int likesCount) {
        this.likescount = likesCount;
    }
}
