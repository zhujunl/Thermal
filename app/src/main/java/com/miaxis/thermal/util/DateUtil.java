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
    public static final DateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.CHINA);
    public static final DateFormat DTO_DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.CHINA);
    public static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);
    public static final DateFormat YEAR_MONTH_DAY_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);

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

    public static boolean compareDate(String start, String end) {
        try {
            Date startTime = DateUtil.DATE_FORMAT.parse(start);
            Date endTime = DateUtil.DATE_FORMAT.parse(end);
            if (startTime.getTime() <= endTime.getTime()) {
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Date getTonight() {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date getNextYear() {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);
        return calendar.getTime();
    }

    public static String getWeekStr() {
        int week = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        switch (week) {
            case 1:
                return App.getInstance().getResources().getString(R.string.Sunday);
            case 2:
                return App.getInstance().getResources().getString(R.string.Monday);
            case 3:
                return App.getInstance().getResources().getString(R.string.Tuesday);
            case 4:
                return App.getInstance().getResources().getString(R.string.Wednesday);
            case 5:
                return App.getInstance().getResources().getString(R.string.Thursday);
            case 6:
                return App.getInstance().getResources().getString(R.string.Friday);
            case 7:
                return App.getInstance().getResources().getString(R.string.Saturday);
        }
        return "";
    }

}
