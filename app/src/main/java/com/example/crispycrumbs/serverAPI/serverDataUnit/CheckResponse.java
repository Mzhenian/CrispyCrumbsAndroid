package com.example.crispycrumbs.serverAPI.serverDataUnit;

import com.example.crispycrumbs.dataUnit.UserItem;

public class CheckResponse {
    private Boolean available;

    public CheckResponse(Boolean available) {
        this.available = available;
    }

    // Getters and Setters
    public Boolean isAvailable() {
        return available;
    }

}