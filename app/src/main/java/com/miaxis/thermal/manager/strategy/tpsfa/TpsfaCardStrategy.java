package com.miaxis.thermal.manager.strategy.tpsfa;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.miaxis.thermal.data.entity.IDCardMessage;
import com.miaxis.thermal.manager.CardManager;
import com.miaxis.thermal.util.FileUtil;
import com.telpo.tps550.api.idcard.IdentityMsg;
import com.telpo.tps550.api.idcard.T2OReader;
import com.zkteco.android.IDReader.IDPhotoHelper;
import com.zkteco.android.IDReader.WLTService;

import java.io.File;

public class TpsfaCardStrategy implements CardManager.CardStrategy {

    private T2OReader cardReader;

    private CardManager.OnCardStatusListener statusListener;
    private CardManager.OnCardReadListener cardListener;

    private volatile boolean running = false;
    private volatile boolean needReadCard = true;

    @Override
    public void initDevice(Context context, CardManager.OnCardStatusListener listener) {
        this.statusListener = listener;
        cardReader = new T2OReader();
        boolean result = false;
        try {
            if (cardReader.isUSBReader(context)) {
                result = cardReader.openReader(context);
            } else {
                result = cardReader.openReader();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        listener.onCardStatus(result);
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
        cardReader.closeReader();
    }

    @Override
    public void needNextRead(boolean need) {
        this.needReadCard = need;
    }

    private class ReadCardThread extends Thread {

        @Override
        public void run() {
            while (running) {
                if (cardReader != null) {
                    if (needReadCard) {
                        try {
                            IDCardMessage transform = null;
                            IdentityMsg msg = cardReader.checkIDCard();
                            if(msg != null){
                                byte[] src = msg.getHead_photo();
                                byte[] header = new byte[1024];
                                System.arraycopy(src, 0, header, 0, header.length);
                                Bitmap bitmap = handleCardPhoto(header);
                                if (TextUtils.equals(msg.getCard_type(), " ")) {
                                    transform = transformID(msg, bitmap);
                                } else if (TextUtils.equals(msg.getCard_type(), "J")) {
                                    transform = transformGreen(msg, bitmap);
                                } else if (TextUtils.equals(msg.getCard_type(), "I")) {
                                    transform = transformGAT(msg, bitmap);
                                }
                                if (cardListener != null && transform != null) {
                                    needReadCard = false;
                                    cardListener.onCardRead(transform);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e("asd", "读卡异常" + e.getMessage());
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

        private Bitmap handleCardPhoto(byte[] data) {
            Bitmap bitmap = null;
            if (data != null && data.length > 0) {
                byte[] buf = new byte[WLTService.imgLength];
                if (1 == WLTService.wlt2Bmp(data, buf)) {
                    bitmap = IDPhotoHelper.Bgr2Bitmap(buf);
                }
            }
            return bitmap;
        }

        private IDCardMessage transformID(IdentityMsg msg, Bitmap bitmap) {
            String[] split = msg.getPeriod().split(" - ");
            return new IDCardMessage.Builder()
                    .cardType("")
                    .name(TextUtils.isEmpty(msg.getName()) ? "" : msg.getName().trim())
                    .birthday(TextUtils.isEmpty(msg.getBorn()) ? "" :msg.getBorn().trim())
                    .address(TextUtils.isEmpty(msg.getAddress()) ? "" : msg.getAddress().trim())
                    .cardNumber(TextUtils.isEmpty(msg.getNo()) ? "" : msg.getNo().trim())
                    .issuingAuthority(TextUtils.isEmpty(msg.getApartment()) ? "" : msg.getApartment().trim())
                    .validateStart(split.length > 1 ? split[0] : msg.getPeriod())
                    .validateEnd(split.length > 1 ? split[1] : msg.getPeriod())
                    .sex(TextUtils.isEmpty(msg.getSex()) ? "" : msg.getSex().substring(0, 1))
                    .nation(TextUtils.isEmpty(msg.getNation()) ? "" : msg.getNation().trim())
                    .cardBitmap(bitmap)
                    .build();
        }

        private IDCardMessage transformGreen(IdentityMsg msg, Bitmap bitmap) {
            String[] split = msg.getPeriod().split(" - ");
            return new IDCardMessage.Builder()
                    .cardType("I")
                    .name(TextUtils.isEmpty(msg.getName()) ? "" : msg.getName().trim())
                    .birthday(TextUtils.isEmpty(msg.getBorn()) ? "" :msg.getBorn().trim())
                    .cardNumber(TextUtils.isEmpty(msg.getNo()) ? "" : msg.getNo().trim())
                    .issuingAuthority(TextUtils.isEmpty(msg.getApartment()) ? "" : msg.getApartment().trim())
                    .validateStart(split.length > 1 ? split[0] : msg.getPeriod())
                    .validateEnd(split.length > 1 ? split[1] : msg.getPeriod())
                    .sex(TextUtils.isEmpty(msg.getSex()) ? "" : msg.getSex().substring(0, 1))
                    .nation(TextUtils.isEmpty(msg.getCountry()) ? "" : msg.getCountry().trim())
                    .chineseName(TextUtils.isEmpty(msg.getCn_name()) ? "" : msg.getCn_name().trim())
                    .version(TextUtils.isEmpty(msg.getIdcard_version()) ? "" : msg.getIdcard_version().trim())
                    .cardBitmap(bitmap)
                    .build();
        }

        private IDCardMessage transformGAT(IdentityMsg msg, Bitmap bitmap) {
            String[] split = TextUtils.isEmpty(msg.getPeriod()) ? new String[]{} : msg.getPeriod().split(" - ");
            return new IDCardMessage.Builder()
                    .cardType("J")
                    .name(TextUtils.isEmpty(msg.getName()) ? "" : msg.getName().trim())
                    .birthday(TextUtils.isEmpty(msg.getBorn()) ? "" :msg.getBorn().trim())
                    .address(TextUtils.isEmpty(msg.getAddress()) ? "" : msg.getAddress().trim())
                    .cardNumber(TextUtils.isEmpty(msg.getNo()) ? "" : msg.getNo().trim())
                    .issuingAuthority(TextUtils.isEmpty(msg.getApartment()) ? "" : msg.getApartment().trim())
                    .validateStart(split.length > 1 ? split[0] : msg.getPeriod())
                    .validateEnd(split.length > 1 ? split[1] : msg.getPeriod())
                    .sex(TextUtils.isEmpty(msg.getSex()) ? "" : msg.getSex().substring(0, 1))
                    .nation(TextUtils.isEmpty(msg.getNation()) ? "" : msg.getNation().trim())
                    .passNumber(TextUtils.isEmpty(msg.getPassNum()) ? "" : msg.getPassNum().trim())
                    .issueCount(TextUtils.isEmpty(msg.getIssuesNum()) ? "" : msg.getIssuesNum().trim())
                    .cardBitmap(bitmap)
                    .build();
        }

    }

}
