package com.example.crispycrumbs.List;

import static com.example.crispycrumbs.view.MainPage.getDataManager;

import com.example.crispycrumbs.dataUnit.UserItem;

import java.util.Dictionary;
import java.util.List;
import java.util.Map;


public class UserList  {
    private List<UserItem> users;
    Map<String, String> userMap;
    Dictionary<String, String> userDict;

    public List<UserItem> getUsers() {
        return users;
    }

    public void setUsers(List<UserItem> users) {
        this.users = users;
    }

    public void addUser(UserItem user) {
        this.users.add(user);
    }

    public static boolean isEmailUnique(String email) {
        for (UserItem user : getDataManager().getUserList()) {
            if (user.getEmail().equals(email)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isUsernameUnique(String username) {
        for (UserItem user : getDataManager().getUserList()) {
            if (user.getUserName().equals(username)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isPhoneNumberUnique(String phoneNumber) {
        for (UserItem user : getDataManager().getUserList()) {
            if (user.getPhoneNumber().equals(phoneNumber)) {
                return false;
            }
        }
        return true;
    }


}
