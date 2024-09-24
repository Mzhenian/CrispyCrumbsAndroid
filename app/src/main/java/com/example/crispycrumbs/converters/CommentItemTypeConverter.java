package com.example.crispycrumbs.converters;

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
        // Clone the list before converting to JSON to avoid modification issues
        ArrayList<CommentItem> safeList = new ArrayList<>(list);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<CommentItem>>() {}.getType();
        return gson.toJson(safeList, type);
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

