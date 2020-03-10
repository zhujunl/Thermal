package com.miaxis.thermal.viewModel;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.TextureView;

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import com.miaxis.thermal.bridge.SingleLiveEvent;
import com.miaxis.thermal.bridge.Status;
import com.miaxis.thermal.data.entity.PhotoFaceFeature;
import com.miaxis.thermal.data.event.FaceRegisterEvent;
import com.miaxis.thermal.data.exception.MyException;
import com.miaxis.thermal.manager.CameraManager;
import com.miaxis.thermal.manager.FaceManager;
import com.miaxis.thermal.manager.ToastManager;

import org.greenrobot.eventbus.EventBus;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class FaceRegisterViewModel extends BaseViewModel {

    public MutableLiveData<Status> shootFlag = new MutableLiveData<>(Status.FAILED);
    public ObservableField<String> hint = new ObservableField<>("");
    public MutableLiveData<Boolean> confirmFlag = new SingleLiveEvent<>();

    private String faceFeatureCache;
    private String maskFaceFeatureCache;
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
        } else {
            toast.setValue(ToastManager.getToastBody("摄像头未正常启动，请退出后重试", ToastManager.ERROR));
        }
    }

    public void retry() {
        faceFeatureCache = null;
        maskFaceFeatureCache = null;
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
        if (!TextUtils.isEmpty(faceFeatureCache) && !TextUtils.isEmpty(maskFaceFeatureCache) && headerCache != null) {
            EventBus.getDefault().postSticky(new FaceRegisterEvent(faceFeatureCache, maskFaceFeatureCache,  headerCache));
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
                .doOnNext(bitmap -> {
                    PhotoFaceFeature photoFaceFeature = FaceManager.getInstance().getPhotoFaceFeatureByBitmapForRegisterPosting(bitmap);
                    if (photoFaceFeature.getFaceFeature() != null && photoFaceFeature.getMaskFaceFeature() != null) {
                        faceFeatureCache = Base64.encodeToString(photoFaceFeature.getFaceFeature(), Base64.NO_WRAP);
                        maskFaceFeatureCache = Base64.encodeToString(photoFaceFeature.getMaskFaceFeature(), Base64.NO_WRAP);
                        headerCache = bitmap;
                    } else {
                        throw new MyException(photoFaceFeature.getMessage());
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
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
                        hint.set("出现错误，请重新拍摄\n" + FaceManager.getInstance().getErrorMessage());
                    }
                });
    }

}
