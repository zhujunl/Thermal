package com.miaxis.thermal.data.dao.converter;

import androidx.room.TypeConverter;

import java.util.Date;

public class DateConverter {
    @TypeConverter
    public static Date revertDate(long value) {
        if (value == 0L) {
            return null;
        } else {
            return new Date(value);
        }
    }

    @TypeConverter
    public static long converterDate(Date value) {
        if (value == null) {
            return 0L;
        } else {
            return value.getTime();
        }
    }
}