package com.example.crispycrumbs.viewModel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.crispycrumbs.dataUnit.PreviewVideoCard;
import com.example.crispycrumbs.localDB.AppDB;
import com.example.crispycrumbs.localDB.LoggedInUser;
import com.example.crispycrumbs.repository.VideoRepository;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class EditVideoViewModel extends AndroidViewModel {
    private final VideoRepository videoRepository;
    private static final String TAG = "EditVideoViewModel";

    private MutableLiveData<PreviewVideoCard> video;

    public EditVideoViewModel(Application application) {
        super(application);
        AppDB db = AppDB.getDatabase(application);
        videoRepository = new VideoRepository(db);
    }

    public LiveData<PreviewVideoCard> getVideo() {
        return video;
    }

    public void setVideo(MutableLiveData<PreviewVideoCard> video) {
        this.video = video;
    }

    public void setVideo(String videoId) {
        video = (MutableLiveData<PreviewVideoCard>) videoRepository.getVideo(videoId);
    }

    public void updateVideo(Map<String, RequestBody> videoFields, MultipartBody.Part thumbnail) {
        if (null == LoggedInUser.getUser().getValue() || null == video.getValue() || !LoggedInUser.getUser().getValue().getUserId().equals(video.getValue().getUserId()) ) {
            Log.e(TAG, "Uploader not logged in, so forbidden to update video");
            return;
        }
        String userId = LoggedInUser.getUser().getValue().getUserId();
        String videoId = video.getValue().getVideoId();
        videoRepository.updateVideo (userId, videoId, videoFields, thumbnail);
    }

    public void deleteVideo() {
        videoRepository.deleteVideo(video.getValue().getVideoId());
    }
}
