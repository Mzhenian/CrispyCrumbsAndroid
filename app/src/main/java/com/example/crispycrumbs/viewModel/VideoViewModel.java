package com.example.crispycrumbs.viewModel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.crispycrumbs.dataUnit.PreviewVideoCard;
import com.example.crispycrumbs.localDB.AppDB;
import com.example.crispycrumbs.localDB.AppDB;
import com.example.crispycrumbs.repository.VideoRepository;
import java.util.List;

public class VideoViewModel extends AndroidViewModel {
    private VideoRepository videoRepository;
    private LiveData<List<PreviewVideoCard>> allVideos;
    private LiveData<PreviewVideoCard> video;

    public VideoViewModel(Application application) {
        super(application);
        AppDB db = AppDB.getDatabase(application);
        videoRepository = new VideoRepository(db); // Initialize the repository with AppDatabase
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
