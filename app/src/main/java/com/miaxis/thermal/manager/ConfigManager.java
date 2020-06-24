package com.miaxis.thermal.manager;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.miaxis.thermal.app.App;
import com.miaxis.thermal.data.entity.Config;
import com.miaxis.thermal.data.model.ConfigModel;
import com.miaxis.thermal.data.net.ThermalApi;
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
                    .forcedMask(ValueUtil.DEFAULT_FORCED_MASK)
                    .strangerRecord(ValueUtil.DEFAULT_STRANGER_RECORD)
                    .deviceMode(ValueUtil.DEFAULT_DEVICE_MODE)
                    .accessSign(ValueUtil.DEFAULT_ACCESS_SIGN)
                    .gateLimit(ValueUtil.DEFAULT_GATE_LIMIT)
                    .verifyScore(ValueUtil.DEFAULT_VERIFY_SCORE)
                    .maskVerifyScore(ValueUtil.DEFAULT_MASK_VERIFY_SCORE)
                    .livenessScore(ValueUtil.DEFAULT_LIVENESS_SCORE)
                    .dormancyInterval(ValueUtil.DEFAULT_DORMANCY_INTERVAL)
                    .dormancyTime(ValueUtil.DEFAULT_DORMANCY_TIME)
                    .feverScore(ValueUtil.DEFAULT_FEVER_SCORE)
                    .tempScore(ValueUtil.DEFAULT_TEMP_SCORE)
                    .heatMap(ValueUtil.DEFAULT_HEAT_MAP)
                    .tempRealTime(ValueUtil.DEFAULT_TEMP_REAL_TIME)
                    .heartBeatInterval(ValueUtil.DEFAULT_HEART_BEAT_INTERVAL)
                    .failedQueryCold(ValueUtil.DEFAULT_FAILED_QUERY_COLD)
                    .recordClearThreshold(ValueUtil.DEFAULT_RECORD_CLEAR_THRESHOLD)
                    .verifyCold(ValueUtil.DEFAULT_VERIFY_COLD)
                    .failedVerifyCold(ValueUtil.DEFAULT_FAILED_VERIFY_COLD)
                    .flashTime(ValueUtil.DEFAULT_FLASH_TIME)
                    .devicePassword(ValueUtil.DEFAULT_DEVICE_PASSWORD)
                    .timeStamp(ValueUtil.DEFAULT_TIME_STAMP)
                    .build();
            if (ValueUtil.DEFAULT_SIGN == Sign.ZH
                    || ValueUtil.DEFAULT_SIGN == Sign.MR890) {
                config.setMac(DeviceUtil.getDeviceId(App.getInstance()));
            } else {
                config.setMac(DeviceUtil.getMacFromHardware());
            }
            if (ValueUtil.DEFAULT_SIGN == Sign.XH
                    || ValueUtil.DEFAULT_SIGN == Sign.XH_N
                    || ValueUtil.DEFAULT_SIGN == Sign.MR890) {
                config.setPupilDistanceMin(ValueUtil.DEFAULT_PUPIL_DISTANCE_MIN_HORIZONTAL);
                config.setPupilDistanceMax(ValueUtil.DEFAULT_PUPIL_DISTANCE_MAX_HORIZONTAL);
            }  else if (ValueUtil.DEFAULT_SIGN == Sign.MR870
                    || ValueUtil.DEFAULT_SIGN == Sign.ZH
                    || ValueUtil.DEFAULT_SIGN == Sign.TPS980P
                    || ValueUtil.DEFAULT_SIGN == Sign.TPS980P_C) {
                config.setPupilDistanceMin(ValueUtil.DEFAULT_PUPIL_DISTANCE_MIN_VERTICAL);
                config.setPupilDistanceMax(ValueUtil.DEFAULT_PUPIL_DISTANCE_MAX_VERTICAL);
            } else if (ValueUtil.DEFAULT_SIGN == Sign.MR870A
                    || ValueUtil.DEFAULT_SIGN == Sign.XH_C) {
                config.setPupilDistanceMin(ValueUtil.DEFAULT_PUPIL_DISTANCE_MIN_NO_LIMIT);
                config.setPupilDistanceMax(ValueUtil.DEFAULT_PUPIL_DISTANCE_MAX_NO_LIMIT);
            }
            ConfigModel.saveConfig(config);
        }
    }

    public void saveConfigSync(@NonNull Config config) {
        ConfigModel.saveConfig(config);
        this.config = config;
        ThermalApi.rebuildRetrofit();
    }

    public void saveConfig(@NonNull Config config, @NonNull OnConfigSaveListener listener) {
        Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            ConfigModel.saveConfig(config);
            this.config = config;
            ThermalApi.rebuildRetrofit();
            emitter.onNext(Boolean.TRUE);
            emitter.onComplete();
        })
                .subscribeOn(Schedulers.from(App.getInstance().getThreadExecutor()))
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
                .subscribeOn(Schedulers.from(App.getInstance().getThreadExecutor()))
                .subscribe(s -> {
                    config.setMac(s);
                    saveConfigSync(config);
                }, Throwable::printStackTrace);
        return macFromHardware;
    }

    public interface OnConfigSaveListener {
        void onConfigSave(boolean result, String message);
    }

    public static boolean isGateDevice() {
        if (ValueUtil.DEFAULT_SIGN == Sign.MR870
                || ValueUtil.DEFAULT_SIGN == Sign.MR890
                || ValueUtil.DEFAULT_SIGN == Sign.XH
                || ValueUtil.DEFAULT_SIGN == Sign.XH_N
                || ValueUtil.DEFAULT_SIGN == Sign.XH_C
                || ValueUtil.DEFAULT_SIGN == Sign.TPS980P
                || ValueUtil.DEFAULT_SIGN == Sign.TPS980P_C) {
            return true;
        }
        return false;
    }

    public static boolean isCardDevice() {
        if (ValueUtil.DEFAULT_SIGN == Sign.ZH
                || ValueUtil.DEFAULT_SIGN == Sign.MR890
                || ValueUtil.DEFAULT_SIGN == Sign.MR870A) {
            return true;
        }
        return false;
    }

    public static boolean isFingerDevice() {
        if (ValueUtil.DEFAULT_SIGN == Sign.MR870A) {
            return true;
        }
        return false;
    }

    public static boolean isLandCameraDevice() {
        if (ValueUtil.DEFAULT_SIGN == Sign.XH
                || ValueUtil.DEFAULT_SIGN == Sign.XH_N
                || ValueUtil.DEFAULT_SIGN == Sign.XH_C
                || ValueUtil.DEFAULT_SIGN == Sign.MR870A) {
            return true;
        } else if (ValueUtil.DEFAULT_SIGN == Sign.MR870
                || ValueUtil.DEFAULT_SIGN == Sign.ZH
                || ValueUtil.DEFAULT_SIGN == Sign.TPS980P
                || ValueUtil.DEFAULT_SIGN == Sign.TPS980P_C
                || ValueUtil.DEFAULT_SIGN == Sign.MR890) {
            return false;
        }
        return false;
    }

    public static boolean isNeedPatternMatcherDevice() {
        if (ValueUtil.DEFAULT_SIGN == Sign.XH
                || ValueUtil.DEFAULT_SIGN == Sign.XH_N
                || ValueUtil.DEFAULT_SIGN == Sign.XH_C
                || ValueUtil.DEFAULT_SIGN == Sign.MR870A) {
            return true;
        } else if (ValueUtil.DEFAULT_SIGN == Sign.MR870
                || ValueUtil.DEFAULT_SIGN == Sign.ZH
                || ValueUtil.DEFAULT_SIGN == Sign.TPS980P
                || ValueUtil.DEFAULT_SIGN == Sign.TPS980P_C
                || ValueUtil.DEFAULT_SIGN == Sign.MR890) {
            return false;
        }
        return false;
    }

    public static boolean isNetworking() {
        Config config = getInstance().getConfig();
        return TextUtils.equals(config.getServerMode(), ValueUtil.WORK_MODE_NET);
    }

}
