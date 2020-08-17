package com.miaxis.thermal.data.repository;

import android.text.TextUtils;

import com.miaxis.thermal.data.dto.RecordDto;
import com.miaxis.thermal.data.entity.Config;
import com.miaxis.thermal.data.entity.Record;
import com.miaxis.thermal.data.entity.RecordSearch;
import com.miaxis.thermal.data.exception.MyException;
import com.miaxis.thermal.data.exception.NetResultFailedException;
import com.miaxis.thermal.data.model.RecordModel;
import com.miaxis.thermal.data.net.ResponseEntity;
import com.miaxis.thermal.data.net.ThermalApi;
import com.miaxis.thermal.manager.ConfigManager;
import com.miaxis.thermal.util.DateUtil;
import com.miaxis.thermal.util.FileUtil;
import com.miaxis.thermal.util.ValueUtil;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import retrofit2.Response;

public class RecordRepository {

    private RecordRepository() {
    }

    public static RecordRepository getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static final RecordRepository instance = new RecordRepository();
    }

    /**
     * ================================ 静态内部类单例写法 ================================
     **/

    public void uploadRecord(Record record) throws IOException, MyException, NetResultFailedException {
        Config config = ConfigManager.getInstance().getConfig();
        String url = config.getHost() + config.getUploadRecordPath();
        String mac = ConfigManager.getInstance().getMacAddress();
        File file = null;
        try {
            file = new File(record.getVerifyPicturePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Response<ResponseEntity> execute = ThermalApi.uploadRecord(url,
                record.getIdentifyNumber(),
                record.getName(),
                record.getPhone(),
                DateUtil.DATE_FORMAT.format(record.getVerifyTime()),
                record.getScore() * 100,
                record.getTemperature(),
                record.getType(),
                record.getFaceType(),
                mac,
                record.getAccess(),
                file)
                .execute();
        try {
            ResponseEntity body = execute.body();
            if (body != null) {
                if (TextUtils.equals(body.getCode(), ValueUtil.SUCCESS)) {
                    return;
                } else if (!TextUtils.equals(body.getCode(), ValueUtil.SUCCESS)) {
                    throw new NetResultFailedException("服务端返回，" + body.getMessage());
                }
            }
        } catch (NetResultFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new MyException(e.getMessage());
        }
        throw new MyException("服务端返回数据解析失败，或为空");
    }

    public void saveRecord(Record record) {
        RecordModel.saveRecord(record);
    }

    public List<Record> loadRecordByPage(int pageNum, int pageSize) {
        return RecordModel.loadRecordByPage(pageNum, pageSize);
    }

    public int loadRecordCount() {
        return RecordModel.loadRecordCount();
    }

    public Record findOldestRecord() {
        return RecordModel.findOldestRecord();
    }

    public void clearAll() {
        RecordModel.deleteAll();
        FileUtil.deleteDirectory(new File(FileUtil.FACE_IMAGE_PATH));
    }

    public List<Record> searchRecord(RecordSearch recordSearch) {
        return RecordModel.searchRecord(recordSearch, false);
    }

    public int searchRecordCount(RecordSearch recordSearch) {
        return RecordModel.searchRecordCount(recordSearch);
    }

    public void deleteRecordList(List<Record> recordList) {
        for (Record record : recordList) {
            FileUtil.deleteImg(record.getVerifyPicturePath());
        }
        RecordModel.deleteRecordList(recordList);
    }

    public List<Record> searchRecordInTime(Date startTime, Date endTime) {
        return RecordModel.searchRecordInTime(startTime, endTime);
    }

}
