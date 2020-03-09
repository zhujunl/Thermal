package com.miaxis.thermal.data.dao.converter;

import androidx.room.TypeConverter;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.miaxis.thermal.util.ValueUtil;

import java.util.List;

public class StringListConverter {

    @TypeConverter
    public static List<String> revert(String s) {
        try {
            return ValueUtil.GSON.fromJson(s, new TypeToken<List<String>>() {}.getType());
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    @TypeConverter
    public static String converter(List<String> list) {
        return ValueUtil.GSON.toJson(list);
    }

}
