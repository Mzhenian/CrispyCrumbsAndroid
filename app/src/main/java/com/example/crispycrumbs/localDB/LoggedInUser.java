package com.example.crispycrumbs.localDB;

import static com.example.crispycrumbs.view.MainPage.getInstance;

import android.widget.TextView;

import com.example.crispycrumbs.R;
import com.example.crispycrumbs.dataUnit.PreviewVideoCard;
import com.example.crispycrumbs.dataUnit.UserItem;

public class LoggedInUser {
    private static UserItem loggedInUser = null;
    private static String token = null;


    public static UserItem getUser() {
        return loggedInUser;
    }

    public static String getToken() {
        return token;
    }

    public static void setLoggedInUser(UserItem userItem) {
        LoggedInUser.loggedInUser = userItem;

        if (loggedInUser == null) {
            logOut();
        } else {
            getInstance().updateNavHeader();
        }
    }
    public static void setToken(String token) {
        LoggedInUser.token = token;
    }


    public static void logOut() {
        LoggedInUser.loggedInUser = null;
        LoggedInUser.token = null;
        TextView user_name = getInstance().findViewById(R.id.user_name);
        if (user_name != null) {
            user_name.setText("guest");
        }

        TextView user_email = getInstance().findViewById(R.id.user_email);
        if (user_email != null) {
            user_email.setText("");
        }

    }

    public static void removeVideo(PreviewVideoCard videoItem) {
        loggedInUser.delUploadedVideo(videoItem.getVideoId());
    }
}