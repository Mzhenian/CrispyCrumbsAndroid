package com.example.crispycrumbs.serverAPI.serverDataUnit;

import com.example.crispycrumbs.dataUnit.PreviewVideoCard;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class VideoListsResponse {
    @SerializedName("mostViewedVideos")
    private List<PreviewVideoCard> mostViewedVideos;

    @SerializedName("mostRecentVideos")
    private List<PreviewVideoCard> mostRecentVideos;

    @SerializedName("followingVideos")
    private List<PreviewVideoCard> followingVideos;

    @SerializedName("randomVideos")
    private List<PreviewVideoCard> randomVideos;

    // Getters and Setters
    public List<PreviewVideoCard> getMostViewedVideos() {
        return mostViewedVideos;
    }

    public void setMostViewedVideos(List<PreviewVideoCard> mostViewedVideos) {
        this.mostViewedVideos = mostViewedVideos;
    }

    public List<PreviewVideoCard> getMostRecentVideos() {
        return mostRecentVideos;
    }

    public void setMostRecentVideos(List<PreviewVideoCard> mostRecentVideos) {
        this.mostRecentVideos = mostRecentVideos;
    }

    public List<PreviewVideoCard> getFollowingVideos() {
        return followingVideos;
    }

    public void setFollowingVideos(List<PreviewVideoCard> followingVideos) {
        this.followingVideos = followingVideos;
    }

    public List<PreviewVideoCard> getRandomVideos() {
        return randomVideos;
    }

    public void setRandomVideos(List<PreviewVideoCard> randomVideos) {
        this.randomVideos = randomVideos;
    }


}

