package com.miaxis.thermal.manager.strategy.xh;

import android.app.Application;

import com.android.xhapimanager.XHApiManager;
import com.miaxis.thermal.manager.GpioManager;

public class XhGpioStrategy implements GpioManager.GpioStrategy {

    private Application context;
    private XHApiManager xhApiManager;

    @Override
    public void init(Application application) {
        this.context = application;
        xhApiManager = new XHApiManager();
    }

    @Override
    public void resetGpio() {
        xhApiManager.XHSetGpioValue(0, 0);
        xhApiManager.XHSetGpioValue(1, 0);
        xhApiManager.XHSetGpioValue(2, 0);
        xhApiManager.XHSetGpioValue(3, 0);
        xhApiManager.XHSetGpioValue(4, 0);
    }

    @Override
    public void controlWhiteLed(boolean status) {
        xhApiManager.XHSetGpioValue(0, status ? 1 : 0);
    }

    @Override
    public void controlGreenLed(boolean status) {
        xhApiManager.XHSetGpioValue(1, status ? 1 : 0);
    }

    @Override
    public void controlRedLed(boolean status) {
        xhApiManager.XHSetGpioValue(2, status ? 1 : 0);
    }

    @Override
    public void setStatusBar(boolean show) {
        xhApiManager.XHShowOrHideStatusBar(show);
    }

    public void setGpio(int gpio, int value) {
        xhApiManager.XHSetGpioValue(gpio, value);
    }
}
