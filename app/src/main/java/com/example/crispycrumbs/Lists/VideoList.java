package com.example.crispycrumbs.Lists;

import com.example.crispycrumbs.data.PreviewVideoCard;

import java.util.ArrayList;

public class VideoList  {
    private ArrayList<PreviewVideoCard> videos;

    public ArrayList<PreviewVideoCard> getVideos() {
        return videos;
    }

    public VideoList() {
        setVideos(new ArrayList<>());
    }
    public VideoList(ArrayList<PreviewVideoCard> videos) {
        setVideos(videos);
    }
    public void setVideos(ArrayList<PreviewVideoCard> videos) {
        this.videos = videos;
    }

    public void addVideo(PreviewVideoCard video) {
        videos.add(video);
    }
}
