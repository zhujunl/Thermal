package com.miaxis.thermal.manager.strategy.mr860dz;

import android.app.Application;

import com.lztek.toolkit.Lztek;
import com.miaxis.thermal.manager.HumanSensorManager;

public class MR860DZHumanSensorStrategy implements HumanSensorManager.HumanSensorStrategy {

    private Lztek gpioController;

    @Override
    public void initDevice(Application application) {
        gpioController = Lztek.create(application);
        gpioController.gpioEnable(226);
    }

    @Override
    public boolean readSensorStatus() {
        int gpioValue = gpioController.getGpioValue(226);
        return gpioValue != 0;
    }
}
