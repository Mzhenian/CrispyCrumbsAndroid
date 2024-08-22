package com.example.crispycrumbs.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.example.crispycrumbs.dataUnit.UserItem;
import com.example.crispycrumbs.repository.UserRepository;

public class UserViewModel extends ViewModel {
    private UserRepository userRepository;
    private LiveData<UserItem> user;

    public UserViewModel() {
        userRepository = new UserRepository(); // Initialize your repository
    }

    public LiveData<UserItem> getUser(String userId) {
        if (user == null) {
            user = userRepository.getUser(userId);
        }
        return user;
    }

    public void updateUser(UserItem updatedUser) {
        userRepository.updateUser(updatedUser);
    }

    public void insertUser(UserItem newUser) {
        userRepository.insertUser(newUser);
    }
}

