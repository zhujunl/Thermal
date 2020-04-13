package com.miaxis.thermal.manager;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.miaxis.thermal.data.entity.MxRGBImage;
import com.miaxis.thermal.data.entity.Person;
import com.miaxis.thermal.data.entity.Record;
import com.miaxis.thermal.data.exception.MyException;
import com.miaxis.thermal.data.repository.RecordRepository;
import com.miaxis.thermal.util.DateUtil;
import com.miaxis.thermal.util.FileUtil;

import java.io.File;
import java.util.Date;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class RecordManager {

    private RecordManager() {
    }

    public static RecordManager getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static final RecordManager instance = new RecordManager();
    }

    /**
     * ================================ 静态内部类单例写法 ================================
     **/

    private HandlerThread handlerThread;
    private Handler handler;

    private volatile boolean uploading = false;

    public void init() {
        handlerThread = new HandlerThread("UploadRecord");
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
        Observable.create((ObservableOnSubscribe<Record>) emitter -> {
            uploading = true;
            Record record = RecordRepository.getInstance().findOldestRecord();
            if (record != null) {
                emitter.onNext(record);
            } else {
                emitter.onError(new MyException("未找到待上传日志"));
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnNext(record -> {
                    RecordRepository.getInstance().uploadRecord(record);
                })
                .subscribe(record -> {
                    Log.e("asd", "上传日志成功");
                    record.setUpload(true);
                    RecordRepository.getInstance().saveRecord(record);
                    handler.sendMessage(handler.obtainMessage(0));
                }, throwable -> {
                    throwable.printStackTrace();
                    Log.e("asd", "" + throwable.getMessage());
                    uploading = false;
                    handler.sendMessageDelayed(handler.obtainMessage(0), 60 * 60 * 1000);
                });
    }

    public void startUploadRecord() {
        if (uploading) return;
        handler.sendMessage(handler.obtainMessage(0));
    }

    public void handlerFaceRecord(Person person, MxRGBImage mxRGBImage, float score, float temperature) {
        Observable.create((ObservableOnSubscribe<Record>) emitter -> {
            String filePath = FileUtil.FACE_IMAGE_PATH + File.separator + person.getName() + "-" + person.getIdentifyNumber() + "-" + System.currentTimeMillis() + ".jpg";
            byte[] fileImage = FaceManager.getInstance().imageEncode(mxRGBImage.getRgbImage(), mxRGBImage.getWidth(), mxRGBImage.getHeight());
            Bitmap bitmap = BitmapFactory.decodeByteArray(fileImage, 0, fileImage.length);
            FileUtil.saveBitmap(bitmap, filePath);
            Record record = makeFaceRecord(person, score, filePath, temperature);
            emitter.onNext(record);
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnNext(record -> RecordRepository.getInstance().saveRecord(record))
                .subscribe(record -> {
                    startUploadRecord();
                }, throwable -> {
                    throwable.printStackTrace();
                    Log.e("asd", "保存日志失败，handlerRecord抛出信息：" + throwable.getMessage());
                });
    }

    private Record makeFaceRecord(Person person, float score, String facePicture, float temperature) {
        return new Record.Builder()
                .personId(person.getId())
                .identifyNumber(person.getIdentifyNumber())
                .phone(person.getPhone())
                .name(person.getName())
                .type(person.getType())
                .verifyTime(new Date())
                .verifyPicturePath(facePicture)
                .score(score)
                .upload(false)
                .temperature(temperature)
                .build();
    }

}
