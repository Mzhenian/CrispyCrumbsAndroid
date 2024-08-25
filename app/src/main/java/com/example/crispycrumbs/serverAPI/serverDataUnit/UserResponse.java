package com.example.crispycrumbs.serverAPI.serverDataUnit;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UserResponse {

    @SerializedName("dislikedVideoIds")
    private List<String> dislikedVideoIds;

    @SerializedName("_id")
    private String userId;

    @SerializedName("userName")
    private String userName;

    @SerializedName("email")
    private String email;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("phoneNumber")
    private String phoneNumber;

    @SerializedName("country")
    private String country;

    @SerializedName("profilePhoto")
    private String profilePhoto;

    @SerializedName("followers")
    private List<String> followers;

    @SerializedName("following")
    private List<String> following;

    @SerializedName("videosIds")
    private List<String> videosIds;

    @SerializedName("likedVideoIds")
    private List<String> likedVideoIds;

    // Getters and Setters
    public List<String> getDislikedVideoIds() {
        return dislikedVideoIds;
    }

    public void setDislikedVideoIds(List<String> dislikedVideoIds) {
        this.dislikedVideoIds = dislikedVideoIds;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public List<String> getFollowers() {
        return followers;
    }

    public void setFollowers(List<String> followers) {
        this.followers = followers;
    }

    public List<String> getFollowing() {
        return following;
    }

    public void setFollowing(List<String> following) {
        this.following = following;
    }

    public List<String> getVideosIds() {
        return videosIds;
    }

    public void setVideosIds(List<String> videosIds) {
        this.videosIds = videosIds;
    }

    public List<String> getLikedVideoIds() {
        return likedVideoIds;
    }

    public void setLikedVideoIds(List<String> likedVideoIds) {
        this.likedVideoIds = likedVideoIds;
    }
}

