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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CommentRepository {
    private CommentDao commentDao;
    private ServerAPInterface serverAPI;
    private Executor executor = Executors.newSingleThreadExecutor();

    public CommentRepository(AppDB db) {
        commentDao = db.commentDao();
        serverAPI = ServerAPI.getInstance().getAPI();
    }

    public LiveData<List<CommentItem>> getCommentsForVideo(String videoId) {
        LiveData<List<CommentItem>> comments = commentDao.getCommentsForVideo(videoId);

        executor.execute(() -> {
            serverAPI.getCommentsForVideo(videoId).enqueue(new Callback<List<CommentItem>>() {
                @Override
                public void onResponse(Call<List<CommentItem>> call, Response<List<CommentItem>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        executor.execute(() -> {
                            for (CommentItem comment : response.body()) {
                                commentDao.insertComment(comment);
                            }
                        });
                    }
                }

                @Override
                public void onFailure(Call<List<CommentItem>> call, Throwable t) {
                    // Handle error
                }
            });
        });

        return comments;
    }


    public void insertComment(CommentItem comment) {
        executor.execute(() -> commentDao.insertComment(comment));
    }

    public void updateComment(CommentItem comment) {
        executor.execute(() -> commentDao.updateComment(comment));
    }

    // Methods for fetching and syncing comments with the server can be added here
}

