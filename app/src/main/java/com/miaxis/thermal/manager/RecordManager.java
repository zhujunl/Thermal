package com.miaxis.thermal.manager;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.miaxis.thermal.app.App;
import com.miaxis.thermal.data.entity.IDCardMessage;
import com.miaxis.thermal.data.entity.MxRGBImage;
import com.miaxis.thermal.data.entity.Person;
import com.miaxis.thermal.data.entity.Record;
import com.miaxis.thermal.data.exception.MyException;
import com.miaxis.thermal.data.exception.NetResultFailedException;
import com.miaxis.thermal.data.repository.RecordRepository;
import com.miaxis.thermal.util.DateUtil;
import com.miaxis.thermal.util.FileUtil;
import com.miaxis.thermal.util.ValueUtil;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

//    private ExecutorService threadExecutor = Executors.newSingleThreadExecutor();

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

    private void uploadRecord() {
        try {
            uploading = true;
            handler.removeMessages(0);
            RecordRepository.getInstance().clearRecordWithThreshold();
            Record record = RecordRepository.getInstance().findOldestRecord();
            if (record == null) throw new MyException("未找到待上传日志");
            RecordRepository.getInstance().uploadRecord(record);
            Log.e("asd", "日志上传成功");
            record.setUpload(true);
            RecordRepository.getInstance().saveRecord(record);
            uploading = false;
            handler.sendMessage(handler.obtainMessage(0));
        } catch (Exception e) {
            Log.e("asd", "UploadRecord" + e.getMessage());
            handler.sendMessageDelayed(handler.obtainMessage(0), 30 * 60 * 1000);
        } finally {
            uploading = false;
        }
    }

    private void startUploadRecord() {
        if (uploading) return;
        handler.removeMessages(0);
        handler.sendMessage(handler.obtainMessage(0));
    }

    public void handlerIDCardRecordNoVerify(IDCardMessage idCardMessage, float temperature, boolean attendance) {
        App.getInstance().getThreadExecutor().execute(() -> {
            try {
                String filePath = FileUtil.FACE_IMAGE_PATH + File.separator + idCardMessage.getName() + "-" + idCardMessage.getCardNumber() + "-" + System.currentTimeMillis() + ".jpg";
                FileUtil.saveBitmap(idCardMessage.getCardBitmap(), filePath);
                Record record = makeIDCardRecord(idCardMessage, -1, filePath, temperature);
                record.setAttendance(attendance ? "1" : "0");
                RecordRepository.getInstance().saveRecord(record);
                startUploadRecord();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("asd", "保存日志失败，handlerRecord抛出信息：" + e.getMessage());
            }
        });
    }

    public void handlerIDCardRecord(IDCardMessage idCardMessage, MxRGBImage mxRGBImage, float score, float temperature, boolean attendance) {
        App.getInstance().getThreadExecutor().execute(() -> {
            try {
                String filePath = FileUtil.FACE_IMAGE_PATH + File.separator + idCardMessage.getName() + "-" + idCardMessage.getCardNumber() + "-" + System.currentTimeMillis() + ".jpg";
                byte[] fileImage = FaceManager.getInstance().imageEncode(mxRGBImage.getRgbImage(), mxRGBImage.getWidth(), mxRGBImage.getHeight());
                Bitmap bitmap = BitmapFactory.decodeByteArray(fileImage, 0, fileImage.length);
                FileUtil.saveBitmap(bitmap, filePath);
                bitmap.recycle();
                Record record = makeIDCardRecord(idCardMessage, score, filePath, temperature);
                record.setAttendance(attendance ? "1" : "0");
                RecordRepository.getInstance().saveRecord(record);
                startUploadRecord();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("asd", "保存日志失败，handlerRecord抛出信息：" + e.getMessage());
            }
        });
    }

    public void handlerFaceRecord(Person person, MxRGBImage mxRGBImage, float score, float temperature, boolean attendance) {
        App.getInstance().getThreadExecutor().execute(() -> {
            try {
                String filePath = FileUtil.FACE_IMAGE_PATH + File.separator + person.getName() + "-" + person.getIdentifyNumber() + "-" + System.currentTimeMillis() + ".jpg";
                byte[] fileImage = FaceManager.getInstance().imageEncode(mxRGBImage.getRgbImage(), mxRGBImage.getWidth(), mxRGBImage.getHeight());
                Bitmap bitmap = BitmapFactory.decodeByteArray(fileImage, 0, fileImage.length);
                FileUtil.saveBitmap(bitmap, filePath);
                bitmap.recycle();
                Record record = makeFaceRecord(person, score, filePath, temperature);
                record.setAttendance(attendance ? "1" : "0");
                RecordRepository.getInstance().saveRecord(record);
                startUploadRecord();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("asd", "保存日志失败，handlerRecord抛出信息：" + e.getMessage());
            }
        });
    }

    public void handlerStrangerRecord(MxRGBImage mxRGBImage, float temperature) {
        App.getInstance().getThreadExecutor().execute(() -> {
            try {
                String filePath = FileUtil.FACE_IMAGE_PATH + File.separator + "Stranger-" + System.currentTimeMillis() + ".jpg";
                byte[] fileImage = FaceManager.getInstance().imageEncode(mxRGBImage.getRgbImage(), mxRGBImage.getWidth(), mxRGBImage.getHeight());
                Bitmap bitmap = BitmapFactory.decodeByteArray(fileImage, 0, fileImage.length);
                FileUtil.saveBitmap(bitmap, filePath);
                bitmap.recycle();
                Record record = makeStrangerRecord(filePath, temperature);
                RecordRepository.getInstance().saveRecord(record);
                startUploadRecord();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("asd", "保存日志失败，handlerRecord抛出信息：" + e.getMessage());
            }
        });
    }

    private Record makeIDCardRecord(IDCardMessage idCardMessage, float score, String facePicture, float temperature) {
        return new Record.Builder()
                .personId(0L)
                .identifyNumber(idCardMessage.getCardNumber())
                .phone(idCardMessage.getCardNumber())
                .name(idCardMessage.getName())
                .type(ValueUtil.PERSON_TYPE_VISITOR)
                .verifyTime(new Date())
                .verifyPicturePath(facePicture)
                .score(score)
                .upload(false)
                .temperature(temperature)
                .access(ConfigManager.getInstance().getConfig().isAccessSign() ? "0" : "1")
                .build();
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
                .access(ConfigManager.getInstance().getConfig().isAccessSign() ? "0" : "1")
                .build();
    }

    private Record makeStrangerRecord(String facePicture, float temperature) {
        return new Record.Builder()
                .name("比对人员")
                .identifyNumber("-1")
                .phone("-1")
                .verifyTime(new Date())
                .verifyPicturePath(facePicture)
                .upload(false)
                .temperature(temperature)
                .attendance("0")
                .access(ConfigManager.getInstance().getConfig().isAccessSign() ? "0" : "1")
                .build();
    }

}
