package com.example.crispycrumbs.serverAPI;

import com.example.crispycrumbs.dataUnit.CommentItem;
import com.example.crispycrumbs.dataUnit.PreviewVideoCard;
import com.example.crispycrumbs.serverAPI.serverDataUnit.ApiResponse;
import com.example.crispycrumbs.serverAPI.serverDataUnit.CommentRequest;
import com.example.crispycrumbs.serverAPI.serverDataUnit.DeleteCommentRequest;
import com.example.crispycrumbs.serverAPI.serverDataUnit.EditCommentRequest;
import com.example.crispycrumbs.serverAPI.serverDataUnit.LoginRequest;
import com.example.crispycrumbs.serverAPI.serverDataUnit.LoginResponse;
import com.example.crispycrumbs.serverAPI.serverDataUnit.UserResponse;
import com.example.crispycrumbs.serverAPI.serverDataUnit.VideoIdRequest;
import com.example.crispycrumbs.serverAPI.serverDataUnit.VideoListsResponse;
import com.example.crispycrumbs.serverAPI.serverDataUnit.LikeDislikeRequest;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import java.util.List;
import java.util.Map;

public interface ServerAPInterface {
    @POST("users/tokens")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @GET("users/{id}")
    Call<UserResponse> getUser(@Path("id") String id);

    // Video endpoints
    @GET("videos")
    Call<VideoListsResponse> getAllVideos();

    @GET("videos/{id}")
    Call<PreviewVideoCard> getVideoById(@Path("id") String videoId);

    // Comment endpoints
    @GET("videos/{videoId}/comments")
    Call<List<CommentItem>> getCommentsForVideo(@Path("videoId") String videoId);

    @POST("videos/comment")
    Call<CommentItem> postComment(@Body CommentRequest commentRequest);

    @PUT("videos/comment")
    Call<PreviewVideoCard> editComment(@Body EditCommentRequest request);



    @HTTP(method = "DELETE", path = "videos/comment", hasBody = true)
    Call<Void> deleteComment(@Body DeleteCommentRequest request);


    @POST("videos/incrementViews")
    Call<ApiResponse<Void>> incrementVideoViews(@Body VideoIdRequest videoIdRequest);

    @POST("videos/like")
    Call<PreviewVideoCard> likeVideo(@Body LikeDislikeRequest request);

    @POST("videos/dislike")
    Call<PreviewVideoCard> dislikeVideo(@Body LikeDislikeRequest request);



    @GET("users/{userId}/videos")
    Call<List<PreviewVideoCard>> getVideosByUser(@Path("userId") String userId);

    // Update user via PUT request (with profile photo)
    @Multipart
    @PUT("users/{id}")
    Call<UserResponse> updateUser(
            @Path("id") String userId,
            @PartMap Map<String, RequestBody> userFields,  // Other fields like displayedName, email, etc.
            @Part MultipartBody.Part profilePhoto // Profile photo (optional)
    );
}
