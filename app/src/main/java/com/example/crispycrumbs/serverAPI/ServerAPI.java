package com.example.crispycrumbs.serverAPI;

import com.example.crispycrumbs.dataUnit.CommentItem;
import com.example.crispycrumbs.dataUnit.PreviewVideoCard;
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
    private String IP = DEFAULT_IP ;

    public static final String DEFAULT_IP = "10.0.2.2"; // Default IP, assuming the server is running on the same machine as the emulator

    private ServerAPI() {
        buildRetrofit();
    }

    public static ServerAPI getInstance() {
        return serverAPI;
    }

    private void buildRetrofit() {
        if (null == retrofit || !retrofit.baseUrl().toString().equals("http://" + IP + ":" + port + "/api/")) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new authInterceptor())
                    .addInterceptor(new PingServerInterceptor())
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl("http://" + IP + ":" + port + "/api/")
                    .client(client)
                    .callbackExecutor(Executors.newSingleThreadExecutor())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            serverAPInterface = retrofit.create(ServerAPInterface.class);
        }
    }

    public void setIP(String IP) {
        this.IP = IP;
        buildRetrofit();
    }

    public String getIP() {
        return IP;
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

    // Additional methods for video and comment interactions
    public void getAllVideos(Callback<VideoListsResponse> callback) {
        Call<VideoListsResponse> call = serverAPInterface.getAllVideos();
        call.enqueue(callback);
    }

    public void getVideoById(String videoId, Callback<PreviewVideoCard> callback) {
        Call<PreviewVideoCard> call = serverAPInterface.getVideoById(videoId);
        call.enqueue(callback);
    }

    public void getCommentsForVideo(String videoId, Callback<List<CommentItem>> callback) {
        Call<List<CommentItem>> call = serverAPInterface.getCommentsForVideo(videoId);
        call.enqueue(callback);
    }

    public void postComment(String videoId, CommentItem comment, Callback<CommentItem> callback) {
        Call<CommentItem> call = serverAPInterface.postComment(videoId, comment);
        call.enqueue(callback);
    }
}

