package com.miaxis.thermal.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.github.anrwatchdog.ANRWatchDog;
import com.miaxis.thermal.app.App;
import com.miaxis.thermal.view.activity.MainActivity;

public class WatchDogManager {

    private WatchDogManager() {
    }

    public static WatchDogManager getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static final WatchDogManager instance = new WatchDogManager();
    }

    /**
     * ================================ 静态内部类单例写法 ================================
     **/

    private Context context;
    private WatchDog faceDog;
    private ANRWatchDog anrWatchDog;

    public void init(Context context) {
        this.context = context;
    }

    public void startFaceFeedDog() {
        if (faceDog != null) {
            faceDog.stopFeedDog();
        }
        faceDog = new WatchDog();
        faceDog.feedDog();
        faceDog.start();
    }

    public void stopFaceFeedDog() {
        if (faceDog != null) {
            faceDog.stopFeedDog();
        }
    }

    public void feedFaceDog() {
        if (faceDog != null) {
            faceDog.feedDog();
        }
    }

    class WatchDog extends Thread {

        private boolean running;
        private long food = 0L;

        WatchDog() {
            super("FaceWatchDog");
            this.running = true;
        }

        void stopFeedDog() {
            this.running = false;
        }

        void feedDog() {
            this.food = System.currentTimeMillis();
        }

        @Override
        public void run() {
            super.run();
            try {
                this.food = System.currentTimeMillis();
                while (this.running) {
                    long interval = System.currentTimeMillis() - this.food;
                    if (interval > 10000) {
                        onNeedHandleError("看门狗检测一段时间内未收到相应，开始重新启动应用");
                    }
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void onNeedHandleError(String message) throws InterruptedException {
        Log.e("asd", "开始进行应用重启，事故原因：" + message);
        new Thread(() -> {
            Looper.prepare();
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            Looper.loop();
        }).start();
        Thread.sleep(1000);
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

    //implementation 'com.github.anrwatchdog:anrwatchdog:1.4.0'
    public void startANRWatchDog() {
        anrWatchDog = new ANRWatchDog();
        anrWatchDog.setANRListener(error -> {
            try {
                onNeedHandleError("监测到可能发生了ANR，开始重启应用");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        anrWatchDog.start();
    }

    public void stopANRWatchDog() {
        if (anrWatchDog != null) {
            anrWatchDog.interrupt();
        }
    }

}
