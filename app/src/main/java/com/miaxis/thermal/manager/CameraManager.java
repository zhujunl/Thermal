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
import com.miaxis.thermal.manager.strategy.mr870.MR870CameraStrategy;
import com.miaxis.thermal.manager.strategy.tps.TpsCameraStrategy;
import com.miaxis.thermal.manager.strategy.xh.XhCameraStrategy;
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

    public interface CameraStrategy {
        void openCamera(@NonNull TextureView textureView, OnCameraOpenListener listener);
        void closeCamera();
        Camera getShowingCamera();
        Size getCameraPreviewSize();
        Size getPreviewSize();
        int getOrientation();
        boolean faceRectFlip();
    }

    public interface OnCameraOpenListener {
        void onCameraOpen(Camera.Size previewSize);
    }

}
