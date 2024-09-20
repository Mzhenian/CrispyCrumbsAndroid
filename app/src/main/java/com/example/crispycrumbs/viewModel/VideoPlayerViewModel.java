package com.example.crispycrumbs.viewModel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.crispycrumbs.R;
import com.example.crispycrumbs.dataUnit.CommentItem;
import com.example.crispycrumbs.dataUnit.PreviewVideoCard;
import com.example.crispycrumbs.dataUnit.UserItem;
import com.example.crispycrumbs.localDB.AppDB;
import com.example.crispycrumbs.localDB.LoggedInUser;
import com.example.crispycrumbs.repository.VideoRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VideoPlayerViewModel extends AndroidViewModel {
    public static final String TAG = "VideoPlayerViewModel";
    private final VideoRepository videoRepository;
    private final MutableLiveData<PreviewVideoCard> video = new MutableLiveData<>();

    public VideoPlayerViewModel(Application application) {
        super(application);
        AppDB db = AppDB.getDatabase(application);
        videoRepository = new VideoRepository(db); // Initialize the repository with AppDatabase
    }

    public LiveData<PreviewVideoCard> getVideo() {
        if (video.getValue() == null) {
            Log.d(TAG, "getVideo: video is null");
        }

        return video;
    }

    public void setVideo(String videoId) {
        videoRepository.getVideo(videoId).observeForever(video::setValue);
    }

    public void insertVideo(PreviewVideoCard video) {
        videoRepository.insertVideo(video);
    }

    public LiveData<List<PreviewVideoCard>> getVideosByUser(String userId) {
        return videoRepository.getVideosByUser(userId);
    }

    public void incrementVideoViews() {
        videoRepository.incrementVideoViews(video.getValue().getVideoId());
    }

    public void likeVideo() {
        videoRepository.likeVideo(video);
    }

    public void dislikeVideo() {
        videoRepository.dislikeVideo(video);
    }

    public String formatDate(String dateString) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("d/M/yyyy HH:mm", Locale.getDefault());
        Date date;
        try {
            date = inputFormat.parse(dateString);
        } catch (ParseException e) {
            Log.e(TAG, "Date parsing error: " + e.getMessage());
            return dateString; // Return the original string if parsing fails
        }
        return outputFormat.format(date);
    }

    public void insertComment(String videoId, String commentText) {
        Log.d("VideoPlayerViewModel", "Inserting comment for videoId: " + videoId);

        // Get the current date in the correct format
        String currentDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(new Date());

        // Retrieve the LiveData for the video
        MutableLiveData<PreviewVideoCard> videoLiveData = (MutableLiveData<PreviewVideoCard>) videoRepository.getVideo(videoId);

        // Call the repository to handle comment insertion
        videoRepository.insertComment(videoLiveData, videoId, commentText, currentDate);  // <-- Pass the LiveData instead of just videoId

        // Refresh the video to update the comment section UI
        setVideo(videoId);
    }


    public void deleteComment(String videoId, String commentId, String userId) {
        videoRepository.deleteComment(videoId, commentId, userId); // Pass commentId as String
    }

    public void refreshVideo(String videoId) {
        setVideo(videoId); // This should fetch the latest data from the server or Room
    }


}