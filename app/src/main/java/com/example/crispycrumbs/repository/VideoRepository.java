package com.example.crispycrumbs.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.crispycrumbs.List.VideoList;
import com.example.crispycrumbs.dataUnit.CommentItem;
import com.example.crispycrumbs.dataUnit.PreviewVideoCard;
import com.example.crispycrumbs.localDB.AppDB;
import com.example.crispycrumbs.dao.VideoDao;
import com.example.crispycrumbs.serverAPI.ServerAPI;
import com.example.crispycrumbs.serverAPI.ServerAPInterface;
import com.example.crispycrumbs.serverAPI.serverDataUnit.VideoResponse;

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

    public LiveData<List<PreviewVideoCard>> getMostViewedVideos() {
        MutableLiveData<List<PreviewVideoCard>> mostViewedLiveData = new MutableLiveData<>();

        // Step 1: Load data from Room and post to LiveData
        executor.execute(() -> {
            VideoList localVideos = new VideoList(videoDao.getAllVideosSync());
            Log.d("VideoRepository", "Loaded videos from Room: " + localVideos.getVideos().size());
            mostViewedLiveData.postValue(localVideos.getVideos());
        });

        // Step 2: Fetch data from the server and update Room and LiveData
        serverAPI.getAllVideos().enqueue(new Callback<VideoResponse>() {
            @Override
            public void onResponse(Call<VideoResponse> call, Response<VideoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    VideoList videoList = new VideoList(response.body().getMostViewedVideos());

                    Log.d("VideoRepository", "Fetched videos from server: " + videoList.getVideos().size());
                    mostViewedLiveData.postValue(videoList.getVideos());

                    // Update Room database with the new data
                    executor.execute(() -> {
                        videoDao.insertVideos(videoList.getVideos());
                        Log.d("VideoRepository", "Inserted videos into Room DB: " + videoList.getVideos().size());
                    });
                }
            }

            @Override
            public void onFailure(Call<VideoResponse> call, Throwable t) {
                Log.e("VideoRepository", "Failed to fetch videos from server", t);
            }
        });

        return mostViewedLiveData;
    }

    public LiveData<VideoList> getMostRecentVideos() {
        MutableLiveData<VideoList> mostRecentLiveData = new MutableLiveData<>();

        // Step 1: Load data from Room and post to LiveData
        executor.execute(() -> {
            VideoList localVideos = new VideoList(videoDao.getAllVideosSync());
            mostRecentLiveData.postValue(localVideos);
        });

        // Step 2: Fetch data from the server and update Room and LiveData
        serverAPI.getAllVideos().enqueue(new Callback<VideoResponse>() {
            @Override
            public void onResponse(Call<VideoResponse> call, Response<VideoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    VideoList videoList = new VideoList(response.body().getMostRecentVideos());
                    mostRecentLiveData.postValue(videoList);

                    // Update Room database with the new data
                    executor.execute(() -> {
                        videoDao.insertVideos(videoList.getVideos());
                    });
                }
            }

            @Override
            public void onFailure(Call<VideoResponse> call, Throwable t) {
                Log.e("VideoRepository", "Failed to fetch most recent videos from server", t);
            }
        });

        return mostRecentLiveData;
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
                videoLiveData.postValue(video);
            }
        });

        // Step 2: Fetch the video from the server and update Room and LiveData
        serverAPI.getVideoById(videoId).enqueue(new Callback<VideoResponse>() {
            @Override
            public void onResponse(Call<VideoResponse> call, Response<VideoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Determine the appropriate list based on your API response structure
                    PreviewVideoCard video;
                    List<PreviewVideoCard> videos = response.body().getMostViewedVideos(); // Assuming it's in mostViewedVideos

                    // Check other lists if needed, depending on your API structure
                    if (videos == null || videos.isEmpty()) {
                        videos = response.body().getMostRecentVideos();
                    }
                    if (videos == null || videos.isEmpty()) {
                        videos = response.body().getFollowingVideos();
                    }
                    if (videos == null || videos.isEmpty()) {
                        videos = response.body().getRandomVideos();
                    }

                    // Assuming the first video is the one you want, or you have a method to identify the correct one
                    if (videos != null && !videos.isEmpty()) {
                        video = videos.get(0); // This assumes that the correct video is the first one; adjust as necessary
                    } else {
                        video = null;
                    }

                    if (video != null) {
                        videoLiveData.postValue(video);

                        // Update Room database with the new data
                        executor.execute(() -> videoDao.insertVideo(video));
                    }
                }
            }

            @Override
            public void onFailure(Call<VideoResponse> call, Throwable t) {
                Log.e("VideoRepository", "Failed to fetch video from server", t);
            }
        });

        return videoLiveData;
    }



    public void insertComment(CommentItem comment, String videoId) {
        executor.execute(() -> {
            PreviewVideoCard video = videoDao.getVideoByIdSync(videoId);
            if (video != null) {
                video.getComments().add(comment);
                videoDao.insertVideo(video);
            }
        });
    }
}
