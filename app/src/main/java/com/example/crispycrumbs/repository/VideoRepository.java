package com.example.crispycrumbs.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.crispycrumbs.List.VideoList;
import com.example.crispycrumbs.dao.VideoDao;
import com.example.crispycrumbs.dataUnit.CommentItem;
import com.example.crispycrumbs.dataUnit.PreviewVideoCard;
import com.example.crispycrumbs.localDB.AppDB;
import com.example.crispycrumbs.localDB.LoggedInUser;
import com.example.crispycrumbs.serverAPI.ServerAPI;
import com.example.crispycrumbs.serverAPI.ServerAPInterface;
import com.example.crispycrumbs.serverAPI.serverDataUnit.ApiResponse;
import com.example.crispycrumbs.serverAPI.serverDataUnit.CommentRequest;
import com.example.crispycrumbs.serverAPI.serverDataUnit.DeleteCommentRequest;
import com.example.crispycrumbs.serverAPI.serverDataUnit.LikeDislikeRequest;
import com.example.crispycrumbs.serverAPI.serverDataUnit.VideoIdRequest;
import com.example.crispycrumbs.serverAPI.serverDataUnit.VideoListsResponse;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VideoRepository {
    private final ServerAPInterface serverAPINOAPI;
    private VideoDao videoDao;
    private ServerAPInterface serverAPI;
    private Executor executor = Executors.newSingleThreadExecutor();
    private final String TAG = "VideoRepository";

    public VideoRepository(AppDB db) {
        videoDao = db.videoDao();
        serverAPI = ServerAPI.getInstance().getAPI();
        serverAPINOAPI = ServerAPI.getInstance().getAPIWithoutAPI();
    }

    public LiveData<List<PreviewVideoCard>> getMostViewedVideos() {
        MutableLiveData<List<PreviewVideoCard>> mostViewedLiveData = new MutableLiveData<>();

        // Step 1: Load data from Room and post to LiveData
        executor.execute(() -> {
            VideoList localVideos = new VideoList(videoDao.getAllVideosSync());
            Log.d(TAG, "Loaded videos from Room: " + localVideos.getVideos().size());
            mostViewedLiveData.postValue(localVideos.getVideos());
        });

        // Step 2: Fetch data from the server and update Room and LiveData
        serverAPI.getAllVideos().enqueue(new Callback<VideoListsResponse>() {
            @Override
            public void onResponse(Call<VideoListsResponse> call, Response<VideoListsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    VideoList videoList = new VideoList(response.body().getMostViewedVideos());

                    Log.d(TAG, "Fetched videos from server: " + videoList.getVideos().size());
                    mostViewedLiveData.postValue(videoList.getVideos());

                    // Update Room database with the new data
                    executor.execute(() -> {
                        videoDao.insertVideos(videoList.getVideos());
                        Log.d(TAG, "Inserted videos into Room DB: " + videoList.getVideos().size());
                    });
                }
            }

            @Override
            public void onFailure(Call<VideoListsResponse> call, Throwable t) {
                Log.e(TAG, "Failed to fetch videos from server", t);

                executor.execute(() -> {
                    VideoList localVideosFallback = new VideoList(videoDao.getAllVideosSync());
                    mostViewedLiveData.postValue(localVideosFallback.getVideos());
                });
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
        serverAPI.getAllVideos().enqueue(new Callback<VideoListsResponse>() {
            @Override
            public void onResponse(Call<VideoListsResponse> call, Response<VideoListsResponse> response) {
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
            public void onFailure(Call<VideoListsResponse> call, Throwable t) {
                Log.e(TAG, "Failed to fetch most recent videos from server", t);
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
        serverAPI.getVideoById(videoId).enqueue(new Callback<PreviewVideoCard>() {
            @Override
            public void onResponse(Call<PreviewVideoCard> call, Response<PreviewVideoCard> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PreviewVideoCard video = response.body();
                    if (video != null) {
                        videoLiveData.postValue(video);

                        // Update Room database with the new data
                        executor.execute(() -> videoDao.insertVideo(video));

                    }
                }
            }

            @Override
            public void onFailure(Call<PreviewVideoCard> call, Throwable t) {
                Log.e(TAG, "Failed to fetch video from server", t);
//                MainPage.getInstance().getSupportFragmentManager().popBackStack();
            }
        });

        return videoLiveData;
    }

    public void insertComment(MutableLiveData<PreviewVideoCard> videoLiveData, String commentText, String date) {
        if (videoLiveData.getValue() == null) {
            Log.e("Comment update", "Video is null. Comment action is not permitted.");
            return;
        }

        String videoId = videoLiveData.getValue().getVideoId();
        Log.d("Comment update", "Inserting comment for videoId: " + videoId + " with text: " + commentText);

        CommentRequest commentRequest = new CommentRequest(videoId, commentText, date);

        serverAPI.postComment(commentRequest).enqueue(new Callback<CommentItem>() {
            @Override
            public void onResponse(Call<CommentItem> call, Response<CommentItem> response) {
                Log.d("Comment update", "Server response: isSuccessful=" + response.isSuccessful() + ", body=" + response.body());

                if (response.isSuccessful() && response.body() != null) {
                    CommentItem newComment = response.body();
                    Log.d("Comment update", "Successfully added comment on the server: " + newComment.getComment());

                    executor.execute(() -> {
                        PreviewVideoCard video = videoLiveData.getValue();

                        Log.d("Comment update", "Current number of comments before adding: " + video.getComments().size());

                        video.getComments().add(newComment);
                        Log.d("Comment update", "New comment added. Total comments now: " + video.getComments().size());

                        // Post the updated video to LiveData and ensure it reflects in the UI
                        videoLiveData.postValue(video);
                        Log.d("Comment update", "Posted updated video to LiveData for videoId: " + videoId);

                        // Update Room database with the new data
                        videoDao.insertVideo(video);
                        Log.d("Comment update", "Inserted video with updated comments into Room.");
                    });
                } else {
                    Log.e("Comment update", "Failed to add comment: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<CommentItem> call, Throwable t) {
                Log.e("Comment update", "Error posting comment", t);
            }
        });
    }



    public void deleteComment(String videoId, String commentId, String userId) {
        Log.d("Comment delete", "Sending delete request with videoId: " + videoId + ", commentId: " + commentId + ", userId: " + userId);

        DeleteCommentRequest request = new DeleteCommentRequest(videoId, commentId, userId); // Pass commentId as String

        serverAPI.deleteComment(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("Comment delete", "Successfully deleted comment from the server");

                    executor.execute(() -> {
                        PreviewVideoCard video = videoDao.getVideoByIdSync(videoId);
                        if (video != null) {
                            video.getComments().removeIf(comment -> comment.getId().equals(commentId)); // Use String equals
                            videoDao.insertVideo(video); // Update Room after comment removal
                        }
                    });
                } else {
                    Log.e("Comment delete", "Failed to delete comment: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("Comment delete", "Error deleting comment", t);
            }
        });
    }

    public void incrementVideoViews(String videoId) {
        Log.d(TAG, "Attempting to increment views for videoId: " + videoId);

        VideoIdRequest request = new VideoIdRequest(videoId);

        serverAPI.incrementVideoViews(request).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Successfully incremented views on server for videoId: " + videoId);
                    executor.execute(() -> {

                        // Fetch the latest data from Room after updating views
                        PreviewVideoCard video = videoDao.getVideoByIdSync(videoId);

                        if (video != null) {
                            video.setViews(video.getViews() + 1);
                            videoDao.insertVideo(video);

                            // Explicitly fetch the updated data again to ensure UI gets the latest view count
                            PreviewVideoCard updatedVideo = videoDao.getVideoByIdSync(videoId);
                            Log.d(TAG, "Fetched updated views from Room: " + updatedVideo.getViews());

                            // Post the latest data to LiveData
                            MutableLiveData<PreviewVideoCard> liveVideo = (MutableLiveData<PreviewVideoCard>) getVideo(videoId);
                            liveVideo.postValue(updatedVideo);
                        }
                    });
                } else {
                    Log.e(TAG, "Failed to increment views on server: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Log.e(TAG, "Error while incrementing views", t);
            }
        });
    }

    public void likeVideo(MutableLiveData<PreviewVideoCard> video) {
        if (null == video.getValue()) {
            Log.e(TAG, "Video is null. Like action is not permitted.");
            return;
        }
        String videoId = video.getValue().getVideoId();
        if (null == videoId) {
            Log.e(TAG, "Video not found. Like action is not permitted.");
            return;
        }

        if (null == LoggedInUser.getUser().getValue()) {
            Log.e(TAG, "User is not logged in. Like action is not permitted.");
            return;
        }
        String userId = LoggedInUser.getUser().getValue().getUserId();
        if (null == userId) {
            Log.e(TAG, "Logged in user not found. Like action is not permitted.");
            return;
        }

        Log.d(TAG, "Attempting to like video with ID: " + videoId + " by user: " + userId);

        LikeDislikeRequest request = new LikeDislikeRequest(videoId, userId);
        serverAPI.likeVideo(request).enqueue(new Callback<PreviewVideoCard>() {
            @Override
            public void onResponse(Call<PreviewVideoCard> call, Response<PreviewVideoCard> response) {
                if (!response.isSuccessful() || null == response.body()) {
                    Log.e(TAG, "Failed to like video on server. Response code: " + response.code() + ", message: " + response.message());
                }

                Log.d(TAG, "Successfully liked video. Response: " + response.body().toString());


                executor.execute(() -> {
                    PreviewVideoCard updatedVideo = response.body();
                    if (null == updatedVideo) {
                        Log.e(TAG, "Updated video is null.");
                        return;
                    }
                    // Update Room with the new video data
                    videoDao.insertVideo(updatedVideo);

                    // Notify observers with the updated video
                    video.postValue(updatedVideo);
                });
            }

            @Override
            public void onFailure(Call<PreviewVideoCard> call, Throwable t) {
                Log.e(TAG, "Error while liking video", t);
            }
        });
    }

    public void dislikeVideo(MutableLiveData<PreviewVideoCard> video) {
        if (null == video.getValue()) {
            Log.e(TAG, "Video is null. disLike action is not permitted.");
            return;
        }
        String videoId = video.getValue().getVideoId();
        if (null == videoId) {
            Log.e(TAG, "Video not found. disLike action is not permitted.");
            return;
        }

        if (null == LoggedInUser.getUser().getValue()) {
            Log.e(TAG, "User is not logged in. disLike action is not permitted.");
            return;
        }
        String userId = LoggedInUser.getUser().getValue().getUserId();
        if (null == userId) {
            Log.e(TAG, "Logged in user not found. disLike action is not permitted.");
            return;
        }

        Log.d(TAG, "Attempting to dislike video with ID: " + videoId + " by user: " + userId);

        LikeDislikeRequest request = new LikeDislikeRequest(videoId, userId);
        serverAPI.dislikeVideo(request).enqueue(new Callback<PreviewVideoCard>() {
            @Override
            public void onResponse(Call<PreviewVideoCard> call, Response<PreviewVideoCard> response) {
                if (!response.isSuccessful() || null == response.body()) {
                    Log.e(TAG, "Failed to like video on server. Response code: " + response.code() + ", message: " + response.message());
                }

                Log.d(TAG, "Successfully disliked video. Response: " + response.body().toString());


                executor.execute(() -> {
                    PreviewVideoCard updatedVideo = response.body();
                    if (null == updatedVideo) {
                        Log.e(TAG, "Updated video is null.");
                        return;
                    }
                    // Update Room with the new video data
                    videoDao.insertVideo(updatedVideo);

                    // Notify observers with the updated video
                    video.postValue(updatedVideo);
                });
            }

            @Override
            public void onFailure(Call<PreviewVideoCard> call, Throwable t) {
                Log.e(TAG, "Error while liking video", t);
            }
        });
    }
}
