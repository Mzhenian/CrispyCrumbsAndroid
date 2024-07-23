package com.example.crispycrumbs.data;

import static com.example.crispycrumbs.ui.MainPage.getInstance;

import android.widget.TextView;

import com.example.crispycrumbs.R;

public class LoggedInUser {
    private static UserItem loggedInUser = null;


    public static UserItem getUser() {
        return loggedInUser;
    }

    public static void SetLoggedInUser(UserItem userItem) {
        LoggedInUser.loggedInUser = userItem;

        if (loggedInUser == null) {
            LogOut();
        } else {
            getInstance().updateNavHeader();
        }
    }

    public static void LogOut() {
        LoggedInUser.loggedInUser = null;
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