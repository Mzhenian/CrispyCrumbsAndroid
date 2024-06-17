package com.example.crispycrumbs;

import android.content.Context;
import android.content.res.AssetManager;

import com.example.crispycrumbs.ui.MainPage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class UserList extends ArrayList<UserItem> {
    private static UserList instance = null;

    private UserList() {

        String json = readUsersDB(MainPage.getAppContext());
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

    private String readUsersDB(Context context) {
        String jsonString;
        AssetManager assetManager = context.getAssets();
        try (InputStream inputStream = assetManager.open("usersDB.json");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            jsonString = jsonBuilder.toString();
            return jsonString;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    private List<UserItem> parseUsers(String jsonString) {
        //todo remove second
//        Gson gson = new Gson();
//        Type userListType = new TypeToken<ArrayList<UserItem>>() {}.getType();
//        ArrayList<UserItem> users = gson.fromJson(jsonString, userListType);
//        this.addAll(users);

        Gson gson = new Gson();
        Type userListType = new TypeToken<ArrayList<UserItem>>(){}.getType();
        return gson.fromJson(jsonString, userListType);
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
