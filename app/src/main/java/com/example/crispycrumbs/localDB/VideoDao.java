package com.example.crispycrumbs.localDB;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.crispycrumbs.dataUnit.PreviewVideoCard;

import java.util.List;

@Dao
public interface VideoDao {
    @Insert
    void insertVideo(PreviewVideoCard video);

    @Query("SELECT * FROM videos WHERE videoId = :videoId")
    LiveData<PreviewVideoCard> getVideoById(String videoId);

    @Query("SELECT * FROM videos")
    LiveData<List<PreviewVideoCard>> getAllVideos();
}


