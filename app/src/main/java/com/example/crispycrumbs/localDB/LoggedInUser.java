package com.example.crispycrumbs.localDB;

import static com.example.crispycrumbs.view.MainPage.getInstance;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.crispycrumbs.dataUnit.PreviewVideoCard;
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
        Application application = MainPage.getInstance().getApplication();
        AppDB db = AppDB.getDatabase(application);
        UserRepository userRepository = new UserRepository(db);
        LiveData<UserItem> user = userRepository.getUser(userId);
        user.observe(getInstance(), new Observer<UserItem>() {
            @Override
            public void onChanged(UserItem userItem) {
                loggedInUser.postValue(userItem);
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
        LoggedInUser.loggedInUser.postValue(null);
        LoggedInUser.token = null;
        sharedPreferences.edit()
                .remove(LIU_ID_KEY)
                .remove(LIU_TOKEN_KEY)
                .apply();
    }
}