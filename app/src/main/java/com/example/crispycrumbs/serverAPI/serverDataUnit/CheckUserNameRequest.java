package com.example.crispycrumbs.serverAPI.serverDataUnit;


public class CheckUserNameRequest {
    private String username;


    public CheckUserNameRequest(String username) {
        this.username = username;
    }

    // Getters and Setters
    public String getUserName() {
        return username;
    }

    public void setUserName(String userName) {
        this.username = userName;
    }
}