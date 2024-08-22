package com.example.crispycrumbs.localDB;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public class SetTypeConverter {

    @TypeConverter
    public String fromSet(Set<String> set) {
        if (set == null) {
            return null;
        }
        Gson gson = new Gson();
        Type type = new TypeToken<Set<String>>() {}.getType();
        return gson.toJson(set, type);
    }

    @TypeConverter
    public Set<String> toSet(String json) {
        if (json == null) {
            return new HashSet<>();
        }
        Gson gson = new Gson();
        Type type = new TypeToken<Set<String>>() {}.getType();
        return gson.fromJson(json, type);
    }
}
