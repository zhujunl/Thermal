package com.miaxis.thermal.util;

import com.miaxis.thermal.R;
import com.miaxis.thermal.app.App;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    public static final DateFormat DTO_DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.CHINA);
    public static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);

    public static boolean betweenByFormatDate(String start, String end) {
        try {
            Date startDate = DATE_FORMAT.parse(start);
            Date endDate = DATE_FORMAT.parse(end);
            Date now = new Date();
            if (now.after(startDate) && now.before(endDate)) {
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean betweenByMillisecond(long start, long end) {
        long now = new Date().getTime();
        if (now >= start && now <= end) {
            return true;
        }
        return false;
    }

    public static String getWeekStr() {
        int week = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        switch (week) {
            case 1:
                return App.getInstance().getResources().getString(R.string.Monday);
            case 2:
                return App.getInstance().getResources().getString(R.string.Tuesday);
            case 3:
                return App.getInstance().getResources().getString(R.string.Wednesday);
            case 4:
                return App.getInstance().getResources().getString(R.string.Thursday);
            case 5:
                return App.getInstance().getResources().getString(R.string.Friday);
            case 6:
                return App.getInstance().getResources().getString(R.string.Saturday);
            case 7:
                return App.getInstance().getResources().getString(R.string.Sunday);
        }
        return "";
    }

}
