package com.example.crispycrumbs.data;

import com.example.crispycrumbs.Lists.UserList;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class UserItem {
    private final Integer userId;
    private String username;
    private String email;
    private String password;
    private String displayedName;
    private String phoneNumber;
    private Date dateOfBirth;
    private String country;
    private String profilePicPath; // path to the profile picture
    private final Set<UserItem> followers = new HashSet<>();
    private final Set<UserItem> following = new HashSet<>();
    private final Set<Integer> UploadedVideoIds = new HashSet<>();
    private final Set<Integer> likedVideoIds = new HashSet<>();
    private final Set<Integer> dislikedVideoIds = new HashSet<>();


    public UserItem(String username, String password, String displayedName) {
        this.userId = UserList.getInstance().takeNextUserId();
        this.username = username;
        this.password = password;
        this.displayedName = displayedName;
        UserList.getInstance().add(this);
    }
    public UserItem(String username, String password, String displayedName, String email, String phoneNumber, Date dateOfBirth, String country, String profilePicPath) {
        this.userId = UserList.getInstance().takeNextUserId();
        this.username = username;
        this.password = password;
        this.displayedName = displayedName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.country = country;
        this.profilePicPath = profilePicPath;
        UserList.getInstance().add(this);
    }

    public Integer getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getUserName() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    public void setPassword(String password) {
        this.password = password;
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

    public void setCountry() {
        this.country = country;
    }

    public String getProfilePicPath() {
        return profilePicPath;
    }

    public void setProfilePicPath(String profilePicPath) {
        this.profilePicPath = profilePicPath;
    }

    public Boolean isFollowed(UserItem user) {
        return followers.contains(user);
    }

    public Boolean isFollowing(UserItem user) {
        return following.contains(user);
    }
    public void follow(UserItem user) {
        following.add(user);
        user.followers.add(this);
    }

    public void unfollow(UserItem user) {
        following.remove(user);
        user.followers.remove(this);
    }

    public void uploadVideo(Integer videoId) {
        UploadedVideoIds.add(videoId);
    }

    public void likeVideo(Integer videoId) {
        likedVideoIds.add(videoId);
    }

    public void dislikeVideo(Integer videoId) {
        dislikedVideoIds.add(videoId);
    }

    public void removeLike(Integer videoId) {
        likedVideoIds.remove(videoId);
    }

    public void removeDislike(Integer videoId) {
        dislikedVideoIds.remove(videoId);
    }

    public void removeUploadedVideo(Integer videoId) {
        UploadedVideoIds.remove(videoId);
    }

    public Integer getfollowersCount() {
        return followers.size();
    }

    public Integer getFollowingCount() {
        return following.size();
    }

    public Integer getUploadedVideosCount() {
        return UploadedVideoIds.size();
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserItem userItem = (UserItem) o;
        return userId.equals(userItem.userId);
    }
}
