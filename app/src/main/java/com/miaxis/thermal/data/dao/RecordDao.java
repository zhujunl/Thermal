package com.miaxis.thermal.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.miaxis.thermal.data.entity.Record;
import com.miaxis.thermal.data.entity.RecordSearch;

import java.util.List;

@Dao
public interface RecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Record record);

    @Query("select * from Record order by Record.id desc limit :pageSize offset :pageSize * (:pageNum - 1)")
    List<Record> loadRecordByPage(int pageNum, int pageSize);

    @Query("select count(*) from Record")
    int loadRecordCount();

    @Query("select * from Record where Record.upload = 0 order by Record.verifyTime asc limit 1")
    Record findOldestRecord();

    @Query("delete from Record")
    void deleteAll();

    @Delete
    void deleteRecordList(List<Record> recordList);

    @Query("select * from Record where Record.verifyTime >= :startTime and Record.verifyTime <= :endTime")
    List<Record> searchRecordInTime(long startTime, long endTime);

}
