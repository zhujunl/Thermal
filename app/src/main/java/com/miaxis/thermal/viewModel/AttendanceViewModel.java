package com.miaxis.thermal.viewModel;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.miaxis.thermal.R;
import com.miaxis.thermal.app.App;
import com.miaxis.thermal.bridge.SingleLiveEvent;
import com.miaxis.thermal.data.entity.Config;
import com.miaxis.thermal.data.entity.FaceDraw;
import com.miaxis.thermal.data.entity.IDCardMessage;
import com.miaxis.thermal.data.entity.MatchPerson;
import com.miaxis.thermal.data.entity.MxRGBImage;
import com.miaxis.thermal.data.entity.Person;
import com.miaxis.thermal.data.entity.PhotoFaceFeature;
import com.miaxis.thermal.data.exception.MyException;
import com.miaxis.thermal.data.repository.PersonRepository;
import com.miaxis.thermal.manager.CardManager;
import com.miaxis.thermal.manager.ConfigManager;
import com.miaxis.thermal.manager.FaceManager;
import com.miaxis.thermal.manager.FingerManager;
import com.miaxis.thermal.manager.GpioManager;
import com.miaxis.thermal.manager.HeartBeatManager;
import com.miaxis.thermal.manager.HumanSensorManager;
import com.miaxis.thermal.manager.PersonManager;
import com.miaxis.thermal.manager.RecordManager;
import com.miaxis.thermal.manager.TTSManager;
import com.miaxis.thermal.manager.TemperatureManager;
import com.miaxis.thermal.manager.ToastManager;
import com.miaxis.thermal.manager.WatchDogManager;
import com.miaxis.thermal.manager.strategy.Sign;
import com.miaxis.thermal.manager.strategy.xhn.XhnTempForward;
import com.miaxis.thermal.util.DateUtil;
import com.miaxis.thermal.util.ValueUtil;

import org.zz.api.MXFaceInfoEx;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class AttendanceViewModel extends BaseViewModel {

    private static final int MSG_VERIFY_LOCK = 0x50;

    public ObservableField<String> date = new ObservableField<>();
    public ObservableField<String> time = new ObservableField<>();
    public ObservableField<String> week = new ObservableField<>();
    public ObservableField<String> weather = new ObservableField<>();
    public ObservableField<String> hint = new ObservableField<>();
    public ObservableField<String> temperature = new ObservableField<>();
    public ObservableField<String> countDown = new ObservableField<>();
    public MutableLiveData<FaceDraw> faceDraw = new MutableLiveData<>();

    public MutableLiveData<Boolean> updateHeader = new SingleLiveEvent<>();
    public MutableLiveData<Boolean> fever = new SingleLiveEvent<>();
    public MutableLiveData<Boolean> faceDormancy = new SingleLiveEvent<>();
    public MutableLiveData<Boolean> heatMapUpdate = new SingleLiveEvent<>();

    public MutableLiveData<Boolean> initCard = new SingleLiveEvent<>();
    public MutableLiveData<Boolean> cardStatus = new SingleLiveEvent<>();

    public MutableLiveData<Boolean> initFinger = new SingleLiveEvent<>();
    public MutableLiveData<Boolean> fingerStatus = new SingleLiveEvent<>();

    public MutableLiveData<Boolean> humanDetect = new SingleLiveEvent<>();

    public Bitmap headerCache;
    private IDCardMessage idCardMessage;
    public Bitmap heatMapCache;

    private Handler handler;

    private volatile boolean lock = false;
    private volatile boolean cardMode = false;
    private volatile boolean fingerVerify = true;

    private AtomicInteger cardDelay = new AtomicInteger(6);

    public AttendanceViewModel() {
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == MSG_VERIFY_LOCK) {
                    detectColdDown();
                }
            }
        };
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        handler.removeMessages(MSG_VERIFY_LOCK);
        handler.removeCallbacks(cardVerifyDelayRunnable);
    }

    public void startFaceDetect() {
        hint.set("");
        temperature.set("");
        countDown.set("");
        TemperatureManager.getInstance().open();
        PersonManager.getInstance().loadPersonDataFromCache();
        FaceManager.getInstance().setFaceHandleListener(faceHandleListener);
        FaceManager.getInstance().setDormancyListener(dormancyListener);
        FaceManager.getInstance().startLoop();
        WatchDogManager.getInstance().startFaceFeedDog();
        if (ConfigManager.isCardDevice()) {
            initCard.setValue(Boolean.TRUE);
        }
        if (ConfigManager.isFingerDevice()) {
            initFinger.setValue(Boolean.TRUE);
        }
    }

    public void stopFaceDetect() {
        WatchDogManager.getInstance().stopFaceFeedDog();
        TemperatureManager.getInstance().close();
        FaceManager.getInstance().setFaceHandleListener(null);
        FaceManager.getInstance().setDormancyListener(null);
        FaceManager.getInstance().stopLoop();
        handler.removeMessages(MSG_VERIFY_LOCK);
    }

    private FaceManager.OnFaceHandleListener faceHandleListener = new FaceManager.OnFaceHandleListener() {
        @Override
        public void onFeatureExtract(MxRGBImage mxRGBImage, MXFaceInfoEx mxFaceInfoEx, byte[] feature, boolean mask) {
            hint.set("测量温度中");
            TemperatureManager.getInstance().readTemperature(new TemperatureManager.TemperatureListener() {
                @Override
                public void onTemperature(float temperature) {
                    if (temperature == -2f) {
                        toast.postValue(ToastManager.getToastBody("读温错误", ToastManager.INFO));
                    }
                    if (temperature == -1f) {
                        hint.set("检测到人脸");
                    }
                    Log.e("asd", "温度" + temperature);
                    Config config = ConfigManager.getInstance().getConfig();
                    if (temperature < config.getTempScore() && temperature != -1f) {
                        hint.set("");
                        FaceManager.getInstance().setNeedNextFeature(true);
                        return;
                    }
                    if (config.isTempRealTime()) {
                        if (temperature == 0f || temperature == -1f || temperature == -2f) {
                            AttendanceViewModel.this.temperature.set("");
                        } else {
                            AttendanceViewModel.this.temperature.set(temperature + "°C");
                        }
                    }
                    if (ValueUtil.DEFAULT_SIGN == Sign.XH_N) {
                        XhnTempForward.getInstance().forward(temperature);
                    }
                    matchPerson(mxRGBImage, mxFaceInfoEx, feature, mask, temperature);
                }

                @Override
                public void onHeatMap(Bitmap bitmap) {
                    heatMapCache = bitmap;
                    Config config = ConfigManager.getInstance().getConfig();
                    if (!lock && config.isTempRealTime() && config.isHeatMap()) {
                        AttendanceViewModel.this.heatMapUpdate.postValue(Boolean.TRUE);
                    } else if (lock && config.isHeatMap()) {
                        AttendanceViewModel.this.heatMapUpdate.postValue(Boolean.TRUE);
                    }
                }
            });
        }

        @Override
        public void onFaceDetect(int faceNum, MXFaceInfoEx[] faceInfoExes) {
            faceDraw.postValue(new FaceDraw(faceNum, faceInfoExes));
            if (!lock && faceNum == 0) {
                hint.set("");
                temperature.set("");
                if (heatMapCache != null) {
                    heatMapCache = null;
                    heatMapUpdate.postValue(Boolean.TRUE);
                }
            }
        }

        @Override
        public void onFaceIntercept(int code, String message) {
            if (!lock) {
//                hint.set(message);
                switch (code) {
                    case -1:
                        hint.set(getString(R.string.please_face_the_screen));
                        break;
                    case -2:
                        hint.set(getString(R.string.please_get_close_to_the_screen));
                        break;
                    case -6:
                        hint.set(getString(R.string.please_stay_away_from_the_screen));
                        break;
                    case -4:
                        hint.set(getString(R.string.live_detection_failed));
                        break;
                }
            }
        }
    };

    private void matchPerson(MxRGBImage mxRGBImage, MXFaceInfoEx mxFaceInfoEx, byte[] feature, boolean mask, float temperature) {
        if (cardMode) {
            onCardVerify(mxRGBImage, mxFaceInfoEx, temperature, feature, mask);
        } else {
            PersonManager.getInstance().handleFeature(feature, mask, new PersonManager.OnPersonMatchResultListener() {
                @Override
                public void onMatchFailed() {
                    Config config = ConfigManager.getInstance().getConfig();
                    if (config.isStrangerRecord()) {
                        detectCold(false);
                        showHeader(mxRGBImage, mxFaceInfoEx);
                        showTemperature(temperature);
                        showMatchFailedHint(temperature, mask);
                        decideOpenGate(false, temperature, mask);
                        RecordManager.getInstance().handlerStrangerRecord(mxRGBImage, temperature);
                    } else {
                        hint.set("未找到人员");
                        FaceManager.getInstance().setNeedNextFeature(true);
                    }
                    HeartBeatManager.getInstance().heartBeatLimitBurst();
                }

                @Override
                public void onMatchSuccess(MatchPerson matchPerson, PersonManager.Overdue overdue) {
                    boolean attendanceSuccess = isAttendanceSuccess(temperature, overdue, mask);
                    detectCold(true);
                    showHeader(mxRGBImage, mxFaceInfoEx);
                    showTemperature(temperature);
                    showMatchSuccessHint(temperature, mask, matchPerson.getPerson(), overdue);
                    HeartBeatManager.getInstance().relieveLimit();
                    decideOpenGate(attendanceSuccess, temperature, mask);
                    RecordManager.getInstance().handlerFaceRecord(matchPerson.getPerson(), mxRGBImage, matchPerson.getScore(), temperature, true);
                }
            });
        }
    }

    private boolean isAttendanceSuccess(float temperature, PersonManager.Overdue overdue, boolean mask) {
        Config config = ConfigManager.getInstance().getConfig();
        if (temperature >= config.getFeverScore()) {
            return false;
        } else {
            if (overdue == PersonManager.Overdue.effective) {
                if (!config.isForcedMask() || mask) {
                    return true;
                }
            }
            return false;
        }
    }

    private void showTemperature(float temperature) {
        if (temperature > 0) {
            AttendanceViewModel.this.temperature.set(temperature + "°C");
            Config config = ConfigManager.getInstance().getConfig();
            if (config.isHeatMap()) {
                heatMapUpdate.postValue(Boolean.TRUE);
            }
        } else {
            AttendanceViewModel.this.temperature.set("");
        }
    }

    private void showMatchSuccessHint(float temperature, boolean mask, Person person, PersonManager.Overdue overdue) {
        Config config = ConfigManager.getInstance().getConfig();
        String voice;
        if (temperature >= config.getFeverScore()) {//判定优先级最高：发热异常
            GpioManager.getInstance().openRedLed();
            fever.postValue(Boolean.TRUE);
            hint.set(person.getName() + "-体温异常");
            voice = "体温异常";
        } else {
            if (overdue == PersonManager.Overdue.effective) {
                GpioManager.getInstance().openGreenLed();
                if (!config.isForcedMask() || mask) {
                    hint.set(person.getName() + "-" + (ConfigManager.isGateDevice() && !config.isDeviceMode() ? getString(R.string.open_gate_success) : getString(R.string.attendance_success)));
                    voice = (ConfigManager.isGateDevice() && !config.isDeviceMode() ? "开门成功" : "考勤成功")
                            + (temperature == -1f ? "" : "，体温正常");
                } else {
                    hint.set(person.getName() + "-" + getString(R.string.please_put_on_mask));
                    voice = "请戴口罩" + (temperature == -1f ? "" : "，体温正常");
                }
            } else {
                hint.set(person.getName() + (overdue == PersonManager.Overdue.expired ? "-已过期" : "-未生效"));
                voice = (overdue == PersonManager.Overdue.expired ? "已过期" : "未生效") + (temperature == -1f ? "" : "，体温正常");
            }
        }
        TTSManager.getInstance().playVoiceMessageFlush(voice);
    }

    private void showMatchFailedHint(float temperature, boolean mask) {
        Config config = ConfigManager.getInstance().getConfig();
        String voice;
        if (temperature >= config.getFeverScore()) {
            GpioManager.getInstance().openRedLed();
            fever.postValue(Boolean.TRUE);
            hint.set("访客人员-温度异常");
            voice = "体温异常";
        } else {
            if (!config.isForcedMask() || mask) {
                hint.set("访客人员-检测成功");
                voice = temperature > 0 ? "检测成功，体温正常" : "检测成功";
            } else {
                hint.set("访客人员-" + getString(R.string.please_put_on_mask));
                voice = temperature > 0 ? "请戴口罩，体温正常" : "请戴口罩";
            }
        }
        TTSManager.getInstance().playVoiceMessageFlush(voice);
    }

    private void decideOpenGate(boolean attendance, float temperature, boolean mask) {
        Config config = ConfigManager.getInstance().getConfig();
        if (ConfigManager.isGateDevice() && !config.isDeviceMode()) {
            if ((attendance || !config.isGateLimit())
                    && (mask || !config.isForcedMask())
                    && temperature < config.getFeverScore()) {
                Log.e("asd", "开门啦啦啦啦啦啦啦啦");
                GpioManager.getInstance().openDoorForGate();
            }
        }
    }

    private void detectCold(boolean result) {
        lock = true;
        CardManager.getInstance().needNextRead(false);
        handler.removeMessages(MSG_VERIFY_LOCK);
        Message message = handler.obtainMessage(MSG_VERIFY_LOCK);
        handler.sendMessageDelayed(message, result
                ? ConfigManager.getInstance().getConfig().getVerifyCold() * 1000
                : ConfigManager.getInstance().getConfig().getFailedVerifyCold() * 1000);
    }

    private void detectColdDown() {
        hint.set("");
        temperature.set("");
        countDown.set("");
        headerCache = null;
        updateHeader.postValue(Boolean.TRUE);
        idCardMessage = null;
        fever.postValue(Boolean.FALSE);
        heatMapCache = null;
        heatMapUpdate.postValue(Boolean.TRUE);
        lock = false;
        cardMode = false;
        fingerVerify = true;
        FaceManager.getInstance().setNeedNextFeature(true);
        if (ConfigManager.isCardDevice()) {
            CardManager.getInstance().needNextRead(true);
        }
    }

    private void showHeader(MxRGBImage mxRGBImage, MXFaceInfoEx mxFaceInfoEx) {
        if (mxRGBImage == null || mxFaceInfoEx == null) return;
        App.getInstance().getThreadExecutor().execute(() -> {
            try {
                headerCache = FaceManager.getInstance().tailoringFace(mxRGBImage, mxFaceInfoEx);
            } catch (Exception e) {
                e.printStackTrace();
                headerCache = null;
            } finally {
                updateHeader.postValue(Boolean.TRUE);
            }
        });
    }

    private FaceManager.OnDormancyListener dormancyListener = dormancy -> {
        faceDormancy.postValue(dormancy);
    };

    private CardManager.OnCardReadListener cardListener = idCardMessage -> {
        if (idCardMessage != null) {
            if (lock) return;
            //如果已经在人证核验模式下，直接返回
            if (cardMode) return;
            Observable.create((ObservableOnSubscribe<PhotoFaceFeature>) emitter -> {
                PhotoFaceFeature cardFaceFeature = FaceManager.getInstance().getCardFaceFeatureByBitmapPosting(idCardMessage.getCardBitmap());
                fingerVerify = !(!TextUtils.isEmpty(idCardMessage.getFingerprint0()) && !TextUtils.isEmpty(idCardMessage.getFingerprint1()));
                emitter.onNext(cardFaceFeature);
                emitter.onComplete();
            })
                    .subscribeOn(Schedulers.from(App.getInstance().getThreadExecutor()))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(cardFaceFeature -> {
                        if (cardFaceFeature.getFaceFeature() != null || cardFaceFeature.getMaskFaceFeature() != null) {
                            if (ConfigManager.getInstance().getConfig().isIdCardVerify()) {
                                onIDCardMessage(idCardMessage, cardFaceFeature.getFaceFeature(), cardFaceFeature.getMaskFaceFeature());
                            } else {
                                onIDCardMessageNoVerify(idCardMessage, cardFaceFeature.getFaceFeature(), cardFaceFeature.getMaskFaceFeature());
                            }
                        } else {
                            toast.setValue(ToastManager.getToastBody("证件照片处理失败", ToastManager.ERROR));
                            CardManager.getInstance().needNextRead(true);
                        }
                    }, throwable -> {
                        throwable.printStackTrace();
                        Log.e("asd", "证件照片处理失败");
                        toast.setValue(ToastManager.getToastBody("证件照片处理失败", ToastManager.ERROR));
                        CardManager.getInstance().needNextRead(true);
                    });
        }
    };

    public CardManager.OnCardStatusListener statusListener = result -> {
        if (result) {
            cardStatus.postValue(Boolean.TRUE);
            CardManager.getInstance().startReadCard(cardListener);
        } else {
            cardStatus.postValue(Boolean.FALSE);
            CardManager.getInstance().release();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                initCard.setValue(Boolean.TRUE);
            }, 10 * 1000);
        }
    };

    private void onIDCardMessage(IDCardMessage mIdCardMessage, byte[] faceFeature, byte[] maskFaceFeature) {
        TTSManager.getInstance().stop();
        idCardMessage = mIdCardMessage;
        idCardMessage.setCardFeature(faceFeature);
        idCardMessage.setMaskCardFeature(maskFaceFeature);
        handler.removeMessages(MSG_VERIFY_LOCK);
        lock = true;
        cardMode = true;
        hint.set(idCardMessage.getName() + "-请看镜头");
        temperature.set("");
        handler.removeCallbacks(cardVerifyDelayRunnable);
        cardDelay.set(6);
        handler.post(cardVerifyDelayRunnable);
        headerCache = idCardMessage.getCardBitmap();
        updateHeader.setValue(Boolean.TRUE);
        FaceManager.getInstance().setNeedNextFeature(true);
    }

    private void onIDCardMessageNoVerify(IDCardMessage mIdCardMessage, byte[] faceFeature, byte[] maskFaceFeature) {
        TTSManager.getInstance().stop();
        idCardMessage = mIdCardMessage;
        idCardMessage.setCardFeature(faceFeature);
        idCardMessage.setMaskCardFeature(maskFaceFeature);
        handler.removeMessages(MSG_VERIFY_LOCK);
        detectCold(true);
        cardMode = true;
        temperature.set("");
        handler.removeCallbacks(cardVerifyDelayRunnable);
        headerCache = idCardMessage.getCardBitmap();
        updateHeader.setValue(Boolean.TRUE);
        Config config = ConfigManager.getInstance().getConfig();
        handler.removeCallbacks(cardVerifyDelayRunnable);
        countDown.set("");
        hint.set(idCardMessage.getName() + (config.isDeviceMode() ? "-考勤成功" : "-开门成功"));
        TTSManager.getInstance().playVoiceMessageFlush("比对成功");
        if (ConfigManager.isGateDevice()) {
            GpioManager.getInstance().openDoorForGate();
        }
        RecordManager.getInstance().handlerIDCardRecordNoVerify(idCardMessage, -1f, true);
        if (config.isIdCardEntry()) {
            onIdCardEntry(idCardMessage);
        }
    }

    private void onCardVerify(MxRGBImage mxRGBImage, MXFaceInfoEx mxFaceInfoEx, float temperature, byte[] feature, boolean mask) {
        if (idCardMessage == null) {
            cardMode = false;
            return;
        }
        if (!ConfigManager.getInstance().getConfig().isIdCardVerify()) {
            return;
        }
        Observable.create((ObservableOnSubscribe<Float>) emitter -> {
            float faceMatchScore = FaceManager.getInstance().matchFeature(feature, idCardMessage.getCardFeature());
            float maskFaceMatchScore = FaceManager.getInstance().matchMaskFeature(feature, idCardMessage.getMaskCardFeature());
            Log.e("asd", "score      " + faceMatchScore + "     " + maskFaceMatchScore);
            emitter.onNext(Math.max(faceMatchScore, maskFaceMatchScore));
            emitter.onComplete();
        })
                .subscribeOn(Schedulers.from(App.getInstance().getThreadExecutor()))
                .observeOn(Schedulers.from(App.getInstance().getThreadExecutor()))
                .subscribe(score -> {
                    Config config = ConfigManager.getInstance().getConfig();
                    if (score >= config.getVerifyScore()) {
                        handler.removeCallbacks(cardVerifyDelayRunnable);
                        countDown.set("");
                        if (!config.isForcedMask() || mask) {
                            hint.set(idCardMessage.getName() + "-比对成功");
                            TTSManager.getInstance().playVoiceMessageFlush("比对成功");
                        } else {
                            hint.set(idCardMessage.getName() + "-请戴口罩");
                            TTSManager.getInstance().playVoiceMessageFlush("请戴口罩");
                        }
                        if (temperature > 0) {
                            this.temperature.set(temperature + "°C");
                        }
                        showHeader(mxRGBImage, mxFaceInfoEx);
                        detectCold(true);
                        if (ConfigManager.isGateDevice()) {
                            if (!config.isForcedMask() || mask) {
                                GpioManager.getInstance().openDoorForGate();
                            }
                        }
                        RecordManager.getInstance().handlerIDCardRecord(idCardMessage, mxRGBImage, score, -1f, true);
                        if (config.isIdCardEntry()) {
                            onIdCardEntry(idCardMessage);
                        }
                    } else {
                        hint.set(idCardMessage.getName() + "-比对失败");
                        FaceManager.getInstance().setNeedNextFeature(true);
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                    Log.e("asd", "" + throwable.getMessage());
                    FaceManager.getInstance().setNeedNextFeature(true);
                });
    }

    private void onIdCardEntry(IDCardMessage idCardMessage) {
        App.getInstance().getThreadExecutor().execute(() -> {
            PersonManager.getInstance().savePersonFromIdCard(idCardMessage);
        });
    }

    private Runnable cardVerifyDelayRunnable = new Runnable() {
        @Override
        public void run() {
            cardDelay.decrementAndGet();
            countDown.set(cardDelay + "S");
            if (cardDelay.get() == 0) {
                if (ConfigManager.isFingerDevice()) {
                    if (fingerVerify) {
                        detectColdDown();
                    } else {
                        fingerVerify = true;
                        verifyFinger();
                    }
                } else {
                    detectColdDown();
                }
            } else {
                handler.postDelayed(cardVerifyDelayRunnable, 1000);
            }
        }
    };

    private void verifyFinger() {
        hint.set(idCardMessage.getName() + "-请按手指");
        FingerManager.getInstance().readFinger(fingerReadListener);
        cardDelay.set(5);
        countDown.set(cardDelay + "S");
        handler.postDelayed(cardVerifyDelayRunnable, 1000);
    }

    public FingerManager.OnFingerStatusListener fingerStatusListener = result -> {
        if (result) {
            fingerStatus.postValue(Boolean.TRUE);
        } else {
            fingerStatus.postValue(Boolean.FALSE);
            FingerManager.getInstance().release();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                initFinger.setValue(Boolean.TRUE);
            }, 10 * 1000);
        }
    };

    private FingerManager.OnFingerReadListener fingerReadListener = (feature, image) -> {
        try {
            if (idCardMessage == null) {
                cardMode = false;
                detectColdDown();
                return;
            }
            if (feature == null) {
                handler.removeCallbacks(cardVerifyDelayRunnable);
                countDown.set("");
                hint.set(idCardMessage.getName() + "-探测失败");
                detectCold(true);
                return;
            }
            if (!TextUtils.isEmpty(idCardMessage.getFingerprint0()) && !TextUtils.isEmpty(idCardMessage.getFingerprint1())) {
                boolean result0 = FingerManager.getInstance().matchFeature(feature, Base64.decode(idCardMessage.getFingerprint0(), Base64.NO_WRAP));
                boolean result1 = FingerManager.getInstance().matchFeature(feature, Base64.decode(idCardMessage.getFingerprint1(), Base64.NO_WRAP));
                handler.removeCallbacks(cardVerifyDelayRunnable);
                countDown.set("");
                if (result0 || result1) {
                    hint.set(idCardMessage.getName() + "-比对成功");
                    TTSManager.getInstance().playVoiceMessageFlush("比对成功");
                    if (ConfigManager.isGateDevice()) {
                        GpioManager.getInstance().openDoorForGate();
                    }
                } else {
                    hint.set(idCardMessage.getName() + "-比对失败");
                }
                detectCold(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            detectColdDown();
        }
    };

    public void startHumanDetect() {
        HumanSensorManager.getInstance().setStatusListener(humanSensorStatusListener);
        HumanSensorManager.getInstance().startHumanDetect();
    }

    public void stopHumanDetect() {
        HumanSensorManager.getInstance().stopHumanDetect();
    }

    public HumanSensorManager.OnHumanSensorStatusListener humanSensorStatusListener = status -> {
        humanDetect.postValue(status);
    };

}
