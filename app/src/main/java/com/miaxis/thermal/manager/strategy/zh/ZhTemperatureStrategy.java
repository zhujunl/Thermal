package com.miaxis.thermal.manager.strategy.zh;

import com.example.myapplication.jniNfcDev;
import com.miaxis.thermal.manager.TemperatureManager;

public class ZhTemperatureStrategy implements TemperatureManager.TemperatureStrategy {

    private jniNfcDev device;

    @Override
    public void open() {
        device = new jniNfcDev();
        device.jniOpenNfc();
    }

    @Override
    public void close() {
        if (device != null) {
            device.jniCloseNfc();
        }
    }

    @Override
    public float readTemperature() {
        int[] array = new int[] {0};
        device.jni_Get_IDNum(array);
        return rectifyDeviation(array[0]);
    }

    private float rectifyDeviation(float value) {
        if (value < 300) value += 38;
        else {
            if (value < 310 && value >= 300) value += 30;
            if (value < 320 && value >= 310) value += 20;
            if (value < 330 && value >= 320) value += 15;
        }
        if (value < 337 && value >= 330) {
            value += 26;
        }
        else if (value <= 340 && value > 337) value += 23;
        else if (value <= 342 && value > 340) value += 22;
        else if (value <= 344 && value > 342) value += 21.8;
        else if (value <= 346 && value > 344) value += 21.6;
        else if (value <= 348 && value > 346) value += 21.4;
        else if (value <= 350 && value > 348) value += 21.2;
        else if (value <= 352 && value > 350) value += 19.8;
        else if (value <= 354 && value > 352) value += 19.4;
        else if (value <= 356 && value > 354) value += 19;
        else if (value <= 358 && value > 356) value += 19;
        else if (value <= 360 && value > 358) value += 18;
        else if (value <= 362 && value > 360) value += 18;
//        else if (value <= 364 && value > 362) value += 23;
//        else if (value <= 366 && value > 364) value += 23;
//        else if (value <= 368 && value > 366) value += 23;
//        else if (value <= 370 && value > 368) value += 23;
//        else if (value <= 372 && value > 370) value += 23;
//        else if (value <= 374 && value > 372) value += 23;


        return (float) value / 10;
    }

}
