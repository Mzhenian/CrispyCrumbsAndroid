package com.example.crispycrumbs.serverAPI.serverInterface;

public interface UserUpdateCallback {
    void onSuccess();
    void onFailure(String errorMessage);
}
