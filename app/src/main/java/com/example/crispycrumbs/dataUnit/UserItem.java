package com.example.crispycrumbs.dataUnit;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.crispycrumbs.converters.DateConverter;
import com.example.crispycrumbs.converters.SetTypeConverter;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity(tableName = "users")
public class UserItem implements Serializable {
    @PrimaryKey
    @SerializedName("_id")
    @NonNull
    private String userId;

    @TypeConverters(SetTypeConverter.class)
    private Set<String> videosIds = new HashSet<>();

    @TypeConverters(SetTypeConverter.class)
    @SerializedName("followers")
    private Set<String> followerIds = new HashSet<>();

    @TypeConverters(SetTypeConverter.class)
    @SerializedName("following")
    private Set<String> followingIds = new HashSet<>();

    @TypeConverters(SetTypeConverter.class)
    private Set<String> likedVideoIds = new HashSet<>();

    @TypeConverters(SetTypeConverter.class)
    private Set<String> dislikedVideoIds = new HashSet<>();

    private String userName;
    private String email;
    private String password;
    @SerializedName("fullName")
    private String displayedName;
    private String phoneNumber;
    @SerializedName("birthday")
    @TypeConverters(DateConverter.class)
    private Date dateOfBirth;
    private String country;
    private String profilePhoto; // path to the profile picture

    // Constructor (without userId) - userId comes from the server
    public UserItem(String userName, String password, String displayedName, String email, String phoneNumber, Date dateOfBirth, String country, String profilePhoto) {
        this.userName = userName;
        this.password = password;
        this.displayedName = displayedName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.country = country;
        this.profilePhoto = profilePhoto;
        this.userId = "tempUserId"; //official ID received from the server
    }

    //constructor without password to upload to server without corrupting it
    @Ignore
    public UserItem(String userName, String displayedName, String email, String phoneNumber, Date dateOfBirth, String country, String profilePhoto) {
        this.userName = userName;
        this.displayedName = displayedName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.country = country;
        this.profilePhoto = profilePhoto;
        this.userId = "tempUserId";
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
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

    public String getDisplayedName() {
        return displayedName;
    }

    public void setDisplayedName(String displayedName) {
        this.displayedName = displayedName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setCountry() {
        this.country = country;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public Boolean getIsFollowed(UserItem user) {
        return followerIds.contains(user.getUserId());
    }

    public Boolean getIsFollowing(UserItem user) {
        return followingIds.contains(user.getUserId());
    }

    //adds a new
    public void addFallow(UserItem user) {
        followingIds.add(user.getUserId());
        user.followerIds.add(this.getUserId());
    }

    public void addVideo(String videoId) {
        videosIds.add(videoId);
    }

    public Set<String> getUploadedVideos() {
        return videosIds;
    }

    public void delFollow(UserItem user) {
        followingIds.remove(user.getUserId());
        user.followerIds.remove(this.getUserId());
    }

    public void SetLikeVideo(String videoId) {
        likedVideoIds.add(videoId);
    }

    public void setDislikeVideo(String videoId) {
        dislikedVideoIds.add(videoId);
    }

    public void delLike(String videoId) {
        likedVideoIds.remove(videoId);
    }

    public void delDislike(String videoId) {
        dislikedVideoIds.remove(videoId);
    }

    public void delUploadedVideo(String videoId) {
        videosIds.remove(videoId);
    }

    public Integer getfollowersCount() {
        return followerIds.size();
    }

    public Integer getFollowingCount() {
        return followingIds.size();
    }

    public Integer getUploadedVideosCount() {
        return videosIds.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserItem userItem = (UserItem) o;
        return userId.equals(userItem.userId);
    }

    public void likeVideo(String videoId) {
        likedVideoIds.add(videoId);
    }

    public void dislikeVideo(String videoId) {
        dislikedVideoIds.add(videoId);
    }

    public void removeLike(String videoId) {
        likedVideoIds.remove(videoId);
    }

    public void removeDislike(String videoId) {
        dislikedVideoIds.remove(videoId);
    }

    public boolean hasLiked(String videoId) {
        return likedVideoIds.contains(videoId);
    }

    public boolean hasDisliked(String videoId) {
        return dislikedVideoIds.contains(videoId);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<String> getFollowerIds() {
        return followerIds;
    }

    public void setFollowerIds(Set<String> followerIds) {
        this.followerIds = followerIds;
    }

    public Set<String> getFollowingIds() {
        return followingIds;
    }

    public void setFollowingIds(Set<String> followingIds) {
        this.followingIds = followingIds;
    }

    public Set<String> getVideosIds() {
        return videosIds;
    }

    public void setVideosIds(Set<String> videosIds) {
        this.videosIds = videosIds;
    }

    public Set<String> getLikedVideoIds() {
        return likedVideoIds;
    }

    public void setLikedVideoIds(Set<String> likedVideoIds) {
        this.likedVideoIds = likedVideoIds;
    }

    public Set<String> getDislikedVideoIds() {
        return dislikedVideoIds;
    }

    public void setDislikedVideoIds(Set<String> dislikedVideoIds) {
        this.dislikedVideoIds = dislikedVideoIds;
    }
}
