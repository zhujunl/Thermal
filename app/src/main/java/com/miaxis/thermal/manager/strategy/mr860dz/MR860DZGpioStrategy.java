package com.miaxis.thermal.manager.strategy.mr860dz;

import android.app.Application;

import com.lztek.fx.FxTool;
import com.lztek.toolkit.Lztek;
import com.miaxis.thermal.manager.GpioManager;

public class MR860DZGpioStrategy implements GpioManager.GpioStrategy {

    private Lztek gpioController;

    @Override
    public void init(Application application) {
        gpioController = Lztek.create(application);
    }

    @Override
    public void resetGpio() {
        FxTool.fxDoorControl(false);
        FxTool.fxLED1Control(false);
        FxTool.fxLED2Control(false);
        FxTool.fxLED3Control(false);
    }

    @Override
    public void controlWhiteLed(boolean status) {
        FxTool.fxLED1Control(status);
    }

    @Override
    public void controlGreenLed(boolean status) {
        FxTool.fxLED3Control(status);
    }

    @Override
    public void controlRedLed(boolean status) {
        FxTool.fxLED2Control(status);
    }

    @Override
    public void setStatusBar(boolean show) {
        if (show) {
            gpioController.showNavigationBar();
        } else {
            gpioController.hideNavigationBar();
        }
    }

    @Override
    public void openGate(boolean open) {
        FxTool.fxDoorControl(open);
    }

}
