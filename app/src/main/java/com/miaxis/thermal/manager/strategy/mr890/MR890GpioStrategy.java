package com.miaxis.thermal.manager.strategy.mr890;

import android.app.Application;
import android.serialport.api.SerialPort;

import com.miaxis.thermal.manager.GpioManager;
import com.nexgo.oaf.apiv3.APIProxy;
import com.nexgo.oaf.apiv3.DeviceEngine;
import com.nexgo.oaf.apiv3.device.led.LEDDriver;
import com.nexgo.oaf.apiv3.device.led.LightModeEnum;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class MR890GpioStrategy implements GpioManager.GpioStrategy {

    private DeviceEngine deviceEngine;
    private LEDDriver ledDriver;

    @Override
    public void init(Application application) {
        try {
            deviceEngine = APIProxy.getDeviceEngine(application);
            ledDriver = deviceEngine.getLEDDriver();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void resetGpio() {

    }

    @Override
    public void controlWhiteLed(boolean status) {
        try {
            ledDriver.setLed(LightModeEnum.YELLOW, status);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void controlGreenLed(boolean status) {

    }

    @Override
    public void controlRedLed(boolean status) {

    }

    @Override
    public void setStatusBar(boolean show) {

    }

    public void controlDoor(boolean bOpen) {
        try {
            ledDriver.setLed(LightModeEnum.BLUE, bOpen);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
