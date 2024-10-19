package com.example.crispycrumbs.viewModel;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.crispycrumbs.R;
import com.example.crispycrumbs.serverAPI.ServerAPI;
import com.example.crispycrumbs.serverAPI.ServerAPInterface;
import com.google.gson.JsonObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubscribeButton extends LinearLayout {

    private TextView buttonText;
    private boolean isFollowing = false;
    private String userIdToCheck;
    private ServerAPInterface userApi;

    // Constructor to initialize the component
    public SubscribeButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    // Initialize the layout and Retrofit setup
    private void init(Context context) {
        // Inflate the layout for the custom button
        LayoutInflater.from(context).inflate(R.layout.subscribe_button, this, true);
        buttonText = findViewById(R.id.subscribe_button_text);

        // Use the existing Retrofit API interface
        userApi = ServerAPI.getInstance().getAPI();

        // Set click listener to toggle follow/unfollow state
        setOnClickListener(v -> toggleFollowStatus());
    }

    // Setter to specify the userId to check for subscription status
    public void setUserIdToCheck(String userId) {
        this.userIdToCheck = userId;
        checkFollowingStatus();  // Check if the user is already followed
    }

    // Check the initial subscription status
    private void checkFollowingStatus() {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("userIdToCheck", userIdToCheck);

        Call<JsonObject> call = userApi.isFollowing(requestBody);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                // Ensure that UI updates are run on the main thread
                post(() -> {
                    if (response.isSuccessful() && response.body() != null) {
                        isFollowing = response.body().get("isFollowing").getAsBoolean();
                        updateButtonUI();
                    } else {
                        Toast.makeText(getContext(), "Failed to fetch status", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                // Ensure that UI updates are run on the main thread
                post(() -> {
                    Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // Toggle follow/unfollow status
    private void toggleFollowStatus() {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("userId", userIdToCheck);

        Call<ResponseBody> call = userApi.followUnfollowUser(requestBody);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                // Ensure that UI updates are run on the main thread
                post(() -> {
                    if (response.isSuccessful()) {
                        isFollowing = !isFollowing;  // Toggle state
                        updateButtonUI();
                        Toast.makeText(getContext(), isFollowing ? "Subscribed" : "Unsubscribed", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Failed to update status", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // Ensure that UI updates are run on the main thread
                post(() -> {
                    Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // Update the button appearance based on follow status
    private void updateButtonUI() {
        if (isFollowing) {
            buttonText.setText("Subscribed");
            buttonText.setBackgroundResource(R.drawable.subscribe_outline);
            buttonText.setTextColor(getResources().getColor(R.color.crispy_orange));
        } else {
            buttonText.setText("Subscribe");
            buttonText.setBackgroundResource(R.drawable.subscribe_filled);
            buttonText.setTextColor(getResources().getColor(android.R.color.white));
        }
    }
}
