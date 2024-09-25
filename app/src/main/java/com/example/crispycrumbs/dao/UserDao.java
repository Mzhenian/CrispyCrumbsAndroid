package com.example.crispycrumbs.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.crispycrumbs.dataUnit.UserItem;

import java.util.List;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(UserItem user);


    @Update
    void updateUser(UserItem user);

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    LiveData<UserItem> getUserById(String userId);

    //todo limit to 10 plus the loggedin user
    @Query("SELECT * FROM users")
    LiveData<List<UserItem>> getAllUsers();

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    UserItem getUserByIdSync(String userId);
}

