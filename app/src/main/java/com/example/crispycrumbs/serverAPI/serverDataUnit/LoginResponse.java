package com.example.crispycrumbs.serverAPI.serverDataUnit;

import com.example.crispycrumbs.dataUnit.UserItem;

import java.io.Serializable;

public class LoginResponse implements Serializable {
    // matching @SerializedName();
    private String token;
    // matching @SerializedName();
    private UserItem user;

    //todo remove
    public LoginResponse(String token, UserItem user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public UserItem getUser() {
        return user;
    }
}
