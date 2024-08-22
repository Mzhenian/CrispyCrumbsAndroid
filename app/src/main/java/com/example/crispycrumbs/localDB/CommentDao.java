package com.example.crispycrumbs.localDB;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.crispycrumbs.dataUnit.CommentItem;

import java.util.List;

@Dao
public interface CommentDao {
    @Insert
    void insertComment(CommentItem comment);

    @Query("SELECT * FROM comments WHERE videoId = :videoId")
    LiveData<List<CommentItem>> getCommentsForVideo(String videoId);

    @Query("SELECT * FROM comments")
    LiveData<List<CommentItem>> getAllComments();
}

