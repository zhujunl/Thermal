package com.miaxis.thermal.viewModel;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import com.miaxis.thermal.bridge.SingleLiveEvent;
import com.miaxis.thermal.bridge.Status;
import com.miaxis.thermal.data.event.FaceRegisterEvent;
import com.miaxis.thermal.data.exception.MyException;
import com.miaxis.thermal.manager.CameraManager;
import com.miaxis.thermal.manager.FaceManager;
import com.miaxis.thermal.manager.ToastManager;

import org.greenrobot.eventbus.EventBus;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class FaceRegisterViewModel extends BaseViewModel {

    public MutableLiveData<Status> shootFlag = new MutableLiveData<>(Status.FAILED);
    public ObservableField<String> hint = new ObservableField<>("请自拍一张大头照");
    public MutableLiveData<Boolean> confirmFlag = new SingleLiveEvent<>();

    private String featureCache;
    private Bitmap headerCache;

    public FaceRegisterViewModel() {
    }

    public void takePicture(TextureView textureView) {
        shootFlag.setValue(Status.LOADING);
        hint.set("处理中");
        Camera camera = CameraManager.getInstance().getShowingCamera();
        if (camera != null) {
            camera.stopPreview();
            Bitmap bitmap = textureView.getBitmap();
            handlePhoto(bitmap, camera);
//            camera.takePicture(null, null, this::handlePhoto);
        } else {
            toast.setValue(ToastManager.getToastBody("摄像头未正常启动，请退出后重试", ToastManager.ERROR));
        }
    }

    public void retry() {
        featureCache = null;
        headerCache = null;
        shootFlag.setValue(Status.FAILED);
        hint.set("请自拍一张大头照");
        Camera camera = CameraManager.getInstance().getShowingCamera();
        if (camera != null) {
            camera.startPreview();
        } else {
            toast.setValue(ToastManager.getToastBody("摄像头未正常启动，请退出后重试", ToastManager.ERROR));
        }
    }

    public void confirm() {
        if (!TextUtils.isEmpty(featureCache) && headerCache != null) {
            EventBus.getDefault().postSticky(new FaceRegisterEvent(featureCache, headerCache));
            confirmFlag.setValue(Boolean.TRUE);
        }
    }

    private void handlePhoto(Bitmap mBitmap, Camera camera) {
        Observable.create((ObservableOnSubscribe<Bitmap>) emitter -> {
            Bitmap bitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getHeight(), mBitmap.getHeight(), null, false);
            emitter.onNext(bitmap);
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(bitmap -> {
                    byte[] feature = FaceManager.getInstance().getPhotoFeatureByBitmapPosting(bitmap);
                    if (feature != null) {
                        headerCache = bitmap;
                        return Base64.encodeToString(feature, Base64.NO_WRAP);
                    }
                    throw new MyException(FaceManager.getInstance().getErrorMessage());
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    featureCache = s;
                    shootFlag.setValue(Status.SUCCESS);
                    hint.set("人脸特征提取成功");
                }, throwable -> {
                    shootFlag.setValue(Status.FAILED);
                    camera.startPreview();
                    if (throwable instanceof MyException) {
                        hint.set(throwable.getMessage() + "，请重新拍摄");
                    } else {
                        throwable.printStackTrace();
                        Log.e("asd", "" + throwable.getMessage());
                        hint.set("出现错误，请重新拍摄");
                    }
                });
    }

//    private void handlePhoto(byte[] data, Camera camera) {
//        Observable.create((ObservableOnSubscribe<Bitmap>) emitter -> {
//            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//            Matrix matrix = new Matrix();
//            Camera.CameraInfo info = new Camera.CameraInfo();
//            Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, info);
//            int previewOrientation = CameraManager.getInstance().getPreviewOrientation(info);
//            matrix.postRotate(previewOrientation);
//            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//            emitter.onNext(bitmap);
//        })
//                .subscribeOn(Schedulers.io())
//                .observeOn(Schedulers.io())
//                .map(bitmap -> {
//                    byte[] feature = FaceManager.getInstance().getPhotoFeatureByBitmapPosting(bitmap);
//                    if (feature != null) {
//                        headerCache = bitmap;
//                        return Base64.encodeToString(feature, Base64.NO_WRAP);
//                    }
//                    throw new MyException(FaceManager.getInstance().getErrorMessage());
//                })
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(s -> {
//                    featureCache = s;
//                    shootFlag.setValue(Status.SUCCESS);
//                    hint.set("人脸特征提取成功");
//                }, throwable -> {
//                    shootFlag.setValue(Status.FAILED);
//                    camera.startPreview();
//                    if (throwable instanceof MyException) {
//                        hint.set(throwable.getMessage() + "，请重新拍摄");
//                    } else {
//                        throwable.printStackTrace();
//                        Log.e("asd", "" + throwable.getMessage());
//                        hint.set("出现错误，请重新拍摄");
//                    }
//                });
//    }

}
