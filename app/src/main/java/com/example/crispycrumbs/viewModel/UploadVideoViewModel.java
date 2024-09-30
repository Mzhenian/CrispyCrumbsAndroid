package com.example.crispycrumbs.viewModel;

import android.app.Application;
import android.net.Uri;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.crispycrumbs.localDB.AppDB;
import com.example.crispycrumbs.repository.VideoRepository;

import java.util.List;

public class UploadVideoViewModel extends AndroidViewModel {
//    public static final String TAG = "UploadVideoViewModel";
    public static final List<String> CATEGORIES = List.of(
            "Schnitzel",
            "Crispy Chicken",
            "Cheese Bites",
            "Crispy Fish",
            "Crispy Vegetables",
            "Crispy Desserts",
            "Crispy Snacks",
            "Crispy Recipes",
            "Crispy Ice Cream",
            "Crispy Steak");
    private final VideoRepository videoRepository;

    public UploadVideoViewModel(Application application) {
        super(application);
        AppDB db = AppDB.getDatabase(application);
        videoRepository = new VideoRepository(db); // Initialize the repository with AppDatabase
    }

    public void upload(Uri videoUri, Uri thumbnailUri, String title, String description, String category, List<String> tags, MutableLiveData<Boolean> uploadStatus) {
        videoRepository.upload(videoUri, thumbnailUri, title, description, category, tags, uploadStatus);
    }
}
