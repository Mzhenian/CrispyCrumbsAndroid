package com.example.crispycrumbs.repository;

import androidx.lifecycle.LiveData;
import com.example.crispycrumbs.dataUnit.PreviewVideoCard;
import com.example.crispycrumbs.localDB.AppDB;
import com.example.crispycrumbs.localDB.AppDB;
import com.example.crispycrumbs.localDB.VideoDao;
import com.example.crispycrumbs.serverAPI.ServerAPI;
import com.example.crispycrumbs.serverAPI.ServerAPInterface;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class VideoRepository {
    private VideoDao videoDao;
    private ServerAPInterface serverAPI;
    private Executor executor = Executors.newSingleThreadExecutor();

    public VideoRepository(AppDB db) {
        videoDao = db.videoDao();
        serverAPI = ServerAPI.getInstance().getAPI();
    }

    public LiveData<List<PreviewVideoCard>> getAllVideos() {
        return videoDao.getAllVideos();
    }

    public LiveData<PreviewVideoCard> getVideo(String videoId) {
        return videoDao.getVideoById(videoId);
    }

    public void insertVideo(PreviewVideoCard video) {
        executor.execute(() -> videoDao.insertVideo(video));
    }

    public void updateVideo(PreviewVideoCard video) {
        executor.execute(() -> videoDao.updateVideo(video));
    }

    // Methods for syncing with the server can be added here
}

