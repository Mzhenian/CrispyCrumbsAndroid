package com.example.crispycrumbs.repository;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.crispycrumbs.List.VideoList;
import com.example.crispycrumbs.R;
import com.example.crispycrumbs.dao.VideoDao;
import com.example.crispycrumbs.dataUnit.CommentItem;
import com.example.crispycrumbs.dataUnit.PreviewVideoCard;
import com.example.crispycrumbs.dataUnit.UserItem;
import com.example.crispycrumbs.localDB.AppDB;
import com.example.crispycrumbs.localDB.LoggedInUser;
import com.example.crispycrumbs.serverAPI.ServerAPI;
import com.example.crispycrumbs.serverAPI.ServerAPInterface;
import com.example.crispycrumbs.serverAPI.serverDataUnit.ApiResponse;
import com.example.crispycrumbs.serverAPI.serverDataUnit.CommentRequest;
import com.example.crispycrumbs.serverAPI.serverDataUnit.DeleteCommentRequest;
import com.example.crispycrumbs.serverAPI.serverDataUnit.EditCommentRequest;
import com.example.crispycrumbs.serverAPI.serverDataUnit.LikeDislikeRequest;
import com.example.crispycrumbs.serverAPI.serverDataUnit.VideoIdRequest;
import com.example.crispycrumbs.serverAPI.serverDataUnit.VideoListsResponse;
import com.example.crispycrumbs.view.MainPage;

import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VideoRepository {
    private final String TAG = "VideoRepository";
    private VideoDao videoDao;
    private ServerAPInterface serverAPInterface;
    private Executor executor = Executors.newSingleThreadExecutor();

    public VideoRepository(AppDB db) {
        videoDao = db.videoDao();
        serverAPInterface = ServerAPI.getInstance().getAPI();
    }

    public static File drawableToFile(int drawableResId, String fileName) {
        // Get the drawable resource
        Drawable drawable = MainPage.getInstance().getResources().getDrawable(drawableResId);


        // Convert drawable to Bitmap
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

        // Create a file in the external storage
        File file = new File(MainPage.getInstance().getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName + ".png");

        // Write the bitmap to the file
        try (FileOutputStream outStream = new FileOutputStream(file)) {
            // Compress bitmap and write it to the output stream
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
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
        serverAPInterface.getAllVideos().enqueue(new Callback<VideoListsResponse>() {
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
        serverAPInterface.getAllVideos().enqueue(new Callback<VideoListsResponse>() {
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
        serverAPInterface.getVideosByUser(userId).enqueue(new Callback<List<PreviewVideoCard>>() {
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

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = MainPage.getInstance().getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                result = cursor.getString(nameIndex);
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public void upload(Uri videoUri, Uri thumbnailUri, String title, String description, String category, List<String> tags, MutableLiveData<Boolean> uploadStatus) {
        ContentResolver contentResolver = MainPage.getInstance().getContentResolver();

        UserItem loggedInUser = LoggedInUser.getUser().getValue();
        if (null == loggedInUser) {
            MainPage.getInstance().runOnUiThread(() -> Toast.makeText(MainPage.getInstance(), "Please Login to upload a video.", Toast.LENGTH_SHORT).show());
            return;
        }
        String userId = loggedInUser.getUserId();

        if (title.isEmpty() || description.isEmpty() || category.isEmpty() || videoUri == null) {
            MainPage.getInstance().runOnUiThread(() -> Toast.makeText(MainPage.getInstance(), "Please fill in all required fields.", Toast.LENGTH_SHORT).show());
            return;
        }

        MultipartBody.Builder videoDataBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        try {
            InputStream videoInputStream = contentResolver.openInputStream(videoUri);
            if (null == videoInputStream) {
                throw new FileNotFoundException("Unable to open input stream for video URI");
            }

            // Create a RequestBody from the InputStream
            RequestBody requestBodyVideo = new RequestBody() {
                @Override
                public MediaType contentType() {
                    return MediaType.parse("video/*");  // Set the media type to video
                }

                @Override
                public void writeTo(BufferedSink sink) throws IOException {
                    // Write the InputStream to the BufferedSink
                    try (Source source = Okio.source(videoInputStream)) {
                        sink.writeAll(source);
                    }
                }
            };
            // Build the multipart form data
            videoDataBuilder.addFormDataPart("videoFile", getFileNameFromUri(videoUri), requestBodyVideo);

            if (thumbnailUri == null) {
                thumbnailUri = Uri.parse("android.resource://" + MainPage.getInstance().getPackageName() + "/" + R.drawable.default_video_thumbnail);
            }

            InputStream thumbnailInputStream = contentResolver.openInputStream(thumbnailUri);
            if (thumbnailInputStream == null) {
                throw new FileNotFoundException("Unable to open input stream for thumbnail URI");
            }

            RequestBody requestBodyImage = new RequestBody() {
                @Override
                public MediaType contentType() {
                    return MediaType.parse("image/*");  // Set the media type to video
                }

                @Override
                public void writeTo(BufferedSink sink) throws IOException {
                    // Write the InputStream to the BufferedSink
                    try (Source source = Okio.source(thumbnailInputStream)) {
                        sink.writeAll(source);
                    }
                }
            };
            videoDataBuilder.addFormDataPart("thumbnail", getFileNameFromUri(thumbnailUri), requestBodyImage);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        videoDataBuilder.addFormDataPart("title", title);
        videoDataBuilder.addFormDataPart("description", description);
        videoDataBuilder.addFormDataPart("category", category);
        videoDataBuilder.addFormDataPart("tags", String.join(",", tags));
        videoDataBuilder.addFormDataPart("userId", userId);

        MultipartBody videoData = videoDataBuilder.build();

        try {
            serverAPInterface.upload(userId, videoData.part(0), videoData.part(1), videoData.part(2).body(), videoData.part(3).body(),
                    videoData.part(4).body(), videoData.part(5).body(), videoData.part(6).body()).enqueue(new Callback<PreviewVideoCard>() {
                @Override
                public void onResponse(Call<PreviewVideoCard> call, Response<PreviewVideoCard> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        uploadStatus.postValue(true);
                    } else {
                        MainPage.getInstance().runOnUiThread(() -> Toast.makeText(MainPage.getInstance(), response.message(), Toast.LENGTH_SHORT).show());
                        uploadStatus.postValue(false);
                        Log.e(TAG, "failed to connect to server");

                    }
                }

                @Override
                public void onFailure(Call<PreviewVideoCard> call, Throwable t) {
                    uploadStatus.postValue(false);
                    Log.e(TAG, "failed to connect to server", t);
                }
            });
        } catch (Exception e) {
            MainPage.getInstance().runOnUiThread(() -> Toast.makeText(MainPage.getInstance(), e.getMessage(), Toast.LENGTH_SHORT).show());
            uploadStatus.postValue(false);
            Log.e(TAG, "failed to connect to server", e);

        }
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

        serverAPInterface.getVideoById(videoId).enqueue(new Callback<PreviewVideoCard>() {
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

                        // Clone the comments list to avoid concurrent modification issues
                        ArrayList<CommentItem> updatedComments = new ArrayList<>(video.getComments());
                        Log.d("Comment update", "Current number of comments before adding: " + updatedComments.size());

                        updatedComments.add(newComment);
                        Log.d("Comment update", "New comment added. Total comments now: " + updatedComments.size());

                        // Update the video with the cloned list
                        video.setComments(updatedComments);

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

    public void editComment(MutableLiveData<PreviewVideoCard> videoLiveData, String commentId, String newContent, String date) {
        if (videoLiveData.getValue() == null) {
            Log.e("Comment edit", "Video is null. Edit action is not permitted.");
            return;
        }

        String videoId = videoLiveData.getValue().getVideoId();
        Log.d("Comment edit", "Editing comment for videoId: " + videoId + ", commentId: " + commentId);

        // Create the new EditCommentRequest object
        EditCommentRequest editCommentRequest = new EditCommentRequest(videoId, commentId, LoggedInUser.getUser().getValue().getUserId(), newContent, date);

        // Send the edit request to the server
        serverAPI.editComment(editCommentRequest).enqueue(new Callback<PreviewVideoCard>() {
            @Override
            public void onResponse(Call<PreviewVideoCard> call, Response<PreviewVideoCard> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PreviewVideoCard updatedVideo = response.body();
                    Log.d("Comment edit", "Successfully edited comment on the server");

                    // Update Room database with the new video (including the edited comment)
                    executor.execute(() -> {
                        // Update the Room database
                        videoDao.insertVideo(updatedVideo);
                        Log.d("Comment edit", "Updated video in Room with the edited comment");

                        // Post the updated video to LiveData to update the UI
                        videoLiveData.postValue(updatedVideo);
                        Log.d("Comment edit", "Posted updated video to LiveData");
                    });
                } else {
                    Log.e("Comment edit", "Failed to edit comment on the server: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<PreviewVideoCard> call, Throwable t) {
                Log.e("Comment edit", "Error while editing comment", t);
            }
        });
    }

    public void deleteComment(MutableLiveData<PreviewVideoCard> videoLivaData, String commentId, String userId) {
        Log.d("Comment delete", "Sending delete request with videoId: " + videoLivaData.getValue().getVideoId() + ", commentId: " + commentId + ", userId: " + userId);

        DeleteCommentRequest request = new DeleteCommentRequest(videoLivaData.getValue().getVideoId(), commentId, userId); // Pass commentId as String

        serverAPI.deleteComment(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("Comment delete", "Successfully deleted comment from the server");

                    executor.execute(() -> {
                        PreviewVideoCard video = videoDao.getVideoByIdSync(videoLivaData.getValue().getVideoId());
                        if (video != null) {
                            video.getComments().removeIf(comment -> comment.getId().equals(commentId)); // Use String equals
                            videoDao.insertVideo(video); // Update Room after comment removal
                            videoLivaData.postValue(video);
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

        serverAPInterface.incrementVideoViews(request).enqueue(new Callback<ApiResponse<Void>>() {
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
        serverAPInterface.likeVideo(request).enqueue(new Callback<PreviewVideoCard>() {
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
        serverAPInterface.dislikeVideo(request).enqueue(new Callback<PreviewVideoCard>() {
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
