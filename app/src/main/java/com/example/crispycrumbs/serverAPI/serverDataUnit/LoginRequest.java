package com.example.crispycrumbs.serverAPI.serverDataUnit;

import java.io.Serializable;

public class LoginRequest implements Serializable {
    private String userName;
    private String password;
    private boolean rememberMe;

    public LoginRequest(String userName, String password, boolean rememberMe) {
        this.userName = userName;
        this.password = password;
        this.rememberMe = rememberMe;
    }

}
