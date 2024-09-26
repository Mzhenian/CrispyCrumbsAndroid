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

        Request.Builder requestBuilder = chain.request().newBuilder();

        // If the token is not null, add it to the header
        if (token != null && !token.isEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer " + token);
        }

        // Proceed with the request
        Request request = requestBuilder.build();
        return chain.proceed(request);
    }
}
