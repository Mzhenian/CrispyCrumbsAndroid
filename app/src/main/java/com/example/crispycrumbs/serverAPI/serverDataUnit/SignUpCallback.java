package com.example.crispycrumbs.serverAPI.serverDataUnit;

import com.example.crispycrumbs.serverAPI.serverDataUnit.SignUpResponse;

public interface SignUpCallback {
    void onSuccess(SignUpResponse signUpResponse);
    void onFailure(Throwable t, int statusCode);
}