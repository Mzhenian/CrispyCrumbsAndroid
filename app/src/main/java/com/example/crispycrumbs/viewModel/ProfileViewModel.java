package com.example.crispycrumbs.viewModel;

import android.app.Activity;
import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.crispycrumbs.dataUnit.UserItem;
import com.example.crispycrumbs.localDB.AppDB;
import com.example.crispycrumbs.localDB.LoggedInUser;
import com.example.crispycrumbs.repository.UserRepository;
import com.example.crispycrumbs.serverAPI.serverInterface.UserUpdateCallback;
import com.example.crispycrumbs.view.MainPage;

import java.io.File;
import java.io.IOException;

public class ProfileViewModel extends AndroidViewModel {
    private UserRepository userRepository;
    private MutableLiveData<UserItem> user = new MutableLiveData<>();

    // Constructor that accepts an Application
    public ProfileViewModel(Application application) {
        super(application);
        userRepository = UserRepository.getInstance();
    }

    public LiveData<UserItem> getUser(String userId) {
        // If userId is null, try to fetch the logged-in user
        if (user.getValue() == null) {
            if (userId == null) {
                UserItem loggedUser = LoggedInUser.getUser().getValue();
                if (loggedUser != null) {
                    userId = loggedUser.getUserId();
                }
            }
            user = (MutableLiveData<UserItem>) userRepository.getUser(userId);
        }
        return user;
    }


    public void updateUser(UserItem updatedUser, Uri profilePhotoUri, UserUpdateCallback callback) {
        Log.d("Update user", "Updating user: " + updatedUser.getUserId());

        if (profilePhotoUri != null) {
            Log.d("Update user", "Profile photo Uri: " + profilePhotoUri);
        } else {
            Log.d("Update user", "No profile photo to update.");
        }

        // Update Room database
        userRepository.updateUserFromRoom(updatedUser);

        // Update on server with optional profile photo and callback
        userRepository.updateUserOnServer(updatedUser, profilePhotoUri, callback);
    }

    public void deleteUser() {
        userRepository.deleteUser(user.getValue().getUserId());
    }




    public void refreshUser(String userId) {
        // Force re-fetching from Room or server
        user = (MutableLiveData<UserItem>) userRepository.getUser(userId);
    }

}




