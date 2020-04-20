package com.miaxis.thermal.viewModel;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.TextureView;

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import com.miaxis.thermal.R;
import com.miaxis.thermal.app.App;
import com.miaxis.thermal.bridge.SingleLiveEvent;
import com.miaxis.thermal.bridge.Status;
import com.miaxis.thermal.data.entity.MatchPerson;
import com.miaxis.thermal.data.entity.PhotoFaceFeature;
import com.miaxis.thermal.data.event.FaceRegisterEvent;
import com.miaxis.thermal.data.exception.MyException;
import com.miaxis.thermal.manager.CameraManager;
import com.miaxis.thermal.manager.FaceManager;
import com.miaxis.thermal.manager.PersonManager;
import com.miaxis.thermal.manager.ToastManager;

import org.greenrobot.eventbus.EventBus;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class FaceRegisterViewModel extends BaseViewModel {

    public MutableLiveData<Status> shootFlag = new MutableLiveData<>(Status.FAILED);
    public ObservableField<String> hint = new ObservableField<>("");
    public MutableLiveData<Boolean> confirmFlag = new SingleLiveEvent<>();
    public MutableLiveData<MatchPerson> repeatFaceFlag = new SingleLiveEvent<>();

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
            bitmap = Bitmap.createBitmap( bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), textureView.getTransform(null), true );
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
            Bitmap bitmap;
            if (mBitmap.getWidth() >= mBitmap.getHeight()) {
                bitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getHeight(), mBitmap.getHeight(), null, false);
            } else {
                bitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getWidth(), null, false);
            }
            emitter.onNext(bitmap);
            emitter.onComplete();
        })
                .subscribeOn(Schedulers.from(App.getInstance().getThreadExecutor()))
                .observeOn(Schedulers.from(App.getInstance().getThreadExecutor()))
                .map(bitmap -> {
                    PhotoFaceFeature photoFaceFeature = FaceManager.getInstance().getPhotoFaceFeatureByBitmapForRegisterPosting(bitmap);
                    if (photoFaceFeature.getFaceFeature() != null && photoFaceFeature.getMaskFaceFeature() != null) {
                        faceFeatureCache = Base64.encodeToString(photoFaceFeature.getFaceFeature(), Base64.NO_WRAP);
                        maskFaceFeatureCache = Base64.encodeToString(photoFaceFeature.getMaskFaceFeature(), Base64.NO_WRAP);
                        headerCache = bitmap;
                        return photoFaceFeature.getFaceFeature();
                    } else {
                        throw new MyException(photoFaceFeature.getMessage());
                    }
                })
                .doOnNext(faceFeatureCache -> {
                    MatchPerson matchPerson = PersonManager.getInstance().getMatchPerson(faceFeatureCache, false);
                    repeatFaceFlag.postValue(matchPerson);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    shootFlag.postValue(Status.SUCCESS);
                    hint.set(getString(R.string.fragment_face_register_extract_success));
                }, throwable -> {
                    shootFlag.setValue(Status.FAILED);
                    camera.startPreview();
                    if (throwable instanceof MyException) {
                        hint.set(throwable.getMessage() + "，请重新拍摄");
                    } else {
                        throwable.printStackTrace();
                        Log.e("asd", "" + throwable.getMessage());
                        hint.set("出现错误，请重新拍摄\n" + throwable.getMessage());
                    }
                });
    }

}
