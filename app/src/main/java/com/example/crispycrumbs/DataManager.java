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
        commentsMap.get(videoId).add(comment);
    }

    public void loadVideosFromJson(Context context) {
        try {
            // Open the JSON file named "videosDB.json" from the assets folder
            InputStream is = context.getAssets().open("videosDB.json");

            // Get the size of the input stream (i.e., the file size)
            int size = is.available();

            // Create a buffer array to hold the data from the input stream
            byte[] buffer = new byte[size];

            // Read the data from the input stream into the buffer
            is.read(buffer);

            // Close the input stream as it's no longer needed
            is.close();

            // Convert the buffer array to a string using UTF-8 encoding
            String json = new String(buffer, StandardCharsets.UTF_8);

            // Create a new instance of Gson to handle JSON deserialization
            Gson gson = new Gson();

            // Deserialize the JSON string into a VideoList object
            VideoList videoListWrapper = gson.fromJson(json, VideoList.class);

            // Check if the deserialized object is not null and contains a list of videos
            if (videoListWrapper != null && videoListWrapper.getVideos() != null) {
                // Iterate over each video in the list
                for (PreviewVideoCard video : videoListWrapper.getVideos()) {
                    // Get the resource ID for the video's thumbnail using its name
                    int thumbnailResId = context.getResources().getIdentifier(
                            video.getThumbnail(), "drawable", context.getPackageName());

                    // Set the resource ID for the video's thumbnail
                    video.setThumbnailResId(thumbnailResId);

                    // Add the video to the video list
                    videoList.add(video);

                    // Map the video's ID to its comments and add it to the comments map
                    commentsMap.put(video.getVideoId(), video.getComments());
                }
            }
        } catch (IOException e) {
            // Print the stack trace if an IOException occurs
            e.printStackTrace();
        }
    }

}
