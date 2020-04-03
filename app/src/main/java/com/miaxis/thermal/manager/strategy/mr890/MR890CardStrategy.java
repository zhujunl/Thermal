package com.miaxis.thermal.manager.strategy.mr890;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.miaxis.thermal.data.entity.IDCardMessage;
import com.miaxis.thermal.manager.CardManager;
import com.zz.impl.IDCardDeviceImpl;
import com.zz.impl.IDCardInterfaceService;

public class MR890CardStrategy implements CardManager.CardStrategy {

    private static final String SERIAL_PORT = "/dev/ttyHSL2";
    private static final int BAUD_RATE = 115200;

    private IDCardInterfaceService cardManager;
    private CardManager.OnCardStatusListener statusListener;
    private CardManager.OnCardReadListener cardListener;

    private volatile boolean running = false;
    private volatile boolean needReadCard = true;

    @Override
    public void initDevice(Context context, CardManager.OnCardStatusListener listener) {
        this.statusListener = listener;
        cardManager = new IDCardDeviceImpl();
        String samVersion = readSamVersion();
        if (!TextUtils.isEmpty(samVersion)) {
            listener.onCardStatus(true);
        } else {
            listener.onCardStatus(false);
        }
    }

    @Override
    public void startReadCard(CardManager.OnCardReadListener listener) {
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
            if (cardManager != null) {
                running = false;
                needReadCard = false;
                cardManager = null;
            }
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
                if (cardManager != null) {
                    if (needReadCard) {
                        byte[] message = new byte[100];
                        try {
                            int result = cardManager.readIDCard(SERIAL_PORT, BAUD_RATE, 10, message);
                            if (result == 0x90) {
                                IDCardMessage transform;
                                int cardType = cardManager.getIDCardType();
                                if (cardType == 0 || cardType == 1 || cardType == 2) {
                                    if (cardType == 0) {
                                        transform = transformID(cardManager);
                                    } else if (cardType == 1) {
                                        transform = transformGreen(cardManager);
                                    } else {
                                        transform = transformGAT(cardManager);
                                    }
                                    if (cardListener != null) {
                                        needReadCard = false;
                                        cardListener.onCardRead(transform);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e("asd", "读卡异常" + new String(message));
                        }
                    }
                } else {
                    if (statusListener != null) {
                        statusListener.onCardStatus(false);
                    }
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private IDCardMessage transformID(IDCardInterfaceService cardManager) {
        return new IDCardMessage.Builder()
                .cardType("")
                .name(cardManager.getName())
                .birthday(cardManager.getBorn())
                .address(cardManager.getAddress())
                .cardNumber(cardManager.getIdNumber())
                .issuingAuthority(cardManager.getIssueOffice())
                .validateStart(cardManager.getBeginDate())
                .validateEnd(cardManager.getEndDate())
                .sex(cardManager.getSex())
                .nation(cardManager.getNation())
                .passNumber("")
                .issueCount("")
                .chineseName("")
                .version("")
                .cardBitmap(cardManager.getPhotoBmp())
                .build();
    }

    private IDCardMessage transformGreen(IDCardInterfaceService cardManager) {
        return new IDCardMessage.Builder()
                .cardType("I")
                .name(cardManager.getEnglishName())
                .birthday(cardManager.getBorn())
                .cardNumber(cardManager.getIdNumber())
                .issuingAuthority(cardManager.getIssueOffice())
                .validateStart(cardManager.getBeginDate())
                .validateEnd(cardManager.getEndDate())
                .sex(cardManager.getSex())
                .nation(cardManager.getAreaCode())
                .chineseName(cardManager.getName())
                .version(cardManager.getCardVersionNum())
                .cardBitmap(cardManager.getPhotoBmp())
                .build();
    }

    private IDCardMessage transformGAT(IDCardInterfaceService cardManager) {
        return new IDCardMessage.Builder()
                .cardType("J")
                .name(cardManager.getName())
                .birthday(cardManager.getBorn())
                .address(cardManager.getAddress())
                .cardNumber(cardManager.getIdNumber())
                .issuingAuthority(cardManager.getIssueOffice())
                .validateStart(cardManager.getBeginDate())
                .validateEnd(cardManager.getEndDate())
                .sex(cardManager.getSex())
                .nation(cardManager.getNation())
                .passNumber(cardManager.getPassportNum())
                .issueCount(cardManager.getIssueCount())
                .cardBitmap(cardManager.getPhotoBmp())
                .build();
    }

    private String readSamVersion() {
        try {
            byte[] message = new byte[201];
            byte[] samVersion = new byte[201];
            int result = cardManager.getSAMVersion(SERIAL_PORT, BAUD_RATE, samVersion, message);
            if (result == 0x90) {
                return new String(samVersion);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

}
