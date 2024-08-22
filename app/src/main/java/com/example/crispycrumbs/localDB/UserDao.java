package com.example.crispycrumbs.localDB;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.crispycrumbs.dataUnit.UserItem;

import java.util.List;

@Dao
public interface UserDao {
    @Insert
    void insertUser(UserItem user);

    @Update
    void updateUser(UserItem user);

    @Query("SELECT * FROM users WHERE userId = :userId")
    LiveData<UserItem> getUserById(String userId);

    @Query("SELECT * FROM users")
    LiveData<List<UserItem>> getAllUsers();
}

