package com.example.crispycrumbs.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.crispycrumbs.dao.UserDao;
import com.example.crispycrumbs.dataUnit.UserItem;
import com.example.crispycrumbs.localDB.AppDB;
import com.example.crispycrumbs.serverAPI.ServerAPI;
import com.example.crispycrumbs.serverAPI.ServerAPInterface;
import com.example.crispycrumbs.serverAPI.serverDataUnit.UserResponse;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {
    private UserDao userDao;
    private ServerAPInterface serverAPI;
    private Executor executor = Executors.newSingleThreadExecutor();

    public UserRepository(AppDB db) {
        userDao = db.userDao();
        serverAPI = ServerAPI.getInstance().getAPI();
    }

    public LiveData<UserItem> getUser(String userId) {
        MutableLiveData<UserItem> userLiveData = new MutableLiveData<>();

        // Fetch the user from Room
        executor.execute(() -> {
            UserItem localUser = userDao.getUserByIdSync(userId);
            if (localUser != null) {
                userLiveData.postValue(localUser);
            }
        });

        // Fetch the user from the server and update Room and LiveData
        serverAPI.getUser(userId).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserItem userItem = response.body().toUserItem();
                    userLiveData.postValue(userItem);

                    // Update Room database with the new data
                    executor.execute(() -> userDao.insertUser(userItem));
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e("UserRepository", "Failed to fetch user from server", t);
            }
        });

        return userLiveData;
    }


    public void insertUserToRoom(UserItem user) {
        executor.execute(() -> userDao.insertUser(user));
    }

    public void updateUserFromRoom(UserItem user) {
        executor.execute(() -> userDao.updateUser(user));
    }

    // Additional methods for interacting with the server and handling sessions could be added here
}
