package com.example.crispycrumbs.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import com.example.crispycrumbs.R;
import com.example.crispycrumbs.serverAPI.ServerAPI;
import com.google.gson.JsonObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SubscribeButton extends LinearLayout {

    private TextView buttonText;
    private boolean isFollowing = false;
    private String userIdToCheck;
    private ServerAPI userApi;

    public SubscribeButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.subscribe_button, this, true);
        buttonText = findViewById(R.id.subscribe_button_text);

        // Initialize Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://your-api-url.com") // Replace with your API base URL
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        userApi = retrofit.create(ServerAPI.class);

        setOnClickListener(v -> toggleFollowStatus());
    }

    public void setUserIdToCheck(String userId) {
        this.userIdToCheck = userId;
        checkFollowingStatus(); // Check initial follow status
    }

    private void checkFollowingStatus() {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("userIdToCheck", userIdToCheck);

        Call<JsonObject> call = userApi.isFollowing(requestBody);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    isFollowing = response.body().get("isFollowing").getAsBoolean();
                    updateButtonUI();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to fetch follow status", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleFollowStatus() {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("userId", userIdToCheck);

        Call<ResponseBody> call = userApi.followUnfollowUser(requestBody);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    isFollowing = !isFollowing; // Toggle state
                    updateButtonUI();
                    Toast.makeText(getContext(), isFollowing ? "Subscribed" : "Unsubscribed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to update status", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateButtonUI() {
        if (isFollowing) {
            buttonText.setText("Subscribed");
            buttonText.setBackgroundResource(R.drawable.subscribe_outline); // Outline background
            buttonText.setTextColor(getResources().getColor(R.color.crispy_orange));
        } else {
            buttonText.setText("Subscribe");
            buttonText.setBackgroundResource(R.drawable.subscribe_filled); // Filled background
            buttonText.setTextColor(getResources().getColor(android.R.color.white));
        }
    }
}
