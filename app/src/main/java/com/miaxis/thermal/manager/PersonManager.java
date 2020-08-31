package com.miaxis.thermal.manager;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.FutureTarget;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.miaxis.thermal.app.App;
import com.miaxis.thermal.bridge.GlideApp;
import com.miaxis.thermal.data.entity.Config;
import com.miaxis.thermal.data.entity.IDCardMessage;
import com.miaxis.thermal.data.entity.MatchPerson;
import com.miaxis.thermal.data.entity.Person;
import com.miaxis.thermal.data.entity.PhotoFaceFeature;
import com.miaxis.thermal.data.exception.MyException;
import com.miaxis.thermal.data.exception.NetResultFailedException;
import com.miaxis.thermal.data.repository.PersonRepository;
import com.miaxis.thermal.util.DateUtil;
import com.miaxis.thermal.util.FileUtil;
import com.miaxis.thermal.util.ValueUtil;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class PersonManager {

    private PersonManager() {
    }

    public static PersonManager getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static final PersonManager instance = new PersonManager();
    }

    /**
     * ================================ 静态内部类单例写法 ================================
     **/

    private ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("PERSON_UPLOAD-%d").build();
    private ExecutorService threadExecutor = Executors.newSingleThreadExecutor(namedThreadFactory);

    private List<Person> personList;

    private HandlerThread handlerThread;
    private Handler handler;

    private volatile boolean uploading = false;

    public void init() {
        loadPersonDataFromCache();
        handlerThread = new HandlerThread("UploadPerson");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                uploadRecord();
            }
        };
        handler.sendMessage(handler.obtainMessage(0));
    }

    private void uploadRecord() {
        try {
            uploading = true;
            handler.removeMessages(0);
            PersonRepository.getInstance().clearOverduePerson();
            Person person = PersonRepository.getInstance().findOldestRecord();
            if (person == null) throw new MyException("未找到待上传人员");
            PersonRepository.getInstance().updatePerson(person);
            Log.e("asd", "人员上传成功");
            person.setUpload(true);
            PersonRepository.getInstance().savePerson(person);
            HeartBeatManager.getInstance().forcedHeartBeat();
            handler.sendMessage(handler.obtainMessage(0));
        } catch (Exception e) {
            Log.e("asd", "" + e.getMessage());
            handler.sendMessageDelayed(handler.obtainMessage(0), 60 * 60 * 1000);
        } finally {
            uploading = false;
        }
    }

    public void startUploadPerson() {
        if (uploading) return;
        handler.removeMessages(0);
        handler.sendMessage(handler.obtainMessage(0));
    }

    public List<Person> getPersonList() {
        return personList;
    }

    public synchronized void loadPersonDataFromCache() {
        Observable.create((ObservableOnSubscribe<List<Person>>) emitter -> {
            List<Person> personList = PersonRepository.getInstance().loadUsability();
            if (personList != null) {
                emitter.onNext(personList);
                emitter.onComplete();
            } else {
                emitter.onError(new MyException("查询结果为空"));
            }
        })
                .subscribeOn(Schedulers.from(threadExecutor))
                .subscribe(personList -> {
                    this.personList = Collections.synchronizedList(personList);
                }, throwable -> {
                    throwable.printStackTrace();
                    Log.e("asd", "" + throwable.getMessage());
                });
    }

    public void handlePersonHeartBeat(Person person) {
        if (person != null) {
            if (TextUtils.equals(person.getStatus(), ValueUtil.PERSON_STATUS_DELETE)) {
                Log.e("asd", "同步删除人员");
                handleDeletePerson(person);
            } else {
                Log.e("asd", "同步新增人员");
                handleSavePerson(person);
            }
        }
    }

    private void handleDeletePerson(Person person) {
        try {
            if (!TextUtils.isEmpty(person.getIdentifyNumber())) {
                Person findPerson = PersonRepository.getInstance().findPerson(person.getIdentifyNumber());
                if (findPerson != null) {
                    PersonRepository.getInstance().deletePerson(findPerson);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Config config = ConfigManager.getInstance().getConfig();
            long timeStamp = person.getTimeStamp();
            if (timeStamp != 0 && timeStamp > config.getTimeStamp()) {
                config.setTimeStamp(timeStamp);
                ConfigManager.getInstance().saveConfigSync(config);
            }
        }
    }

    private void handleSavePerson(Person person) {
        try {
            Bitmap bitmap = null;
            try {
                bitmap = downloadPicture(person.getFacePicturePath());
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            if (bitmap != null) {
                String filePath = FileUtil.FACE_STOREHOUSE_PATH + File.separator + person.getName() + "-" + person.getIdentifyNumber() + "-" + System.currentTimeMillis() + ".jpg";
                FileUtil.saveQualityBitmap(bitmap, filePath);
                person.setFacePicturePath(filePath);
                PhotoFaceFeature photoFaceFeature = FaceManager.getInstance().getPhotoFaceFeatureByBitmapForRegisterPosting(bitmap);
                if (photoFaceFeature.getFaceFeature() != null && photoFaceFeature.getMaskFaceFeature() != null) {
                    person.setFaceFeature(Base64.encodeToString(photoFaceFeature.getFaceFeature(), Base64.NO_WRAP));
                    person.setMaskFaceFeature(Base64.encodeToString(photoFaceFeature.getMaskFaceFeature(), Base64.NO_WRAP));
                } else {
                    person.setFaceFeature(null);
                    person.setMaskFaceFeature(null);
                    person.setRemarks("图片处理失败，" + photoFaceFeature.getMessage());
                }
            } else {
                person.setFacePicturePath("");
                person.setRemarks("图片下载失败");
            }
            if (person.getEffectiveTime() == null) {
                person.setEffectiveTime(new Date());
            }
            person.setUpdateTime(new Date());
            person.setStatus(ValueUtil.PERSON_STATUS_READY);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            PersonRepository.getInstance().savePerson(person);
            Config config = ConfigManager.getInstance().getConfig();
            long timeStamp = person.getTimeStamp();
            if (timeStamp != 0 && timeStamp > config.getTimeStamp()) {
                config.setTimeStamp(timeStamp);
                ConfigManager.getInstance().saveConfigSync(config);
            }
        }
    }

    public void savePersonFromIdCard(IDCardMessage idCardMessage) {
        if (idCardMessage.getCardFeature() == null || idCardMessage.getMaskCardFeature() == null) {
            return;
        }
        try {
            Person findPerson = PersonRepository.getInstance().findPerson(idCardMessage.getCardNumber());
            if (findPerson == null) {
                String filePath = FileUtil.FACE_STOREHOUSE_PATH + File.separator + idCardMessage.getName() + "-" + idCardMessage.getCardNumber() + "-" + System.currentTimeMillis() + ".jpg";
                FileUtil.saveQualityBitmap(idCardMessage.getCardBitmap(), filePath);
                Person person = new Person.Builder()
                        .identifyNumber(idCardMessage.getCardNumber())
                        .phone(ValueUtil.getRandomString(10) + System.currentTimeMillis())
                        .name(idCardMessage.getName())
                        .type(ValueUtil.PERSON_TYPE_VISITOR)
                        .effectiveTime(new Date())
                        .invalidTime(DateUtil.getNextYear())
                        .updateTime(new Date())
                        .faceFeature(Base64.encodeToString(idCardMessage.getCardFeature(), Base64.NO_WRAP))
                        .maskFaceFeature(Base64.encodeToString(idCardMessage.getMaskCardFeature(), Base64.NO_WRAP))
                        .facePicturePath(filePath)
                        .timeStamp(0)
                        .remarks("")
                        .upload(false)
                        .status(ValueUtil.PERSON_STATUS_READY)
                        .build();
                PersonRepository.getInstance().savePerson(person);
                loadPersonDataFromCache();
                startUploadPerson();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap downloadPicture(String url) throws ExecutionException, InterruptedException {
        if (TextUtils.isEmpty(url)) return null;
        FutureTarget<Bitmap> futureTarget = GlideApp.with(App.getInstance().getApplicationContext())
                .asBitmap()
                .load(url + "?" + System.currentTimeMillis())
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .submit();
        return futureTarget.get();
    }

    public void handleFeature(byte[] feature, boolean mask, @NonNull OnPersonMatchResultListener listener) {
        Observable.create((ObservableOnSubscribe<MatchPerson>) emitter -> {
            MatchPerson matchPerson = getMatchPerson(feature, mask);
            if (matchPerson != null) {
                emitter.onNext(matchPerson);
                emitter.onComplete();
            } else {
                emitter.onError(new MyException("未找到匹配人员"));
            }
        })
                .subscribe(matchPerson -> {
                    Person person = matchPerson.getPerson();
                    Date now = new Date();
                    if (now.after(person.getEffectiveTime())) {
                        if (now.before(person.getInvalidTime())) {
                            listener.onMatchSuccess(matchPerson, Overdue.effective);
                        } else {
                            listener.onMatchSuccess(matchPerson, Overdue.expired);
                        }
                    } else {
                        listener.onMatchSuccess(matchPerson, Overdue.ineffective);
                    }
                }, throwable -> {
                    listener.onMatchFailed();
                    throwable.printStackTrace();
                    Log.e("asd", "" + throwable.getMessage());
                });
    }

    public MatchPerson getMatchPerson(byte[] feature, boolean mask) {
        float maxScore = 0f;
        Person bestMatchPerson = null;
        for (Person person : PersonManager.getInstance().getPersonList()) {
            float score = -1f;
            if (!TextUtils.isEmpty(person.getFaceFeature()) && !mask) {
                try {
                    score = FaceManager.getInstance().matchFeature(Base64.decode(person.getFaceFeature(), Base64.NO_WRAP), feature);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (!TextUtils.isEmpty(person.getMaskFaceFeature()) && mask) {
                try {
                    score = FaceManager.getInstance().matchMaskFeature(Base64.decode(person.getMaskFaceFeature(), Base64.NO_WRAP), feature);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (score > maxScore) {
                maxScore = score;
                bestMatchPerson = person;
            }
        }
        Log.e("asd", "比对最高分数：" + maxScore);
        if (!mask && maxScore > ConfigManager.getInstance().getConfig().getVerifyScore() && bestMatchPerson != null) {
            return new MatchPerson(bestMatchPerson, maxScore, mask);
        } else if (mask && maxScore > ConfigManager.getInstance().getConfig().getMaskVerifyScore() && bestMatchPerson != null) {
            return new MatchPerson(bestMatchPerson, maxScore, mask);
        } else {
            return null;
        }
    }

    public interface OnPersonMatchResultListener {
        void onMatchFailed();

        void onMatchSuccess(MatchPerson matchPerson, Overdue overdue);
    }

    public enum Overdue {
        ineffective, effective, expired
    }

}
