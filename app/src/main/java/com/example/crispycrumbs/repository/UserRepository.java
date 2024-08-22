package com.example.crispycrumbs.repository;


import androidx.lifecycle.LiveData;
import com.example.crispycrumbs.dataUnit.UserItem;
import com.example.crispycrumbs.localDB.AppDB;
import com.example.crispycrumbs.localDB.UserDao;
import com.example.crispycrumbs.serverAPI.ServerAPI;
import com.example.crispycrumbs.serverAPI.ServerAPInterface;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserRepository {
    private UserDao userDao;
    private ServerAPInterface serverAPI;
    private Executor executor = Executors.newSingleThreadExecutor();

    public UserRepository(AppDB db) {
        userDao = db.userDao();
        serverAPI = ServerAPI.getInstance().getAPI();
    }

    public LiveData<UserItem> getUser(String userId) {
        LiveData<UserItem> user = userDao.getUserById(userId);

        executor.execute(() -> {
            serverAPI.getUser(userId).enqueue(new Callback<UserItem>() {
                @Override
                public void onResponse(Call<UserItem> call, Response<UserItem> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        executor.execute(() -> userDao.insertUser(response.body()));
                    }
                }

                @Override
                public void onFailure(Call<UserItem> call, Throwable t) {
                    // Handle error
                }
            });
        });

        return user;
    }

    public void insertUser(UserItem user) {
        executor.execute(() -> userDao.insertUser(user));
    }

    public void updateUser(UserItem user) {
        executor.execute(() -> userDao.updateUser(user));
    }

    // Additional methods for interacting with the server and handling sessions could be added here
}

