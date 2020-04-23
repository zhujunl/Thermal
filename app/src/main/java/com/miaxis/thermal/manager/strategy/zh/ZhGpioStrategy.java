package com.miaxis.thermal.manager.strategy.zh;

import android.app.Application;

import com.miaxis.thermal.manager.GpioManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class ZhGpioStrategy implements GpioManager.GpioStrategy {

    /**
     * 各指示灯和继电器的驱动设备节点
     */
    private static final String LED_CTL_PATH = "/sys/class/zh_gpio_out/out";
    //关灯
    private static final String[] LED_OFF_VAL = {
            "2", //红灯
            "4", //绿灯
            "6", //摄像头白灯
            "8", //摄像头红灯
            "12" //工作指示灯
    };
    //开灯
    private static final String[] LED_ON_VAL = {
            "1", //红灯
            "3", //绿灯
            "5", //摄像头白灯
            "7", //摄像头红灯
            "11" //工作指示灯
    };
    //继电器
    private static final String RELAY_ON = "9";
    private static final String RELAY_OFF = "10";

    @Override
    public void init(Application application) {

    }

    @Override
    public void resetGpio() {
        ctlLedRelay("2");
        ctlLedRelay("4");
        ctlLedRelay("6");
        ctlLedRelay("8");
        ctlLedRelay("12");
    }

    @Override
    public void controlWhiteLed(boolean status) {
        ctlLedRelay(status ? "3" : "4");
    }

    @Override
    public void controlGreenLed(boolean status) {
        ctlLedRelay(status ? "5" : "6");
    }

    @Override
    public void controlRedLed(boolean status) {
        ctlLedRelay(status ? "1" : "2");
    }

    @Override
    public void setStatusBar(boolean show) {

    }

    @Override
    public void openGate(boolean open) {

    }

    private void ctlLedRelay(String val) {
        File file = new File(LED_CTL_PATH);
        if (!file.exists() || !file.canWrite()) {
            return;
        }
        try {
            FileOutputStream fout = new FileOutputStream(file);
            PrintWriter pWriter = new PrintWriter(fout);
            pWriter.println(val);
            pWriter.flush();
            pWriter.close();
            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
