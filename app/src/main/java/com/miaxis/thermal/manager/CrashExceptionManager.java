package com.miaxis.thermal.manager;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.miaxis.thermal.app.App;
import com.miaxis.thermal.view.activity.MainActivity;

import java.util.HashMap;
import java.util.Map;

public class CrashExceptionManager implements Thread.UncaughtExceptionHandler {

    private CrashExceptionManager() {
    }

    public static CrashExceptionManager getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static final CrashExceptionManager instance = new CrashExceptionManager();
    }

    /**
     * ================================ 静态内部类单例写法 ================================
     **/

    public static String TAG = "MyCrash";
    // 系统默认的UncaughtException处理类
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private Context mContext;
    // 用来存储设备信息和异常信息
    private Map<String, String> infos = new HashMap<>();
    // 用于格式化日期,作为日志文件名的一部分

    /**
     * 初始化
     */
    public void init(Application application) {
        mContext = application;
        // 获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        // 设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        ex.printStackTrace();
        CameraManager.getInstance().closeCamera();
        Log.e("asd", "" + ex.getMessage());
        new Thread(() -> {
            Looper.prepare();
            Toast.makeText(mContext, "很抱歉，程序出现异常，即将重新启动", Toast.LENGTH_LONG).show();
            Looper.loop();
        }).start();
        restartApp();
    }

    private void restartApp() {
        Context context = App.getInstance().getApplicationContext();
        Intent mStartActivity = new Intent(context, MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }

}
