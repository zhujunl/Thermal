package com.miaxis.thermal.manager;

import android.graphics.Bitmap;

import com.miaxis.thermal.manager.strategy.Sign;
import com.miaxis.thermal.manager.strategy.tps.TpsTemperatureStrategy;
import com.miaxis.thermal.manager.strategy.xh.XhTemperatureStrategy;
import com.miaxis.thermal.manager.strategy.xhn.XhnTemperatureStrategy;
import com.miaxis.thermal.manager.strategy.zh.ZhTemperatureStrategy;
import com.miaxis.thermal.util.ValueUtil;

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
        void readTemperature(TemperatureListener listener);
    }

    public void init() {
        if (ValueUtil.DEFAULT_SIGN == Sign.XH) {
            temperatureStrategy = new XhTemperatureStrategy();
        }else if (ValueUtil.DEFAULT_SIGN == Sign.ZH) {
            temperatureStrategy = new ZhTemperatureStrategy();
        } else if (ValueUtil.DEFAULT_SIGN == Sign.TPS980P) {
            temperatureStrategy = new TpsTemperatureStrategy();
        } else if (ValueUtil.DEFAULT_SIGN == Sign.XH_N) {
            temperatureStrategy = new XhnTemperatureStrategy();
        } else {
            temperatureStrategy = new DefaultTemperatureStrategy();
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

    public void readTemperature(TemperatureListener listener) {
        if (temperatureStrategy != null) {
            temperatureStrategy.readTemperature(listener);
        }
    }

    public TemperatureStrategy getTemperatureStrategy() {
        return temperatureStrategy;
    }

    public interface TemperatureListener {
        void onTemperature(float temperature);
        void onHeatMap(Bitmap bitmap);
    }

    private static class DefaultTemperatureStrategy implements TemperatureManager.TemperatureStrategy {

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

}
