package com.miaxis.thermal.manager;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.text.TextUtils;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.TextureView;

import androidx.annotation.NonNull;

import com.miaxis.thermal.data.entity.Config;
import com.miaxis.thermal.manager.strategy.Sign;
import com.miaxis.thermal.manager.strategy.mr860dz.MR860DZCameraStrategy;
import com.miaxis.thermal.manager.strategy.mr870.MR870CameraStrategy;
import com.miaxis.thermal.manager.strategy.mr870a.MR870ACameraStrategy;
import com.miaxis.thermal.manager.strategy.mr890.MR890CameraStrategy;
import com.miaxis.thermal.manager.strategy.tps.TpsCameraStrategy;
import com.miaxis.thermal.manager.strategy.tpsc.TpscCameraStrategy;
import com.miaxis.thermal.manager.strategy.tpsf.TpsfCameraStrategy;
import com.miaxis.thermal.manager.strategy.xh.XhCameraStrategy;
import com.miaxis.thermal.manager.strategy.xhc.XhcCameraStrategy;
import com.miaxis.thermal.manager.strategy.xhn.XhnCameraStrategy;
import com.miaxis.thermal.manager.strategy.zh.ZhCameraStrategy;
import com.miaxis.thermal.util.ValueUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.List;

public class CameraManager {

    private CameraManager() {
    }

    public static CameraManager getInstance () {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static final CameraManager instance = new CameraManager();
    }

    /** ================================ 静态内部类单例 ================================ **/

    private CameraStrategy cameraStrategy;

    public void init() {
        if (ValueUtil.DEFAULT_SIGN == Sign.XH) {
            cameraStrategy = new XhCameraStrategy();
        } else if (ValueUtil.DEFAULT_SIGN == Sign.MR870) {
            cameraStrategy = new MR870CameraStrategy();
        } else if (ValueUtil.DEFAULT_SIGN == Sign.ZH) {
            cameraStrategy = new ZhCameraStrategy();
        } else if (ValueUtil.DEFAULT_SIGN == Sign.TPS980P) {
            cameraStrategy = new TpsCameraStrategy();
        } else if (ValueUtil.DEFAULT_SIGN == Sign.TPS980P_C) {
            cameraStrategy = new TpscCameraStrategy();
        } else if (ValueUtil.DEFAULT_SIGN == Sign.TPS980P_F) {
            cameraStrategy = new TpsfCameraStrategy();
        } else if (ValueUtil.DEFAULT_SIGN == Sign.XH_N) {
            cameraStrategy = new XhnCameraStrategy();
        } else if (ValueUtil.DEFAULT_SIGN == Sign.MR890) {
            cameraStrategy = new MR890CameraStrategy();
        } else if (ValueUtil.DEFAULT_SIGN == Sign.XH_C) {
            cameraStrategy = new XhcCameraStrategy();
        } else if (ValueUtil.DEFAULT_SIGN == Sign.MR870A) {
            cameraStrategy = new MR870ACameraStrategy();
        } else if (ValueUtil.DEFAULT_SIGN == Sign.MR860DZ) {
            cameraStrategy = new MR860DZCameraStrategy();
        }
    }

    public void openCamera(@NonNull TextureView textureView, OnCameraOpenListener listener) {
        if (cameraStrategy != null) {
            cameraStrategy.openCamera(textureView, listener);
        }
    }

    public void closeCamera() {
        if (cameraStrategy != null) {
            cameraStrategy.closeCamera();
        }
    }

    public Camera getShowingCamera() {
        if (cameraStrategy != null) {
            return cameraStrategy.getShowingCamera();
        }
        return null;
    }

    public Size getCameraPreviewSize() {
        if (cameraStrategy != null) {
            return cameraStrategy.getCameraPreviewSize();
        }
        return null;
    }

    public Size getPreviewSize() {
        if (cameraStrategy != null) {
            return cameraStrategy.getPreviewSize();
        }
        return null;
    }

    public int getOrientation() {
        if (cameraStrategy != null) {
            return cameraStrategy.getOrientation();
        }
        return 0;
    }

    public boolean faceRectFlip() {
        if (cameraStrategy != null) {
            return cameraStrategy.faceRectFlip();
        }
        return false;
    }

    public void release() {
        if (cameraStrategy != null) {
            cameraStrategy.release();
        }
    }

    public interface CameraStrategy {
        void openCamera(@NonNull TextureView textureView, @NonNull OnCameraOpenListener listener);
        void closeCamera();
        Camera getShowingCamera();
        Size getCameraPreviewSize();
        Size getPreviewSize();
        int getOrientation();
        boolean faceRectFlip();
        void release();
    }

    public interface OnCameraOpenListener {
        void onCameraOpen(Camera.Size previewSize, String message);
    }

}
