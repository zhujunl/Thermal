package com.miaxis.thermal.manager;

import android.content.Context;

import com.miaxis.thermal.data.entity.IDCardMessage;
import com.miaxis.thermal.manager.strategy.Sign;
import com.miaxis.thermal.manager.strategy.mr870a.MR870ACardStrategy;
import com.miaxis.thermal.manager.strategy.mr890.MR890CardStrategy;
import com.miaxis.thermal.manager.strategy.tpsf.TpsfCardStrategy;
import com.miaxis.thermal.manager.strategy.tpsfa.TpsfaCardStrategy;
import com.miaxis.thermal.manager.strategy.zh.ZhCardStrategy;
import com.miaxis.thermal.util.ValueUtil;


public class CardManager {

    private CardManager() {
    }

    public static CardManager getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static final CardManager instance = new CardManager();
    }

    /**
     * ================================ 静态内部类单例 ================================
     **/

    private CardStrategy cardStrategy;

    public void init() {
        if (ValueUtil.DEFAULT_SIGN == Sign.ZH) {
            cardStrategy = new ZhCardStrategy();
        } else if (ValueUtil.DEFAULT_SIGN == Sign.MR890) {
            cardStrategy = new MR890CardStrategy();
        } else if (ValueUtil.DEFAULT_SIGN == Sign.MR870A) {
            cardStrategy = new MR870ACardStrategy();
        } else if (ValueUtil.DEFAULT_SIGN == Sign.TPS980P_F) {
            cardStrategy = new TpsfCardStrategy();
        } else if (ValueUtil.DEFAULT_SIGN == Sign.TPS980P_FA) {
            cardStrategy = new TpsfaCardStrategy();
        } else {
            cardStrategy = new DefaultCardStrategy();
        }
    }


    public interface CardStrategy {
        void initDevice(Context context, OnCardStatusListener listener);

        void startReadCard(OnCardReadListener listener);

        void release();

        void needNextRead(boolean need);
    }

    private static class DefaultCardStrategy implements CardStrategy {
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
        void onCardRead(IDCardMessage idCardMessage);
    }

}
