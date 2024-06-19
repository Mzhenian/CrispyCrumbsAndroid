package com.example.crispycrumbs.data;

import android.graphics.drawable.Drawable;

import android.media.Image;

import com.example.crispycrumbs.model.UserLogic;
import com.example.crispycrumbs.ui.MainPage;
import com.example.crispycrumbs.Lists.UserList;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class UserItem {
    private String userId;
    private String userName;
    private String email;
    private String password;
    private String displayedName;
    private String phoneNumber;
    private Date dateOfBirth;
    private String country;
    private String profilePicURI; // path to the profile picture
    private Set<String> videosIds = new HashSet<>();
    private final Set<String> followerIds = new HashSet<>();
    private final Set<String> followingIds = new HashSet<>();
    private final Set<String> likedVideoIds = new HashSet<>();
    private final Set<String> dislikedVideoIds = new HashSet<>();

    public UserItem(String userName, String password, String displayedName, String email, String phoneNumber, Date dateOfBirth, String country, String profilePicURI) {
        this.userName = userName;
        this.password = password;
        this.displayedName = displayedName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.country = country;
        this.profilePicURI = profilePicURI;

        String lastUserId = MainPage.getDataManager().lastUserId();
        this.userId = UserLogic.nextId(lastUserId);
    }
    public UserItem(String userName, String password, String displayedName, String email, String phoneNumber, Date dateOfBirth, String country, int profilePicResId) {
        this(userName, password, displayedName, email, phoneNumber, dateOfBirth, country, MainPage.getInstance().getResources().getResourceEntryName(profilePicResId));
    }

    public Image getProfilePicture;

    public String getUserId() {
        return userId;
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

    public String getProfilePicURI() {
        return profilePicURI;
    }

    public void setProfilePicURI(String profilePicURI) {
        this.profilePicURI = profilePicURI;
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

    public void delFollow(UserItem user) {
        followingIds.remove(user.getUserId());
        user.followerIds.remove(this.getUserId());
    }

    public void setUploadedVideo(String videoId) {
        videosIds.add(videoId);
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
    public Boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    public String[] getFollowerIds() {
        return followerIds.toArray(new String[0]);
    }
    public String[] getFollowingIds() {
        return followingIds.toArray(new String[0]);
    }
}
