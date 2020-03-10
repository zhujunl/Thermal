package com.miaxis.thermal.manager;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bumptech.glide.request.FutureTarget;
import com.miaxis.thermal.app.App;
import com.miaxis.thermal.bridge.GlideApp;
import com.miaxis.thermal.data.entity.Config;
import com.miaxis.thermal.data.entity.MatchPerson;
import com.miaxis.thermal.data.entity.Person;
import com.miaxis.thermal.data.entity.PhotoFaceFeature;
import com.miaxis.thermal.data.exception.MyException;
import com.miaxis.thermal.data.repository.PersonRepository;
import com.miaxis.thermal.util.FileUtil;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

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

//    private static ExecutorService executorService = Executors.newFixedThreadPool(20);

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

    public void uploadRecord() {
        handler.removeMessages(0);
        Observable.create((ObservableOnSubscribe<Person>) emitter -> {
            uploading = true;
            Person person = PersonRepository.getInstance().findOldestRecord();
            if (person != null) {
                emitter.onNext(person);
            } else {
                emitter.onError(new MyException("未找到待上传人员"));
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnNext(person -> {
                    PersonRepository.getInstance().updatePerson(person);
                })
                .subscribe(person -> {
                    Log.e("asd", "上传人员成功");
                    person.setUpload(true);
                    PersonRepository.getInstance().updatePerson(person);
                    handler.sendMessage(handler.obtainMessage(0));
                }, throwable -> {
                    throwable.printStackTrace();
                    Log.e("asd", "" + throwable.getMessage());
                    uploading = false;
                    handler.sendMessageDelayed(handler.obtainMessage(0), 60 * 60 * 1000);
                });
    }

    public void startUploadPerson() {
        if (uploading) return;
        handler.sendMessage(handler.obtainMessage(0));
    }

    public List<Person> getPersonList() {
        return personList;
    }

    public synchronized void loadPersonDataFromCache() {
        Observable.create((ObservableOnSubscribe<List<Person>>) emitter -> {
            List<Person> personList = PersonRepository.getInstance().loadAll();
            if (personList != null) {
                emitter.onNext(personList);
            } else {
                emitter.onError(new MyException("查询结果为空"));
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(personList -> {
                    this.personList = Collections.synchronizedList(personList);
                }, throwable -> {
                    throwable.printStackTrace();
                    Log.e("asd", "" + throwable.getMessage());
                });
    }

    public void handlePersonHeartBeat(Person person) {
        try {
            Bitmap bitmap = null;
            try {
                bitmap = downloadPicture(person.getFacePicturePath());
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            if ((TextUtils.isEmpty(person.getFaceFeature()) || TextUtils.isEmpty(person.getMaskFaceFeature())) && bitmap != null) {
                PhotoFaceFeature photoFaceFeature = FaceManager.getInstance().getPhotoFaceFeatureByBitmapForRegisterPosting(bitmap);
                if (photoFaceFeature.getFaceFeature() != null && photoFaceFeature.getMaskFaceFeature() != null) {
                    person.setFaceFeature(Base64.encodeToString(photoFaceFeature.getFaceFeature(), Base64.NO_WRAP));
                    person.setMaskFaceFeature(Base64.encodeToString(photoFaceFeature.getMaskFaceFeature(), Base64.NO_WRAP));
                } else {
                    person.setRemarks("图片处理失败：" + photoFaceFeature.getMessage());
                }
            }
            if (bitmap != null) {
                String fileName = person.getName() + "-" + person.getIdentifyNumber() + "-" + System.currentTimeMillis() + ".png";
                String facePicturePath = FileUtil.FACE_STOREHOUSE_PATH + File.separator + fileName;
                FileUtil.saveBitmap(bitmap, FileUtil.FACE_STOREHOUSE_PATH, fileName);
                person.setFacePicturePath(facePicturePath);
            } else {
                person.setRemarks("图片下载失败");
            }
            person.setUpdateTime(new Date());
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

    private Bitmap downloadPicture(String url) throws ExecutionException, InterruptedException {
        if (TextUtils.isEmpty(url)) return null;
        FutureTarget<Bitmap> futureTarget = GlideApp.with(App.getInstance().getApplicationContext())
                .asBitmap()
                .load(url)
                .submit();
        return futureTarget.get();
    }

    public void handleFeature(byte[] feature, boolean mask, @NonNull OnPersonMatchResultListener listener) {
        Observable.create((ObservableOnSubscribe<MatchPerson>) emitter -> {
            MatchPerson matchPerson = getMatchPerson(feature, mask);
            if (matchPerson != null) {
                emitter.onNext(matchPerson);
            } else {
                emitter.onError(new MyException("未找到匹配人员"));
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(matchPerson -> {
                    Person person = matchPerson.getPerson();
                    Date now = new Date();
                    if (now.after(person.getEffectiveTime())) {
                        if (now.before(person.getInvalidTime())) {
                            listener.onMatchSuccess(matchPerson);
                        } else {
                            listener.onMatchOutOfRange(true, person);
                        }
                    } else {
                        listener.onMatchOutOfRange(false, person);
                    }
                }, throwable -> {
                    listener.onMatchFailed();
                    throwable.printStackTrace();
                    Log.e("asd", "" + throwable.getMessage());
                });
    }

    private MatchPerson getMatchPerson(byte[] feature, boolean mask) {
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
        void onMatchSuccess(MatchPerson matchPerson);
        void onMatchOutOfRange(boolean overdue, Person person);
    }

}
