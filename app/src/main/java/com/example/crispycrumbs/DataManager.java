package com.example.crispycrumbs;

import android.content.Context;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DataManager {
    private static DataManager instance;
    private ArrayList<PreviewVideoCard> videoList;
    private Map<String, ArrayList<CommentItem>> commentsMap;

    private DataManager() {
        videoList = new ArrayList<>();
        commentsMap = new HashMap<>();
    }

    public static synchronized DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    public ArrayList<PreviewVideoCard> getVideoList() {
        return videoList;
    }

    public ArrayList<CommentItem> getCommentsForVideo(String videoId) {
        return commentsMap.getOrDefault(videoId, new ArrayList<>());
    }

    public void addCommentToVideo(String videoId, CommentItem comment) {
        if (!commentsMap.containsKey(videoId)) {
            commentsMap.put(videoId, new ArrayList<>());
        }
        // Avoid adding duplicate comments
        if (!commentsMap.get(videoId).contains(comment)) {
            commentsMap.get(videoId).add(comment);
        }
    }

    public void removeCommentFromVideo(String videoId, int position) {
        if (commentsMap.containsKey(videoId)) {
            ArrayList<CommentItem> comments = commentsMap.get(videoId);
            if (comments != null && position >= 0 && position < comments.size()) {
                comments.remove(position);
            }
        }
    }



    public void loadVideosFromJson(Context context) {
        if (!videoList.isEmpty()) {
            return; // Prevent reloading if already loaded
        }
        try {
            InputStream is = context.getAssets().open("videosDB.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            Gson gson = new Gson();
            VideoList videoListWrapper = gson.fromJson(json, VideoList.class);
            if (videoListWrapper != null && videoListWrapper.getVideos() != null) {
                for (PreviewVideoCard video : videoListWrapper.getVideos()) {
                    int thumbnailResId = context.getResources().getIdentifier(
                            video.getThumbnail(), "drawable", context.getPackageName());
                    video.setThumbnailResId(thumbnailResId);
                    videoList.add(video);
                    commentsMap.put(video.getVideoId(), video.getComments());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
