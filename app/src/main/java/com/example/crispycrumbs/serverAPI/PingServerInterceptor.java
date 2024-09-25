package com.example.crispycrumbs.serverAPI;

import android.util.Log;

import com.example.crispycrumbs.view.MainPage;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class PingServerInterceptor implements Interceptor {
    private MainPage mainPage = MainPage.getInstance();

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        try {
            Response response = chain.proceed(request);
            if (mainPage != null) {
                if (response.isSuccessful()) {
                    mainPage.runOnUiThread(() -> mainPage.stopConnectToServerAlert());
                } else {
                    mainPage.runOnUiThread(() -> mainPage.startConnectToServerAlert());
                }
            }
            return response;
        } catch (IOException e) {
            if (mainPage != null) {
                mainPage.runOnUiThread(() -> mainPage.startConnectToServerAlert());
                Log.e("PingServerInterceptor", "Failed to connect to server");
            }
            throw e;
        }
    }
}