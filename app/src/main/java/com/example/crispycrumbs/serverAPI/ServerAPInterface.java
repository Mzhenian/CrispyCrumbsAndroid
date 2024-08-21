package com.example.crispycrumbs.serverAPI;

import com.example.crispycrumbs.dataUnit.UserItem;
import com.example.crispycrumbs.serverAPI.serverDataUnit.LoginRequest;
import com.example.crispycrumbs.serverAPI.serverDataUnit.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ServerAPInterface {
    @POST("users/tokens")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @GET("users/{id}")
    Call<UserItem> getUser(@Path("id") String id);
}
