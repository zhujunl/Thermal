package com.miaxis.thermal.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.Size;

import com.miaxis.thermal.data.entity.Config;
import com.miaxis.thermal.data.entity.Intermediary;
import com.miaxis.thermal.data.entity.MxRGBImage;
import com.miaxis.thermal.data.entity.PhotoFaceFeature;
import com.miaxis.thermal.data.exception.MyException;
import com.miaxis.thermal.util.FileUtil;

import org.zz.api.MXFaceAPI;
import org.zz.api.MXFaceInfoEx;
import org.zz.jni.mxImageTool;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class FaceManager {

    private FaceManager() {
        mxFaceAPI = new MXFaceAPI();
        dtTool = new mxImageTool();
    }

    public static FaceManager getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static final FaceManager instance = new FaceManager();
    }

    /**
     * ================================ 静态内部类单例 ================================
     **/

    public static final int ERR_LICENCE = -2009;
    public static final int ERR_FILE_COMPARE = -101;
    public static final int INIT_SUCCESS = 0;
    private static int zoomWidth = 640;
    private static int zoomHeight = 480;

    private static final int MAX_FACE_NUM = 50;
    private static final Byte lock1 = 1;
    private static final Byte lock2 = 2;

    private MXFaceAPI mxFaceAPI;
    private mxImageTool dtTool;
    private HandlerThread asyncDetectThread;
    private Handler asyncDetectHandler;
    private volatile boolean detectLoop = true;
    private HandlerThread asyncExtractThread;
    private Handler asyncExtractHandler;
    private volatile boolean extractLoop = true;

    private volatile boolean needNextFeature = false;
    private volatile boolean nova = false;
    private volatile Intermediary intermediaryData;

    private byte[] lastVisiblePreviewData;
    private byte[] lastInfraredPreviewData;

    private OnFaceHandleListener faceHandleListener;

    private String errorMessage = "";

    public interface OnFaceHandleListener {
        void onFeatureExtract(MxRGBImage mxRGBImage, MXFaceInfoEx mxFaceInfoEx, float temperature, byte[] feature, boolean mask);

        void onFaceDetect(int faceNum, MXFaceInfoEx[] faceInfoExes, float temperature);

        void onFaceIntercept(int code, String message);
    }

    public void startLoop() {
        detectLoop = true;
        extractLoop = true;
        lastVisiblePreviewData = null;
        lastInfraredPreviewData = null;
        intermediaryData = null;
        needNextFeature = true;
        Size previewSize = CameraManager.getInstance().getPreviewSize();
        zoomWidth = previewSize.getWidth();
        zoomHeight = previewSize.getHeight();
        asyncDetectHandler.sendEmptyMessage(0);
        asyncExtractHandler.sendEmptyMessage(0);
    }

    public void stopLoop() {
        detectLoop = false;
        extractLoop = false;
        asyncDetectHandler.removeMessages(0);
        asyncExtractHandler.removeMessages(0);
    }

    public void setNeedNextFeature(boolean needNextFeature) {
        this.needNextFeature = needNextFeature;
    }

    public void setLastVisiblePreviewData(byte[] lastVisiblePreviewData) {
        this.lastVisiblePreviewData = lastVisiblePreviewData;
    }

    public void setLastInfraredPreviewData(byte[] lastInfraredPreviewData) {
        this.lastInfraredPreviewData = lastInfraredPreviewData;
    }

    public void setFaceHandleListener(OnFaceHandleListener faceHandleListener) {
        this.faceHandleListener = faceHandleListener;
    }

    private void previewDataLoop() {
        if (this.lastVisiblePreviewData == null && this.lastInfraredPreviewData == null) {
            asyncDetectHandler.sendEmptyMessage(0);
        } else {
            Config config = ConfigManager.getInstance().getConfig();
            if (config.isLiveness()) {
                if (config.isFaceCamera() && lastInfraredPreviewData != null) {
                    verify(lastInfraredPreviewData, lastInfraredPreviewData);
                } else if (lastVisiblePreviewData != null && lastInfraredPreviewData != null) {
                    verify(lastVisiblePreviewData, lastInfraredPreviewData);
                } else {
                    asyncDetectHandler.sendEmptyMessage(0);
                }
            } else if (config.isFaceCamera() && lastInfraredPreviewData != null) {
                verify(lastInfraredPreviewData, null);
            } else if (!config.isFaceCamera() && lastVisiblePreviewData != null) {
                verify(lastVisiblePreviewData, null);
            } else {
                asyncDetectHandler.sendEmptyMessage(0);
            }
        }
    }

    private void verify(byte[] detectData, byte[] livenessData) {
        try {
            long time = System.currentTimeMillis();
            WatchDogManager.getInstance().feedFaceDog();
            Size cameraPreviewSize = CameraManager.getInstance().getCameraPreviewSize();
            if (cameraPreviewSize == null) {
                throw new MyException("未获取到摄像头尺寸信息");
            }
            byte[] zoomedRgbData = cameraPreviewConvert(detectData,
                    cameraPreviewSize.getWidth(),
                    cameraPreviewSize.getHeight(),
                    CameraManager.getInstance().getOrientation(),
                    zoomWidth,
                    zoomHeight);
            if (zoomedRgbData == null) {
                if (faceHandleListener != null) {
                    faceHandleListener.onFaceDetect(0, null, 0f);
                }
                throw new MyException("数据转码失败");
            }
            int[] faceNum = new int[]{MAX_FACE_NUM};
            MXFaceInfoEx[] faceBuffer = makeFaceContainer(faceNum[0]);
            boolean result = faceDetect(zoomedRgbData, zoomWidth, zoomHeight, faceNum, faceBuffer);
            if (result) {
                float temperature = TemperatureManager.getInstance().readTemperature();
//                float temperature = 36.5f;
                if (faceHandleListener != null) {
                    faceHandleListener.onFaceDetect(faceNum[0], faceBuffer, temperature);
                }
                GpioManager.getInstance().openWhiteLedInTime();
                MXFaceInfoEx mxFaceInfoEx = sortMXFaceInfoEx(faceBuffer);
                result = faceQuality(zoomedRgbData, zoomWidth, zoomHeight, 1, new MXFaceInfoEx[]{mxFaceInfoEx});
                if (result) {
                    if (temperature > 0f || temperature == -1f) {
                        Intermediary intermediary = new Intermediary();
                        intermediary.width = zoomWidth;
                        intermediary.height = zoomHeight;
                        intermediary.mxFaceInfoEx = new MXFaceInfoEx(mxFaceInfoEx);
                        intermediary.data = zoomedRgbData;
                        intermediary.liveness = livenessData;
                        intermediary.temperature = temperature;
                        intermediaryData = intermediary;
                        nova = true;
                    } else {
                        Log.e("asd", "温控返回");
                    }
//                    Log.e("asd", "检测耗时" + (System.currentTimeMillis() - time) + "-----" + mxFaceInfoEx.quality);
                }
            } else {
                if (faceHandleListener != null) {
                    faceHandleListener.onFaceDetect(0, null, 0f);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            asyncDetectHandler.sendEmptyMessage(0);
        }
    }

    private void intermediaryDataLoop() {
        if (nova && intermediaryData != null) {
            nova = false;
            extract(intermediaryData);
            intermediaryData = null;
        } else {
            asyncExtractHandler.sendEmptyMessage(0);
        }
    }

    private void extract(Intermediary intermediary) {
        try {
            if (needNextFeature) {
                if (intermediary.temperature > 36.0f || intermediary.temperature == -1f) {
                    if (intermediary.mxFaceInfoEx.quality > ConfigManager.getInstance().getConfig().getQualityScore()) {
                        if (calculationPupilDistance(intermediary.mxFaceInfoEx) > ConfigManager.getInstance().getConfig().getPupilDistance()) {
                            boolean result = detectMask(intermediary.data, zoomWidth, zoomHeight, intermediary.mxFaceInfoEx);
                            if (result) {
                                boolean mask = intermediary.mxFaceInfoEx.mask > ConfigManager.getInstance().getConfig().getMaskScore();
                                byte[] feature = null;
                                if (intermediary.liveness == null) {
                                    if (mask) {
                                        feature = extractMaskFeature(intermediary.data, zoomWidth, zoomHeight, intermediary.mxFaceInfoEx);
                                    } else {
                                        feature = extractFeature(intermediary.data, zoomWidth, zoomHeight, intermediary.mxFaceInfoEx);
                                    }
                                } else {
                                    Config config = ConfigManager.getInstance().getConfig();
                                    boolean liveness;
                                    if (config.isFaceCamera()) {
                                        liveness = livenessDetect(intermediary.data, false, intermediary.mxFaceInfoEx);
                                    } else {
                                        liveness = livenessDetect(intermediary.liveness, true, null);
                                    }
                                    if (liveness) {
                                        if (mask) {
                                            feature = extractMaskFeature(intermediary.data, zoomWidth, zoomHeight, intermediary.mxFaceInfoEx);
                                        } else {
                                            feature = extractFeature(intermediary.data, zoomWidth, zoomHeight, intermediary.mxFaceInfoEx);
                                        }
                                    } else {
                                        if (faceHandleListener != null) {
                                            faceHandleListener.onFaceIntercept(-4, "活体阈值拦截");
                                        }
                                    }
                                }
                                if (feature != null) {
                                    needNextFeature = false;
                                    if (faceHandleListener != null) {
                                        faceHandleListener.onFeatureExtract(new MxRGBImage(intermediary.data, zoomWidth, zoomHeight),
                                                intermediary.mxFaceInfoEx,
                                                intermediary.temperature,
                                                feature,
                                                mask);
                                    }
                                }
                            } else {
                                //是否戴口罩检测失败，直接丢弃
                                Log.e("asd", "检测是否戴口罩失败");
                            }
                        } else {
                            if (faceHandleListener != null) {
                                faceHandleListener.onFaceIntercept(-2, "瞳距阈值拦截");
                            }
                        }
                    } else {
                        if (faceHandleListener != null) {
                            faceHandleListener.onFaceIntercept(-1, "质量阈值拦截");
                        }
                    }
                } else {
                    if (faceHandleListener != null) {
                        faceHandleListener.onFaceIntercept(-5, "温度阈值拦截");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            asyncExtractHandler.sendEmptyMessage(0);
        }
    }

    private boolean livenessDetect(byte[] data, boolean needExtraHandle, MXFaceInfoEx mxFaceInfoEx) throws MyException {
        boolean result;
        if (needExtraHandle) {
            Size cameraPreviewSize = CameraManager.getInstance().getCameraPreviewSize();
            if (cameraPreviewSize == null) {
                throw new MyException("未获取到摄像头尺寸信息");
            }
            byte[] zoomedRgbData = cameraPreviewConvert(data,
                    cameraPreviewSize.getWidth(),
                    cameraPreviewSize.getHeight(),
                    CameraManager.getInstance().getOrientation(),
                    zoomWidth,
                    zoomHeight);
            if (zoomedRgbData != null) {
                int[] faceNum = new int[]{MAX_FACE_NUM};
                MXFaceInfoEx[] faceBuffer = makeFaceContainer(faceNum[0]);
                result = faceDetect(zoomedRgbData, zoomWidth, zoomHeight, faceNum, faceBuffer);
                if (result) {
                    MXFaceInfoEx faceInfo = sortMXFaceInfoEx(faceBuffer);
                    result = infraredLivenessDetect(zoomedRgbData, zoomWidth, zoomHeight, 1, faceInfo);
//                    Log.e("asd", "liveness:" + faceInfo.liveness);
                    return result && faceInfo.liveness > ConfigManager.getInstance().getConfig().getLivenessScore();
                }
            }
            return false;
        } else {
            result = infraredLivenessDetect(data, zoomWidth, zoomHeight, 1, mxFaceInfoEx);
//            Log.e("asd", "liveness:" + mxFaceInfoEx.liveness);
            return result && mxFaceInfoEx.liveness > ConfigManager.getInstance().getConfig().getLivenessScore();
        }
    }

    public byte[] getCardFeatureByBitmapPosting(Bitmap bitmap) {
        errorMessage = "";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(bitmap.getByteCount());
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        byte[] rgbData = imageFileDecode(outputStream.toByteArray(), bitmap.getWidth(), bitmap.getHeight());
        if (rgbData == null) {
            errorMessage = "图片转码失败";
            return null;
        }
        int[] pFaceNum = new int[]{0};
        MXFaceInfoEx[] pFaceBuffer = makeFaceContainer(MAX_FACE_NUM);
        boolean result = faceDetect(rgbData, bitmap.getWidth(), bitmap.getHeight(), pFaceNum, pFaceBuffer);
        if (result) {
            result = faceQuality(rgbData, bitmap.getWidth(), bitmap.getHeight(), pFaceNum[0], pFaceBuffer);
            MXFaceInfoEx mxFaceInfoEx = sortMXFaceInfoEx(pFaceBuffer);
//            if (result && mxFaceInfoEx.quality > 50) {
            byte[] feature = extractFeature(rgbData, bitmap.getWidth(), bitmap.getHeight(), mxFaceInfoEx);
            if (feature != null) {
                return feature;
            } else {
                errorMessage = "提取特征失败";
            }
//            } else {
//                errorMessage = "人脸质量过低";
//            }
        } else {
            errorMessage = "未检测到人脸";
        }
        return null;
    }

    public PhotoFaceFeature getCardFaceFeatureByBitmapPosting(Bitmap bitmap) {
        String message = "";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(bitmap.getByteCount());
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        byte[] rgbData = imageFileDecode(outputStream.toByteArray(), bitmap.getWidth(), bitmap.getHeight());
        if (rgbData == null) {
            message = "图片转码失败";
            return new PhotoFaceFeature(message);
        }
        int[] pFaceNum = new int[]{0};
        MXFaceInfoEx[] pFaceBuffer = makeFaceContainer(MAX_FACE_NUM);
        boolean result = faceDetect(rgbData, bitmap.getWidth(), bitmap.getHeight(), pFaceNum, pFaceBuffer);
        if (result && pFaceNum[0] > 0) {
            if (pFaceNum[0] == 1) {
                result = faceQuality(rgbData, bitmap.getWidth(), bitmap.getHeight(), pFaceNum[0], pFaceBuffer);
                MXFaceInfoEx mxFaceInfoEx = sortMXFaceInfoEx(pFaceBuffer);
//                if (result && mxFaceInfoEx.quality > ConfigManager.getInstance().getConfig().getRegisterQualityScore()) {
                    byte[] faceFeature = extractFeature(rgbData, bitmap.getWidth(), bitmap.getHeight(), mxFaceInfoEx);
                    if (faceFeature != null) {
                        byte[] maskFaceFeature = extractMaskFeatureForRegister(rgbData, bitmap.getWidth(), bitmap.getHeight(), mxFaceInfoEx);
                        if (maskFaceFeature != null) {
                            return new PhotoFaceFeature(faceFeature, maskFaceFeature, "提取成功");
                        }
                    } else {
                        message = "提取特征失败";
                    }
//                } else {
//                    message = "人脸质量过低";
//                }
            } else {
                message = "检测到多张人脸";
            }
        } else {
            message = "未检测到人脸";
        }
        return new PhotoFaceFeature(message);
    }

    public PhotoFaceFeature getPhotoFaceFeatureByBitmapForRegisterPosting(Bitmap bitmap) {
        String message = "";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(bitmap.getByteCount());
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        byte[] rgbData = imageFileDecode(outputStream.toByteArray(), bitmap.getWidth(), bitmap.getHeight());
        if (rgbData == null) {
            message = "图片转码失败";
            return new PhotoFaceFeature(message);
        }
        int[] pFaceNum = new int[]{0};
        MXFaceInfoEx[] pFaceBuffer = makeFaceContainer(MAX_FACE_NUM);
        boolean result = faceDetect(rgbData, bitmap.getWidth(), bitmap.getHeight(), pFaceNum, pFaceBuffer);
        if (result && pFaceNum[0] > 0) {
            if (pFaceNum[0] == 1) {
                result = faceQuality(rgbData, bitmap.getWidth(), bitmap.getHeight(), pFaceNum[0], pFaceBuffer);
                MXFaceInfoEx mxFaceInfoEx = sortMXFaceInfoEx(pFaceBuffer);
                if (result && mxFaceInfoEx.quality > ConfigManager.getInstance().getConfig().getRegisterQualityScore()) {
                    byte[] faceFeature = extractFeature(rgbData, bitmap.getWidth(), bitmap.getHeight(), mxFaceInfoEx);
                    if (faceFeature != null) {
                        byte[] maskFaceFeature = extractMaskFeatureForRegister(rgbData, bitmap.getWidth(), bitmap.getHeight(), mxFaceInfoEx);
                        if (maskFaceFeature != null) {
                            return new PhotoFaceFeature(faceFeature, maskFaceFeature, "提取成功");
                        }
                    } else {
                        message = "提取特征失败";
                    }
                } else {
                    message = "人脸质量过低";
                }
            } else {
                message = "检测到多张人脸";
            }
        } else {
            message = "未检测到人脸";
        }
        return new PhotoFaceFeature(message);
    }

    public byte[] getPhotoFeatureByBitmapPosting(Bitmap bitmap) {
        errorMessage = "";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(bitmap.getByteCount());
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        byte[] rgbData = imageFileDecode(outputStream.toByteArray(), bitmap.getWidth(), bitmap.getHeight());
        if (rgbData == null) {
            errorMessage = "图片转码失败";
            return null;
        }
        int[] pFaceNum = new int[]{0};
        MXFaceInfoEx[] pFaceBuffer = makeFaceContainer(MAX_FACE_NUM);
        boolean result = faceDetect(rgbData, bitmap.getWidth(), bitmap.getHeight(), pFaceNum, pFaceBuffer);
        if (result && pFaceNum[0] > 0) {
            if (pFaceNum[0] == 1) {
                result = faceQuality(rgbData, bitmap.getWidth(), bitmap.getHeight(), pFaceNum[0], pFaceBuffer);
                MXFaceInfoEx mxFaceInfoEx = sortMXFaceInfoEx(pFaceBuffer);
                if (result && mxFaceInfoEx.quality > ConfigManager.getInstance().getConfig().getRegisterQualityScore()) {
                    byte[] feature = extractFeature(rgbData, bitmap.getWidth(), bitmap.getHeight(), mxFaceInfoEx);
                    if (feature != null) {
                        return feature;
                    } else {
                        errorMessage = "提取特征失败";
                    }
                } else {
                    errorMessage = "人脸质量过低";
                }
            } else {
                errorMessage = "检测到多张人脸";
            }
        } else {
            errorMessage = "未检测到人脸";
        }
        return null;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * 初始化人脸算法
     *
     * @param context     设备上下文
     * @param szModelPath 人脸模型文件目录
     * @param licencePath 授权文件路径
     * @return 状态码
     */
    public int initFaceST(Context context, String szModelPath, String licencePath) {
        final String sLicence = FileUtil.readLicence(licencePath);
        if (TextUtils.isEmpty(sLicence)) {
            return ERR_LICENCE;
        }
        int re = initFaceModel(context, szModelPath);
        if (re == 0) {
            re = mxFaceAPI.mxInitAlg(context, szModelPath, sLicence);
        }
        initThread();
        return re;
    }

    private void initThread() {
        asyncDetectThread = new HandlerThread("detect_thread");
        asyncDetectThread.start();
        asyncDetectHandler = new Handler(asyncDetectThread.getLooper()) {
            public void handleMessage(Message msg) {
                if (detectLoop) {
                    try {
                        previewDataLoop();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        asyncExtractThread = new HandlerThread("extract_thread");
        asyncExtractThread.start();
        asyncExtractHandler = new Handler(asyncExtractThread.getLooper()) {
            public void handleMessage(Message msg) {
                if (extractLoop) {
                    try {
                        intermediaryDataLoop();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    /**
     * 拷贝人脸模型文件
     *
     * @param context
     * @param modelPath
     * @return
     */
    private int initFaceModel(Context context, String modelPath) {
        String hsLibDirName = "MIAXISModelsV5";
        String modelFile1 = "MIAXIS_V5.0.0_FaceDetect.model";
        String modelFile2 = "MIAXIS_V5.0.0_FaceMaskAlign.model";
        String modelFile3 = "MIAXIS_V5.0.0_FaceMaskDetect.model";
        String modelFile4 = "MIAXIS_V5.0.0_FaceMaskRecog.model";
        String modelFile5 = "MIAXIS_V5.0.0_FaceQuality.model";
        String modelFile6 = "MIAXIS_V5.0.0_FaceRecog.model";
        String modelFile7 = "MIAXIS_V5.0.0_LivenessDetect.model";
        File modelDir = new File(modelPath);
        if (modelDir.exists()) {
            if (!new File(modelDir + File.separator + modelFile1).exists()) {
                FileUtil.copyAssetsFile(context, hsLibDirName + File.separator + modelFile1, modelDir + File.separator + modelFile1);
            }
            if (!new File(modelDir + File.separator + modelFile2).exists()) {
                FileUtil.copyAssetsFile(context, hsLibDirName + File.separator + modelFile2, modelDir + File.separator + modelFile2);
            }
            if (!new File(modelDir + File.separator + modelFile3).exists()) {
                FileUtil.copyAssetsFile(context, hsLibDirName + File.separator + modelFile3, modelDir + File.separator + modelFile3);
            }
            if (!new File(modelDir + File.separator + modelFile4).exists()) {
                FileUtil.copyAssetsFile(context, hsLibDirName + File.separator + modelFile4, modelDir + File.separator + modelFile4);
            }
            if (!new File(modelDir + File.separator + modelFile5).exists()) {
                FileUtil.copyAssetsFile(context, hsLibDirName + File.separator + modelFile5, modelDir + File.separator + modelFile5);
            }
            if (!new File(modelDir + File.separator + modelFile6).exists()) {
                FileUtil.copyAssetsFile(context, hsLibDirName + File.separator + modelFile6, modelDir + File.separator + modelFile6);
            }
            if (!new File(modelDir + File.separator + modelFile7).exists()) {
                FileUtil.copyAssetsFile(context, hsLibDirName + File.separator + modelFile7, modelDir + File.separator + modelFile7);
            }
            return 0;
        } else {
            return -1;
        }
    }

    public static String getFaceInitResultDetail(int result) {
        switch (result) {
            case ERR_LICENCE:
                return "读取授权文件失败";
            case ERR_FILE_COMPARE:
                return "文件校验失败";
            case INIT_SUCCESS:
                return "初始化人脸算法成功";
            default:
                return "初始化算法失败";
        }
    }

    /**
     * 比对特征，人证比对0.7，人像比对0.8
     *
     * @param alpha
     * @param beta
     * @return
     */
    public float matchFeature(byte[] alpha, byte[] beta) {
        if (alpha != null && beta != null) {
            float[] score = new float[1];
            int re = mxFaceAPI.mxFeatureMatch(alpha, beta, score);
            if (re == 0) {
                return score[0];
            }
            return -1;
        }
        return 0;
    }

    /**
     * 比对口罩人脸特征
     *
     * @param alpha
     * @param beta
     * @return
     */
    public float matchMaskFeature(byte[] alpha, byte[] beta) {
        if (alpha != null && beta != null) {
            float[] score = new float[1];
            int re = mxFaceAPI.mxMaskFeatureMatch(alpha, beta, score);
            if (re == 0) {
                return score[0];
            }
            return -1;
        }
        return 0;
    }

    public byte[] imageEncode(byte[] rgbBuf, int width, int height) {
        byte[] fileBuf = new byte[width * height * 4];
        int[] fileLength = new int[]{0};
        int re = dtTool.ImageEncode(rgbBuf, width, height, ".jpg", fileBuf, fileLength);
        if (re == 1 && fileLength[0] != 0) {
            byte[] fileImage = new byte[fileLength[0]];
            System.arraycopy(fileBuf, 0, fileImage, 0, fileImage.length);
            return fileImage;
        } else {
            return null;
        }
    }

    /**
     * 图像文件解码成RGB裸数据
     *
     * @param data
     * @param width
     * @param height
     * @return
     */
    public byte[] imageFileDecode(byte[] data, int width, int height) {
        byte[] rgbData = new byte[width * height * 3];
        int[] oX = new int[1];
        int[] oY = new int[1];
        int result = dtTool.ImageDecode(data, data.length, rgbData, oX, oY);
        if (result > 0) {
            return rgbData;
        }
        return null;
    }

    /**
     * 摄像头预览数据转换
     *
     * @param data        摄像头onPreviewFrame-data
     * @param width       摄像头实际分辨率-宽
     * @param height      摄像头实际分辨率-高
     * @param orientation 旋转角度
     * @param zoomWidth   实际分辨率旋转压缩后的宽度
     * @param zoomHeight  实际分辨率旋转压缩后的高度
     * @return
     */
    private byte[] cameraPreviewConvert(byte[] data, int width, int height, int orientation, int zoomWidth, int zoomHeight) {
        // 原始YUV数据转换RGB裸数据
        byte[] rgbData = new byte[width * height * 3];
        dtTool.YUV2RGB(data, width, height, rgbData);
        int[] rotateWidth = new int[1];
        int[] rotateHeight = new int[1];
        // 旋转相应角度
        int re = dtTool.ImageRotate(rgbData, width, height, orientation, rgbData, rotateWidth, rotateHeight);
        if (re != 1) {
            Log.e("asd", "旋转失败");
            return null;
        }
        //镜像后画框位置按照正常坐标系，不镜像的话按照反坐标系也可画框
//        re = dtTool.ImageFlip(rgbData, rotateWidth[0], rotateHeight[0], 1, rgbData);
//        if (re != 1) {
//            Log.e("asd", "镜像失败");
//            return null;
//        }
        // RGB数据压缩到指定宽高
        byte[] zoomedRgbData = new byte[zoomWidth * zoomHeight * 3];
        re = dtTool.Zoom(rgbData, rotateWidth[0], rotateHeight[0], 3, zoomWidth, zoomHeight, zoomedRgbData);
        if (re != 1) {
            Log.e("asd", "压缩失败");
            return null;
        }
        return zoomedRgbData;
    }

    /**
     * 摄像头预览数据转换
     *
     * @param data        摄像头onPreviewFrame-data
     * @param width       摄像头实际分辨率-宽
     * @param height      摄像头实际分辨率-高
     * @param orientation 旋转角度
     * @param zoomWidth   实际分辨率旋转压缩后的宽度
     * @param zoomHeight  实际分辨率旋转压缩后的高度
     * @return
     */
    private byte[] cameraPreviewConvertWithFlip(byte[] data, int width, int height, int orientation, int zoomWidth, int zoomHeight) {
        // 原始YUV数据转换RGB裸数据
        byte[] rgbData = new byte[width * height * 3];
        dtTool.YUV2RGB(data, width, height, rgbData);
        int[] rotateWidth = new int[1];
        int[] rotateHeight = new int[1];
        // 旋转相应角度
        int re = dtTool.ImageRotate(rgbData, width, height, orientation, rgbData, rotateWidth, rotateHeight);
        if (re != 1) {
            Log.e("asd", "旋转失败");
            return null;
        }
        //镜像后画框位置按照正常坐标系，不镜像的话按照反坐标系也可画框
        re = dtTool.ImageFlip(rgbData, rotateWidth[0], rotateHeight[0], 1, rgbData);
        if (re != 1) {
            Log.e("asd", "镜像失败");
            return null;
        }
        // RGB数据压缩到指定宽高
        byte[] zoomedRgbData = new byte[zoomWidth * zoomHeight * 3];
        re = dtTool.Zoom(rgbData, rotateWidth[0], rotateHeight[0], 3, zoomWidth, zoomHeight, zoomedRgbData);
        if (re != 1) {
            Log.e("asd", "压缩失败");
            return null;
        }
        return zoomedRgbData;
    }

    /**
     * 组装人脸信息存储容器数组
     *
     * @param size
     * @return
     */
    private MXFaceInfoEx[] makeFaceContainer(int size) {
        MXFaceInfoEx[] pFaceBuffer = new MXFaceInfoEx[size];
        for (int i = 0; i < size; i++) {
            pFaceBuffer[i] = new MXFaceInfoEx();
        }
        return pFaceBuffer;
    }

    /**
     * 检测人脸信息
     *
     * @param rgbData    RGB裸图像数据
     * @param width      图像数据宽度
     * @param height     图像数据高度
     * @param faceNum    native输出，检测到的人脸数量
     * @param faceBuffer native输出，人脸信息
     * @return true - 算法执行成功，并且检测到人脸，false - 算法执行失败，或者执行成功但是未检测到人脸
     */
    private boolean faceDetect(byte[] rgbData, int width, int height, int[] faceNum, MXFaceInfoEx[] faceBuffer) {
        synchronized (lock2) {
            int result = mxFaceAPI.mxDetectFace(rgbData, width, height, faceNum, faceBuffer);
            return result == 0 && faceNum[0] > 0;
        }
    }

    private boolean faceTrace(byte[] rgbData, int width, int height, int[] faceNum, MXFaceInfoEx[] faceBuffer) {
        synchronized (lock2) {
            int result = mxFaceAPI.mxTrackFace(rgbData, width, height, faceNum, faceBuffer);
            return result == 0 && faceNum[0] > 0;
        }
    }

    /**
     * 人脸质量检测
     *
     * @param rgbData    RGB裸图像数据
     * @param width      图像数据宽度
     * @param height     图像数据高度
     * @param faceNum    检测到人脸数量
     * @param faceBuffer 输入，人脸检测结果
     * @return
     */
    private boolean faceQuality(byte[] rgbData, int width, int height, int faceNum, MXFaceInfoEx[] faceBuffer) {
        int result = mxFaceAPI.mxFaceQuality(rgbData, width, height, faceNum, faceBuffer);
        return result == 0;
    }

    /**
     * 口罩检测
     *
     * @param rgbData  RGB裸图像数据
     * @param width    图像数据宽度
     * @param height   图像数据高度
     * @param faceInfo 输入，人脸检测结果
     * @return
     */
    private boolean detectMask(byte[] rgbData, int width, int height, MXFaceInfoEx faceInfo) {
        int result = mxFaceAPI.mxMaskDetect(rgbData, width, height, 1, new MXFaceInfoEx[]{faceInfo});
        return result == 0;
    }

    /**
     * 红外活体检测
     *
     * @param rgbData    RGB裸图像数据
     * @param width      图像数据宽度
     * @param height     图像数据高度
     * @param faceNum    检测到人脸数量
     * @param faceBuffer 输入，人脸检测结果，native输出，根据瞳距进行从大到小排序
     * @return
     */
    private boolean infraredLivenessDetect(byte[] rgbData, int width, int height, int faceNum, MXFaceInfoEx faceBuffer) {
        int result = mxFaceAPI.mxNIRLivenessDetect(rgbData, width, height, faceNum, new MXFaceInfoEx[]{faceBuffer});
        return result == 0;
    }

    /**
     * RGB裸图像数据提取人脸特征
     *
     * @param pImage
     * @param width
     * @param height
     * @param faceInfo
     * @return
     */
    private byte[] extractFeature(byte[] pImage, int width, int height, MXFaceInfoEx faceInfo) {
        synchronized (lock1) {
            byte[] feature = new byte[mxFaceAPI.mxGetFeatureSize()];
            int result = mxFaceAPI.mxFeatureExtract(pImage, width, height, 1, new MXFaceInfoEx[]{faceInfo}, feature);
            return result == 0 ? feature : null;
        }
    }

    /**
     * @param pImage   - 输入，RGB图像数据
     * @param width    - 输入，图像宽度
     * @param height   - 输入，图像高度
     * @param faceInfo - 输入，人脸信息
     * @return 0-成功，其他-失败
     * @category 人脸特征提取, 用于比对（戴口罩算法）
     */
    public byte[] extractMaskFeature(byte[] pImage, int width, int height, MXFaceInfoEx faceInfo) {
        synchronized (lock1) {
            byte[] feature = new byte[mxFaceAPI.mxGetFeatureSize()];
            int result = mxFaceAPI.mxMaskFeatureExtract(pImage, width, height, 1, new MXFaceInfoEx[]{faceInfo}, feature);
            return result == 0 ? feature : null;
        }
    }

    /**
     * @param pImage   - 输入，RGB图像数据
     * @param width    - 输入，图像宽度
     * @param height   - 输入，图像高度
     * @param faceInfo - 输入，人脸信息
     * @return 0-成功，其他-失败
     * @category 人脸特征提取, 用于注册（戴口罩算法）
     */
    public byte[] extractMaskFeatureForRegister(byte[] pImage, int width, int height, MXFaceInfoEx faceInfo) {
        synchronized (lock1) {
            byte[] feature = new byte[mxFaceAPI.mxGetFeatureSize()];
            int result = mxFaceAPI.mxMaskFeatureExtract4Reg(pImage, width, height, 1, new MXFaceInfoEx[]{faceInfo}, feature);
            return result == 0 ? feature : null;
        }
    }

    private MXFaceInfoEx sortMXFaceInfoEx(MXFaceInfoEx[] mxFaceInfoExList) {
        MXFaceInfoEx maxMXFaceInfoEx = mxFaceInfoExList[0];
        for (MXFaceInfoEx mxFaceInfoEx : mxFaceInfoExList) {
            if (mxFaceInfoEx.width > maxMXFaceInfoEx.width) {
                maxMXFaceInfoEx = mxFaceInfoEx;
            }
        }
        return maxMXFaceInfoEx;
    }

    //TODO:瞳距计算好像哪里不太对
    private static double calculationPupilDistance(MXFaceInfoEx mxFaceInfoEx) {
        int a = mxFaceInfoEx.keypt_x[1] - mxFaceInfoEx.keypt_x[0];
        int b = mxFaceInfoEx.keypt_y[1] - mxFaceInfoEx.keypt_y[0];
        double pow = Math.pow(a, 2) + Math.pow(b, 2);
        return Math.sqrt(pow);
    }

    public Bitmap tailoringFace(MxRGBImage mxRGBImage, MXFaceInfoEx mxFaceInfoEx) {
        Bitmap b = null;
        try {
            byte[] fileImageData = FaceManager.getInstance().imageEncode(mxRGBImage.getRgbImage(), mxRGBImage.getWidth(), mxRGBImage.getHeight());
            b = BitmapFactory.decodeByteArray(fileImageData, 0, fileImageData.length);
            try {
                int imageWidth = b.getWidth();
                int x = (int) (mxFaceInfoEx.x * 0.75f);
                if (x <= mxFaceInfoEx.width / 4) x = 0;
                int width = mxFaceInfoEx.width + (mxFaceInfoEx.x - x) * 2;
                while (x >= 0 || x <= mxFaceInfoEx.width) {
                    if (imageWidth - x > width) {
                        break;
                    } else {
                        x += 1;
                        width = mxFaceInfoEx.width + (mxFaceInfoEx.x - x) * 2;
                    }
                }
                if (x < 0 || x > imageWidth || width < mxFaceInfoEx.width) {
                    x = mxFaceInfoEx.x;
                    width = mxFaceInfoEx.width;
                }
                int imageHeight = b.getHeight();
                int y = (int) (mxFaceInfoEx.y * 0.75f);
                if (y <= mxFaceInfoEx.height / 4) y = 0;
                int height = mxFaceInfoEx.height + (mxFaceInfoEx.y - y) * 2;
                while (y >= 0 || y <= mxFaceInfoEx.height) {
                    if (imageHeight - y > height) {
                        break;
                    } else {
                        y += 1;
                        height = mxFaceInfoEx.height + (mxFaceInfoEx.y - y) * 2;
                    }
                }
                if (y < 0 || y > imageHeight || height < mxFaceInfoEx.height) {
                    y = mxFaceInfoEx.y;
                    height = mxFaceInfoEx.height;
                }
                Bitmap rectBitmap = Bitmap.createBitmap(b, x, y, width, height);
                if (rectBitmap != null) {
                    b.recycle();
                    return rectBitmap;
                } else {
                    Bitmap bitmap = Bitmap.createBitmap(b, mxFaceInfoEx.x, mxFaceInfoEx.y, mxFaceInfoEx.width, mxFaceInfoEx.height);
                    b.recycle();
                    return bitmap;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Bitmap bitmap = Bitmap.createBitmap(b, mxFaceInfoEx.x, mxFaceInfoEx.y, mxFaceInfoEx.width, mxFaceInfoEx.height);
                b.recycle();
                return bitmap;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
