package com.example.crispycrumbs;

import android.widget.TextView;

public class VideoCardItem {

    private TextView title;
    private TextView videoDate;
    private TextView videoViews;
    private TextView videoLikes;
    public VideoCardItem() {
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
}