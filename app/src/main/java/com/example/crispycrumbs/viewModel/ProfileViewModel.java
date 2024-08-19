package com.example.crispycrumbs.viewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.crispycrumbs.data.LoggedInUser;
import com.example.crispycrumbs.data.UserItem;

public class ProfileViewModel extends ViewModel {
    private MutableLiveData<UserItem> user;

    public MutableLiveData<UserItem> getUser() {
        if (user == null) {
            user = (LoggedInUser.getUser() != null) ? new MutableLiveData<>(LoggedInUser.getUser()) : new MutableLiveData<>();
        }
            return user;
    }
}
