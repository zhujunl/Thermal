package com.miaxis.thermal.manager.strategy.mr890;

import android.app.Application;
import android.serialport.api.SerialPort;

import com.miaxis.thermal.manager.GpioManager;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class MR890GpioStrategy implements GpioManager.GpioStrategy {

    private SerialPort serialPort;
    private InputStream inputStream;
    private OutputStream outputStream;

    @Override
    public void init(Application application) {
        try {
            serialPort = new SerialPort();
            FileDescriptor fileDescriptor = serialPort.open("/dev/ttyS3", 9600, 0);
            inputStream = new FileInputStream(fileDescriptor);
            outputStream = new FileOutputStream(fileDescriptor);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public void controlDoor(boolean bOpen) {
        try {
            if (bOpen) {
                outputStream.write((byte) 0x01);//01开门
            } else {
                outputStream.write((byte) 0xf1);//f1关门
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
