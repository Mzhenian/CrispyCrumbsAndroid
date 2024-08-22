package com.example.crispycrumbs.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.example.crispycrumbs.dataUnit.CommentItem;
import com.example.crispycrumbs.repository.CommentRepository;
import java.util.List;

public class CommentViewModel extends ViewModel {
    private CommentRepository commentRepository;
    private LiveData<List<CommentItem>> comments;

    public CommentViewModel() {
        commentRepository = new CommentRepository(); // Initialize your repository
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

