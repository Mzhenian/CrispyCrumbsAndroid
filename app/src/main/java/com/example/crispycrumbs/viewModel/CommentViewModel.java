package com.example.crispycrumbs.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.crispycrumbs.dataUnit.CommentItem;
import com.example.crispycrumbs.dataUnit.PreviewVideoCard;
import com.example.crispycrumbs.localDB.AppDB;
import com.example.crispycrumbs.repository.VideoRepository;

import java.util.List;

public class CommentViewModel extends AndroidViewModel {
    private VideoRepository videoRepository;
    private MutableLiveData<PreviewVideoCard> video;
    private MutableLiveData<List<CommentItem>> comments;

    // Constructor now accepts Application context and videoId
    public CommentViewModel(Application application, String videoId) {
        super(application);
        // Initialize the repository with the AppDatabase instance
        AppDB db = AppDB.getDatabase(application);
        videoRepository = new VideoRepository(db);
        video = new MutableLiveData<>();
        comments = new MutableLiveData<>();

        // Load the video data and set it to video LiveData
        loadVideoData(videoId);
    }

    private void loadVideoData(String videoId) {
        videoRepository.getVideo(videoId).observeForever(videoData -> {
            video.setValue(videoData);
            if (videoData != null) {
                comments.setValue(videoData.getComments());
            }
        });
    }

    public LiveData<List<CommentItem>> getComments() {
        return comments;
    }


}
