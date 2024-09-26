package com.example.crispycrumbs.model;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.crispycrumbs.List.UserList;
import com.example.crispycrumbs.List.VideoList;
import com.example.crispycrumbs.R;
import com.example.crispycrumbs.dataUnit.CommentItem;
import com.example.crispycrumbs.dataUnit.PreviewVideoCard;
import com.example.crispycrumbs.dataUnit.UserItem;
import com.example.crispycrumbs.localDB.LoggedInUser;
import com.example.crispycrumbs.view.MainPage;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DataManager {
    public static final String PACKAGE_NAME = MainPage.getInstance().getPackageName();
    public static final int NO_LIKE_DISLIKE = 0, LIKE = 1, DISLIKE = -1;
    private static final String TAG = "DataManager";
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static DataManager instance;
    private final VideoList videoList;
    private final Map<String, ArrayList<CommentItem>> commentsMap;
    private final ArrayList<UserItem> UserList;
    private final Map<String, Integer> likesMap;
    private final Map<String, Integer> dislikesMap;
    String lastUserId;
    String lastVideoId;
    int nextUserId;
    private VideoList personalVideoList;

    private DataManager() {
        videoList = new VideoList();
        if (LoggedInUser.getUser() != null) {
            personalVideoList = new VideoList();
        }
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

    public static Uri getUriFromResOrFile(String path) {
        try {
            int resId = MainPage.getInstance().getResources().getIdentifier(path, "raw", PACKAGE_NAME);
            if (resId != 0) {
                return Uri.parse("android.resource://" + PACKAGE_NAME + "/" + resId);
            } else {
                return Uri.parse(path);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse URI: " + path, e);
            return null;
        }
    }

    public static String getDefaultProfilePhoto() {
        return Uri.parse("android.resource://" + MainPage.getInstance().getPackageName() + "/" + R.drawable.default_profile_picture).toString();
    }

    public static Boolean checkStoragePermissions() {
        if (ContextCompat.checkSelfPermission(MainPage.getInstance(), READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainPage.getInstance(),
                    new String[]{READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);

            return (ContextCompat.checkSelfPermission(MainPage.getInstance(), READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        } else {
            return true;
        }
    }

    public ArrayList<UserItem> getUserList() {
        return UserList;
    }

    //todo connect to server
    public UserItem getUserById(String userId) {
        for (UserItem user : UserList) {
            if (user.getUserId().equals(userId)) {
                return user;
            }
        }
        return null;
    }

    public PreviewVideoCard getVideoById(String videoId) {
        for (PreviewVideoCard video : videoList.getVideos()) {
            if (video.getVideoId().equals(videoId)) {
                return video;
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

    public UserItem createUser(Context context, String username, String password, String displayedName, String email, String phoneNumber, Date dateOfBirth, String country, String profilePicPath) {
        UserItem newUser = new UserItem(username, password, displayedName, email, phoneNumber, dateOfBirth, country, profilePicPath);
        return newUser;
    }

    public void addUser(UserItem user) {
        UserList.add(user);
    }

    public String getFileExtension(Uri uri) {
        ContentResolver contentResolver = MainPage.getInstance().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        // Return file extension
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    public void deleteVideo(PreviewVideoCard video) {
        videoList.getVideos().remove(video);
    }

    public int likeClick(String videoId) {
        UserItem user = LoggedInUser.getUser().getValue();
        if (user == null) {
            return NO_LIKE_DISLIKE;
        }
        if (user.hasLiked(videoId)) {
            user.removeLike(videoId);

            int newLikes = likesMap.get(videoId) - 1;
            likesMap.put(videoId, newLikes);
            getVideoById(videoId).setLikes(newLikes);

            return NO_LIKE_DISLIKE;
        } else if (user.hasDisliked(videoId)) {
            user.delDislike(videoId);
            user.likeVideo(videoId);

            int newLikes = likesMap.get(videoId) + 1;
            int newDisLikes = dislikesMap.get(videoId) - 1;
            likesMap.put(videoId, newLikes);
            dislikesMap.put(videoId, newDisLikes);
            getVideoById(videoId).setLikes(newLikes);
            getVideoById(videoId).setDislikes(newDisLikes);
            return LIKE;
        } else { //didn't like or dislike
            user.likeVideo(videoId);
            int newLikes = likesMap.get(videoId) + 1;
            likesMap.put(videoId, newLikes);
            getVideoById(videoId).setLikes(newLikes);
            return LIKE;
        }
    }

    public int dislikeClick(String videoId) {
        UserItem user = LoggedInUser.getUser().getValue();
        if (user == null) {
            return NO_LIKE_DISLIKE;
        }
        if (user.hasDisliked(videoId)) {
            user.removeDislike(videoId);

            int newDisLikes = dislikesMap.get(videoId) - 1;
            dislikesMap.put(videoId, newDisLikes);
            getVideoById(videoId).setDislikes(newDisLikes);

            return NO_LIKE_DISLIKE;
        } else if (user.hasLiked(videoId)) {
            user.delLike(videoId);
            user.dislikeVideo(videoId);

            int newDisLikes = dislikesMap.get(videoId) + 1;
            int newLikes = likesMap.get(videoId) - 1;
            dislikesMap.put(videoId, newDisLikes);
            likesMap.put(videoId, newLikes);
            getVideoById(videoId).setDislikes(newDisLikes);
            getVideoById(videoId).setLikes(newLikes);
            return DISLIKE;
        } else { //didn't like or dislike
            user.dislikeVideo(videoId);
            int newDisLikes = dislikesMap.get(videoId) + 1;
            dislikesMap.put(videoId, newDisLikes);
            getVideoById(videoId).setDislikes(newDisLikes);
            return DISLIKE;
        }
    }

    public int getLikeDislike(String videoId) {
        UserItem user = LoggedInUser.getUser().getValue();
        if (user == null) {
            return NO_LIKE_DISLIKE;
        }
        if (user.hasLiked(videoId)) {
            return LIKE;
        } else if (user.hasDisliked(videoId)) {
            return DISLIKE;
        } else {
            return NO_LIKE_DISLIKE;
        }
    }

    public int getLikesCount(String videoId) {
        return likesMap.getOrDefault(videoId, 0);
    }

    public int getDislikesCount(String videoId) {
        return dislikesMap.getOrDefault(videoId, 0);
    }
}


