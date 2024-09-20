package com.example.crispycrumbs.serverAPI.serverInterface;

import com.example.crispycrumbs.serverAPI.serverDataUnit.LoginResponse;

public interface LoginCallback {
    void onSuccess(LoginResponse loginResponse);
    void onFailure(Throwable t, int statusCode);
}