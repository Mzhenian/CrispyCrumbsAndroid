package com.example.crispycrumbs.serverAPI;

import android.util.Log;

import com.example.crispycrumbs.dataUnit.UserItem;
import com.example.crispycrumbs.serverAPI.serverDataUnit.LoginRequest;
import com.example.crispycrumbs.serverAPI.serverDataUnit.LoginResponse;
import com.example.crispycrumbs.serverAPI.serverInterface.LoginCallback;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServerAPI {
    private static ServerAPI serverAPI = new ServerAPI();

    private Retrofit retrofit = null;
    private ServerAPInterface serverAPInterface = null;
//    private String IP = "10.0.2.2"; // Default IP, assuming the server is running on the same machine as the emulator
    private String IP = "192.168.1.227";

    private void buildRetrofit() {
        if (null == retrofit || !retrofit.baseUrl().toString().equals("http://" + IP + ":1324/api/")) {
            retrofit = new Retrofit.Builder()
                    .baseUrl("http://" + IP + ":1324/api/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            serverAPInterface = retrofit.create(ServerAPInterface.class);
        }
    }

    private ServerAPI() {
        buildRetrofit();
    }

    public static ServerAPI getInstance() {
        return serverAPI;
    }

    public void setIP(String IP) {
        this.IP = IP;
        buildRetrofit();
    }

    public void login(String userName, String password, boolean rememberMe, LoginCallback callback) {
        LoginRequest loginRequest = new LoginRequest(userName, password, rememberMe);
        Call<LoginResponse> call = serverAPInterface.login(loginRequest);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    String errorMessage = response.body() != null ? response.body().toString() : response.message();
                    callback.onFailure(new Exception(errorMessage), response.code());
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                callback.onFailure(t, -1); // -1 indicates that the failure was not due to an HTTP error
            }
        });
    }

}
