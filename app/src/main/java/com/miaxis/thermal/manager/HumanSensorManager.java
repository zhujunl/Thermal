package com.miaxis.thermal.manager;

import android.app.Application;
import android.util.Log;

import com.miaxis.thermal.manager.strategy.Sign;
import com.miaxis.thermal.manager.strategy.mr860dz.MR860DZHumanSensorStrategy;
import com.miaxis.thermal.util.ValueUtil;

import java.util.concurrent.atomic.AtomicBoolean;

public class HumanSensorManager {

    private HumanSensorManager() {
    }

    public static HumanSensorManager getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static final HumanSensorManager instance = new HumanSensorManager();
    }

    /**
     * ================================ 静态内部类单例 ================================
     **/

    private HumanSensorStrategy humanSensorStrategy;

    public void init(Application application) {
        if (ValueUtil.DEFAULT_SIGN == Sign.MR860DZ) {
            humanSensorStrategy = new MR860DZHumanSensorStrategy();
        } else {
            humanSensorStrategy = new DefaultHumanSensorStrategy();
        }
        initGpio(application);
    }

    private void initGpio(Application application) {
        if (humanSensorStrategy != null) {
            humanSensorStrategy.initDevice(application);
        }
    }

    private boolean readSensorStatus() {
        if (humanSensorStrategy != null) {
            return humanSensorStrategy.readSensorStatus();
        }
        return false;
    }

    public interface HumanSensorStrategy {
        void initDevice(Application application);
        boolean readSensorStatus();
    }

    private static class DefaultHumanSensorStrategy implements HumanSensorStrategy {
        @Override
        public void initDevice(Application application) {}
        @Override
        public boolean readSensorStatus() {
            return false;
        }
    }

    /**
     * ================================================================
     **/

    private OnHumanSensorStatusListener statusListener;
    private HumanBodySensorWatcher humanSensor;

    public void setStatusListener(OnHumanSensorStatusListener statusListener) {
        this.statusListener = statusListener;
    }

    public void startHumanDetect() {
        if (humanSensor != null) {
            humanSensor.stopWatch();
        }
        humanSensor = new HumanBodySensorWatcher();
        humanSensor.start();
    }

    public void faceDetect() {
        if (humanSensor != null) {
            humanSensor.faceDetect();
        }
    }

    public void stopHumanDetect() {
        if (humanSensor != null) {
            if (!humanSensor.isInterrupted()) {
                humanSensor.interrupt();
            }
            humanSensor.stopWatch();
        }
    }

    public interface OnHumanSensorStatusListener {
        void onStatus(boolean status);
    }

    class HumanBodySensorWatcher extends Thread {

        private boolean running;
        private AtomicBoolean mark;
        private long lastHumanBodyDetectTime;

        HumanBodySensorWatcher() {
            super("HumanBodySensorWatcher");
            this.running = true;
            this.mark = new AtomicBoolean(true);
        }

        void faceDetect() {
            this.lastHumanBodyDetectTime = System.currentTimeMillis();
        }

        void stopWatch() {
            this.running = false;
        }

        @Override
        public void run() {
            super.run();
            try {
                lastHumanBodyDetectTime = System.currentTimeMillis();
                mark.set(true);
                while (this.running) {
                    boolean humanDetect = readSensorStatus();
//                    Log.e("asd", "HumanSensor:" + humanDetect);
                    if (humanDetect) {
                        lastHumanBodyDetectTime = System.currentTimeMillis();
                    }
                    long interval = System.currentTimeMillis() - lastHumanBodyDetectTime;
                    if (interval > ConfigManager.getInstance().getConfig().getDormancyTime() * 1000 && mark.get()) {
                        mark.set(false);
                        if (statusListener != null) {
                            statusListener.onStatus(false);
                        }
                    } else if (humanDetect && !mark.get()) {
                        mark.set(true);
                        if (statusListener != null) {
                            statusListener.onStatus(true);
                        }
                    }
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
