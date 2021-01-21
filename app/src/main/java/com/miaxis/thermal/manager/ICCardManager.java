package com.miaxis.thermal.manager;

import android.content.Context;

import com.miaxis.thermal.manager.strategy.Sign;
import com.miaxis.thermal.manager.strategy.tpsm.TpsmICCardStrategy;
import com.miaxis.thermal.util.ValueUtil;

public class ICCardManager {

    private ICCardManager() {
    }

    public static ICCardManager getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static final ICCardManager instance = new ICCardManager();
    }

    /**
     * ================================ 静态内部类单例 ================================
     **/

    private ICCardManager.CardStrategy cardStrategy;

    public void init() {
        if (ValueUtil.DEFAULT_SIGN == Sign.ZH) {
            cardStrategy = new TpsmICCardStrategy();
        } else {
            cardStrategy = new DefaultICCardStrategy();
        }
    }


    public interface CardStrategy {
        void initDevice(Context context, OnCardStatusListener listener);

        void startReadCard(OnCardReadListener listener);

        void release();

        void needNextRead(boolean need);
    }

    private static class DefaultICCardStrategy implements CardStrategy {
        @Override
        public void initDevice(Context context, OnCardStatusListener listener) {
        }

        @Override
        public void startReadCard(OnCardReadListener listener) {
        }

        @Override
        public void release() {
        }

        @Override
        public void needNextRead(boolean need) {
        }
    }

    public void initDevice(Context context, OnCardStatusListener listener) {
        if (cardStrategy != null) {
            cardStrategy.initDevice(context, listener);
        }
    }

    public void startReadCard(OnCardReadListener listener) {
        if (cardStrategy != null) {
            cardStrategy.startReadCard(listener);
        }
    }

    public void release() {
        if (cardStrategy != null) {
            cardStrategy.release();
        }
    }

    public void needNextRead(boolean need) {
        if (cardStrategy != null) {
            cardStrategy.needNextRead(need);
        }
    }

    public interface OnCardStatusListener {
        void onCardStatus(boolean result);
    }

    public interface OnCardReadListener {
        void onCardRead(String cardCode);
    }

}
