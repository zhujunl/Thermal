package com.miaxis.thermal.manager.strategy.mr870a;

import android.graphics.Bitmap;

import com.miaxis.thermal.app.App;
import com.miaxis.thermal.manager.FingerManager;
import com.mx.finger.alg.MxFingerAlg;
import com.mx.finger.api.msc.MxMscBigFingerApi;
import com.mx.finger.api.msc.MxMscBigFingerApiFactory;
import com.mx.finger.common.MxImage;
import com.mx.finger.common.Result;
import com.mx.finger.utils.RawBitmapUtils;

public class MR870AFingerStrategy implements FingerManager.FingerStrategy {

    private MxMscBigFingerApi mxMscBigFingerApi;
    private MxFingerAlg mxFingerAlg;

    private FingerManager.OnFingerStatusListener statusListener;
    private FingerManager.OnFingerReadListener readListener;

    @Override
    public void init(FingerManager.OnFingerStatusListener statusListener) {
        try {
            this.statusListener = statusListener;
            MxMscBigFingerApiFactory fingerFactory = new MxMscBigFingerApiFactory(App.getInstance());
            mxMscBigFingerApi = fingerFactory.getApi();
            mxFingerAlg = fingerFactory.getAlg();
            statusListener.onFingerStatus(true);
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
        statusListener.onFingerStatus(false);
    }

    @Override
    public void readFinger(FingerManager.OnFingerReadListener readListener) {
        this.readListener = readListener;
        if (mxMscBigFingerApi == null || mxFingerAlg == null) {
            statusListener.onFingerStatus(false);
            readListener.onFingerRead(null, null);
            return;
        }
        App.getInstance().getThreadExecutor().execute(() -> {
            try {
                Result<MxImage> result = mxMscBigFingerApi.getFingerImageBig(5000);
                if (result.isSuccess()) {
                    MxImage image = result.data;
                    if (image != null) {
                        byte[] feature = mxFingerAlg.extractFeature(image.data, image.width, image.height);
                        if (feature != null) {
                            Bitmap bitmap = RawBitmapUtils.raw2Bimap(image.data, image.width, image.height);
                            readListener.onFingerRead(feature, bitmap);
                            return;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            readListener.onFingerRead(null, null);
        });
    }

    @Override
    public boolean matchFeature(byte[] feature1, byte[] feature2) {
        if (mxFingerAlg != null) {
            return mxFingerAlg.match(feature1, feature2, 3) == 0;
        }
        return false;
    }

    @Override
    public void release() {
        mxMscBigFingerApi = null;
        mxFingerAlg = null;
    }
}
