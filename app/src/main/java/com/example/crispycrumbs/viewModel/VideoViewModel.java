package com.example.crispycrumbs.viewModel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.crispycrumbs.List.VideoList;
import com.example.crispycrumbs.dataUnit.CommentItem;
import com.example.crispycrumbs.dataUnit.PreviewVideoCard;
import com.example.crispycrumbs.dataUnit.UserItem;
import com.example.crispycrumbs.localDB.AppDB;
import com.example.crispycrumbs.localDB.AppDB;
import com.example.crispycrumbs.repository.VideoRepository;
import java.util.List;

public class VideoViewModel extends AndroidViewModel {
    private VideoRepository videoRepository;
    private LiveData<List<PreviewVideoCard>> allVideos;
    private LiveData<PreviewVideoCard> video;
    public static final int LIKE = 1;
    public static final int DISLIKE = -1;


    public VideoViewModel(Application application) {
        super(application);
        AppDB db = AppDB.getDatabase(application);
        videoRepository = new VideoRepository(db); // Initialize the repository with AppDatabase
    }

    public LiveData<List<PreviewVideoCard>> getAllVideos() {
        if (allVideos == null) {
            allVideos = videoRepository.getMostViewedVideos();
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

    public LiveData<List<PreviewVideoCard>> getVideosByUser(String userId) {
        return videoRepository.getVideosByUser(userId);
    }

    public void incrementVideoViews(String videoId) {
        videoRepository.incrementVideoViews(videoId);
    }

    public void likeVideo(String videoId, String userId) {
        videoRepository.likeVideo(videoId, userId);
    }

    public void dislikeVideo(String videoId, String userId) {
        videoRepository.dislikeVideo(videoId, userId);
    }


}
