package com.miaxis.thermal.manager.strategy.tpsm;

import android.content.Context;
import android.util.Log;

import com.miaxis.thermal.manager.CardManager;
import com.miaxis.thermal.manager.ICCardManager;
import com.miaxis.thermal.manager.strategy.tpsf.TpsfCardStrategy;
import com.miaxis.thermal.util.DataUtils;
import com.telpo.tps550.api.TelpoException;
import com.telpo.tps550.api.iccard.Picc;

public class TpsmICCardStrategy implements ICCardManager.CardStrategy {

    private volatile boolean running = false;
    private volatile boolean needReadCard = true;

    private ICCardManager.OnCardStatusListener statusListener;
    private ICCardManager.OnCardReadListener cardListener;

    @Override
    public void initDevice(Context context, ICCardManager.OnCardStatusListener listener) {
        this.statusListener = listener;
        try {
            Picc.openReader();
            listener.onCardStatus(true);
        } catch (Exception e) {
            e.printStackTrace();
            listener.onCardStatus(false);
        }
    }

    @Override
    public void startReadCard(ICCardManager.OnCardReadListener listener) {
        this.cardListener = listener;
        if (!running) {
            running = true;
            needReadCard = true;
            new Thread(new ReadCardThread()).start();
        }
    }

    @Override
    public void release() {
        try {
            running = false;
            needReadCard = false;
            Picc.closeReader();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void needNextRead(boolean need) {
        this.needReadCard = need;
    }

    private class ReadCardThread extends Thread {

        @Override
        public void run() {
            while (running) {
                if (needReadCard) {
                    byte[] sn = new byte[64];
                    byte[] sak = new byte[1];
                    byte[] tag = new byte[2];
                    try {
                        int ret = Picc.selectCard(sn, sak, tag);
                        if (ret > 0) {
                            byte[] codeData = new byte[ret];
                            System.arraycopy(sn, 0, codeData, 0, codeData.length);
                            if (cardListener != null) {
                                needReadCard = false;
                                cardListener.onCardRead(DataUtils.byteToHex(codeData));
                            }
                        }
                    } catch (TelpoException e) {
                        e.printStackTrace();
                        Log.e("asd", "读卡异常" + e);
                    }
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
