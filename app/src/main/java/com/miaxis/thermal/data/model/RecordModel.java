package com.miaxis.thermal.data.model;

import android.database.Cursor;
import android.text.TextUtils;

import androidx.sqlite.db.SupportSQLiteQueryBuilder;

import com.miaxis.thermal.data.dao.AppDatabase;
import com.miaxis.thermal.data.entity.Record;
import com.miaxis.thermal.data.entity.RecordSearch;
import com.miaxis.thermal.util.DateUtil;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RecordModel {

    public static void saveRecord(Record record) {
        AppDatabase.getInstance().recordDao().insert(record);
    }

    public static List<Record> loadRecordByPage(int pageNum, int pageSize) {
        return AppDatabase.getInstance().recordDao().loadRecordByPage(pageNum, pageSize);
    }

    public static int loadRecordCount() {
        return AppDatabase.getInstance().recordDao().loadRecordCount();
    }

    public static Record findOldestRecord() {
        return AppDatabase.getInstance().recordDao().findOldestRecord();
    }

    public static void deleteAll() {
        AppDatabase.getInstance().recordDao().deleteAll();
    }

    public static void deleteRecordList(List<Record> recordList) {
        AppDatabase.getInstance().recordDao().deleteRecordList(recordList);
    }

    public static List<Record> searchRecordInTime(Date startTime, Date endTime) {
        return AppDatabase.getInstance().recordDao().searchRecordInTime(startTime.getTime(), endTime.getTime());
    }

    public static List<Record> searchRecord(RecordSearch recordSearch) {
        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder("select * from Record");
        if (!TextUtils.isEmpty(recordSearch.getIdentifyNumber())) {
            sql.append(" where Record.identifyNumber = ?");
            args.add(recordSearch.getIdentifyNumber());
        }
        if (!TextUtils.isEmpty(recordSearch.getPhone())) {
            sql.append(" where Record.phone = ?");
            args.add(recordSearch.getPhone());
        }
        if (recordSearch.isUpload() != null) {
            sql.append(" where Record.upload = ?");
            args.add(recordSearch.isUpload() ? "1" : "0");
        }
        if (!TextUtils.isEmpty(recordSearch.getStartTime())) {
            try {
                Date startTime = DateUtil.DATE_FORMAT.parse(recordSearch.getStartTime());
                if (startTime != null) {
                    sql.append(" where Record.verifyTime >= ?");
                    args.add(startTime.getTime());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (!TextUtils.isEmpty(recordSearch.getEndTime())) {
            try {
                Date endTime = DateUtil.DATE_FORMAT.parse(recordSearch.getEndTime());
                if (endTime != null) {
                    sql.append(" where Record.verifyTime <= ?");
                    args.add(endTime.getTime());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (recordSearch.getMinTemperature() != null) {
            sql.append(" where Record.temperature >= ?");
            args.add(recordSearch.getMinTemperature());
        }
        sql.append(" order by Record.id desc limit ? offset ? * (? - 1)");
        args.add(recordSearch.getPageSize());
        args.add(recordSearch.getPageSize());
        args.add(recordSearch.getPageNum());
        String sqlStr = sql.toString();
        Cursor cursor = null;
        try {
            cursor = AppDatabase.getInstance().query(sqlStr, args.toArray());
            if (cursor.getCount() > 0) {
                List<Record> recordList = new ArrayList<>(cursor.getCount());
                while (cursor.moveToNext()) {
                    Date date = new Date();
                    date.setTime(cursor.getLong(cursor.getColumnIndex("verifyTime")));
                    Record build = new Record.Builder()
                            .id(cursor.getLong(cursor.getColumnIndex("id")))
                            .personId(cursor.getLong(cursor.getColumnIndex("personId")))
                            .identifyNumber(cursor.getString(cursor.getColumnIndex("identifyNumber")))
                            .phone(cursor.getString(cursor.getColumnIndex("phone")))
                            .name(cursor.getString(cursor.getColumnIndex("name")))
                            .type(cursor.getString(cursor.getColumnIndex("type")))
                            .verifyTime(date)
                            .verifyPicturePath(cursor.getString(cursor.getColumnIndex("verifyPicturePath")))
                            .score(cursor.getFloat(cursor.getColumnIndex("score")))
                            .upload(cursor.getInt(cursor.getColumnIndex("upload")) == 1)
                            .temperature(cursor.getFloat(cursor.getColumnIndex("temperature")))
                            .build();
                    recordList.add(build);
                }
                return recordList;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return new ArrayList<>();
    }

}
