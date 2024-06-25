package com.example.crispycrumbs.model;

import static com.example.crispycrumbs.ui.MainPage.getDataManager;

import com.example.crispycrumbs.Lists.UserList;
import com.example.crispycrumbs.data.UserItem;
import com.example.crispycrumbs.ui.MainPage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UserLogic {
    private static UserLogic instance;

    public static synchronized UserLogic getInstance() {
        if (instance == null) {
            instance = new UserLogic();
        }
        return instance;
    }

    public static boolean isPasswordValid(String password) {
        if (password.length() < 8) {
            return false;
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        return hasLetter && hasDigit;
    }
    public static String ValidateSignUp(String email, String username, String password, String confirmPassword, String displayedName, String phoneNumber, String birthDate) {//todo add//, String profilePicPath) {
        if (username == null || username.isEmpty()) {
            return "Username is required";
        }
        if (password == null || password.isEmpty()) {
            return "Password is required";
        }
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            return "Confirm password is required";
        }
        if (!password.equals(confirmPassword)) {
            return "Passwords do not match";
        }

        if (email != null && !email.isEmpty()) {
//            String regexPattern = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
//            EmailValidation.patternMatches(email, regexPattern);

            if (!UserList.isEmailUnique(email)) {
                return "Email is already in use (try to log in)";
            }
        }
        if (username != null && !username.isEmpty()) {
            if (!UserList.isUsernameUnique(username)) {
                return "Username is already in use (try to log in)";
            }
        }

        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            if (!phoneNumber.matches("\\d{10}")) {
                return "Phone number must be 10 digits long";
            }
        }
        if ( phoneNumber != null && !phoneNumber.isEmpty()) {
            if (!UserList.isPhoneNumberUnique(phoneNumber)) {
                return "Phone number is already in use (try to log in)";
            }
        }
        if (!(isPasswordValid(password))) {
            return "Password must be at least 8 characters long and contain a mix of letters and digits.";
        }
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        Date parsedBirthDate = null;
        try {
            parsedBirthDate = format.parse(birthDate);
        } catch (ParseException e) {
            birthDate = null;
        }
        //TODO add image validation
//        if (profilePicPath != null && !profilePicPath.isEmpty()) {
//            if (!profilePicPath.matches(".*\\.(jpg|png|jpeg)")) {
//                return "Profile picture must be a jpg, jpeg, or png file";
//            }
//        }

        //if there are no errors
        return null;
    }


    public static UserItem ValidateLogin(String username, String password) {
        if (username == null || username.isEmpty()) {
            return null;
        }
        if (password == null || password.isEmpty()) {
            return null;
        }

        for (UserItem user : getDataManager().getUserList()) {
            if (user == null) {
                continue;
            }
            if (user.getUserName().equals(username) && user.checkPassword(password)) {
                return user;
            }
        }
        return null;
    }

    //todo debug math
    public static String nextId(String last) {
        Boolean succeeded = false;
        char[] charArray = last.toCharArray();
        for (int i = charArray.length - 1; i >= 0; i--) {
            if (('0' <= charArray[i] && charArray[i] < '9')
                    || ('a' <= charArray[i] && charArray[i] < 'z')
                    || ('A' <= charArray[i] && charArray[i] < 'Z')) {
                charArray[i]++;
                succeeded = true;
                break;
            } else if (charArray[i] == '9') {
                charArray[i] = 'a';
                succeeded = true;
                break;
            } else if (charArray[i] == 'z') {
                charArray[i] = 'A';
                succeeded = true;
                break;
            } else {
                charArray[i] = '0';
            }
        }
        if (!succeeded) {
            charArray = new char[charArray.length + 1];
            charArray[0] = '0';
            for (int i = 1; i < charArray.length; i++) {
                charArray[i] = '0';
            }
        }
        return new String(charArray);
    }
}
