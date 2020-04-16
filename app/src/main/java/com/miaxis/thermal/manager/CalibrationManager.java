package com.miaxis.thermal.manager;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.miaxis.thermal.app.App;
import com.miaxis.thermal.data.entity.Calibration;
import com.miaxis.thermal.data.model.CalibrationModel;
import com.miaxis.thermal.manager.strategy.Sign;
import com.miaxis.thermal.util.DeviceUtil;
import com.miaxis.thermal.util.ValueUtil;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class CalibrationManager {

    private CalibrationManager() {
    }

    public static CalibrationManager getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static final CalibrationManager instance = new CalibrationManager();
    }

    /**
     * ================================ 静态内部类单例写法 ================================
     **/

    private Calibration calibration;

    public Calibration getCalibration() {
        return calibration;
    }

    public void setCalibration(Calibration calibration) {
        this.calibration = calibration;
    }

    public void checkCalibration() {
        calibration = CalibrationModel.loadCalibration();
        if (calibration == null) {
            calibration = new Calibration.Builder()
                    .id(1L)
                    .xhnEmissivity(950)
                    .xhnModel(3)
                    .build();
            CalibrationModel.saveCalibration(calibration);
        }
    }

    public void saveCalibrationSync(@NonNull Calibration calibration) {
        CalibrationModel.saveCalibration(calibration);
        this.calibration = calibration;
    }

    public void saveCalibration(@NonNull Calibration calibration, @NonNull OnCalibrationSaveListener listener) {
        Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            CalibrationModel.saveCalibration(calibration);
            this.calibration = calibration;
            emitter.onNext(Boolean.TRUE);
            emitter.onComplete();
        })
                .subscribeOn(Schedulers.from(App.getInstance().getThreadExecutor()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> listener.onCalibrationSave(true, "保存成功")
                        , throwable -> listener.onCalibrationSave(false, "保存失败，" + throwable.getMessage()));
    }

    public interface OnCalibrationSaveListener {
        void onCalibrationSave(boolean result, String message);
    }

}
