package com.example.crispycrumbs.localDB;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import android.content.Context;

import com.example.crispycrumbs.converters.CommentItemTypeConverter;
import com.example.crispycrumbs.converters.SetTypeConverter;
import com.example.crispycrumbs.converters.StringListTypeConverter;
import com.example.crispycrumbs.dao.CommentDao;
import com.example.crispycrumbs.dao.UserDao;
import com.example.crispycrumbs.dao.VideoDao;
import com.example.crispycrumbs.dataUnit.CommentItem;
import com.example.crispycrumbs.dataUnit.PreviewVideoCard;
import com.example.crispycrumbs.dataUnit.UserItem;

@Database(entities = {UserItem.class, PreviewVideoCard.class, CommentItem.class}, version = 2) // Updated version
@TypeConverters({SetTypeConverter.class, StringListTypeConverter.class, CommentItemTypeConverter.class})
public abstract class AppDB extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract VideoDao videoDao();
    public abstract CommentDao commentDao();

    private static volatile AppDB INSTANCE;

    public static AppDB getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDB.class, "app_database")
                            .fallbackToDestructiveMigration() // This will clear the database on schema change
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}


