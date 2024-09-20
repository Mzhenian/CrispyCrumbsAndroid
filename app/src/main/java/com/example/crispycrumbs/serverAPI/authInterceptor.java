package com.example.crispycrumbs.serverAPI;

import com.example.crispycrumbs.localDB.LoggedInUser;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class authInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        String token = LoggedInUser.getToken();
        Request request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer " + token)
                .build();
        return chain.proceed(request);
    }
}
