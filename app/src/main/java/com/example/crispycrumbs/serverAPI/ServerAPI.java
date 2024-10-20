package com.example.crispycrumbs.serverAPI;
import com.example.crispycrumbs.dataUnit.CommentItem;
import com.example.crispycrumbs.dataUnit.PreviewVideoCard;
import com.example.crispycrumbs.repository.UserRepository;
import com.example.crispycrumbs.serverAPI.serverDataUnit.CheckResponse;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.example.crispycrumbs.serverAPI.serverDataUnit.CheckEmailRequest;
import com.example.crispycrumbs.serverAPI.serverDataUnit.CheckUserNameRequest;
import com.example.crispycrumbs.serverAPI.serverDataUnit.SignUpRequest;
import com.example.crispycrumbs.serverAPI.serverDataUnit.SignUpResponse;
import com.example.crispycrumbs.view.MainPage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Callback;


public class ServerAPI {
    public static final String DEFAULT_IP = "10.0.2.2"; // the emulator's host IP
    public static final String port = "1324";
    public static final String IP_KEY = "IP_KEY";
    private static final ServerAPI serverAPI = new ServerAPI();
    private Retrofit retrofit = null;
    private ServerAPInterface serverAPInterface = null; // todo upgrade to LiveData
    private String IP;
    //    private MainPage mainPage = MainPage.getInstance();
    private SharedPreferences sharedPreferences = MainPage.getInstance().getPreferences(Context.MODE_PRIVATE);

    private ServerAPI() {
        IP = sharedPreferences.getString(IP_KEY, DEFAULT_IP);
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

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
        sharedPreferences.edit().putString(IP_KEY, IP).apply();
        buildRetrofit();
    }

    public ServerAPInterface getAPI() {
        return serverAPInterface;
    }

    // Method to dynamically construct URLs based on the current IP for server assets
    public String constructUrl(String path) {
        return "http://" + IP + ":" + port + "/api/db/" + path;
    }

    public void checkUsernameAvailability(String username, Callback<CheckResponse> callback) {
        CheckUserNameRequest request = new CheckUserNameRequest(username);
        Call<CheckResponse> call = serverAPInterface.checkUsernameAvailability(request);
        call.enqueue(callback);
    }

    public void checkEmailAvailability(String email, Callback<CheckResponse> callback) {
        CheckEmailRequest request = new CheckEmailRequest(email);
        Call<CheckResponse> call = serverAPInterface.checkEmailAvailability(request);
        call.enqueue(callback);
    }

    public void signUp(SignUpRequest signUpRequest, Uri profilePhotoUri, Callback<SignUpResponse> callback) {
        ContentResolver contentResolver = MainPage.getInstance().getContentResolver();
        UserRepository userRepository = UserRepository.getInstance();

        MultipartBody.Part profilePhotoPart = userRepository.setProfilePhotoPart(contentResolver, profilePhotoUri);

        Map<String, RequestBody> signUpFields = new HashMap<>();
        signUpFields.put("userName", RequestBody.create(okhttp3.MediaType.parse("text/plain"), signUpRequest.getUserName()));
        signUpFields.put("email", RequestBody.create(okhttp3.MediaType.parse("text/plain"), signUpRequest.getEmail()));
        signUpFields.put("password", RequestBody.create(okhttp3.MediaType.parse("text/plain"), signUpRequest.getPassword()));
        signUpFields.put("fullName", RequestBody.create(okhttp3.MediaType.parse("text/plain"), signUpRequest.getFullName()));
        signUpFields.put("phoneNumber", RequestBody.create(okhttp3.MediaType.parse("text/plain"), signUpRequest.getPhoneNumber()));
        signUpFields.put("birthday", RequestBody.create(okhttp3.MediaType.parse("text/plain"), signUpRequest.getBirthday()));
        signUpFields.put("country", RequestBody.create(okhttp3.MediaType.parse("text/plain"), signUpRequest.getCountry()));

        Call<SignUpResponse> call = serverAPInterface.signUp(signUpFields, profilePhotoPart);
        call.enqueue(callback);
    }

    public static Call<List<PreviewVideoCard>> getRecommendedVideos(String videoId) {
    return getInstance().getAPI().getRecommendedVideos(videoId);
    }


}

