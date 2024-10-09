package com.example.crispycrumbs.serverAPI.serverDataUnit;


public class CheckEmailRequest {
    private String email;


    public CheckEmailRequest(String email) {
        this.email = email;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}