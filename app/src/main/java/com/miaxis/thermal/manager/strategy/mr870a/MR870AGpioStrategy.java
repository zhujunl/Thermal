package com.miaxis.thermal.manager.strategy.mr870a;

import android.app.Application;
import android.serialport.api.SerialPort;

import com.miaxis.thermal.manager.GpioManager;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class MR870AGpioStrategy implements GpioManager.GpioStrategy {

    @Override
    public void init(Application application) {
    }

    @Override
    public void resetGpio() {

    }

    @Override
    public void controlWhiteLed(boolean status) {

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

    @Override
    public void openGate(boolean open) {
    }

}
