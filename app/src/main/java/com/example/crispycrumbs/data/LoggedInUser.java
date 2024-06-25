package com.example.crispycrumbs.data;

import static com.example.crispycrumbs.ui.MainPage.getInstance;

import android.widget.ImageView;
import android.widget.TextView;

import com.example.crispycrumbs.R;
import com.example.crispycrumbs.ui.MainPage;

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
//            TextView user_name = MainPage.getInstance().findViewById(R.id.user_name);
//            user_name.setText(getUser().getUserName());

//            TextView user_email = MainPage.getInstance().findViewById(R.id.user_email);
//            user_email.setText(getUser().getEmail());

//            ImageView profile_picture = MainPage.getInstance().findViewById(R.id.profile_picture);
            //            todo
            //            profile_picture.setImageDrawable(getUser().getProfilePicture());
        }
    }
    public static void LogOut() {
        LoggedInUser.loggedInUser = null;
        TextView user_name = getInstance().findViewById(R.id.user_name);
        user_name.setText("guest");

        TextView user_email = getInstance().findViewById(R.id.user_email);
        user_email.setText("");

    };
    public static  void  removeVideo(PreviewVideoCard videoItem) {
        loggedInUser.getUploadedVideos().remove(videoItem);
    }
}