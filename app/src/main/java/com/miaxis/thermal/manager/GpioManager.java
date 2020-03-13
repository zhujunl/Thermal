package com.miaxis.thermal.manager;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import com.android.xhapimanager.XHApiManager;
import com.miaxis.thermal.data.entity.Config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GpioManager {

    private GpioManager() {
        xhApiManager = new XHApiManager();
    }

    public static GpioManager getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static final GpioManager instance = new GpioManager();
    }

    /**
     * ================================ 静态内部类单例 ================================
     **/

    private Application application;
    private static ExecutorService executorService = Executors.newFixedThreadPool(5);
    private HandlerThread handlerThread;
    private Handler handler;

    private XHApiManager xhApiManager;

    private volatile boolean warning = false;

    public void init(Application application) {
        this.application = application;
        resetGpio();
        handlerThread = new HandlerThread("FLASH_LED_THREAD");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    public void resetGpio() {
        xhApiManager.XHSetGpioValue(0, 0);
        xhApiManager.XHSetGpioValue(1, 0);
        xhApiManager.XHSetGpioValue(2, 0);
        xhApiManager.XHSetGpioValue(3, 0);
        xhApiManager.XHSetGpioValue(4, 0);
    }

    public void setStatusBar(boolean show) {
        xhApiManager.XHShowOrHideStatusBar(show);
    }

    public void openLedInTime() {
        executorService.execute(() -> {
            Config config = ConfigManager.getInstance().getConfig();
            if (!config.isFaceCamera()) {
                int delay = config.getFlashTime() * 1000;
                handler.removeCallbacks(closeLedRunnable);
                if (xhApiManager.XHReadGpioValue(0) == 0) {
                    openLed();
                }
                handler.postDelayed(closeLedRunnable, delay);
            }
        });
    }

    public void clearLedThread() {
        handler.removeCallbacks(closeLedRunnable);
    }

    private void openLed() {
        executorService.execute(() -> {
            try {
                if (warning) return;
                xhApiManager.XHSetGpioValue(0, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void closeLed() {
        executorService.execute(() -> {
            try {
                xhApiManager.XHSetGpioValue(0, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void openRedLed() {
        executorService.execute(() -> {
            try {
                warning = true;
                xhApiManager.XHSetGpioValue(0, 0);
                xhApiManager.XHSetGpioValue(2, 1);
                Thread.sleep(500);
                xhApiManager.XHSetGpioValue(0, 1);
                xhApiManager.XHSetGpioValue(2, 0);
                Thread.sleep(500);
                xhApiManager.XHSetGpioValue(0, 0);
                xhApiManager.XHSetGpioValue(2, 1);
                Thread.sleep(500);
                xhApiManager.XHSetGpioValue(2, 0);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                warning = false;
            }
        });
    }

    public void openInfraredLed() {
        executorService.execute(() -> {
            try {
                xhApiManager.XHSetGpioValue(3, 1);
                xhApiManager.XHSetGpioValue(4, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void closeInfraredLed() {
        executorService.execute(() -> {
            try {
                xhApiManager.XHSetGpioValue(3, 0);
                xhApiManager.XHSetGpioValue(4, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    //由于闸机关门时间不定，且上下电都会造成门两向开启
    public void openDoor(long delay) {
        executorService.execute(() -> {
//            try {
//                int level = HwitManager.HwitGetIOValue(9);
//                if (level == 1) {
//                    HwitManager.HwitSetIOValue(9, 0);
//                    Thread.sleep(500);
//                    HwitManager.HwitSetIOValue(9, 1);
//                } else {
//                    HwitManager.HwitSetIOValue(9, 1);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        });
    }

    private Runnable closeLedRunnable = this::closeLed;

    public void setGpioWatchDog(boolean status) {
        xhApiManager.XHWatchDogEnable(status);
    }

    public void feedGpioWatchDog() {
        xhApiManager.XHWatchDogFeed();
    }

}
