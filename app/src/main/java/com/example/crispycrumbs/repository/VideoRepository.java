package com.example.crispycrumbs.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.crispycrumbs.dataUnit.PreviewVideoCard;
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

public class VideoRepository {
    private VideoDao videoDao;
    private ServerAPInterface serverAPI;
    private Executor executor = Executors.newSingleThreadExecutor();

    public VideoRepository(AppDB db) {
        videoDao = db.videoDao();
        serverAPI = ServerAPI.getInstance().getAPI();
    }

    public LiveData<List<PreviewVideoCard>> getAllVideos() {
        MutableLiveData<List<PreviewVideoCard>> allVideosLiveData = new MutableLiveData<>();

        // Step 1: Load data from Room and post to LiveData
        executor.execute(() -> {
            List<PreviewVideoCard> localVideos = videoDao.getAllVideosSync(); // Assuming you have a synchronous method
            allVideosLiveData.postValue(localVideos);
        });

        // Step 2: Fetch data from the server and update Room and LiveData
        serverAPI.getAllVideos().enqueue(new Callback<List<PreviewVideoCard>>() {
            @Override
            public void onResponse(Call<List<PreviewVideoCard>> call, Response<List<PreviewVideoCard>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PreviewVideoCard> videoList = response.body();
                    allVideosLiveData.postValue(videoList);

                    // Update Room database with the new data
                    executor.execute(() -> {
                        videoDao.insertVideos(videoList); // Assuming you have a batch insert method
                    });
                }
            }

            @Override
            public void onFailure(Call<List<PreviewVideoCard>> call, Throwable t) {
                // Handle the error
            }
        });

        return allVideosLiveData;
    }

    public void insertVideo(PreviewVideoCard video) {
        executor.execute(() -> videoDao.insertVideo(video));
    }

    public LiveData<PreviewVideoCard> getVideo(String videoId) {
        MutableLiveData<PreviewVideoCard> videoLiveData = new MutableLiveData<>();

        // Step 1: Load the video from Room (synchronously)
        executor.execute(() -> {
            PreviewVideoCard video = videoDao.getVideoByIdSync(videoId);
            if (video != null) {
                videoLiveData.postValue(video); // Post the value if found locally
            }
        });

        // Step 2: Fetch the video from the server and update Room and LiveData
        serverAPI.getVideoById(videoId).enqueue(new Callback<PreviewVideoCard>() {
            @Override
            public void onResponse(Call<PreviewVideoCard> call, Response<PreviewVideoCard> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PreviewVideoCard video = response.body();
                    videoLiveData.postValue(video); // Update LiveData with the server response

                    // Update Room database with the new data
                    executor.execute(() -> videoDao.insertVideo(video));
                }
            }

            @Override
            public void onFailure(Call<PreviewVideoCard> call, Throwable t) {
                // Handle the error (optional logging, notify the user, etc.)
            }
        });

        return videoLiveData;
    }

//    public void updateVideo(PreviewVideoCard video) {
//        executor.execute(() -> videoDao.updateVideo(video));
//    }
}
