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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class VideoRepository {
    private VideoDao videoDao;
    private ServerAPInterface serverAPI;
    private Executor executor = Executors.newSingleThreadExecutor();

    public VideoRepository(AppDB db) {
        videoDao = db.videoDao();
        serverAPI = ServerAPI.getInstance().getAPI();
    }

    public LiveData<List<PreviewVideoCard>> getAllVideos() {
        LiveData<List<PreviewVideoCard>> videos = videoDao.getAllVideos();

        executor.execute(() -> {
            serverAPI.getAllVideos().enqueue(new Callback<List<PreviewVideoCard>>() {
                @Override
                public void onResponse(Call<List<PreviewVideoCard>> call, Response<List<PreviewVideoCard>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        executor.execute(() -> {
                            for (PreviewVideoCard video : response.body()) {
                                videoDao.insertVideo(video);
                            }
                        });
                    }
                }

                @Override
                public void onFailure(Call<List<PreviewVideoCard>> call, Throwable t) {
                    // Handle error
                }
            });
        });

        return videos;
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

