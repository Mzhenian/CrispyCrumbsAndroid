package com.example.crispycrumbs.repository;

import androidx.lifecycle.LiveData;
import com.example.crispycrumbs.dataUnit.CommentItem;
import com.example.crispycrumbs.localDB.AppDB;
import com.example.crispycrumbs.localDB.CommentDao;
import com.example.crispycrumbs.serverAPI.ServerAPI;
import com.example.crispycrumbs.serverAPI.ServerAPInterface;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CommentRepository {
    private CommentDao commentDao;
    private ServerAPInterface serverAPI;
    private Executor executor = Executors.newSingleThreadExecutor();

    public CommentRepository(AppDB db) {
        commentDao = db.commentDao();
        serverAPI = ServerAPI.getInstance().getAPI();
    }

    public LiveData<List<CommentItem>> getCommentsForVideo(String videoId) {
        // Fetch comments for a particular video
        return commentDao.getAllComments(); // Adjust this query if filtering by videoId
    }

    public void insertComment(CommentItem comment) {
        executor.execute(() -> commentDao.insertComment(comment));
    }

    public void updateComment(CommentItem comment) {
        executor.execute(() -> commentDao.updateComment(comment));
    }

    // Methods for fetching and syncing comments with the server can be added here
}

