package com.example.crispycrumbs.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.crispycrumbs.dataUnit.UserItem;
import com.example.crispycrumbs.localDB.LoggedInUser;
import com.example.crispycrumbs.view.MainPage;

public class ProfileViewModel extends ViewModel {
    private MutableLiveData<UserItem> user = new MutableLiveData<>();

    public LiveData<UserItem> getUser() {
        if (user.getValue() == null && LoggedInUser.getUser().getValue() != null) {
            setUser(LoggedInUser.getUser().getValue());
        }
        return user;
    }
    public void setUser(UserItem user) {
        MainPage.getInstance().runOnUiThread(() -> {
            this.user.setValue(user);
        });
    }
}
