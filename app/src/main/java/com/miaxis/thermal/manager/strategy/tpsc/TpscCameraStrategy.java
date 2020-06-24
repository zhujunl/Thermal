package com.miaxis.thermal.manager.strategy.tpsc;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Size;
import android.view.TextureView;

import androidx.annotation.NonNull;

import com.miaxis.thermal.app.App;
import com.miaxis.thermal.data.entity.Config;
import com.miaxis.thermal.manager.CameraManager;
import com.miaxis.thermal.manager.ConfigManager;
import com.miaxis.thermal.manager.FaceManager;

import java.io.IOException;
import java.util.List;

public class TpscCameraStrategy implements CameraManager.CameraStrategy {

    public static final int PRE_WIDTH = 640;
    public static final int PRE_HEIGHT = 480;
    public static final int PIC_WIDTH = 640;
    public static final int PIC_HEIGHT = 480;
    private static final int RETRY_TIMES = 3;

    private Camera visibleCamera;
    private Camera infraredCamera;

    private Camera showingCamera;

    private int retryTime = 0;

    @Override
    public void openCamera(@NonNull TextureView textureView, @NonNull CameraManager.OnCameraOpenListener listener) {
        try {
            resetRetryTime();
            Config config = ConfigManager.getInstance().getConfig();
            if (config.isShowCamera()) { //true:近红外，false:可见光
                openInfraredCamera();
                showingCamera = infraredCamera;
                listener.onCameraOpen(showingCamera.getParameters().getPreviewSize(), "");
                if (!config.isFaceCamera()) {
                    resetRetryTime();
                    openVisibleCamera();
                }
            } else {
                openVisibleCamera();
                showingCamera = visibleCamera;
                listener.onCameraOpen(showingCamera.getParameters().getPreviewSize(), "");
                if (config.isFaceCamera() || config.isLiveness()) {
                    resetRetryTime();
                    openInfraredCamera();
                }
            }
            textureView.setSurfaceTextureListener(textureListener);
        } catch (Exception e) {
            e.printStackTrace();
            listener.onCameraOpen(null, "");
        }
    }

    @Override
    public void closeCamera() {
        resetRetryTime();
        closeVisibleCamera();
        resetRetryTime();
        closeInfraredCamera();
    }

    @Override
    public Camera getShowingCamera() {
        return showingCamera;
    }

    @Override
    public Size getCameraPreviewSize() {
        return new Size(PRE_WIDTH, PRE_HEIGHT);
    }

    @Override
    public Size getPreviewSize() {
        return new Size(PRE_HEIGHT, PRE_WIDTH);
    }

    @Override
    public int getOrientation() {
        Config config = ConfigManager.getInstance().getConfig();
        return config.isFaceCamera() ? 270 : 270;
    }

    @Override
    public boolean faceRectFlip() {
        return true;
    }

    private void resetRetryTime() {
        this.retryTime = 0;
    }

    private void openVisibleCamera() {
        try {
            visibleCamera = Camera.open(0);
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
            visibleCamera.setDisplayOrientation(270);
            visibleCamera.setPreviewCallback(visiblePreviewCallback);
            visibleCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
            App.getInstance().getThreadExecutor().execute(() -> {
                if (retryTime <= RETRY_TIMES) {
                    retryTime++;
                    openVisibleCamera();
                }
            });
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
            App.getInstance().getThreadExecutor().execute(() -> {
                if (retryTime <= RETRY_TIMES) {
                    retryTime++;
                    closeVisibleCamera();
                }
            });
        }
    }

    private void openInfraredCamera() {
        try {
            infraredCamera = Camera.open(1);
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
            infraredCamera.setDisplayOrientation(90);
            infraredCamera.setPreviewCallback(infraredPreviewCallback);
            infraredCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
            App.getInstance().getThreadExecutor().execute(() -> {
                if (retryTime <= RETRY_TIMES) {
                    retryTime++;
                    openInfraredCamera();
                }
            });
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
            App.getInstance().getThreadExecutor().execute(() -> {
                if (retryTime <= RETRY_TIMES) {
                    retryTime++;
                    closeInfraredCamera();
                }
            });
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

    private Camera.PreviewCallback visiblePreviewCallback = (data, camera) -> FaceManager.getInstance().setLastVisiblePreviewData(data);

    private Camera.PreviewCallback infraredPreviewCallback = (data, camera) -> FaceManager.getInstance().setLastInfraredPreviewData(data);

}
