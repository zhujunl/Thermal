package com.miaxis.thermal.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.miaxis.thermal.data.entity.IDCardMessage;
import com.miaxis.thermal.util.FileUtil;
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

    private IDCardReader idCardReader = null;
    private OnCardReadListener listener;

    private volatile boolean running = false;
    private volatile boolean needReadCard = true;

    public void init(Context context) {
        try {
            Map params = new HashMap<>();
            params.put(ParameterHelper.PARAM_SERIAL_SERIALNAME, "/dev/ttyS3");
            params.put(ParameterHelper.PARAM_SERIAL_BAUDRATE, 115200);
            idCardReader = IDCardReaderFactory.createIDCardReader(context, TransportType.SERIALPORT, params);
            idCardReader.open(0);
            if (listener != null) {
                listener.onDeviceStatus(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (listener != null) {
                listener.onDeviceStatus(false);
            }
        }
    }

    public void release() {
        try {
            if (idCardReader != null) {
                running = false;
                needReadCard = false;
                idCardReader.close(0);
                IDCardReaderFactory.destroy(idCardReader);
            }
        } catch (IDCardReaderException e) {
            e.printStackTrace();
        }
    }

    public void setNeedReadCard(boolean needReadCard) {
        this.needReadCard = needReadCard;
    }

    public void startReadCard() {
        if (!running) {
            running = true;
            needReadCard = true;
            new Thread(new ReadCardThread()).start();
        }
    }

    private Bitmap Bgr2Bitmap(byte[] bgrbuf) {
        int width = WLTService.imgWidth;
        int height = WLTService.imgHeight;
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        int row = 0, col = width - 1;
        for (int i = bgrbuf.length - 1; i >= 3; i -= 3) {
            int color = bgrbuf[i] & 0xFF;
            color += (bgrbuf[i - 1] << 8) & 0xFF00;
            color += ((bgrbuf[i - 2]) << 16) & 0xFF0000;
            bmp.setPixel(col--, row, color);
            if (col < 0) {
                col = width - 1;
                row++;
            }
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(bmp.getByteCount());
        bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        byte[] data = outputStream.toByteArray();
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    public class ReadCardThread extends Thread {

        @Override
        public void run() {
            while (running) {
                if (idCardReader != null) {
                    if (needReadCard) {
                        try {
                            IDCardMessage transform;
                            authenticate();
                            int retCardType = idCardReader.readCardEx(0, 1);
                            if (retCardType == 1 || retCardType == 2 || retCardType == 3) {
                                if (retCardType == 1) {
                                    IDCardInfo idCardInfo = idCardReader.getLastIDCardInfo();
                                    transform = transformID(idCardInfo);
                                } else if (retCardType == 2) {
                                    IDPRPCardInfo idprpCardInfo = idCardReader.getLastPRPIDCardInfo();
                                    transform = transformGreen(idprpCardInfo);
                                } else {
                                    IDCardInfo idCardInfo = idCardReader.getLastIDCardInfo();
                                    transform = transformGAT(idCardInfo);
                                }
                                if (listener != null) {
                                    needReadCard = false;
                                    listener.onCardRead(transform);
                                }
                            }
                        } catch (Exception e) {
                            if (listener != null) {
                                listener.onCardRead(null);
                            }
                            e.printStackTrace();
                            Log.e("asd", "" + e.getMessage());
                        }
                    }
                } else {
                    if (listener != null) {
                        listener.onDeviceStatus(false);
                    }
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private boolean authenticate() {
            try {
                idCardReader.findCard(0);
                idCardReader.selectCard(0);
                return true;
            } catch (IDCardReaderException e) {
                e.printStackTrace();
                return false;
            }
        }

        private Bitmap handleCardPhoto(byte[] data) {
            Bitmap bitmap = null;
            if (data != null && data.length > 0) {
                byte[] buf = new byte[WLTService.imgLength];
                if (1 == WLTService.wlt2Bmp(data, buf)) {
                    bitmap = Bgr2Bitmap(buf);
                }
            }
            return bitmap;
        }

        private IDCardMessage transformID(IDCardInfo idCardInfo) {
            return new IDCardMessage.Builder()
                    .cardType("")
                    .name(idCardInfo.getName())
                    .birthday(idCardInfo.getBirth())
                    .address(idCardInfo.getAddress())
                    .cardNumber(idCardInfo.getId())
                    .issuingAuthority(idCardInfo.getDepart())
                    .validateStart(idCardInfo.getValidityTime())
                    .validateEnd(idCardInfo.getValidityTime())
                    .sex(idCardInfo.getSex())
                    .nation(idCardInfo.getNation())
                    .passNumber("")
                    .issueCount("")
                    .chineseName("")
                    .version("")
                    .cardBitmap(handleCardPhoto(idCardInfo.getPhoto()))
                    .build();
        }

        private IDCardMessage transformGreen(IDPRPCardInfo idCardInfo) {
            return new IDCardMessage.Builder()
                    .cardType("I")
                    .name(idCardInfo.getEnName())
                    .birthday(idCardInfo.getBirth())
                    .cardNumber(idCardInfo.getId())
                    .issuingAuthority(idCardInfo.getDeptCode())
                    .validateStart(idCardInfo.getValidityTime())
                    .validateEnd(idCardInfo.getValidityTime())
                    .sex(idCardInfo.getSex())
                    .nation(idCardInfo.getCountry())
                    .chineseName(idCardInfo.getCnName())
                    .version(idCardInfo.getLicVer())
                    .cardBitmap(handleCardPhoto(idCardInfo.getPhoto()))
                    .build();
        }

        private IDCardMessage transformGAT(IDCardInfo idCardInfo) {
            return new IDCardMessage.Builder()
                    .cardType("J")
                    .name(idCardInfo.getName())
                    .birthday(idCardInfo.getBirth())
                    .address(idCardInfo.getAddress())
                    .cardNumber(idCardInfo.getId())
                    .issuingAuthority(idCardInfo.getDepart())
                    .validateStart(idCardInfo.getValidityTime())
                    .validateEnd(idCardInfo.getValidityTime())
                    .sex(idCardInfo.getSex())
                    .nation(idCardInfo.getNation())
                    .passNumber(idCardInfo.getPassNum())
                    .issueCount(String.valueOf(idCardInfo.getVisaTimes()))
                    .cardBitmap(handleCardPhoto(idCardInfo.getPhoto()))
                    .build();
        }

    }

    public void setListener(OnCardReadListener listener) {
        this.listener = listener;
    }

    public interface OnCardReadListener {
        void onDeviceStatus(boolean status);

        void onCardRead(IDCardMessage idCardMessage);
    }

}
