package com.example.crispycrumbs.Lists;

import com.example.crispycrumbs.data.UserItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class UserList extends ArrayList<UserItem> {
    private static UserList instance = null;

    private UserList() {
        String json = readUsersDB();
        List<UserItem> users = parseUsers(json);
        this.addAll(users);
        nextUserId = maxUserId() + 1; //todo move to the permanent users storage
    }
    public int takeNextUserId() {
        return nextUserId++;
    }

    public static UserList getInstance() {
        if (instance == null) {
            instance = new UserList();
        }
        return instance;
    }

    private String readUsersDB() {
        String json = null;
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("usersDB.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return json;
    }

    private List<UserItem> parseUsers(String json) {
        Gson gson = new Gson();
        Type userListType = new TypeToken<ArrayList<UserItem>>(){}.getType();
        return gson.fromJson(json, userListType);
    }

    int nextUserId; //todo move to the permanent users storage
    private int maxUserId() {
        int max = 0;
        for (UserItem user : this) {
            if (user.getUserId() > max) {
                max = user.getUserId();
            }
        }
        return max;
    }
    public static boolean isEmailUnique(String email) {
        for (UserItem user : instance) {
            if (user.getEmail().equals(email)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isUsernameUnique(String username) {
        for (UserItem user : instance) {
            if (user.getUsername().equals(username)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isPhoneNumberUnique(String phoneNumber) {
        for (UserItem user : instance) {
            if (user.getPhoneNumber().equals(phoneNumber)) {
                return false;
            }
        }
        return true;
    }


}
