package com.example.crispycrumbs.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.crispycrumbs.List.VideoList;
import com.example.crispycrumbs.dataUnit.CommentItem;
import com.example.crispycrumbs.dataUnit.PreviewVideoCard;
import com.example.crispycrumbs.dataUnit.UserItem;
import com.example.crispycrumbs.localDB.AppDB;
import com.example.crispycrumbs.dao.VideoDao;
import com.example.crispycrumbs.localDB.LoggedInUser;
import com.example.crispycrumbs.serverAPI.ServerAPI;
import com.example.crispycrumbs.serverAPI.ServerAPInterface;
import com.example.crispycrumbs.serverAPI.serverDataUnit.ApiResponse;
import com.example.crispycrumbs.serverAPI.serverDataUnit.LikeDislikeRequest;
import com.example.crispycrumbs.serverAPI.serverDataUnit.VideoIdRequest;
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

    public LiveData<List<PreviewVideoCard>> getVideosByUser(String userId) {
        MutableLiveData<List<PreviewVideoCard>> userVideosLiveData = new MutableLiveData<>();

        // Step 1: Load from Room (synchronously)
        executor.execute(() -> {
            List<PreviewVideoCard> localVideos = videoDao.getVideosByUserIdSync(userId);
            userVideosLiveData.postValue(localVideos);
        });

        // Step 2: Fetch from the server and update Room and LiveData
        serverAPI.getVideosByUser(userId).enqueue(new Callback<List<PreviewVideoCard>>() {
            @Override
            public void onResponse(Call<List<PreviewVideoCard>> call, Response<List<PreviewVideoCard>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PreviewVideoCard> videoList = response.body();
                    userVideosLiveData.postValue(videoList);

                    // Update Room database with the new data
                    executor.execute(() -> videoDao.insertVideos(videoList));
                }
            }

            @Override
            public void onFailure(Call<List<PreviewVideoCard>> call, Throwable t) {
                // Handle the error
            }
        });

        return userVideosLiveData;
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

    public void incrementVideoViews(String videoId) {
        Log.d("VideoRepository", "Attempting to increment views for videoId: " + videoId);

        VideoIdRequest request = new VideoIdRequest(videoId);

        serverAPI.incrementVideoViews(request).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    Log.d("VideoRepository", "Successfully incremented views on server for videoId: " + videoId);
                    executor.execute(() -> {
                        // Fetch the latest data from Room after updating views
                        PreviewVideoCard video = videoDao.getVideoByIdSync(videoId);
                        if (video != null) {
                            video.setViews(video.getViews() + 1);
                            videoDao.insertVideo(video);

                            // Explicitly fetch the updated data again to ensure UI gets the latest view count
                            PreviewVideoCard updatedVideo = videoDao.getVideoByIdSync(videoId);
                            Log.d("VideoRepository", "Fetched updated views from Room: " + updatedVideo.getViews());

                            // Post the latest data to LiveData
                            MutableLiveData<PreviewVideoCard> liveVideo = (MutableLiveData<PreviewVideoCard>) getVideo(videoId);
                            liveVideo.postValue(updatedVideo);
                        }
                    });
                } else {
                    Log.e("VideoRepository", "Failed to increment views on server: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Log.e("VideoRepository", "Error while incrementing views", t);
            }
        });
    }

    public void likeVideo(String videoId, String userId) {
        if (userId == null) {
            Log.e("VideoRepository", "User is not logged in. Like action is not permitted.");
            return;
        }

        String token = LoggedInUser.getToken();
        Log.d("VideoRepository", "Attempting to like video with ID: " + videoId + " by user: " + userId);

        LikeDislikeRequest request = new LikeDislikeRequest(videoId, userId);
        serverAPI.likeVideo("Bearer " + token, request).enqueue(new Callback<PreviewVideoCard>() {
            @Override
            public void onResponse(Call<PreviewVideoCard> call, Response<PreviewVideoCard> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("VideoRepository", "Successfully liked video. Response: " + response.body().toString());
                    executor.execute(() -> {
                        PreviewVideoCard updatedVideo = response.body();
                        if (updatedVideo != null) {
                            // Update Room with the new video data
                            videoDao.insertVideo(updatedVideo);

                            // Update the user's liked/disliked lists
                            UserItem user = LoggedInUser.getUser();
                            if (user.hasLiked(videoId)) {
                                Log.d("VideoRepository", "User already liked the video. Removing like.");
                                user.removeLike(videoId);
                                updatedVideo.setLikes(updatedVideo.getLikes() - 1);
                            } else {
                                Log.d("VideoRepository", "User is liking the video.");
                                user.likeVideo(videoId);
                                updatedVideo.setLikes(updatedVideo.getLikes() + 1);
                                if (user.hasDisliked(videoId)) {
                                    Log.d("VideoRepository", "User had disliked the video. Removing dislike.");
                                    user.removeDislike(videoId);
                                    updatedVideo.setDislikes(updatedVideo.getDislikes() - 1);
                                }
                            }
                            // Notify observers with the updated video
                            ((MutableLiveData<PreviewVideoCard>) getVideo(videoId)).postValue(updatedVideo);
                        } else {
                            Log.e("VideoRepository", "Updated video is null.");
                        }
                    });
                } else {
                    Log.e("VideoRepository", "Failed to like video on server. Response code: " + response.code() + ", message: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<PreviewVideoCard> call, Throwable t) {
                Log.e("VideoRepository", "Error while liking video", t);
            }
        });
    }

    public void dislikeVideo(String videoId, String userId) {
        if (userId == null) {
            Log.e("VideoRepository", "User is not logged in. Dislike action is not permitted.");
            return;
        }

        String token = LoggedInUser.getToken();
        Log.d("VideoRepository", "Attempting to dislike video with ID: " + videoId + " by user: " + userId);

        LikeDislikeRequest request = new LikeDislikeRequest(videoId, userId);
        serverAPI.dislikeVideo("Bearer " + token, request).enqueue(new Callback<PreviewVideoCard>() {
            @Override
            public void onResponse(Call<PreviewVideoCard> call, Response<PreviewVideoCard> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("VideoRepository", "Successfully disliked video. Response: " + response.body().toString());
                    executor.execute(() -> {
                        PreviewVideoCard updatedVideo = response.body();
                        if (updatedVideo != null) {
                            // Update Room with the new video data
                            videoDao.insertVideo(updatedVideo);

                            // Update the user's liked/disliked lists
                            UserItem user = LoggedInUser.getUser();
                            if (user.hasDisliked(videoId)) {
                                Log.d("VideoRepository", "User already disliked the video. Removing dislike.");
                                user.removeDislike(videoId);
                                updatedVideo.setDislikes(updatedVideo.getDislikes() - 1);
                            } else {
                                Log.d("VideoRepository", "User is disliking the video.");
                                user.dislikeVideo(videoId);
                                updatedVideo.setDislikes(updatedVideo.getDislikes() + 1);
                                if (user.hasLiked(videoId)) {
                                    Log.d("VideoRepository", "User had liked the video. Removing like.");
                                    user.removeLike(videoId);
                                    updatedVideo.setLikes(updatedVideo.getLikes() - 1);
                                }
                            }
                            // Notify observers with the updated video
                            ((MutableLiveData<PreviewVideoCard>) getVideo(videoId)).postValue(updatedVideo);
                        } else {
                            Log.e("VideoRepository", "Updated video is null.");
                        }
                    });
                } else {
                    Log.e("VideoRepository", "Failed to dislike video on server. Response code: " + response.code() + ", message: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<PreviewVideoCard> call, Throwable t) {
                Log.e("VideoRepository", "Error while disliking video", t);
            }
        });
    }



}
