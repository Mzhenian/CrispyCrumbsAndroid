package com.example.crispycrumbs.serverAPI;

import android.util.Log;

import com.example.crispycrumbs.dataUnit.CommentItem;
import com.example.crispycrumbs.dataUnit.PreviewVideoCard;
import com.example.crispycrumbs.serverAPI.serverDataUnit.CommentRequest;
import com.example.crispycrumbs.serverAPI.serverDataUnit.LoginRequest;
import com.example.crispycrumbs.serverAPI.serverDataUnit.LoginResponse;
import com.example.crispycrumbs.serverAPI.serverDataUnit.VideoListsResponse;
import com.example.crispycrumbs.serverAPI.serverInterface.LoginCallback;

import java.util.List;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServerAPI {
    private static final ServerAPI serverAPI = new ServerAPI();
    private String port = "1324";
    private Retrofit retrofit = null;
    private ServerAPInterface serverAPInterface = null;
    //private String IP = "10.0.2.2"; // Default IP, assuming the server is running on the same machine as the emulator
   private String IP = "192.168.0.220";
    private Retrofit retrofitWithoutAPI;
    private ServerAPInterface serverAPInterfaceWithoutAPI;


    private ServerAPI() {
        buildRetrofit();
    }

    public static ServerAPI getInstance() {
        return serverAPI;
    }

    private void buildRetrofit() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new authInterceptor())
                .build();

        // Retrofit instance with /api/ in the base URL
        retrofit = new Retrofit.Builder()
                .baseUrl("http://" + IP + ":" + port + "/api/")
                .client(client)
                .callbackExecutor(Executors.newSingleThreadExecutor())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        serverAPInterface = retrofit.create(ServerAPInterface.class);

        // Retrofit instance without /api/ in the base URL
        retrofitWithoutAPI = new Retrofit.Builder()
                .baseUrl("http://" + IP + ":" + port + "/")
                .client(client)
                .callbackExecutor(Executors.newSingleThreadExecutor())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        serverAPInterfaceWithoutAPI = retrofitWithoutAPI.create(ServerAPInterface.class);
    }


    public ServerAPInterface getAPIWithoutAPI() {
        return serverAPInterfaceWithoutAPI;
    }

    public void setIP(String IP) {
        this.IP = IP;
        buildRetrofit();
    }

    public ServerAPInterface getAPI() {
        return serverAPInterface;
    }

    // Method to dynamically construct URLs based on the current IP for server assets
    public String constructUrl(String path) {
        return "http://" + IP + ":" + port + "/api/db/" + path;
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

