package com.miaxis.thermal.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.miaxis.thermal.data.entity.Calibration;

@Dao
public interface CalibrationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Calibration config);

    @Query("select * from calibration where id = 1")
    Calibration loadCalibration();

    @Query("delete from Calibration")
    void deleteAll();

}
