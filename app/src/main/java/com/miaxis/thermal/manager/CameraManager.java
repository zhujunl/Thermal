package com.miaxis.thermal.manager;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.TextureView;

import androidx.annotation.NonNull;

import com.miaxis.thermal.data.entity.Config;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.List;

@SuppressWarnings("deprecation")
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

    public static final int PRE_WIDTH = 640;
    public static final int PRE_HEIGHT = 480;
    public static final int PIC_WIDTH = 640;
    public static final int PIC_HEIGHT = 480;
    private static final int RETRY_TIMES = 3;

    private Camera visibleCamera;
    private Camera infraredCamera;

    private Camera showingCamera;

    private int retryTime = 0;

    public void openCamera(@NonNull TextureView textureView, OnCameraOpenListener listener) {
        resetRetryTime();
        Config config = ConfigManager.getInstance().getConfig();
        if (config.isShowCamera()) { //true:近红外，false:可见光
            openInfraredCamera();
            showingCamera = infraredCamera;
            if (listener != null) {
                listener.onCameraOpen(showingCamera.getParameters().getPreviewSize());
            }
            if (!config.isFaceCamera()) {
                resetRetryTime();
                openVisibleCamera();
            }
        } else {
            openVisibleCamera();
            showingCamera = visibleCamera;
            if (listener != null) {
                listener.onCameraOpen(showingCamera.getParameters().getPreviewSize());
            }
            if (config.isFaceCamera() || config.isLiveness()) {
                resetRetryTime();
                openInfraredCamera();
            }
        }
        textureView.setSurfaceTextureListener(textureListener);
    }

    public void closeCamera() {
        resetRetryTime();
        closeVisibleCamera();
        resetRetryTime();
        closeInfraredCamera();
    }

    private void resetRetryTime() {
        this.retryTime = 0;
    }

    private void openVisibleCamera() {
        try {
            visibleCamera = Camera.open(1);
            Camera.Parameters parameters = visibleCamera.getParameters();
            List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
            parameters.setPreviewSize(PRE_WIDTH, PRE_HEIGHT);
            parameters.setPictureSize(PIC_WIDTH, PIC_HEIGHT);
            //预览画面镜像
            parameters.set("preview-flip", "flip-h");
            //对焦模式设置
            List<String> supportedFocusModes = parameters.getSupportedFocusModes();
            if (supportedFocusModes != null && supportedFocusModes.size() > 0) {
                if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                }
            }
            visibleCamera.setParameters(parameters);
//            visibleCamera.setDisplayOrientation(90);
            visibleCamera.setPreviewCallback(visiblePreviewCallback);
            visibleCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
            new Thread(() -> {
                if (retryTime <= RETRY_TIMES) {
                    retryTime++;
                    openVisibleCamera();
                }
            }).start();
        }
    }

    private void closeVisibleCamera() {
        try {
            if (visibleCamera != null) {
                visibleCamera.setPreviewCallback(null);
                visibleCamera.stopPreview();
                visibleCamera.release();
                visibleCamera = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            new Thread(() -> {
                if (retryTime <= RETRY_TIMES) {
                    retryTime++;
                    closeVisibleCamera();
                }
            }).start();
        }
    }

    private void openInfraredCamera() {
        try {
            infraredCamera = Camera.open(0);
            Camera.Parameters parameters = infraredCamera.getParameters();
            List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
            parameters.setPreviewSize(PRE_WIDTH, PRE_HEIGHT);
            parameters.setPictureSize(PIC_WIDTH, PIC_HEIGHT);
            //预览画面镜像
            parameters.set("preview-flip", "flip-h");
            //对焦模式设置
            List<String> supportedFocusModes = parameters.getSupportedFocusModes();
            if (supportedFocusModes != null && supportedFocusModes.size() > 0) {
                if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                }
            }
            infraredCamera.setParameters(parameters);
//            infraredCamera.setDisplayOrientation(90);
            infraredCamera.setPreviewCallback(infraredPreviewCallback);
            infraredCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
            new Thread(() -> {
                if (retryTime <= RETRY_TIMES) {
                    retryTime++;
                    openInfraredCamera();
                }
            }).start();
        }
    }

    private void closeInfraredCamera() {
        try {
            if (infraredCamera != null) {
                infraredCamera.setPreviewCallback(null);
                infraredCamera.stopPreview();
                infraredCamera.release();
                infraredCamera = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            new Thread(() -> {
                if (retryTime <= RETRY_TIMES) {
                    retryTime++;
                    closeInfraredCamera();
                }
            }).start();
        }
    }

    private TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            if (showingCamera != null) {
                try {
                    showingCamera.setPreviewTexture(surfaceTexture);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
//            closeCamera();
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }
    };

    public interface OnCameraOpenListener {
        void onCameraOpen(Camera.Size previewSize);
    }

    public Camera getVisibleCamera() {
        return visibleCamera;
    }

    public Camera getInfraredCamera() {
        return infraredCamera;
    }

    public Camera getShowingCamera() {
        return showingCamera;
    }

    private Camera.PreviewCallback visiblePreviewCallback = (data, camera) -> FaceManager.getInstance().setLastVisiblePreviewData(data);

    private Camera.PreviewCallback infraredPreviewCallback = (data, camera) -> FaceManager.getInstance().setLastInfraredPreviewData(data);

}
