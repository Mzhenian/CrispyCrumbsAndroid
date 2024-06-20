package com.example.crispycrumbs.model;

import android.content.Context;
import android.util.Log;

import com.example.crispycrumbs.Lists.UserList;
import com.example.crispycrumbs.Lists.VideoList;
import com.example.crispycrumbs.data.CommentItem;
import com.example.crispycrumbs.data.PreviewVideoCard;
import com.example.crispycrumbs.data.UserItem;
import com.google.gson.Gson;
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
    int nextUserId;

    private DataManager() {
        videoList = new ArrayList<>();
        commentsMap = new HashMap<>();
        UserList = new ArrayList<>();
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
            UserList userListWrapper = gson.fromJson(json, UserList.class);
            if (userListWrapper != null && userListWrapper.getUsers() != null) {
                for (UserItem user : userListWrapper.getUsers()) {
                    // Set the profilePicURI to the profilePhoto value from the JSON file
                    String profilePhoto = user.getProfilePicURI();
                    if (profilePhoto != null && !profilePhoto.isEmpty()) {
                        user.setProfilePicURI("android.resource://" + context.getPackageName() + "/" + profilePhoto);
                    } else {
                        // If profilePhoto is null or empty, set it to the resource name of the default user picture
                        user.setProfilePicURI("android.resource://" + context.getPackageName() + "/drawable/default_user_pic");
                    }
                    this.UserList.add(user);
                }
            }

        } catch (JsonSyntaxException e) {
            Log.e("DataManager", "JsonSyntaxException: Failed to parse JSON", e);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveUsersToJson(Context context) {
        Gson gson = new Gson();
        String json = gson.toJson(UserList);
        try {
            FileOutputStream fos = context.openFileOutput("usersDB.json", Context.MODE_PRIVATE);
            fos.write(json.getBytes(StandardCharsets.UTF_8));
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
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
}
