package com.miaxis.thermal.data.model;

import android.database.Cursor;
import android.text.TextUtils;

import com.miaxis.thermal.data.dao.AppDatabase;
import com.miaxis.thermal.data.entity.Person;
import com.miaxis.thermal.data.entity.PersonSearch;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PersonModel {

    public static List<Person> loadUsability() {
        return AppDatabase.getInstance().personDao().loadUsability();
    }

    public static List<Person> loadPersonByPage(int pageNum, int pageSize) {
        return AppDatabase.getInstance().personDao().loadPersonByPage(pageNum, pageSize);
    }

    public static Person findPerson(String field) {
        return AppDatabase.getInstance().personDao().findPerson(field);
    }

    public static int loadPersonCount() {
        return AppDatabase.getInstance().personDao().loadPersonCount();
    }

    public static Person findOldestPerson() {
        return AppDatabase.getInstance().personDao().findOldestPerson();
    }

    public static void savePerson(Person person) {
        AppDatabase.getInstance().personDao().insert(person);
    }

    public static List<Person> findOverduePerson() {
        return AppDatabase.getInstance().personDao().findOverduePerson(new Date().getTime());
    }

    public static void deletePerson(Person person) {
        AppDatabase.getInstance().personDao().delete(person);
    }

    public static void deleteAll() {
        AppDatabase.getInstance().personDao().deleteAll();
    }

    public static List<Person> searchPerson(PersonSearch personSearch) {
        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder("select * from Person");
        if (!TextUtils.isEmpty(personSearch.getName())) {
            sql.append(" where Person.name = ?");
            args.add(personSearch.getName());
        }
        if (!TextUtils.isEmpty(personSearch.getIdentifyNumber())) {
            sql.append(" where Person.identifyNumber = ?");
            args.add(personSearch.getIdentifyNumber());
        }
        if (!TextUtils.isEmpty(personSearch.getPhone())) {
            sql.append(" where Person.phone = ?");
            args.add(personSearch.getPhone());
        }
        if (personSearch.getUpload() != null) {
            sql.append(" where Person.upload = ?");
            args.add(personSearch.getUpload() ? "1" : "0");
        }
        if (personSearch.getFace() != null) {
            if (personSearch.getFace()) {
                sql.append(" where Person.faceFeature is not null");
            } else {
                sql.append(" where Person.faceFeature is null");
            }
        }
        if (personSearch.getStatus() != null) {
            sql.append(" where Person.status = ?");
            args.add(personSearch.getStatus());
        }
        if (personSearch.getType() != null) {
            sql.append(" where Person.type = ?");
            args.add(personSearch.getType());
        }
        sql.append(" order by Person.updateTime desc limit ? offset ? * (? - 1)");
        args.add(personSearch.getPageSize());
        args.add(personSearch.getPageSize());
        args.add(personSearch.getPageNum());
        String sqlStr = sql.toString();
        Cursor cursor = null;
        try {
            cursor = AppDatabase.getInstance().query(sqlStr, args.toArray());
            if (cursor.getCount() > 0) {
                List<Person> personList = new ArrayList<>(cursor.getCount());
                while (cursor.moveToNext()) {
                    Date effectiveTime = new Date();
                    effectiveTime.setTime(cursor.getLong(cursor.getColumnIndex("effectiveTime")));
                    Date invalidTime = new Date();
                    invalidTime.setTime(cursor.getLong(cursor.getColumnIndex("invalidTime")));
                    Date updateTime = new Date();
                    updateTime.setTime(cursor.getLong(cursor.getColumnIndex("updateTime")));
                    Person build = new Person.Builder()
                            .id(cursor.getLong(cursor.getColumnIndex("id")))
                            .identifyNumber(cursor.getString(cursor.getColumnIndex("identifyNumber")))
                            .phone(cursor.getString(cursor.getColumnIndex("phone")))
                            .name(cursor.getString(cursor.getColumnIndex("name")))
                            .type(cursor.getString(cursor.getColumnIndex("type")))
                            .effectiveTime(effectiveTime)
                            .invalidTime(invalidTime)
                            .updateTime(updateTime)
                            .faceFeature(cursor.getString(cursor.getColumnIndex("faceFeature")))
                            .maskFaceFeature(cursor.getString(cursor.getColumnIndex("maskFaceFeature")))
                            .facePicturePath(cursor.getString(cursor.getColumnIndex("facePicturePath")))
                            .timeStamp(cursor.getLong(cursor.getColumnIndex("timeStamp")))
                            .remarks(cursor.getString(cursor.getColumnIndex("remarks")))
                            .upload(cursor.getInt(cursor.getColumnIndex("upload")) == 1)
                            .status(cursor.getString(cursor.getColumnIndex("status")))
                            .build();
                    personList.add(build);
                }
                return personList;
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
