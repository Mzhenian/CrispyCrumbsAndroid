package com.example.crispycrumbs.localDB;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.crispycrumbs.dataUnit.UserItem;
import com.example.crispycrumbs.repository.UserRepository;
import com.example.crispycrumbs.view.MainPage;

public class LoggedInUser {
    public static final String LIU_ID_KEY = "LIU_KEY";
    public static final String LIU_TOKEN_KEY = "TOKEN_KEY";
    private static MutableLiveData<UserItem> loggedInUser = new MutableLiveData<>();
    private static String token = null;
    private static SharedPreferences sharedPreferences = MainPage.getInstance().getPreferences(Context.MODE_PRIVATE);

    public static LiveData<UserItem> getUser() {
        return loggedInUser;
    }

    public static String getToken() {
        return token;
    }

    public static void setToken(String token) {
        LoggedInUser.token = token;
        sharedPreferences.edit().putString(LIU_TOKEN_KEY, token).apply();
    }

    public static void setLoggedInUser(String userId) {

        if (null == userId) {
            logOut();
            return;
        }
        LiveData<UserItem> user = UserRepository.getInstance().getUser(userId);
        user.observe(MainPage.getInstance(), new Observer<UserItem>() {
            @Override
            public void onChanged(UserItem userItem) {
                setLoggedInUser(userItem);
                user.removeObserver(this);
            }
        });
    }

    public static void setLoggedInUser(UserItem userItem) {
        if (null == userItem) {
            logOut();
            return;
        }
        loggedInUser.postValue(userItem);
        sharedPreferences.edit().putString(LIU_ID_KEY, userItem.getUserId()).apply();
    }

    public static void logOut() {
        if (null != LoggedInUser.loggedInUser) {
            loggedInUser.postValue(null);
        }
        token = null;
        sharedPreferences.edit()
                .remove(LIU_ID_KEY)
                .remove(LIU_TOKEN_KEY)
                .apply();
    }
}