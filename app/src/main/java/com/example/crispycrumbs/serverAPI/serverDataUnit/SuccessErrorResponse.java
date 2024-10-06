package com.example.crispycrumbs.serverAPI.serverDataUnit;

public class SuccessErrorResponse {
    //    @SerializedName("status")
    private String status;

    //    @SerializedName("message")
    private String message;

    private String error;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String  getErro() {
        return error;
    }
}
