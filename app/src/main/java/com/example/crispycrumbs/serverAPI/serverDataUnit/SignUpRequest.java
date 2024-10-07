package com.example.crispycrumbs.serverAPI.serverDataUnit;


public class SignUpRequest {
    private String userName;
    private String email;
    private String password;
    private String fullName;
    private String phoneNumber;
    private String birthday;
    private String country;
    private String profilePhoto;


    public SignUpRequest(String username, String email, String password, String fullName, String phoneNumber, String birthday, String country, String profilePhoto) {
        this.userName = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.birthday = birthday;
        this.country = country;
        this.profilePhoto = profilePhoto;
    }

    // Getters and Setters
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getBirthday() {
        return birthday;
    }

    public void getBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }
}