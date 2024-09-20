package com.example.crispycrumbs.localDB;

import static com.example.crispycrumbs.view.MainPage.getInstance;

import android.widget.TextView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.crispycrumbs.R;
import com.example.crispycrumbs.dataUnit.PreviewVideoCard;
import com.example.crispycrumbs.dataUnit.UserItem;

public class LoggedInUser {
    private static MutableLiveData<UserItem> loggedInUser = new MutableLiveData<>();
    private static String token = null;


    public static LiveData<UserItem> getUser() {
        return loggedInUser;
    }

    public static String getToken() {
        return token;
    }

    public static void setLoggedInUser(UserItem userItem) {
        if (null == userItem) {
            logOut();
            return;
        }
        LoggedInUser.loggedInUser.postValue(userItem);
    }
    public static void setToken(String token) {
        LoggedInUser.token = token;
    }


    public static void logOut() {
        LoggedInUser.loggedInUser.postValue(null);
        LoggedInUser.token = null;
    }

    public static void removeVideo(PreviewVideoCard videoItem) {
        loggedInUser.getValue().delUploadedVideo(videoItem.getVideoId());
    }
}