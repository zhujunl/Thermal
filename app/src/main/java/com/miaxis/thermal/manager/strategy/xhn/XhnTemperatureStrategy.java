package com.miaxis.thermal.manager.strategy.xhn;

import android.graphics.Bitmap;
import android.serialport.api.SerialPort;

import com.miaxis.thermal.app.App;
import com.miaxis.thermal.manager.TemperatureManager;
import com.miaxis.thermal.manager.ToastManager;
import com.miaxis.thermal.util.DataUtils;
import com.vcard.vcardtempsdk.Fsdk;
import com.vcard.vcardtempsdk.FsdkTempCallback;

import org.zz.api.MXFaceInfoEx;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class XhnTemperatureStrategy implements TemperatureManager.TemperatureStrategy {

    private Fsdk sdk;
    private float temperature = -1f;
    private Bitmap heatMap = null;

    private boolean init = false;

    @Override
    public void open() {
        if (!init) {
            sdk = new Fsdk(App.getInstance().getApplicationContext());
            sdk.setDelay(100);
            long re = sdk.Init();
            if (re != 0) {
                ToastManager.toast("温控设备初始化失败", ToastManager.ERROR);
                return;
            }
            sdk.setCallback(new FsdkTempCallback() {
                @Override
                public void Temp(float t) {
                    temperature = (float) Math.round(t * 10) / 10;
                }

                @Override
                public void HeatBitmap(Bitmap bit) {
                    heatMap = bit;
                }
            });
            init = true;
        }
    }

    @Override
    public void close() {
    }

    @Override
    public void readTemperature(TemperatureManager.TemperatureListener listener) {
        if (listener != null) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            listener.onTemperature(temperature);
            listener.onHeatMap(heatMap);
        }
    }
}
