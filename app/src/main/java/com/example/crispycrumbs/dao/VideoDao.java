package com.example.crispycrumbs.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.crispycrumbs.dataUnit.PreviewVideoCard;

import java.util.List;

import retrofit2.http.DELETE;

@Dao
public interface VideoDao {

    // Insert a single video
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertVideo(PreviewVideoCard video);

    // Insert a list of videos
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertVideos(List<PreviewVideoCard> videos);

    // Fetch a single video by its ID (as LiveData)
    @Query("SELECT * FROM videos WHERE videoId = :videoId")
    LiveData<PreviewVideoCard> getVideoById(String videoId);

    // Fetch all videos (as LiveData)
    @Query("SELECT * FROM videos")
    LiveData<List<PreviewVideoCard>> getAllVideos();

    // Synchronous method to fetch all videos (used within a background thread)
    @Query("SELECT * FROM videos")
    List<PreviewVideoCard> getAllVideosSync();

    // Synchronous method to fetch a single video by its ID (used within a background thread)
    @Query("SELECT * FROM videos WHERE videoId = :videoId")
    PreviewVideoCard getVideoByIdSync(String videoId);

    @Query("SELECT * FROM videos WHERE userId = :userId")
    List<PreviewVideoCard> getVideosByUserIdSync(String userId);

    @Query("SELECT * FROM videos WHERE userId = :userId")
    LiveData<List<PreviewVideoCard>> getVideosByUserId(String userId);

    @Query("DELETE FROM videos WHERE videoId = :videoId")
    void deleteVideoById(String videoId);

//    @DELETE("DELETE FROM videos WHERE videoId = :videoId")
//    void deleteVideo(String videoId);
//
//    @DELETE
//    void deleteVideo(PreviewVideoCard video);
}
