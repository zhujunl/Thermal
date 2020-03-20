package com.miaxis.thermal.manager;

import android.app.Application;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
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

    private static final int WHITE_LED = 0;
    private static final int GREEN_LED = 1;
    private static final int RED_LED = 2;
    private static final int CLOSE_WHITE_LED = 3;
    private static final int CLOSE_GREEN_LED = 4;
    private static final int CLOSE_RED_LED = 5;

    private static ExecutorService executorService = Executors.newFixedThreadPool(5);
    private HandlerThread handlerThread;
    private Handler handler;

    private volatile Boolean warning = false;

    public void initThread() {
        handlerThread = new HandlerThread("FLASH_LED_THREAD");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                if (gpioStrategy == null) return;
                switch (msg.what) {
                    case WHITE_LED:
                        if (warning) return;
                        gpioStrategy.controlWhiteLed(true);
                        break;
                    case GREEN_LED:
                        gpioStrategy.controlGreenLed(true);
                        break;
                    case RED_LED:
                        gpioStrategy.controlRedLed(true);
                        break;
                    case CLOSE_WHITE_LED:
                        gpioStrategy.controlWhiteLed(false);
                        break;
                    case CLOSE_GREEN_LED:
                        gpioStrategy.controlGreenLed(false);
                        break;
                    case CLOSE_RED_LED:
                        gpioStrategy.controlRedLed(false);
                        break;
                }
            }
        };
    }

    public void openWhiteLedInTime() {
        if (warning) return;
        executorService.execute(() -> {
            Config config = ConfigManager.getInstance().getConfig();
            if (!config.isFaceCamera()) {
                int delay = config.getFlashTime() * 1000;
                handler.removeCallbacks(closeWhiteLedRunnable);
                handler.sendMessage(handler.obtainMessage(WHITE_LED));
                handler.postDelayed(closeWhiteLedRunnable, delay);
            }
        });
    }

    private Runnable closeWhiteLedRunnable = () -> {
        if (warning) return;
        if (gpioStrategy != null) {
            handler.sendMessage(handler.obtainMessage(CLOSE_WHITE_LED));
        }
    };

    public void clearLedThread() {
        handler.removeCallbacks(closeWhiteLedRunnable);
    }

    public void openGreenLed() {
        if (ValueUtil.DEFAULT_SIGN == Sign.TPS980P || ValueUtil.DEFAULT_SIGN == Sign.MR870) return;
        warning = true;
        executorService.execute(() -> {
            try {
                handler.sendMessage(handler.obtainMessage(CLOSE_WHITE_LED));
                handler.sendMessage(handler.obtainMessage(CLOSE_RED_LED));
                handler.sendMessage(handler.obtainMessage(GREEN_LED));
                Thread.sleep(500);
                handler.sendMessage(handler.obtainMessage(CLOSE_GREEN_LED));
                Thread.sleep(500);
                handler.sendMessage(handler.obtainMessage(GREEN_LED));
                Thread.sleep(500);
                handler.sendMessage(handler.obtainMessage(CLOSE_GREEN_LED));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                warning = false;
            }
        });
    }

    public void openRedLed() {
        if (ValueUtil.DEFAULT_SIGN == Sign.TPS980P || ValueUtil.DEFAULT_SIGN == Sign.MR870) return;
        warning = true;
        executorService.execute(() -> {
            try {
                handler.sendMessage(handler.obtainMessage(CLOSE_WHITE_LED));
                handler.sendMessage(handler.obtainMessage(CLOSE_GREEN_LED));
                handler.sendMessage(handler.obtainMessage(RED_LED));
                Thread.sleep(500);
                handler.sendMessage(handler.obtainMessage(CLOSE_RED_LED));
                Thread.sleep(500);
                handler.sendMessage(handler.obtainMessage(RED_LED));
                Thread.sleep(500);
                handler.sendMessage(handler.obtainMessage(CLOSE_RED_LED));
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
