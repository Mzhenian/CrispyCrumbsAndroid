package com.example.crispycrumbs.viewModel;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.crispycrumbs.R;
import com.example.crispycrumbs.dataUnit.UserItem;
import com.example.crispycrumbs.localDB.LoggedInUser;
import com.example.crispycrumbs.serverAPI.ServerAPI;
import com.example.crispycrumbs.serverAPI.ServerAPInterface;
import com.example.crispycrumbs.serverAPI.serverDataUnit.UserResponse;
import com.google.gson.JsonObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Custom SubscribeButton component that handles subscription logic.
 * It notifies the parent via OnSubscriptionChangeListener when the subscription status changes.
 */
public class SubscribeButton extends LinearLayout {

    private TextView buttonText;
    private TextView subscriberCountText;
    private boolean isFollowing = false;
    private String uploaderUserId; // The user ID of the uploader
    private ServerAPInterface userApi;

    private UserItem uploader; // The uploader's UserItem

    private OnSubscriptionChangeListener subscriptionChangeListener;

    // Callback interface to notify subscription changes
    public interface OnSubscriptionChangeListener {
        /**
         * Called when the subscription status changes.
         *
         * @param isFollowing    Whether the current user is now following the uploader.
         * @param subscriberCount The updated subscriber count.
         */
        void onSubscriptionChanged(boolean isFollowing, int subscriberCount);
    }

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
        subscriberCountText = findViewById(R.id.subscriber_count_text);

        // Use the existing Retrofit API interface
        userApi = ServerAPI.getInstance().getAPI();

        // Set click listener to toggle follow/unfollow state
        setOnClickListener(v -> toggleFollowStatus());
    }

    /**
     * Sets the uploader's user ID and initiates data loading.
     *
     * @param uploaderUserId The user ID of the uploader.
     */
    public void setUploaderUserId(String uploaderUserId) {
        this.uploaderUserId = uploaderUserId;
        loadUploaderData();
    }

    /**
     * Sets the subscription change listener.
     *
     * @param listener The listener to notify about subscription changes.
     */
    public void setOnSubscriptionChangeListener(OnSubscriptionChangeListener listener) {
        this.subscriptionChangeListener = listener;
    }

    // Load uploader data and handle visibility and subscriber count
    private void loadUploaderData() {
        // Get the current logged-in user
        UserItem currentUser = LoggedInUser.getUser().getValue();

        if (currentUser == null || currentUser.getUserId().equals(uploaderUserId)) {
            // User not logged in or viewing own profile, hide subscribe button
            post(() -> setVisibility(GONE));
        } else {
            // Fetch uploader data
            Call<UserResponse> call = userApi.getUser(uploaderUserId);
            call.enqueue(new Callback<UserResponse>() {
                @Override
                public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                    post(() -> {
                        if (response.isSuccessful() && response.body() != null) {
                            uploader = response.body().toUserItem();
                            // Update subscriber count
                            int subscriberCount = uploader.getFollowersCount();
                            subscriberCountText.setText(subscriberCount + " subscribers");
                            // Check following status
                            checkFollowingStatus();
                        } else {
                            // Handle error
                            Toast.makeText(getContext(), "Failed to load uploader data", Toast.LENGTH_SHORT).show();
                        }
                        setVisibility(VISIBLE);
                    });
                }

                @Override
                public void onFailure(Call<UserResponse> call, Throwable t) {
                    post(() -> {
                        Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                        setVisibility(VISIBLE);
                    });
                }
            });
        }
    }

    // Check the initial subscription status
    private void checkFollowingStatus() {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("userIdToCheck", uploaderUserId);

        Call<JsonObject> call = userApi.isFollowing(requestBody);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                post(() -> {
                    if (response.isSuccessful() && response.body() != null) {
                        isFollowing = response.body().get("isFollowing").getAsBoolean();
                        updateButtonUI();
                        // Notify the listener about the initial state
                        if (subscriptionChangeListener != null) {
                            subscriptionChangeListener.onSubscriptionChanged(isFollowing, uploader.getFollowersCount());
                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to fetch status", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                post(() -> {
                    Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // Toggle follow/unfollow status
    private void toggleFollowStatus() {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("userId", uploaderUserId);

        Call<ResponseBody> call = userApi.followUnfollowUser(requestBody);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                post(() -> {
                    if (response.isSuccessful()) {
                        isFollowing = !isFollowing;  // Toggle state
                        updateButtonUI();
                        Toast.makeText(getContext(), isFollowing ? "Subscribed" : "Unsubscribed", Toast.LENGTH_SHORT).show();
                        // Update subscriber count
                        int currentCount = uploader.getFollowersCount();
                        if (isFollowing) {
                            currentCount++;
                            uploader.getFollowerIds().add(LoggedInUser.getUser().getValue().getUserId());
                        } else {
                            currentCount--;
                            uploader.getFollowerIds().remove(LoggedInUser.getUser().getValue().getUserId());
                        }
                        uploader.setFollowersCount(currentCount);
                        subscriberCountText.setText(currentCount + " subscribers");

                        // Notify the listener about the change
                        if (subscriptionChangeListener != null) {
                            subscriptionChangeListener.onSubscriptionChanged(isFollowing, currentCount);
                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to update status", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
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
