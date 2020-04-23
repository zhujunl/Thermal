package com.miaxis.thermal.manager.strategy.xhn;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

import com.miaxis.thermal.app.App;
import com.miaxis.thermal.data.entity.Calibration;
import com.miaxis.thermal.manager.CalibrationManager;
import com.miaxis.thermal.manager.TemperatureManager;
import com.vcard.vcardtempsdk.Fsdk;
import com.vcard.vcardtempsdk.FsdkTempCallback;

import java.math.BigDecimal;

public class XhnTemperatureStrategy implements TemperatureManager.TemperatureStrategy {

    private Fsdk sdk;
    private float temperature = -2f;
    private Bitmap heatMap = null;

    private boolean init = false;

    TemperatureManager.TemperatureListener xhnListener;

    @Override
    public void open() {
        if (!init) {
            XhnTempForward.getInstance().open();
            Calibration calibration = CalibrationManager.getInstance().getCalibration();
            sdk = new Fsdk(App.getInstance().getApplicationContext());
            sdk.setDelay(100);
            sdk.setE(calibration.getXhnEmissivity());
            sdk.setM(calibration.getXhnModel());
            long re = sdk.Init();
            if (re != 0) {
                Log.e("asd", "温控设备初始化失败");
                return;
            }
            sdk.setCallback(new FsdkTempCallback() {
                @Override
                public void Temp(float t) {
                    t = new BigDecimal(t).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
                    temperature = (float) Math.round(t * 10) / 10;
                    if (xhnListener != null) {
                        xhnListener.onTemperature(temperature);
                    }
                }

                @Override
                public void HeatBitmap(Bitmap bit) {
                    heatMap = bit;
                    if (xhnListener != null) {
                        xhnListener.onHeatMap(heatMap);
                    }
                }
            });
            init = true;
        }
    }

    @Override
    public void close() {
        XhnTempForward.getInstance().close();
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
            if (heatMap != null) {
                synchronized (heatMap) {
                    Matrix matrix = new Matrix();
                    matrix.setScale(-1, -1);//垂直翻转
                    int w = heatMap.getWidth();
                    int h = heatMap.getHeight();
                    //生成的翻转后的bitmap
                    Bitmap reversePic = Bitmap.createBitmap(heatMap, 0, 0, w, h, matrix, true);
                    listener.onHeatMap(reversePic);
                }
            } else {
                listener.onHeatMap(null);
            }
        }
    }

    public void setXhnListener(TemperatureManager.TemperatureListener xhnListener) {
        this.xhnListener = xhnListener;
    }

}
