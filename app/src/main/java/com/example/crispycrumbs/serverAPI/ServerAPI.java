package com.example.crispycrumbs.serverAPI;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.crispycrumbs.view.MainPage;

import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServerAPI {
    public static final String DEFAULT_IP = "10.0.2.2"; // the emulator's host IP
    public static final String port = "1324";
    public static final String IP_KEY = "IP_KEY";
    private static final ServerAPI serverAPI = new ServerAPI();
    private Retrofit retrofit = null;
    private ServerAPInterface serverAPInterface = null;
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
}

