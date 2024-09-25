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
import com.example.crispycrumbs.serverAPI.serverDataUnit.LikeDislikeRequest;
import com.example.crispycrumbs.serverAPI.serverDataUnit.VideoIdRequest;
import com.example.crispycrumbs.serverAPI.serverDataUnit.VideoListsResponse;
import com.example.crispycrumbs.view.MainPage;

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
//        MultipartBody.Part videoPart = videoData.part(0);
//        MultipartBody.Part thumbnailPart = videoData.part(1);
//        RequestBody titlePart = videoData.part(2).body();
//        RequestBody descriptionPart = videoData.part(3).body();
//        RequestBody categoryPart = videoData.part(4).body();
//        RequestBody tagsPart = videoData.part(5).body();
//        RequestBody userIdPart = videoData.part(6).body();

        try {
//            serverAPInterface.upload(userId, videoPart, thumbnailPart, titlePart, descriptionPart, categoryPart, tagsPart, userIdPart).enqueue(new Callback<PreviewVideoCard>() {
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

        // Step 2: Fetch the video from the server and update Room and LiveData
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
