package com.example.crispycrumbs.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.crispycrumbs.dataUnit.PreviewVideoCard;
import com.example.crispycrumbs.localDB.AppDB;
import com.example.crispycrumbs.repository.VideoRepository;

import java.util.List;

public class VideoViewModel extends AndroidViewModel {
    public static final String TAG = "VideoViewModel";
    private final VideoRepository videoRepository;
    private final MutableLiveData<List<PreviewVideoCard>> allVideos = new MutableLiveData<>();

    public VideoViewModel(Application application) {
        super(application);
        AppDB db = AppDB.getDatabase(application);
        videoRepository = new VideoRepository(db); // Initialize the repository with AppDatabase
    }

    public LiveData<List<PreviewVideoCard>> getAllVideos() {
        if (null == allVideos.getValue()) {
//            allVideos.setValue(videoRepository.getMostViewedVideos().getValue());
            videoRepository.getMostViewedVideos().observeForever(videoList -> {
                allVideos.setValue(videoList);
            });
        }
        return allVideos;
    }

    public LiveData<List<PreviewVideoCard>> getVideosByUser(String userId) {
        return videoRepository.getVideosByUser(userId);
    }
}
