package com.example.crispycrumbs.model;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
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

    public static  String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = MainPage.getInstance().getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                result = cursor.getString(nameIndex);
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
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
}


