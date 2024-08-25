package com.example.crispycrumbs.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.crispycrumbs.dataUnit.UserItem;
import com.example.crispycrumbs.localDB.AppDB;
import com.example.crispycrumbs.dao.UserDao;
import com.example.crispycrumbs.serverAPI.ServerAPI;
import com.example.crispycrumbs.serverAPI.ServerAPInterface;

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

        // Fetch user from local database first
        executor.execute(() -> {
            UserItem localUser = userDao.getUserByIdSync(userId);
            if (localUser != null) {
                userLiveData.postValue(localUser);
            }
        });

        // Fetch user from the server and update Room and LiveData
        serverAPI.getUser(userId).enqueue(new Callback<ApiResponse<UserItem>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserItem>> call, Response<ApiResponse<UserItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserItem user = response.body().getData();
                    userLiveData.postValue(user);

                    // Insert user into Room database
                    executor.execute(() -> userDao.insertUser(user));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserItem>> call, Throwable t) {
                // Handle error
            }
        });

        return userLiveData;
    }

    public void insertUser(UserItem user) {
        executor.execute(() -> userDao.insertUser(user));
    }

    public void updateUser(UserItem user) {
        executor.execute(() -> userDao.updateUser(user));
    }

    // Additional methods for interacting with the server and handling sessions could be added here
}
