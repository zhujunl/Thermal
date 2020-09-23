package com.miaxis.thermal.manager;

import android.util.Log;

import com.miaxis.thermal.data.entity.Config;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class TimingSwitchManager {

    private TimingSwitchManager() {
    }

    public static TimingSwitchManager getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static final TimingSwitchManager instance = new TimingSwitchManager();
    }

    /**
     * ================================ 静态内部类单例写法 ================================
     **/

    public static final int PERIOD_DAY = 1000 * 60 * 60 * 24;

    private Timer startTimer;
    private Timer endTimer;

    private TimerTask startTimerTask;
    private TimerTask endTimerTask;

    private OnTimingSwitchStatusListener listener;

    public void init() {

    }

    public void startTimingSwitch() {
        Config config = ConfigManager.getInstance().getConfig();
        if (config.isTimingSwitch()) {
            initTimerTask();
            startTimer = new Timer(true);
            endTimer = new Timer(true);
            long now = new Date().getTime();
            Date start = getTimeStamp(config.getSwitchStartTime());
            if (start.getTime() > now) {
                if (listener != null) {
                    listener.onStatus(false);
                }
            } else {
                start = getNextDate(start);
            }
            startTimer.schedule(startTimerTask, start, PERIOD_DAY);
            Log.e("asd", "startTimingSwitch:startTime" + start.toString());
            Date end = getTimeStamp(config.getSwitchEndTime());
            if (end.getTime() < now) {
                if (listener != null) {
                    listener.onStatus(false);
                }
                end = getNextDate(end);
            }
            endTimer.schedule(endTimerTask, end, PERIOD_DAY);
            Log.e("asd", "startTimingSwitch:endTime" + end.toString());
        }
    }

    public void stopTimingSwitch() {
        if (startTimer != null) {
            startTimerTask.cancel();
            startTimer.cancel();
            startTimer.purge();
        }
        if (endTimer != null) {
            endTimerTask.cancel();
            endTimer.cancel();
            endTimer.purge();
        }
        Log.e("asd", "stopTimingSwitch");
    }

    public void setListener(OnTimingSwitchStatusListener listener) {
        this.listener = listener;
    }

    public interface OnTimingSwitchStatusListener {
        void onStatus(boolean status);
    }

    private void initTimerTask() {
        startTimerTask = new TimerTask() {
            @Override
            public void run() {
                Log.e("asd", "startTimerTask");
                Config config = ConfigManager.getInstance().getConfig();
                if (config.isTimingSwitch()) {
                    if (listener != null) {
                        listener.onStatus(true);
                    }
                }
            }
        };
        endTimerTask = new TimerTask() {
            @Override
            public void run() {
                Log.e("asd", "endTimerTask");
                Config config = ConfigManager.getInstance().getConfig();
                if (config.isTimingSwitch()) {
                    if (listener != null) {
                        listener.onStatus(false);
                    }
                }
            }
        };
    }

    private Date getTimeStamp(String time) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.split(":")[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(time.split(":")[1]));
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    private Date getNextDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, 1);
        return calendar.getTime();
    }

    public boolean isInSwitchTime() {
        Config config = ConfigManager.getInstance().getConfig();
        Date start = getTimeStamp(config.getSwitchStartTime());
        Date end = getTimeStamp(config.getSwitchEndTime());
        Date now = new Date();
        if (start.getTime() > end.getTime()) {
            end = getNextDate(end);
        }
        return now.getTime() > start.getTime() && now.getTime() < end.getTime();
    }

}
