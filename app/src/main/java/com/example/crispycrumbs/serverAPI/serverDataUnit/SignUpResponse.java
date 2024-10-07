package com.example.crispycrumbs.serverAPI.serverDataUnit;

import com.example.crispycrumbs.dataUnit.UserItem;

public class SignUpResponse {
    private String token;
    private UserItem user;

    public SignUpResponse(String token, UserItem user) {
        this.token = token;
        this.user = user;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserItem getUser() {
        return user;
    }

    public void setUser(UserItem user) {
        this.user = user;
    }
}