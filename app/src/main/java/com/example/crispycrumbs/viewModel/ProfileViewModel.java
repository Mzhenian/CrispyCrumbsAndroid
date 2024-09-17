package com.example.crispycrumbs.viewModel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.crispycrumbs.dataUnit.UserItem;
import com.example.crispycrumbs.localDB.AppDB;
import com.example.crispycrumbs.localDB.LoggedInUser;
import com.example.crispycrumbs.repository.UserRepository;
import com.example.crispycrumbs.view.MainPage;

import java.io.File;

public class ProfileViewModel extends AndroidViewModel {
    private UserRepository userRepository;
    private MutableLiveData<UserItem> user = new MutableLiveData<>();

    // Constructor that accepts an Application
    public ProfileViewModel(Application application) {
        super(application);
        AppDB db = AppDB.getDatabase(application); // Use application to initialize the DB
        userRepository = new UserRepository(db); // Initialize repository with DB instance
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

    public void updateUser(UserItem updatedUser, File profilePhotoFile) {
        Log.d("ProfileViewModel", "Updating user: " + updatedUser.getUserId());
        Log.d("ProfileViewModel", "Updated user details: " + updatedUser.toString());
        if (profilePhotoFile != null) {
            Log.d("ProfileViewModel", "Profile photo file: " + profilePhotoFile.getAbsolutePath());
        } else {
            Log.d("ProfileViewModel", "No profile photo to update.");
        }

        // Update Room database
        userRepository.updateUser(updatedUser);

        // Update on server with optional profile photo
        userRepository.updateUserOnServer(updatedUser, profilePhotoFile);
    }

    public void refreshUser(String userId) {
        // Force re-fetching from Room or server
        user = (MutableLiveData<UserItem>) userRepository.getUser(userId);
    }

}




