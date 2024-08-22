package com.example.crispycrumbs.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.example.crispycrumbs.dataUnit.PreviewVideoCard;
import com.example.crispycrumbs.repository.VideoRepository;
import java.util.List;

public class VideoViewModel extends ViewModel {
    private VideoRepository videoRepository;
    private LiveData<List<PreviewVideoCard>> allVideos;
    private LiveData<PreviewVideoCard> video;

    public VideoViewModel() {
        videoRepository = new VideoRepository(); // Initialize your repository
    }

    public LiveData<List<PreviewVideoCard>> getAllVideos() {
        if (allVideos == null) {
            allVideos = videoRepository.getAllVideos();
        }
        return allVideos;
    }

    public LiveData<PreviewVideoCard> getVideo(String videoId) {
        if (video == null) {
            video = videoRepository.getVideo(videoId);
        }
        return video;
    }

    public void insertVideo(PreviewVideoCard video) {
        videoRepository.insertVideo(video);
    }

    public void updateVideo(PreviewVideoCard video) {
        videoRepository.updateVideo(video);
    }
}

