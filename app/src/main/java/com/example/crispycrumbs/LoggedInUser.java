package com.example.crispycrumbs;

import com.example.crispycrumbs.data.UserItem;

public class LoggedInUser {
    private static UserItem userItem = null;
    private LoggedInUser() {
    }

    public static UserItem getUser() {
        return userItem;
    }
    public static void SetLoggedInUser(UserItem userItem) {
        LoggedInUser.userItem = userItem;
    }
    public static void LogOut() {
        LoggedInUser.userItem = null;
    }

}