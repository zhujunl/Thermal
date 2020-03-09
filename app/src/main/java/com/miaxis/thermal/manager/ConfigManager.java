package com.miaxis.thermal.manager;

import androidx.annotation.NonNull;

import com.miaxis.thermal.data.entity.Config;
import com.miaxis.thermal.data.model.ConfigModel;
import com.miaxis.thermal.util.DeviceUtil;
import com.miaxis.thermal.util.ValueUtil;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
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
                    .mac(DeviceUtil.getMacFromHardware())
                    .showCamera(ValueUtil.DEFAULT_CAMERA_SHOW)
                    .faceCamera(ValueUtil.DEFAULT_CAMERA_FACE)
                    .liveness(ValueUtil.DEFAULT_LIVENESS)
                    .qualityScore(ValueUtil.DEFAULT_QUALITY_SCORE)
                    .verifyScore(ValueUtil.DEFAULT_VERIFY_SCORE)
                    .livenessScore(ValueUtil.DEFAULT_LIVENESS_SCORE)
                    .pupilDistance(ValueUtil.DEFAULT_PUPIL_DISTANCE)
                    .heartBeatInterval(ValueUtil.DEFAULT_HEART_BEAT_INTERVAL)
                    .failedQueryCold(ValueUtil.DEFAULT_FAILED_QUERY_COLD)
                    .recordClearThreshold(ValueUtil.DEFAULT_RECORD_CLEAR_THRESHOLD)
                    .verifyCold(ValueUtil.DEFAULT_VERIFY_COLD)
                    .flashTime(ValueUtil.DEFAULT_FLASH_TIME)
                    .devicePassword(ValueUtil.DEFAULT_DEVICE_PASSWORD)
                    .timeStamp(ValueUtil.DEFAULT_TIME_STAMP)
                    .build();
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

    public interface OnConfigSaveListener {
        void onConfigSave(boolean result, String message);
    }

}
