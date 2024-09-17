package com.example.crispycrumbs.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.crispycrumbs.dataUnit.UserItem;
import com.example.crispycrumbs.localDB.AppDB;
import com.example.crispycrumbs.dao.UserDao;
import com.example.crispycrumbs.serverAPI.ServerAPI;
import com.example.crispycrumbs.serverAPI.ServerAPInterface;
import com.example.crispycrumbs.serverAPI.serverDataUnit.UserResponse;
import com.example.crispycrumbs.localDB.LoggedInUser;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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

        // If userId is null, get the logged-in user
        if (userId == null) {
            UserItem loggedInUser = LoggedInUser.getUser().getValue();
            if (loggedInUser != null) {
                userId = loggedInUser.getUserId();
            }
        }

        // If we still have no userId, return early (could log out the user)
        if (userId == null) {
            Log.e("UserRepository", "No userId available to fetch the user.");
            return userLiveData;  // Return empty LiveData
        }

        // Fetch the user from Room
        String finalUserId = userId; // Make it final to use in executor block
        executor.execute(() -> {
            UserItem localUser = userDao.getUserByIdSync(finalUserId);
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

    public void insertUser(UserItem user) {
        executor.execute(() -> userDao.insertUser(user));
    }

    public void updateUser(UserItem user) {
        executor.execute(() -> userDao.updateUser(user)); // Update Room
    }

    public void updateUserOnServer(UserItem updatedUser, File profilePhotoFile) {
        // Log the start of the update process
        Log.d("UserRepository", "Starting server update for user ID: " + updatedUser.getUserId());

        // Convert user fields to RequestBody Map
        Map<String, RequestBody> userFields = new HashMap<>();
        userFields.put("userName", RequestBody.create(MediaType.parse("text/plain"), updatedUser.getUserName()));  // Use getUserName() here
        userFields.put("email", RequestBody.create(MediaType.parse("text/plain"), updatedUser.getEmail()));
        userFields.put("phoneNumber", RequestBody.create(MediaType.parse("text/plain"), updatedUser.getPhoneNumber()));
        userFields.put("fullName", RequestBody.create(MediaType.parse("text/plain"), updatedUser.getDisplayedName()));  // Use getDisplayedName() here for full name

        Log.d("UserRepository", "User fields prepared for update: userName=" + updatedUser.getUserName()
                + ", fullName=" + updatedUser.getDisplayedName()
                + ", email=" + updatedUser.getEmail()
                + ", phoneNumber=" + updatedUser.getPhoneNumber());

        if (!userFields.isEmpty()) {
            Log.d("UserRepository", "User fields map size: " + userFields.size());
        }

        // Convert profile photo file to MultipartBody.Part
        MultipartBody.Part profilePhotoPart = null;
        if (profilePhotoFile != null) {
            RequestBody profilePhotoRequestBody = RequestBody.create(MediaType.parse("image/*"), profilePhotoFile);
            profilePhotoPart = MultipartBody.Part.createFormData("profilePhoto", profilePhotoFile.getName(), profilePhotoRequestBody);
            Log.d("UserRepository", "Profile photo part created with file: " + profilePhotoFile.getAbsolutePath());
        } else {
            Log.d("UserRepository", "No profile photo to update.");
        }

        Log.d("UserRepository", "Token being used: " + LoggedInUser.getToken());
        Log.d("UserRepository", "Updating user with ID: " + LoggedInUser.getUser().getValue().getUserId());

        String userId = LoggedInUser.getUser().getValue().getUserId();

        // Make the server API call
        serverAPI.updateUser(userId, userFields, profilePhotoPart).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful()) {
                    Log.d("UserRepository", "User updated successfully on server. Response: " + response.body().toString());
                    // Optionally, update Room after successful server update
                    executor.execute(() -> userDao.updateUser(response.body().toUserItem()));  // Use server response for updating Room
                } else {
                    Log.e("UserRepository", "Failed to update user on server. Response code: " + response.code() + ", Message: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e("UserRepository", "Error updating user on server", t);
            }
        });
    }





    // Additional methods for interacting with the server and handling sessions could be added here
}
