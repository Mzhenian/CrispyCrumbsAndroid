package com.example.crispycrumbs.serverAPI;

import com.example.crispycrumbs.dataUnit.CommentItem;
import com.example.crispycrumbs.dataUnit.PreviewVideoCard;
import com.example.crispycrumbs.serverAPI.serverDataUnit.ApiResponse;
import com.example.crispycrumbs.serverAPI.serverDataUnit.LikeDislikeRequest;
import com.example.crispycrumbs.serverAPI.serverDataUnit.LoginRequest;
import com.example.crispycrumbs.serverAPI.serverDataUnit.LoginResponse;
import com.example.crispycrumbs.serverAPI.serverDataUnit.UserResponse;
import com.example.crispycrumbs.serverAPI.serverDataUnit.VideoIdRequest;
import com.example.crispycrumbs.serverAPI.serverDataUnit.VideoListsResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ServerAPInterface {
    // User endpoints
    @POST("users/tokens")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @GET("users/{id}")
    Call<UserResponse> getUser(@Path("id") String id);

    // Video endpoints
    @GET("videos")
    Call<VideoListsResponse> getAllVideos();

    @GET("videos/{id}")
    Call<PreviewVideoCard> getVideoById(@Path("id") String videoId);

    @POST("videos/incrementViews")
    Call<ApiResponse<Void>> incrementVideoViews(@Body VideoIdRequest videoIdRequest);

    @POST("videos/like")
    Call<PreviewVideoCard> likeVideo(@Body LikeDislikeRequest request);

    @POST("videos/dislike")
    Call<PreviewVideoCard> dislikeVideo(@Body LikeDislikeRequest request);

    @GET("users/{userId}/videos")
    Call<List<PreviewVideoCard>> getVideosByUser(@Path("userId") String userId);

//    @POST("users/{userId}/videos")
//    Call<PreviewVideoCard> uploadVideo(@Path("userId") String userId, @Body UploadVideoRequest video);

    @Multipart
    @POST("users/{userId}/videos")
    Call<PreviewVideoCard> upload(
            @Path("userId") String userId,
            @Part MultipartBody.Part video,
            @Part MultipartBody.Part thumbnail,
            @Part("title") RequestBody title,
            @Part("description") RequestBody description,
            @Part("category") RequestBody category,
            @Part("tags") RequestBody tags,
            @Part("userId") RequestBody loggedInUserId
    );


    // Comment endpoints
    @GET("videos/{videoId}/comments")
    Call<List<CommentItem>> getCommentsForVideo(@Path("videoId") String videoId);

    @POST("videos/{videoId}/comments")
    Call<CommentItem> postComment(@Path("videoId") String videoId, @Body CommentItem comment);
}
