package com.example.crispycrumbs.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.crispycrumbs.dataUnit.UserItem;
import com.example.crispycrumbs.localDB.AppDB;
import com.example.crispycrumbs.repository.UserRepository;

public class UserViewModel extends AndroidViewModel {
    private UserRepository userRepository;
    private LiveData<UserItem> user;

    public UserViewModel(Application application) {
        super(application);
        AppDB db = AppDB.getDatabase(application);
        userRepository = new UserRepository(db); // Initialize the repository with AppDatabase
    }

    public LiveData<UserItem> getUser(String userId) {
        return userRepository.getUser(userId);
    }

    public void updateUser(UserItem updatedUser) {
        userRepository.updateUser(updatedUser);
    }

    public void insertUser(UserItem newUser) {
        userRepository.insertUser(newUser);
    }
}
