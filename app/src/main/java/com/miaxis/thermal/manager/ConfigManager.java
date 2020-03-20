package com.miaxis.thermal.manager;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.miaxis.thermal.app.App;
import com.miaxis.thermal.data.entity.Config;
import com.miaxis.thermal.data.model.ConfigModel;
import com.miaxis.thermal.manager.strategy.Sign;
import com.miaxis.thermal.util.DeviceUtil;
import com.miaxis.thermal.util.ValueUtil;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ConfigManager {

    private ConfigManager() {
    }

    public static ConfigManager getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static final ConfigManager instance = new ConfigManager();
    }

    /**
     * ================================ 静态内部类单例写法 ================================
     **/

    private Config config;

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public void checkConfig() {
        config = ConfigModel.loadConfig();
        if (config == null) {
            config = new Config.Builder()
                    .id(1L)
                    .serverMode(ValueUtil.DEFAULT_SERVER_MODE)
                    .host(ValueUtil.DEFAULT_HOST)
                    .downloadPersonPath(ValueUtil.DEFAULT_DOWNLOAD_PERSON_PATH)
                    .updatePersonPath(ValueUtil.DEFAULT_UPDATE_PERSON_PATH)
                    .uploadRecordPath(ValueUtil.DEFAULT_UPLOAD_RECORD_PATH)
                    .showCamera(ValueUtil.DEFAULT_CAMERA_SHOW)
                    .faceCamera(ValueUtil.DEFAULT_CAMERA_FACE)
                    .liveness(ValueUtil.DEFAULT_LIVENESS)
                    .qualityScore(ValueUtil.DEFAULT_QUALITY_SCORE)
                    .registerQualityScore(ValueUtil.DEFAULT_REGISTER_QUALITY_SCORE)
                    .maskScore(ValueUtil.DEFAULT_MASK_SCORE)
                    .verifyScore(ValueUtil.DEFAULT_VERIFY_SCORE)
                    .maskVerifyScore(ValueUtil.DEFAULT_MASK_VERIFY_SCORE)
                    .livenessScore(ValueUtil.DEFAULT_LIVENESS_SCORE)
                    .feverScore(ValueUtil.DEFAULT_FEVER_SCORE)
                    .heartBeatInterval(ValueUtil.DEFAULT_HEART_BEAT_INTERVAL)
                    .failedQueryCold(ValueUtil.DEFAULT_FAILED_QUERY_COLD)
                    .recordClearThreshold(ValueUtil.DEFAULT_RECORD_CLEAR_THRESHOLD)
                    .verifyCold(ValueUtil.DEFAULT_VERIFY_COLD)
                    .flashTime(ValueUtil.DEFAULT_FLASH_TIME)
                    .devicePassword(ValueUtil.DEFAULT_DEVICE_PASSWORD)
                    .timeStamp(ValueUtil.DEFAULT_TIME_STAMP)
                    .build();
            if (ValueUtil.DEFAULT_SIGN == Sign.ZH) {
                config.setMac(DeviceUtil.getDeviceId(App.getInstance()));
            } else {
                config.setMac(DeviceUtil.getMacFromHardware());
            }
            if (ValueUtil.DEFAULT_SIGN == Sign.XH) {
                config.setPupilDistanceMin(ValueUtil.DEFAULT_PUPIL_DISTANCE_MIN_HORIZONTAL);
                config.setPupilDistanceMax(ValueUtil.DEFAULT_PUPIL_DISTANCE_MAX_HORIZONTAL);
            }  else if (ValueUtil.DEFAULT_SIGN == Sign.MR870
                    || ValueUtil.DEFAULT_SIGN == Sign.ZH
                    || ValueUtil.DEFAULT_SIGN == Sign.TPS980P) {
                config.setPupilDistanceMin(ValueUtil.DEFAULT_PUPIL_DISTANCE_MIN_VERTICAL);
                config.setPupilDistanceMax(ValueUtil.DEFAULT_PUPIL_DISTANCE_MAX_VERTICAL);
            }
            ConfigModel.saveConfig(config);
        }
    }

    public void saveConfigSync(@NonNull Config config) {
        ConfigModel.saveConfig(config);
        this.config = config;
    }

    public void saveConfig(@NonNull Config config, @NonNull OnConfigSaveListener listener) {
        Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            ConfigModel.saveConfig(config);
            this.config = config;
            emitter.onNext(Boolean.TRUE);
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> listener.onConfigSave(true, "保存成功")
                        , throwable -> listener.onConfigSave(false, "保存失败，" + throwable.getMessage()));
    }

    public String getMacAddress() {
        if (config != null && !TextUtils.isEmpty(config.getMac())) {
            return config.getMac();
        }
        String macFromHardware = DeviceUtil.getMacFromHardware();
        Observable.just(macFromHardware)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(s -> {
                    config.setMac(s);
                    saveConfigSync(config);
                }, Throwable::printStackTrace);
        return macFromHardware;
    }

    public interface OnConfigSaveListener {
        void onConfigSave(boolean result, String message);
    }

}
