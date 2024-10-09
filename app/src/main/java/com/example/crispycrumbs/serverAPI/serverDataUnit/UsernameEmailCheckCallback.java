package com.example.crispycrumbs.serverAPI.serverDataUnit;

public interface UsernameEmailCheckCallback {
    void onResult(Boolean isAvailable);
    void onFailure(Throwable t);
}