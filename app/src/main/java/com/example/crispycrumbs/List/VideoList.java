package com.example.crispycrumbs.List;

import com.example.crispycrumbs.dataUnit.PreviewVideoCard;

import java.util.ArrayList;
import java.util.List;

public class VideoList  {
    private List<PreviewVideoCard> videos;

    public List<PreviewVideoCard> getVideos() {
        return videos;
    }

    public VideoList() {
        setVideos(new ArrayList<>());
    }
    public VideoList(List<PreviewVideoCard> videos) {
        setVideos(videos);
    }
    public void setVideos(List<PreviewVideoCard> videos) {
        this.videos = videos;
    }

    public void addVideo(PreviewVideoCard video) {
        videos.add(video);
    }
}
