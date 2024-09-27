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
import com.example.crispycrumbs.serverAPI.serverInterface.UserUpdateCallback;

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
            } else {
                // If the user is not found locally, set it as deleted user
                userLiveData.postValue(new UserItem(
                        "[Deleted user]",          // userName
                        "",                        // password
                        "[Deleted user]",          // displayedName
                        "",                        // email
                        "",                        // phoneNumber
                        null,                      // dateOfBirth
                        "",                        // country
                        "default_profile_picture"  // profilePhoto
                ));
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
                } else {
                    // Handle the case where the user is deleted or not found on the server
                    userLiveData.postValue(new UserItem(
                            "[Deleted user]",          // userName
                            "",                        // password
                            "[Deleted user]",          // displayedName
                            "",                        // email
                            "",                        // phoneNumber
                            null,                      // dateOfBirth
                            "",                        // country
                            "default_profile_picture"  // profilePhoto
                    ));
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e("UserRepository", "Failed to fetch user from server", t);
                // Set the fallback for deleted user or failed network
                userLiveData.postValue(new UserItem(
                        "[Deleted user]",          // userName
                        "",                        // password
                        "[Deleted user]",          // displayedName
                        "",                        // email
                        "",                        // phoneNumber
                        null,                      // dateOfBirth
                        "",                        // country
                        "default_profile_picture"  // profilePhoto
                ));
            }

        });

        return userLiveData;
    }


    public void insertUserToRoom(UserItem user) {
        executor.execute(() -> userDao.insertUser(user));
    }

    public void updateUserFromRoom(UserItem user) {
        executor.execute(() -> userDao.updateUser(user)); // Update Room
    }

    public void updateUserOnServer(UserItem updatedUser, File profilePhotoFile, UserUpdateCallback callback) {
        Log.d("Update user", "Updating user on server: " + updatedUser.getUserId());

        // Convert user fields to RequestBody Map
        Map<String, RequestBody> userFields = new HashMap<>();
        userFields.put("userName", RequestBody.create(MediaType.parse("text/plain"), updatedUser.getUserName()));
        userFields.put("email", RequestBody.create(MediaType.parse("text/plain"), updatedUser.getEmail()));
        userFields.put("phoneNumber", RequestBody.create(MediaType.parse("text/plain"), updatedUser.getPhoneNumber()));
        userFields.put("fullName", RequestBody.create(MediaType.parse("text/plain"), updatedUser.getDisplayedName()));

        // Include the password if it has been changed
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            userFields.put("password", RequestBody.create(MediaType.parse("text/plain"), updatedUser.getPassword()));
        }

        // Convert profile photo file to MultipartBody.Part
        MultipartBody.Part profilePhotoPart = null;
        if (profilePhotoFile != null) {
            RequestBody profilePhotoRequestBody = RequestBody.create(MediaType.parse("image/*"), profilePhotoFile);
            profilePhotoPart = MultipartBody.Part.createFormData("profilePhoto", profilePhotoFile.getName(), profilePhotoRequestBody);
            Log.d("Update user", "Profile photo part created: " + profilePhotoFile.getName());
        }

        // Retrieve the user ID from the logged-in user
        String userId = LoggedInUser.getUser().getValue().getUserId();

        // Make the server API call
        serverAPI.updateUser(userId, userFields, profilePhotoPart).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful()) {
                    Log.d("Update user", "User updated successfully on server.");
                    // Update Room after successful server update using the server's response
                    executor.execute(() -> {
                        userDao.updateUser(response.body().toUserItem());
                        Log.d("Update user", "User updated successfully in Room.");
                    });
                    LoggedInUser.setLoggedInUser(response.body().toUserItem());
                    // Call success callback
                    callback.onSuccess();
                } else {
                    Log.e("Update user", "Failed to update user on server. Response: " + response.message());
                    // Call failure callback with the error message
                    callback.onFailure(response.message());
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e("Update user", "Error updating user on server", t);
                // Call failure callback with the error
                callback.onFailure(t.getMessage());
            }
        });
    }

}
