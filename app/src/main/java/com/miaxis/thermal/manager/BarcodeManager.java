package com.miaxis.thermal.manager;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.Size;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.uuzuche.lib_zxing.camera.PlanarYUVLuminanceSource;
import com.uuzuche.lib_zxing.decoding.DecodeFormatManager;

import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;

public class BarcodeManager {

    private BarcodeManager() {
    }

    public static BarcodeManager getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static final BarcodeManager instance = new BarcodeManager();
    }

    /**
     * ================================ 静态内部类单例写法 ================================
     **/

    private MultiFormatReader multiFormatReader;

    private ArrayBlockingQueue<byte[]> visibleFrameQueue;
    private HandlerThread asyncDetectThread;
    private Handler asyncDetectHandler;
    private volatile boolean detectLoop = false;
    private Size previewSize;

    private OnBarcodeScanListener listener;

    public void init() {
        visibleFrameQueue = new ArrayBlockingQueue<>(1);
        asyncDetectThread = new HandlerThread("detect_thread");
        asyncDetectThread.start();
        asyncDetectHandler = new Handler(asyncDetectThread.getLooper()) {
            public void handleMessage(Message msg) {
                if (detectLoop) {
                    try {
                        decodeLoop();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        multiFormatReader = new MultiFormatReader();
        Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>(3);
        Vector<BarcodeFormat> decodeFormats;
        decodeFormats = new Vector<BarcodeFormat>();
        decodeFormats.addAll(DecodeFormatManager.ONE_D_FORMATS);
        decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS);
        decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        multiFormatReader.setHints(hints);
    }

    public void startLoop() {
        detectLoop = true;
        previewSize = com.miaxis.thermal.manager.CameraManager.getInstance().getCameraPreviewSize();
        asyncDetectHandler.sendEmptyMessage(0);
    }

    public void stopLoop() {
        detectLoop = false;
        asyncDetectHandler.removeMessages(0);
        visibleFrameQueue.clear();
    }

    public void setListener(OnBarcodeScanListener listener) {
        this.listener = listener;
    }

    public boolean isDetectLoop() {
        return detectLoop;
    }

    public void feedVisibleFrame(byte[] visibleFrame) {
        visibleFrameQueue.offer(visibleFrame);
    }

    public void decodeLoop() {
        try {
            byte[] visible = null;
            if (visibleFrameQueue.size() != 0) {
                visible = visibleFrameQueue.poll();
                if (visible != null) {
                    decode(visible, previewSize.getWidth(), previewSize.getHeight());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            asyncDetectHandler.sendEmptyMessageDelayed(0, 100);
        }
    }

    private void decode(byte[] data, int width, int height) {
        Result rawResult = null;

        Rect rect = new Rect();
        rect.left = 0;
        rect.right = width;
        rect.top = 0;
        rect.bottom = height;
        PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top, rect.width(), rect.height());
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        try {
            rawResult = multiFormatReader.decode(bitmap);
        } catch (ReaderException re) {
            // continue
        } finally {
            multiFormatReader.reset();
        }
        if (rawResult != null) {
            Log.e("asd", rawResult.getText());
            if (listener != null) {
                listener.onBarcode(rawResult.getText());
            }
        } else {
            Log.e("asd", "解码失败");
        }
    }

    public interface OnBarcodeScanListener {
        void onBarcode(String barcode);
    }

}
