package com.example.crispycrumbs.model;

import static com.example.crispycrumbs.view.MainPage.getDataManager;

import com.example.crispycrumbs.List.UserList;
import com.example.crispycrumbs.dataUnit.UserItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserLogic {
    private static UserLogic instance;

    public static synchronized UserLogic getInstance() {
        if (instance == null) {
            instance = new UserLogic();
        }
        return instance;
    }

}
