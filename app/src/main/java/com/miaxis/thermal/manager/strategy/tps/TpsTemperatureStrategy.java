package com.miaxis.thermal.manager.strategy.tps;

import android.util.Log;

import com.common.thermalimage.HotImageCallback;
import com.common.thermalimage.TemperatureBitmapData;
import com.common.thermalimage.TemperatureData;
import com.common.thermalimage.ThermalImageUtil;
import com.miaxis.thermal.app.App;
import com.miaxis.thermal.manager.TemperatureManager;

public class TpsTemperatureStrategy implements TemperatureManager.TemperatureStrategy {

    private ThermalImageUtil temperatureUtil;

    @Override
    public void open() {
        temperatureUtil = new ThermalImageUtil(App.getInstance());
    }

    @Override
    public void close() {
        temperatureUtil.release();
    }

    @Override
    public float readTemperature() {
        TemperatureData temperatureData = temperatureUtil.getDataAndBitmap(50,
                1,
                false,
                new HotImageCallback.Stub() {
                    @Override
                    public void onTemperatureFail(String e) {
                        Log.e("asd", "onTemperatureFail " + e);
                    }


                    @Override
                    public void getTemperatureBimapData(final TemperatureBitmapData data) {
                    }
                });
        return temperatureData != null ? temperatureData.getTemperature() : 0f;
    }

}
