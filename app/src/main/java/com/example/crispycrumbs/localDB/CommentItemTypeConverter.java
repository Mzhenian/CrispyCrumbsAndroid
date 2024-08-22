package com.example.crispycrumbs.localDB;

import androidx.room.TypeConverter;

import com.example.crispycrumbs.dataUnit.CommentItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class CommentItemTypeConverter {

    @TypeConverter
    public String fromCommentList(ArrayList<CommentItem> list) {
        if (list == null) {
            return null;
        }
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<CommentItem>>() {}.getType();
        return gson.toJson(list, type);
    }

    @TypeConverter
    public ArrayList<CommentItem> toCommentList(String json) {
        if (json == null) {
            return new ArrayList<>();
        }
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<CommentItem>>() {}.getType();
        return gson.fromJson(json, type);
    }
}
