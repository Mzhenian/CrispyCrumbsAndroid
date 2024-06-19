package com.example.crispycrumbs.Lists;

import com.example.crispycrumbs.data.UserItem;
import com.example.crispycrumbs.ui.MainPage;

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

//todo remove
//public class UserList extends ArrayList<UserItem> {
//    private static UserList instance = null;
//
//    private UserList() {
//
//        String json = readUsersDB(MainPage.getAppContext());
//        List<UserItem> users = parseUsers(json);
//        this.addAll(users);
//        nextUserId = maxUserId() + 1; //todo move to the permanent users storage
//    }

//
//    public static UserList getInstance() {
//        if (instance == null) {
//            instance = new UserList();
//        }
//        return instance;
//    }
//
//    private String readUsersDB(Context context) {
//        String jsonString;
//        AssetManager assetManager = context.getAssets();
//        try (InputStream inputStream = assetManager.open("usersDB.json");
//             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
//
//            StringBuilder jsonBuilder = new StringBuilder();
//            String line;
//            while ((line = reader.readLine()) != null) {
//                jsonBuilder.append(line);
//            }
//
//            jsonString = jsonBuilder.toString();
//            return jsonString;
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//
//
//    private List<UserItem> parseUsers(String jsonString) {
//        //todo remove second
////        Gson gson = new Gson();
////        Type userListType = new TypeToken<ArrayList<UserItem>>() {}.getType();
////        ArrayList<UserItem> users = gson.fromJson(jsonString, userListType);
////        this.addAll(users);
//
//        Gson gson = new Gson();
//        Type userListType = new TypeToken<ArrayList<UserItem>>(){}.getType();
//        return gson.fromJson(jsonString, userListType);
//    }
//
//
//
//}
