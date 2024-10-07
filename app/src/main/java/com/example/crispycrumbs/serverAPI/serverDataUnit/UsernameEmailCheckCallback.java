package com.example.crispycrumbs.serverAPI.serverDataUnit;

public interface UsernameEmailCheckCallback {
    void onResult(boolean isAvailable);
    void onFailure(Throwable t);
}