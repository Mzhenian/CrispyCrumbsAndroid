package com.example.crispycrumbs.dao;

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

    @Query("SELECT * FROM comments")
    LiveData<List<CommentItem>> getAllComments();
}

