package com.miaxis.thermal.manager.strategy.xhn;

import android.serialport.api.SerialPort;

import com.miaxis.thermal.manager.TemperatureManager;
import com.miaxis.thermal.util.DataUtils;

import org.zz.api.MXFaceInfoEx;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class XhnTemperatureStrategy implements TemperatureManager.TemperatureStrategy {

    @Override
    public void open() {
    }

    @Override
    public void close() {
    }

    @Override
    public void readTemperature(TemperatureManager.TemperatureListener listener) {
    }
}
