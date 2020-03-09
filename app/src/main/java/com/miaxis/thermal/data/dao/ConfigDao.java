package com.miaxis.thermal.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.miaxis.thermal.data.entity.Config;

@Dao
public interface ConfigDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Config config);

    @Query("select * from config where id = 1")
    Config loadConfig();

    @Query("delete from config")
    void deleteAll();

}
