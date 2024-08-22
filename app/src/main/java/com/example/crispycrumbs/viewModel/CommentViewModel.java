package com.example.crispycrumbs.viewModel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.crispycrumbs.dataUnit.CommentItem;
import com.example.crispycrumbs.localDB.AppDB;
import com.example.crispycrumbs.localDB.AppDB;
import com.example.crispycrumbs.repository.CommentRepository;

import java.util.List;

public class CommentViewModel extends AndroidViewModel {
    private CommentRepository commentRepository;
    private LiveData<List<CommentItem>> comments;

    // Constructor now accepts Application context
    public CommentViewModel(Application application) {
        super(application);
        // Initialize the repository with the AppDatabase instance
        AppDB db = AppDB.getDatabase(application);
        commentRepository = new CommentRepository(db);
    }

    public LiveData<List<CommentItem>> getComments(String videoId) {
        if (comments == null) {
            comments = commentRepository.getCommentsForVideo(videoId);
        }
        return comments;
    }

    public void insertComment(CommentItem comment) {
        commentRepository.insertComment(comment);
    }

    public void updateComment(CommentItem comment) {
        commentRepository.updateComment(comment);
    }
}
