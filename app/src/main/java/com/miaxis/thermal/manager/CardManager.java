package com.miaxis.thermal.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.miaxis.thermal.data.entity.IDCardMessage;
import com.miaxis.thermal.manager.strategy.Sign;
import com.miaxis.thermal.manager.strategy.mr890.MR890CameraStrategy;
import com.miaxis.thermal.manager.strategy.mr890.MR890CardStrategy;
import com.miaxis.thermal.manager.strategy.zh.ZhCardStrategy;
import com.miaxis.thermal.util.FileUtil;
import com.miaxis.thermal.util.ValueUtil;
import com.zkteco.android.IDReader.WLTService;
import com.zkteco.android.biometric.core.device.ParameterHelper;
import com.zkteco.android.biometric.core.device.TransportType;
import com.zkteco.android.biometric.module.idcard.IDCardReader;
import com.zkteco.android.biometric.module.idcard.IDCardReaderFactory;
import com.zkteco.android.biometric.module.idcard.exception.IDCardReaderException;
import com.zkteco.android.biometric.module.idcard.meta.IDCardInfo;
import com.zkteco.android.biometric.module.idcard.meta.IDPRPCardInfo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;


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
        if (ValueUtil.DEFAULT_SIGN == Sign.XH
                || ValueUtil.DEFAULT_SIGN == Sign.XH_N
                || ValueUtil.DEFAULT_SIGN == Sign.XH_C
                || ValueUtil.DEFAULT_SIGN == Sign.TPS980P
                || ValueUtil.DEFAULT_SIGN == Sign.MR870
                || ValueUtil.DEFAULT_SIGN == Sign.MR870A) {
            cardStrategy = new DefaultCardStrategy();
        } else if (ValueUtil.DEFAULT_SIGN == Sign.ZH) {
            cardStrategy = new ZhCardStrategy();
        } else if (ValueUtil.DEFAULT_SIGN == Sign.MR890) {
            cardStrategy = new MR890CardStrategy();
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
