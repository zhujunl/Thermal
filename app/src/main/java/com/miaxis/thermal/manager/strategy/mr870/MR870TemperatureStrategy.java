package com.miaxis.thermal.manager.strategy.mr870;

import com.miaxis.thermal.manager.TemperatureManager;

public class MR870TemperatureStrategy implements TemperatureManager.TemperatureStrategy {

    @Override
    public void open() {

    }

    @Override
    public void close() {

    }

    @Override
    public void readTemperature(TemperatureManager.TemperatureListener listener) {
        if (listener != null) {
            listener.onTemperature(-1f);
            listener.onHeatMap(null);
        }
    }
}
