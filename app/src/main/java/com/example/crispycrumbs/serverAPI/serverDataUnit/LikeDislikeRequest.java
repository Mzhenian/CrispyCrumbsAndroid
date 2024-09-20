package com.example.crispycrumbs.serverAPI.serverDataUnit;

public class LikeDislikeRequest {
    private String videoId;
    private String userId;

    public LikeDislikeRequest(String videoId, String userId) {
        this.videoId = videoId;
        this.userId = userId;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
