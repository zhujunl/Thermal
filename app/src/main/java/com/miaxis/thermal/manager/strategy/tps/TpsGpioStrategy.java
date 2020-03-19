package com.miaxis.thermal.manager.strategy.tps;

import android.app.Application;
import android.content.Context;

import com.common.pos.api.util.posutil.Util;
import com.miaxis.thermal.manager.GpioManager;

public class TpsGpioStrategy implements GpioManager.GpioStrategy {

    private Context context;

    @Override
    public void init(Application application) {
        this.context = application;
    }

    @Override
    public void resetGpio() {
        Util.setLedBrightness(context, 0);
    }

    @Override
    public void controlWhiteLed(boolean status) {
        Util.setLedBrightness(context, status ? 50 : 0);
    }

    @Override
    public void controlGreenLed(boolean status) {
//        Util.setLedBrightness(context, status ? 50 : 0);
    }

    @Override
    public void controlRedLed(boolean status) {
//        Util.setLedBrightness(context, status ? 50 : 0);
    }

    @Override
    public void setStatusBar(boolean show) {
        if (show) {
            Util.showNavigationBar(context);
            Util.showNoticeBar(context);
        } else {
            Util.hideNavigationBar(context);
            Util.hideNoticeBar(context);
        }
    }
}
