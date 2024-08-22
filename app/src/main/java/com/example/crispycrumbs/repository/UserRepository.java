package com.example.crispycrumbs.repository;

import androidx.lifecycle.LiveData;
import com.example.crispycrumbs.dataUnit.UserItem;
import com.example.crispycrumbs.localDB.AppDB;
import com.example.crispycrumbs.localDB.UserDao;
import com.example.crispycrumbs.serverAPI.ServerAPI;
import com.example.crispycrumbs.serverAPI.ServerAPInterface;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class UserRepository {
    private UserDao userDao;
    private ServerAPInterface serverAPI;
    private Executor executor = Executors.newSingleThreadExecutor();

    public UserRepository(AppDB db) {
        userDao = db.userDao();
        serverAPI = ServerAPI.getInstance().getAPI();
    }

    public LiveData<UserItem> getUser(String userId) {
        // Fetch from Room (offline)
        LiveData<UserItem> user = userDao.getUserById(userId);

        // You might want to add logic here to fetch from the network if needed
        // For example, if the user is not found locally, fetch from the server and save in Room

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

