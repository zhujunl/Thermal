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
import com.miaxis.thermal.bridge.SingleLiveEvent;
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
import com.miaxis.thermal.manager.GpioManager;
import com.miaxis.thermal.manager.HeartBeatManager;
import com.miaxis.thermal.manager.PersonManager;
import com.miaxis.thermal.manager.RecordManager;
import com.miaxis.thermal.manager.TTSManager;
import com.miaxis.thermal.manager.TemperatureManager;
import com.miaxis.thermal.manager.ToastManager;
import com.miaxis.thermal.manager.WatchDogManager;
import com.miaxis.thermal.manager.strategy.Sign;
import com.miaxis.thermal.util.DateUtil;
import com.miaxis.thermal.util.ValueUtil;

import org.zz.api.MXFaceInfoEx;

import java.util.Date;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class AttendanceViewModel extends BaseViewModel {

    private static final int MSG_VERIFY_LOCK = 0x50;

    public ObservableField<String> dateStr = new ObservableField<>();
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
    public MutableLiveData<Boolean> initCard = new SingleLiveEvent<>();

    public Bitmap headerCache;
    private IDCardMessage idCardMessage;

    private Handler handler;

    private volatile boolean lock = false;
    private volatile boolean cardMode = false;

    private int cardDelay = 6;

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
        FaceManager.getInstance().startLoop();
        WatchDogManager.getInstance().startFaceFeedDog();
        if (ValueUtil.DEFAULT_SIGN == Sign.ZH) {
            CardManager.getInstance().setListener(cardReadListener);
            initCard.setValue(Boolean.TRUE);
        }
    }

    public void stopFaceDetect() {
        WatchDogManager.getInstance().stopFaceFeedDog();
        TemperatureManager.getInstance().close();
        FaceManager.getInstance().setFaceHandleListener(null);
        FaceManager.getInstance().stopLoop();
        if (ValueUtil.DEFAULT_SIGN == Sign.ZH) {
            CardManager.getInstance().release();
        }
        handler.removeMessages(MSG_VERIFY_LOCK);
    }

    private FaceManager.OnFaceHandleListener faceHandleListener = new FaceManager.OnFaceHandleListener() {
        @Override
        public void onFeatureExtract(MxRGBImage mxRGBImage, MXFaceInfoEx mxFaceInfoEx,  byte[] feature, boolean mask) {
            hint.set("检测到人脸");
            float temperature = TemperatureManager.getInstance().readTemperature();
            if (!lock) {
                if (temperature == 0f) {
                    AttendanceViewModel.this.temperature.set("");
                } else {
                    AttendanceViewModel.this.temperature.set(temperature + "°C");
                }
            }
            Log.e("asd", "温度" + temperature);
            if (temperature < 36.0f) return;
            if (cardMode) {
                onCardVerify(mxRGBImage, mxFaceInfoEx, temperature, feature);
            } else {
                PersonManager.getInstance().handleFeature(feature, mask, new PersonManager.OnPersonMatchResultListener() {
                    @Override
                    public void onMatchFailed() {
                        personMatchFailed();
                    }

                    @Override
                    public void onMatchSuccess(MatchPerson matchPerson) {
                        if (temperature < ConfigManager.getInstance().getConfig().getFeverScore()) {
                            personMatchSuccess(mxRGBImage, mxFaceInfoEx, matchPerson.getPerson(), matchPerson.getScore(), temperature);
                        } else {
                            personMatchSuccessButFever(mxRGBImage, mxFaceInfoEx, matchPerson.getPerson(), matchPerson.getScore(), temperature);
                        }
                    }

                    @Override
                    public void onMatchOutOfRange(boolean overdue, Person person) {
                        personMatchOutOfRange(mxRGBImage, mxFaceInfoEx, overdue, person);
                    }
                });
            }
        }

        @Override
        public void onFaceDetect(int faceNum, MXFaceInfoEx[] faceInfoExes) {
            faceDraw.postValue(new FaceDraw(faceNum, faceInfoExes));
            if (!lock && faceNum == 0) {
                hint.set("");
            }
        }

        @Override
        public void onFaceIntercept(int code, String message) {
            if (!lock) {
//                hint.set(message);
                switch (code) {
                    case -1:
                        hint.set("请正对屏幕");
                        break;
                    case -2:
                        hint.set("请靠近屏幕");
                        break;
                    case -4:
                        hint.set("活体检测失败");
                        break;
                    case -5:
                        hint.set("检测到人脸");
                        break;
                }
            }
        }
    };

    private void personMatchSuccess(MxRGBImage mxRGBImage, MXFaceInfoEx mxFaceInfoEx, Person person, float score, float temperature) {
        detectCold();
        hint.set(person.getName() + "-考勤成功");
        GpioManager.getInstance().openGreenLed();
        if (temperature > 0) {
            this.temperature.set(temperature + "°C");
            TTSManager.getInstance().playVoiceMessageFlush("考勤成功，体温正常");
        } else {
            this.temperature.set("");
            TTSManager.getInstance().playVoiceMessageFlush("考勤成功");
        }
        showHeader(mxRGBImage, mxFaceInfoEx);
        HeartBeatManager.getInstance().relieveLimit();
        RecordManager.getInstance().handlerFaceRecord(person, mxRGBImage, score, temperature);
    }

    private void personMatchSuccessButFever(MxRGBImage mxRGBImage, MXFaceInfoEx mxFaceInfoEx, Person person, float score, float temperature) {
        detectCold();
        fever.setValue(Boolean.TRUE);
        GpioManager.getInstance().openRedLed();
        hint.set(person.getName() + "-体温异常");
        this.temperature.set(temperature + "°C");
        TTSManager.getInstance().playVoiceMessageFlush("体温异常");
        showHeader(mxRGBImage, mxFaceInfoEx);
        RecordManager.getInstance().handlerFaceRecord(person, mxRGBImage, score, temperature);
    }

    private void personMatchOutOfRange(MxRGBImage mxRGBImage, MXFaceInfoEx mxFaceInfoEx, boolean overdue, Person person) {
        detectCold();
        hint.set(person.getName() + (overdue ? "-已过期" : "-未生效"));
        showHeader(mxRGBImage, mxFaceInfoEx);
    }

    private void personMatchFailed() {
        hint.set("检测到人脸");
        FaceManager.getInstance().setNeedNextFeature(true);
        HeartBeatManager.getInstance().heartBeatLimitBurst();
    }

    private void detectCold() {
        lock = true;
        handler.removeMessages(MSG_VERIFY_LOCK);
        Message message = handler.obtainMessage(MSG_VERIFY_LOCK);
        handler.sendMessageDelayed(message, ConfigManager.getInstance().getConfig().getVerifyCold() * 1000);
    }

    private void detectColdDown() {
        hint.set("");
        temperature.set("");
        countDown.set("");
        headerCache = null;
        updateHeader.setValue(Boolean.TRUE);
        idCardMessage = null;
        fever.setValue(Boolean.FALSE);
        lock = false;
        cardMode = false;
        FaceManager.getInstance().setNeedNextFeature(true);
        CardManager.getInstance().setNeedReadCard(true);
    }

    private void showHeader(MxRGBImage mxRGBImage, MXFaceInfoEx mxFaceInfoEx) {
        if (mxRGBImage == null || mxFaceInfoEx == null) return;
        Observable.create((ObservableOnSubscribe<Bitmap>) emitter -> {
            Bitmap bitmap = FaceManager.getInstance().tailoringFace(mxRGBImage, mxFaceInfoEx);
            if (bitmap != null) {
                emitter.onNext(bitmap);
            } else {
                emitter.onError(new MyException("裁剪图片失败"));
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmap -> {
                    headerCache = bitmap;
                    updateHeader.setValue(Boolean.TRUE);
                }, throwable -> {
                    headerCache = null;
                    updateHeader.setValue(Boolean.TRUE);
                    Log.e("asd", "照片裁剪失败");
                });
    }

    private CardManager.OnCardReadListener cardReadListener = new CardManager.OnCardReadListener() {
        @Override
        public void onDeviceStatus(boolean status) {
            if (status) {
                CardManager.getInstance().startReadCard();
            } else {
                CardManager.getInstance().release();
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    initCard.setValue(Boolean.TRUE);
                }, 5 * 1000);
            }
        }

        @Override
        public void onCardRead(IDCardMessage idCardMessage) {
            if (idCardMessage != null) {
                //如果已经在人证核验模式下，直接返回
                if (cardMode) return;
                Observable.create((ObservableOnSubscribe<PhotoFaceFeature>) emitter -> {
//                    byte[] feature = FaceManager.getInstance().getCardFeatureByBitmapPosting(idCardMessage.getCardBitmap());
                    PhotoFaceFeature cardFaceFeature = FaceManager.getInstance().getCardFaceFeatureByBitmapPosting(idCardMessage.getCardBitmap());
                    emitter.onNext(cardFaceFeature);
                })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(cardFaceFeature -> {
                            if (cardFaceFeature.getFaceFeature() != null || cardFaceFeature.getMaskFaceFeature() != null) {
                                onIDCardMessage(idCardMessage, cardFaceFeature.getFaceFeature(), cardFaceFeature.getMaskFaceFeature());
                            } else {
                                toast.setValue(ToastManager.getToastBody("证件照片处理失败", ToastManager.ERROR));
                                CardManager.getInstance().setNeedReadCard(true);
                            }
                        }, throwable -> {
                            throwable.printStackTrace();
                            Log.e("asd", "证件照片处理失败");
                            toast.setValue(ToastManager.getToastBody("证件照片处理失败", ToastManager.ERROR));
                            CardManager.getInstance().setNeedReadCard(true);
                        });
            } else {

            }
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
        cardDelay = 6;
        handler.post(cardVerifyDelayRunnable);
        headerCache = idCardMessage.getCardBitmap();
        updateHeader.setValue(Boolean.TRUE);
        FaceManager.getInstance().setNeedNextFeature(true);
    }

    private void onCardVerify(MxRGBImage mxRGBImage, MXFaceInfoEx mxFaceInfoEx, float temperature, byte[] feature) {
        if (idCardMessage == null) {
            cardMode = false;
            return;
        }
        Observable.create((ObservableOnSubscribe<Float>) emitter -> {
            float faceMatchScore = FaceManager.getInstance().matchFeature(feature, idCardMessage.getCardFeature());
            float maskFaceMatchScore = FaceManager.getInstance().matchFeature(feature, idCardMessage.getMaskCardFeature());
            emitter.onNext(Math.max(faceMatchScore, maskFaceMatchScore));
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(score -> {
                    if (score >= ConfigManager.getInstance().getConfig().getVerifyScore()) {
                        handler.removeCallbacks(cardVerifyDelayRunnable);
                        countDown.set("");
                        hint.set(idCardMessage.getName() + "-比对成功");
                        TTSManager.getInstance().playVoiceMessageFlush("比对成功");
                        if (temperature > 0) {
                            this.temperature.set(temperature + "°C");
                        }
                        showHeader(mxRGBImage, mxFaceInfoEx);
                        detectCold();
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

    private Runnable cardVerifyDelayRunnable = new Runnable() {
        @Override
        public void run() {
            cardDelay--;
            countDown.set(cardDelay + "S");
            if (cardDelay == 0) {
                detectColdDown();
            } else {
                handler.postDelayed(cardVerifyDelayRunnable, 1000);
            }
        }
    };

}
