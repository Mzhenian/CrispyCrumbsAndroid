package com.example.crispycrumbs.serverAPI;

import com.example.crispycrumbs.dataUnit.PreviewVideoCard;
import com.example.crispycrumbs.dataUnit.CommentItem;
import com.example.crispycrumbs.dataUnit.UserItem;
import com.example.crispycrumbs.serverAPI.serverDataUnit.LoginRequest;
import com.example.crispycrumbs.serverAPI.serverDataUnit.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import java.util.List;

public interface ServerAPInterface {
    @POST("users/tokens")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @GET("users/{id}")
    Call<UserItem> getUser(@Path("id") String id);

    // Video endpoints
    @GET("videos")
    Call<List<PreviewVideoCard>> getAllVideos();

    @GET("videos/{id}")
    Call<PreviewVideoCard> getVideoById(@Path("id") String videoId);

    // Comment endpoints
    @GET("videos/{videoId}/comments")
    Call<List<CommentItem>> getCommentsForVideo(@Path("videoId") String videoId);

    @POST("videos/{videoId}/comments")
    Call<CommentItem> postComment(@Path("videoId") String videoId, @Body CommentItem comment);
}
