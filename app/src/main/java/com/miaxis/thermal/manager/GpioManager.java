package com.miaxis.thermal.manager;

import android.app.Application;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.miaxis.thermal.data.entity.Config;
import com.miaxis.thermal.manager.strategy.Sign;
import com.miaxis.thermal.manager.strategy.mr870.MR870GpioStrategy;
import com.miaxis.thermal.manager.strategy.tps.TpsCameraStrategy;
import com.miaxis.thermal.manager.strategy.tps.TpsGpioStrategy;
import com.miaxis.thermal.manager.strategy.xh.XhGpioStrategy;
import com.miaxis.thermal.manager.strategy.zh.ZhGpioStrategy;
import com.miaxis.thermal.util.ValueUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GpioManager {

    private GpioManager() {
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

    private GpioStrategy gpioStrategy;

    public void init(Application application) {
        if (ValueUtil.DEFAULT_SIGN == Sign.XH) {
            gpioStrategy = new XhGpioStrategy();
        } else if (ValueUtil.DEFAULT_SIGN == Sign.MR870) {
            gpioStrategy = new MR870GpioStrategy();
        } else if (ValueUtil.DEFAULT_SIGN == Sign.ZH) {
            gpioStrategy = new ZhGpioStrategy();
        } else if (ValueUtil.DEFAULT_SIGN == Sign.TPS980P) {
            gpioStrategy = new TpsGpioStrategy();
        }
        initGpio(application);
        resetGpio();
        initThread();
    }

    private void initGpio(Application application) {
        if (gpioStrategy != null) {
            gpioStrategy.init(application);
        }
    }

    public void resetGpio() {
        if (gpioStrategy != null) {
            gpioStrategy.resetGpio();
        }
    }

    public void setStatusBar(boolean show) {
        if (gpioStrategy != null) {
            gpioStrategy.setStatusBar(show);
        }
    }

    public interface GpioStrategy {
        void init(Application application);
        void resetGpio();
        void controlWhiteLed(boolean status);
        void controlGreenLed(boolean status);
        void controlRedLed(boolean status);
        void setStatusBar(boolean show);
    }

    /**
     * ================================================================
     **/

    private static ExecutorService executorService = Executors.newFixedThreadPool(5);
    private HandlerThread handlerThread;
    private Handler handler;

    private volatile boolean warning = false;

    public void initThread() {
        handlerThread = new HandlerThread("FLASH_LED_THREAD");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    public void openWhiteLedInTime() {
        if (warning) return;
        executorService.execute(() -> {
            Config config = ConfigManager.getInstance().getConfig();
            if (!config.isFaceCamera()) {
                int delay = config.getFlashTime() * 1000;
                handler.removeCallbacks(closeWhiteLedRunnable);
                if (gpioStrategy != null) {
                    gpioStrategy.controlGreenLed(false);
                    gpioStrategy.controlRedLed(false);
                    gpioStrategy.controlWhiteLed(true);
                }
                handler.postDelayed(closeWhiteLedRunnable, delay);
            }
        });
    }

    private Runnable closeWhiteLedRunnable = () -> {
        if (warning) return;
        if (gpioStrategy != null) {
            gpioStrategy.controlWhiteLed(false);
        }
    };

    public void openGreenLed() {
        executorService.execute(() -> {
            try {
                warning = true;
                gpioStrategy.controlWhiteLed(false);
                gpioStrategy.controlRedLed(false);
                gpioStrategy.controlGreenLed(true);
                Thread.sleep(500);
                gpioStrategy.controlGreenLed(false);
                Thread.sleep(500);
                gpioStrategy.controlGreenLed(true);
                Thread.sleep(500);
                gpioStrategy.controlGreenLed(false);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                warning = false;
            }
        });
    }

    public void clearLedThread() {
        handler.removeCallbacks(closeWhiteLedRunnable);
    }

    public void openRedLed() {
        executorService.execute(() -> {
            try {
                warning = true;
                gpioStrategy.controlWhiteLed(false);
                gpioStrategy.controlGreenLed(false);
                gpioStrategy.controlRedLed(true);
                Thread.sleep(500);
                gpioStrategy.controlRedLed(false);
                Thread.sleep(500);
                gpioStrategy.controlRedLed(true);
                Thread.sleep(500);
                gpioStrategy.controlRedLed(false);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                warning = false;
            }
        });
    }

    public void setInfraredLedForXH(boolean status) {
        if (gpioStrategy instanceof XhGpioStrategy) {
            XhGpioStrategy xhGpioStrategy = (XhGpioStrategy) gpioStrategy;
            xhGpioStrategy.setGpio(3, status ? 1 : 0);
            xhGpioStrategy.setGpio(4, status ? 1 : 0);
        }
    }

}
