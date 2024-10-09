package com.example.crispycrumbs.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.crispycrumbs.dataUnit.UserItem;
import com.example.crispycrumbs.localDB.AppDB;
import com.example.crispycrumbs.repository.UserRepository;
import com.example.crispycrumbs.serverAPI.ServerAPI;
import com.example.crispycrumbs.serverAPI.serverDataUnit.CheckResponse;
import com.example.crispycrumbs.serverAPI.serverDataUnit.UsernameEmailCheckCallback;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserViewModel extends AndroidViewModel {
    private UserRepository userRepository;
    private LiveData<UserItem> user;

    private ServerAPI serverAPI;


    public UserViewModel(Application application) {
        super(application);
        userRepository = UserRepository.getInstance();
        serverAPI = ServerAPI.getInstance();
    }

    public LiveData<UserItem> getUser(String userId) {
        return userRepository.getUser(userId);
    }

    public void updateUser(UserItem updatedUser) {
        userRepository.updateUserFromRoom(updatedUser);
    }

    public void insertUser(UserItem newUser) {
        userRepository.insertUserToRoom(newUser);
    }

    public void checkUsernameAvailability(String username, UsernameEmailCheckCallback callback) {
        serverAPI.checkUsernameAvailability(username, new Callback<CheckResponse>() {
            @Override
            public void onResponse(Call<CheckResponse> call, Response<CheckResponse> response) {
            callback.onResult(response.isSuccessful() && null != response.body() && response.body().isAvailable());
            }

            @Override
            public void onFailure(Call<CheckResponse> call, Throwable t) {
                callback.onResult(null);
            }
        });
    }

    public void checkEmailAvailability(String email, UsernameEmailCheckCallback callback) {
        serverAPI.checkEmailAvailability(email, new Callback<CheckResponse>() {
            @Override
            public void onResponse(Call<CheckResponse> call, Response<CheckResponse> response) {
                    callback.onResult(response.isSuccessful() && null != response.body() && response.body().isAvailable());
            }

            @Override
            public void onFailure(Call<CheckResponse> call, Throwable t) {
                callback.onResult(null);
            }
        });
    }
}
