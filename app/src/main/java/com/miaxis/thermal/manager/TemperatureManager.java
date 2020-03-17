package com.miaxis.thermal.manager;

import android.serialport.api.SerialPort;
import android.text.TextUtils;
import android.util.Log;

import com.miaxis.thermal.manager.strategy.Sign;
import com.miaxis.thermal.manager.strategy.mr870.MR870TemperatureStrategy;
import com.miaxis.thermal.manager.strategy.xh.XhTemperatureStrategy;
import com.miaxis.thermal.manager.strategy.zh.ZhTemperatureStrategy;
import com.miaxis.thermal.util.DataUtils;
import com.miaxis.thermal.util.ValueUtil;

import org.zz.api.MXFaceInfoEx;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class TemperatureManager {

    private TemperatureManager() {
    }

    public static TemperatureManager getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static final TemperatureManager instance = new TemperatureManager();
    }

    /**
     * ================================ 静态内部类单例写法 ================================
     **/

    private TemperatureStrategy temperatureStrategy;

    public interface TemperatureStrategy {
        void open();
        void close();
        float readTemperature();
    }

    public void init() {
        if (ValueUtil.DEFAULT_SIGN == Sign.XH) {
            temperatureStrategy = new XhTemperatureStrategy();
        } else if (ValueUtil.DEFAULT_SIGN == Sign.MR870) {
            temperatureStrategy = new MR870TemperatureStrategy();
        } else if (ValueUtil.DEFAULT_SIGN == Sign.ZH) {
            temperatureStrategy = new ZhTemperatureStrategy();
        }
    }

    public void open() {
        if (temperatureStrategy != null) {
            temperatureStrategy.open();
        }
    }

    public void close() {
        if (temperatureStrategy != null) {
            temperatureStrategy.close();
        }
    }

    public float readTemperature() {
        if (temperatureStrategy != null) {
            return temperatureStrategy.readTemperature();
        }
        return 0f;
    }

}
