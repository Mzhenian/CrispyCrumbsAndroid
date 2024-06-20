package com.example.crispycrumbs.model;

import android.content.Context;
import android.util.Log;

import com.example.crispycrumbs.Lists.UserList;
import com.example.crispycrumbs.Lists.VideoList;
import com.example.crispycrumbs.data.CommentItem;
import com.example.crispycrumbs.data.PreviewVideoCard;
import com.example.crispycrumbs.data.UserItem;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DataManager {
    private static DataManager instance;
    private ArrayList<PreviewVideoCard> videoList;
    private Map<String, ArrayList<CommentItem>> commentsMap;
    private ArrayList<UserItem> UserList;
    private Map<String, Integer> likesMap;
    private Map<String, Integer> dislikesMap;

    int nextUserId;

    private DataManager() {
        videoList = new ArrayList<>();
        commentsMap = new HashMap<>();
        UserList = new ArrayList<>();
        likesMap = new HashMap<>();
        dislikesMap = new HashMap<>();
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

    public ArrayList<UserItem> getUserList() {
        return UserList;
    }

    public UserItem getUserById(String userId) {
        for (UserItem user : UserList) {
            if (user.getUserId().equals(userId)) {
                return user;
            }
        }
        return null;
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
                    int thumbnailResId = context.getResources().getIdentifier(video.getThumbnail(), "drawable", context.getPackageName());
                    video.setThumbnailResId(thumbnailResId);
                    videoList.add(video);

                    commentsMap.put(video.getVideoId(), video.getComments());
                    likesMap.put(video.getVideoId(), video.getLikesCount());
                    dislikesMap.put(video.getVideoId(), video.getDislikesCount());

                    // Associate video with user
                    UserItem uploader = getUserById(video.getUserId());
                    if (uploader != null) {
                        video.setUploader(uploader);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    public void loadUsersFromJson(Context context) {
        try {
            InputStream is = context.getAssets().open("usersDB.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            Gson gson = new Gson();
            // First, parse the outer object
            JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
            // Extract the "users" array
            JsonArray usersArray = jsonObject.getAsJsonArray("users");

            Type userListType = new TypeToken<ArrayList<UserItem>>(){}.getType();
            ArrayList<UserItem> users = gson.fromJson(usersArray, userListType);

            if (users != null) {
                this.UserList.clear();
                this.UserList.addAll(users);
                Log.d("DataManager", "Loaded " + users.size() + " users.");
            } else {
                Log.e("DataManager", "Failed to parse users JSON: users list is null.");
            }
        } catch (JsonSyntaxException e) {
            Log.e("DataManager", "JsonSyntaxException: Failed to parse JSON", e);
        } catch (IOException e) {
            Log.e("DataManager", "IOException: Failed to read JSON file", e);
        }
    }




    public UserItem createUser(Context context, String username, String password, String displayedName, String email, String phoneNumber, Date dateOfBirth, String country, String profilePicPath) {
        UserItem newUser = new UserItem(username, password, displayedName, email, phoneNumber, dateOfBirth, country, profilePicPath);
        return newUser;
    }

    public UserItem createUser(Context context, String username, String password, String displayedName, String email, String phoneNumber, Date dateOfBirth, String country, int profilePicResId) {
        UserItem newUser = new UserItem(username, password, displayedName, email, phoneNumber, dateOfBirth, country, profilePicResId);
        return newUser;
    }

    public void addUser(UserItem user) {
        UserList.add(user);
    }

    public String lastUserId() {
        String last = "";
        for (UserItem user : UserList) {
            if (user.getUserId().compareTo(last) > 0) {
                last = user.getUserId();
            }
        }
        return last;
    }

    public void incrementLikes(String videoId) {
        if (likesMap.containsKey(videoId)) {
            likesMap.put(videoId, likesMap.get(videoId) + 1);
        }
    }

    public void decrementLikes(String videoId) {
        if (likesMap.containsKey(videoId)) {
            likesMap.put(videoId, likesMap.get(videoId) - 1);
        }
    }

    public void incrementDislikes(String videoId) {
        if (dislikesMap.containsKey(videoId)) {
            dislikesMap.put(videoId, dislikesMap.get(videoId) + 1);
        }
    }

    public void decrementDislikes(String videoId) {
        if (dislikesMap.containsKey(videoId)) {
            dislikesMap.put(videoId, dislikesMap.get(videoId) - 1);
        }
    }

    public int getLikesCount(String videoId) {
        return likesMap.getOrDefault(videoId, 0);
    }

    public int getDislikesCount(String videoId) {
        return dislikesMap.getOrDefault(videoId, 0);
    }

    public PreviewVideoCard getVideoById(String videoId) {
        for (PreviewVideoCard video : videoList) {
            if (video.getVideoId().equals(videoId)) {
                return video;
            }
        }
        return null;
    }
}

