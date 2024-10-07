package com.example.crispycrumbs.serverAPI.serverDataUnit;


public class CheckUserNameRequest {
    private String userName;


    public CheckUserNameRequest(String username) {
        this.userName = username;
    }

    // Getters and Setters
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}