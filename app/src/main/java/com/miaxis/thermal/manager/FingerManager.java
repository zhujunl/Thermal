package com.miaxis.thermal.manager;

import android.graphics.Bitmap;

import com.miaxis.thermal.manager.strategy.Sign;
import com.miaxis.thermal.manager.strategy.mr870a.MR870AFingerStrategy;
import com.miaxis.thermal.util.ValueUtil;

public class FingerManager {

    private FingerManager() {
    }

    public static FingerManager getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static final FingerManager instance = new FingerManager();
    }

    /**
     * ================================ 静态内部类单例 ================================
     **/

    private FingerStrategy fingerStrategy;

    public void init() {
        if (ValueUtil.DEFAULT_SIGN == Sign.MR870A) {
            fingerStrategy = new MR870AFingerStrategy();
        } else {
            fingerStrategy = new DefaultFingerStrategy();
        }
    }

    public interface FingerStrategy {
        void init(OnFingerStatusListener statusListener);
        void readFinger(OnFingerReadListener readListener);
        boolean matchFeature(byte[] feature1, byte[] feature2);
        void release();
    }

    private static class DefaultFingerStrategy implements FingerStrategy {
        @Override
        public void init(OnFingerStatusListener statusListener) {
        }

        @Override
        public void readFinger(OnFingerReadListener readListener) {
        }

        @Override
        public boolean matchFeature(byte[] feature1, byte[] feature2) {
            return false;
        }

        @Override
        public void release() {
        }
    }

    public interface OnFingerStatusListener {
        void onFingerStatus(boolean result);
    }

    public interface OnFingerReadListener {
        void onFingerRead(byte[] feature, Bitmap image);
    }

    public void initDevice(OnFingerStatusListener statusListener) {
        if (fingerStrategy != null) {
            fingerStrategy.init(statusListener);
        }
    }

    public void readFinger(OnFingerReadListener readListener) {
        if (fingerStrategy != null) {
            fingerStrategy.readFinger(readListener);
        }
    }

    public boolean matchFeature(byte[] feature1, byte[] feature2) {
        if (fingerStrategy != null) {
            return fingerStrategy.matchFeature(feature1, feature2);
        }
        return false;
    }

    public void release() {
        if (fingerStrategy != null) {
            fingerStrategy.release();
        }
    }

}
