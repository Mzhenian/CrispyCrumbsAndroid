package com.example.crispycrumbs.Lists;

import com.example.crispycrumbs.data.UserItem;
import com.example.crispycrumbs.ui.MainPage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class UserList  {
    private List<UserItem> users;

    public List<UserItem> getUsers() {
        return users;
    }

    public void setUsers(List<UserItem> users) {
        this.users = users;
    }

    public void addUser(UserItem user) {
        this.users.add(user);
    }

//    private int lastUserId() {
//        int max = 0;
//        for (UserItem user : MainPage.getDataManager().getUserList()) {
//            if (user.getUserId() > max) {
//                max = user.getUserId();
//            }
//        }
//        return max;
//    }


    public static boolean isEmailUnique(String email) {
        for (UserItem user : MainPage.getDataManager().getUserList()) {
            if (user.getEmail().equals(email)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isUsernameUnique(String username) {
        for (UserItem user : MainPage.getDataManager().getUserList()) {
            if (user.getUserName().equals(username)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isPhoneNumberUnique(String phoneNumber) {
        for (UserItem user : MainPage.getDataManager().getUserList()) {
            if (user.getPhoneNumber().equals(phoneNumber)) {
                return false;
            }
        }
        return true;
    }


}
