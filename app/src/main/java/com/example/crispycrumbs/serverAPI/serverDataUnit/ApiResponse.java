package com.example.crispycrumbs.serverAPI.serverDataUnit;

import com.google.gson.annotations.SerializedName;

public class ApiResponse<T> {
    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private T data;

}
