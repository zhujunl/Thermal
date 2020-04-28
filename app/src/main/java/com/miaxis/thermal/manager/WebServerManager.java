package com.miaxis.thermal.manager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.miaxis.thermal.app.App;
import com.miaxis.thermal.data.dto.PersonDto;
import com.miaxis.thermal.data.dto.RecordDto;
import com.miaxis.thermal.data.entity.Person;
import com.miaxis.thermal.data.entity.PersonSearch;
import com.miaxis.thermal.data.entity.PhotoFaceFeature;
import com.miaxis.thermal.data.entity.Record;
import com.miaxis.thermal.data.entity.RecordSearch;
import com.miaxis.thermal.data.entity.WebServerRequest;
import com.miaxis.thermal.data.exception.MyException;
import com.miaxis.thermal.data.net.ResponseEntity;
import com.miaxis.thermal.data.repository.PersonRepository;
import com.miaxis.thermal.data.repository.RecordRepository;
import com.miaxis.thermal.util.DateUtil;
import com.miaxis.thermal.util.DeviceUtil;
import com.miaxis.thermal.util.FileUtil;
import com.miaxis.thermal.util.ValueUtil;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.File;
import java.net.InetSocketAddress;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class WebServerManager {

    private WebServerManager() {
    }

    public static WebServerManager getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static final WebServerManager instance = new WebServerManager();
    }

    /**
     * ================================ 静态内部类单例写法 ================================
     **/

    private static final Gson GSON = new Gson();

    private String address;
    private int port = 12580;

    private MyWebSocketServer socketServer;

    public boolean startServer() {
        try {
            socketServer = new MyWebSocketServer(port);
            socketServer.start();
            address = DeviceUtil.getIP(App.getInstance());
            Log.e("asd", "Start ServerSocket Success...");
            return true;
        } catch (Exception e) {
            Log.e("asd", "Start Failed...");
            e.printStackTrace();
            return false;
        }
    }

    public boolean stopServer() {
        try {
            if (socketServer != null) {
                socketServer.stop();
            }
            Log.e("asd", "Stop ServerSocket Success...");
            return true;
        } catch (Exception e) {
            Log.e("asd", "Stop ServerSocket Failed...");
            e.printStackTrace();
            return false;
        }
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    private void handleWebServerRequest(WebSocket conn, String message) {
        try {
            WebServerRequest webServerRequest;
            try {
                webServerRequest = GSON.fromJson(message, WebServerRequest.class);
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                conn.send(GSON.toJson(new ResponseEntity("400", "无法解析请求Json")));
                return;
            }
            String request = webServerRequest.getRequest();
            if (TextUtils.isEmpty(request)) {
                conn.send(GSON.toJson(new ResponseEntity("400", "请求字段为空")));
                return;
            }
            switch (request) {
                case "device/getDeviceMac":
                    getDeviceMac(conn);
                    break;
                case "person/addPerson":
                    addPerson(conn, message);
                    break;
                case "person/updatePerson":
                    updatePerson(conn, message);
                    break;
                case "person/deletePerson":
                    deletePerson(conn, message);
                    break;
                case "person/downloadPerson":
                    downloadPerson(conn, message);
                    break;
                case "person/getPersonCount":
                    getPersonCount(conn);
                    break;
                case "person/clearPerson":
                    clearPerson(conn);
                    break;
                case "record/downloadRecord":
                    downloadRecord(conn, message);
                    break;
                case "record/getRecordCount":
                    getRecordCount(conn);
                    break;
                case "record/clearRecord":
                    clearRecord(conn);
                    break;
                case "record/deleteRecord":
                    deleteRecord(conn, message);
                    break;
                default:
                    conn.send(GSON.toJson(new ResponseEntity("400", "未找到对应请求")));
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            conn.send(GSON.toJson(new ResponseEntity("400", "服务器错误:" + e.getMessage())));
        }
    }

    private void getDeviceMac(WebSocket conn) {
        try {
            String mac = ConfigManager.getInstance().getMacAddress();
            conn.send(GSON.toJson(new ResponseEntity<>("200", "获取设备MAC成功", mac)));
        } catch (Exception e) {
            e.printStackTrace();
            conn.send(GSON.toJson(new ResponseEntity("400", "获取设备MAC时遇到错误:" + e.getMessage())));
        }
    }

    private void addPerson(WebSocket conn, String message) {
        try {
            WebServerRequest<PersonDto> webServerRequest;
            try {
                webServerRequest = GSON.fromJson(message, new TypeToken<WebServerRequest<PersonDto>>() {
                }.getType());
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                throw new MyException("无法解析请求Json");
            }
            PersonDto parameter = webServerRequest.getParameter();
            Person transform = null;
            try {
                transform = parameter.transform();
            } catch (MyException e) {
                e.printStackTrace();
                throw new MyException("数据解析出错-" + e.getMessage());
            }
            if (TextUtils.isEmpty(transform.getName())
                    || TextUtils.isEmpty(transform.getIdentifyNumber())
                    || TextUtils.isEmpty(transform.getPhone())
                    || transform.getEffectiveTime() == null
                    || transform.getInvalidTime() == null
                    || TextUtils.isEmpty(transform.getFacePicturePath())) {
                throw new MyException("请完善人员信息必填项");
            }
            Person findPerson = PersonRepository.getInstance().findPerson(transform.getIdentifyNumber());
            if (findPerson == null) {
                findPerson = PersonRepository.getInstance().findPerson(transform.getPhone());
                if (findPerson != null) {
                    throw new MyException("该手机号码已重复");
                }
            } else {
                throw new MyException("该身份证号码已重复");
            }
            if (TextUtils.isEmpty(transform.getFacePicturePath())) {
                throw new MyException("人员图片不应为空");
            }
            Bitmap bitmap;
            try {
                byte[] decode = Base64.decode(transform.getFacePicturePath(), Base64.NO_WRAP);
                bitmap = BitmapFactory.decodeByteArray(decode, 0, decode.length);
            } catch (Exception e) {
                e.printStackTrace();
                throw new MyException("人员图片Base64解码出错");
            }
            if (bitmap == null) {
                throw new MyException("人员图片解码出错");
            }
            if (TextUtils.isEmpty(transform.getFaceFeature()) || TextUtils.isEmpty(transform.getMaskFaceFeature())) {
                PhotoFaceFeature photoFaceFeature = FaceManager.getInstance().getPhotoFaceFeatureByBitmapForRegisterPosting(bitmap);
                if (photoFaceFeature.getFaceFeature() != null && photoFaceFeature.getMaskFaceFeature() != null) {
                    transform.setFaceFeature(Base64.encodeToString(photoFaceFeature.getFaceFeature(), Base64.NO_WRAP));
                    transform.setMaskFaceFeature(Base64.encodeToString(photoFaceFeature.getMaskFaceFeature(), Base64.NO_WRAP));
                } else {
                    throw new MyException("图片处理失败-" + photoFaceFeature.getMessage());
                }
            }
            String filePath = FileUtil.FACE_STOREHOUSE_PATH + File.separator + transform.getName() + "-" + transform.getIdentifyNumber() + "-" + System.currentTimeMillis() + ".jpg";
            FileUtil.saveQualityBitmap(bitmap, filePath);
            transform.setFacePicturePath(filePath);
            transform.setUpload(false);
            transform.setUpdateTime(new Date());
            transform.setStatus(ValueUtil.PERSON_STATUS_READY);
            PersonRepository.getInstance().savePerson(transform);
            PersonManager.getInstance().loadPersonDataFromCache();
            conn.send(GSON.toJson(new ResponseEntity("200", "新增人员成功")));
            PersonManager.getInstance().startUploadPerson();
        } catch (Exception e) {
            e.printStackTrace();
            conn.send(GSON.toJson(new ResponseEntity("400", "新增人员时遇到错误：" + e.getMessage())));
        }
    }

    private void updatePerson(WebSocket conn, String message) {
        try {
            WebServerRequest<PersonDto> webServerRequest;
            try {
                webServerRequest = GSON.fromJson(message, new TypeToken<WebServerRequest<PersonDto>>() {
                }.getType());
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                throw new MyException("无法解析请求Json");
            }
            PersonDto parameter = webServerRequest.getParameter();
            Person transform = null;
            try {
                transform = parameter.transform();
            } catch (MyException e) {
                e.printStackTrace();
                throw new MyException("数据解析出错-" + e.getMessage());
            }
            if (TextUtils.isEmpty(transform.getIdentifyNumber())) {
                throw new MyException("请包含需要修改人员的证件号码");
            }
            Person findPerson = PersonRepository.getInstance().findPerson(transform.getIdentifyNumber());
            if (findPerson == null) {
                throw new MyException("未找到该人员");
            }
            if (!TextUtils.isEmpty(transform.getType())) {
                findPerson.setType(transform.getType());
            }
            if (transform.getEffectiveTime() != null) {
                findPerson.setEffectiveTime(transform.getEffectiveTime());
            }
            if (transform.getInvalidTime() != null) {
                findPerson.setInvalidTime(transform.getInvalidTime());
            }
            if (!TextUtils.isEmpty(transform.getFaceFeature())) {
                findPerson.setFaceFeature(transform.getFaceFeature());
            }
            if (!TextUtils.isEmpty(transform.getMaskFaceFeature())) {
                findPerson.setMaskFaceFeature(transform.getMaskFaceFeature());
            }
            if (!TextUtils.isEmpty(transform.getFacePicturePath())) {
                Bitmap bitmap;
                try {
                    byte[] decode = Base64.decode(transform.getFacePicturePath(), Base64.NO_WRAP);
                    bitmap = BitmapFactory.decodeByteArray(decode, 0, decode.length);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new MyException("人员图片解码出错");
                }
                String filePath = FileUtil.FACE_STOREHOUSE_PATH + File.separator + findPerson.getName() + "-" + findPerson.getIdentifyNumber() + "-" + System.currentTimeMillis() + ".jpg";
                FileUtil.saveQualityBitmap(bitmap, filePath);
                FileUtil.deleteImg(findPerson.getFacePicturePath());
                findPerson.setFacePicturePath(filePath);
                if (TextUtils.isEmpty(transform.getFaceFeature()) || TextUtils.isEmpty(transform.getMaskFaceFeature())) {
                    PhotoFaceFeature photoFaceFeature = FaceManager.getInstance().getPhotoFaceFeatureByBitmapForRegisterPosting(bitmap);
                    if (photoFaceFeature.getFaceFeature() != null && photoFaceFeature.getMaskFaceFeature() != null) {
                        findPerson.setFaceFeature(Base64.encodeToString(photoFaceFeature.getFaceFeature(), Base64.NO_WRAP));
                        findPerson.setMaskFaceFeature(Base64.encodeToString(photoFaceFeature.getMaskFaceFeature(), Base64.NO_WRAP));
                    } else {
                        throw new MyException("图片处理失败-" + photoFaceFeature.getMessage());
                    }
                }
            }
            findPerson.setUpload(false);
            findPerson.setUpdateTime(new Date());
            PersonRepository.getInstance().savePerson(findPerson);
            PersonManager.getInstance().loadPersonDataFromCache();
            conn.send(GSON.toJson(new ResponseEntity("200", "修改人员成功")));
            PersonManager.getInstance().startUploadPerson();
        } catch (Exception e) {
            e.printStackTrace();
            conn.send(GSON.toJson(new ResponseEntity("400", "修改人员时遇到错误：" + e.getMessage())));
        }
    }

    private void deletePerson(WebSocket conn, String message) {
        try {
            WebServerRequest<String> webServerRequest;
            try {
                webServerRequest = GSON.fromJson(message, new TypeToken<WebServerRequest<String>>() {
                }.getType());
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                throw new MyException("无法解析请求Json");
            }
            String parameter = webServerRequest.getParameter();
            if (TextUtils.isEmpty(parameter)) {
                throw new MyException("参数不应为空");
            }
            Person person = PersonRepository.getInstance().findPerson(parameter);
            if (person == null) {
                throw new MyException("未找到该人员");
            }
            PersonRepository.getInstance().deletePerson(person);
            PersonManager.getInstance().loadPersonDataFromCache();
            conn.send(GSON.toJson(new ResponseEntity("200", "删除成功")));
        } catch (Exception e) {
            e.printStackTrace();
            conn.send(GSON.toJson(new ResponseEntity("400", "删除人员时遇到错误:" + e.getMessage())));
        }
    }

    private void downloadPerson(WebSocket conn, String message) {
        try {
            WebServerRequest<PersonSearch> webServerRequest;
            try {
                webServerRequest = GSON.fromJson(message, new TypeToken<WebServerRequest<PersonSearch>>() {
                }.getType());
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                throw new MyException("无法解析请求Json");
            }
            PersonSearch parameter = webServerRequest.getParameter();
            if (parameter.getPageNum() == 0 || parameter.getPageSize() == 0) {
                throw new MyException("页码和容量不应为0");
            }
            List<Person> personList = PersonRepository.getInstance().searchPerson(parameter);
            List<PersonDto> personDtoList = new ArrayList<>();
            for (Person person : personList) {
                PersonDto personDto = new PersonDto.Builder()
                        .userName(person.getName())
                        .userType(person.getType())
                        .userPhone(person.getPhone())
                        .identifyNumber(person.getIdentifyNumber())
                        .startTime(DateUtil.DATE_FORMAT.format(person.getEffectiveTime()))
                        .invalidTime(DateUtil.DATE_FORMAT.format(person.getInvalidTime()))
                        .facePicture(FileUtil.bitmapToBase64(FileUtil.openImage(person.getFacePicturePath())))
                        .faceFeature(person.getFaceFeature())
                        .maskFaceFeature(person.getMaskFaceFeature())
                        .build();
                personDtoList.add(personDto);
            }
            conn.send(GSON.toJson(new ResponseEntity<>("200", "获取人员成功", personDtoList)));
        } catch (Exception e) {
            e.printStackTrace();
            conn.send(GSON.toJson(new ResponseEntity("400", "获取人员时遇到错误:" + e.getMessage())));
        }
    }

    private void getPersonCount(WebSocket conn) {
        try {
            int count = PersonRepository.getInstance().loadPersonCount();
            conn.send(GSON.toJson(new ResponseEntity<>("200", "获取人员数目成功", count)));
        } catch (Exception e) {
            e.printStackTrace();
            conn.send(GSON.toJson(new ResponseEntity("400", "获取人员数目时遇到错误:" + e.getMessage())));
        }
    }

    private void clearPerson(WebSocket conn) {
        try {
            PersonRepository.getInstance().clearAll();
            conn.send(GSON.toJson(new ResponseEntity("200", "清除人员成功")));
        } catch (Exception e) {
            e.printStackTrace();
            conn.send(GSON.toJson(new ResponseEntity("400", "清除人员时遇到错误:" + e.getMessage())));
        }
    }

    private void downloadRecord(WebSocket conn, String message) {
        try {
            WebServerRequest<RecordSearch> webServerRequest;
            try {
                webServerRequest = GSON.fromJson(message, new TypeToken<WebServerRequest<RecordSearch>>() {
                }.getType());
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                throw new MyException("无法解析请求Json");
            }
            RecordSearch parameter = webServerRequest.getParameter();
            if (parameter.getPageNum() == 0 || parameter.getPageSize() == 0) {
                throw new MyException("页码和容量不应为0");
            }
            List<Record> recordList = RecordRepository.getInstance().searchRecord(parameter);
            List<RecordDto> recordDtoList = new ArrayList<>();
            for (Record record : recordList) {
                RecordDto build = new RecordDto.Builder()
                        .identifyNumber(record.getIdentifyNumber())
                        .userName(record.getName())
                        .userPhone(record.getPhone())
                        .verifyTime(DateUtil.DATE_FORMAT.format(record.getVerifyTime()))
                        .score(record.getScore())
                        .temperature(record.getTemperature())
                        .type(record.getType())
                        .faceType(record.getFaceType())
                        .verifyImage(FileUtil.bitmapToBase64(FileUtil.openImage(record.getVerifyPicturePath())))
                        .build();
                recordDtoList.add(build);
            }
            conn.send(GSON.toJson(new ResponseEntity<>("200", "获取日志成功", recordDtoList)));
        } catch (Exception e) {
            e.printStackTrace();
            conn.send(GSON.toJson(new ResponseEntity("400", "获取日志时遇到错误:" + e.getMessage())));
        }
    }

    private void getRecordCount(WebSocket conn) {
        try {
            int count = RecordRepository.getInstance().loadRecordCount();
            conn.send(GSON.toJson(new ResponseEntity<>("200", "获取日志数目成功", count)));
        } catch (Exception e) {
            e.printStackTrace();
            conn.send(GSON.toJson(new ResponseEntity("400", "获取日志数目时遇到错误:" + e.getMessage())));
        }
    }

    private void clearRecord(WebSocket conn) {
        try {
            RecordRepository.getInstance().clearAll();
            conn.send(GSON.toJson(new ResponseEntity("200", "清除日志成功")));
        } catch (Exception e) {
            e.printStackTrace();
            conn.send(GSON.toJson(new ResponseEntity("400", "清除日志时遇到错误:" + e.getMessage())));
        }
    }

    private void deleteRecord(WebSocket conn, String message) {
        try {
            WebServerRequest<RecordSearch> webServerRequest;
            try {
                webServerRequest = GSON.fromJson(message, new TypeToken<WebServerRequest<RecordSearch>>() {
                }.getType());
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                throw new MyException("无法解析请求Json");
            }
            RecordSearch parameter = webServerRequest.getParameter();
            if (TextUtils.isEmpty(parameter.getStartTime()) || TextUtils.isEmpty(parameter.getEndTime())) {
                throw new MyException("开始时间和结束时间不能为空");
            }
            Date startTime;
            Date endTime;
            try {
                startTime = DateUtil.DATE_FORMAT.parse(parameter.getStartTime());
                endTime = DateUtil.DATE_FORMAT.parse(parameter.getEndTime());
            } catch (Exception e) {
                e.printStackTrace();
                throw new MyException("时间字符串解析失败-" + e.getMessage());
            }
            List<Record> recordList = RecordRepository.getInstance().searchRecordInTime(startTime, endTime);
            RecordRepository.getInstance().deleteRecordList(recordList);
            conn.send(GSON.toJson(new ResponseEntity("200", "删除日志成功")));
        } catch (Exception e) {
            e.printStackTrace();
            conn.send(GSON.toJson(new ResponseEntity("400", "获取日志时遇到错误:" + e.getMessage())));
        }
    }

    private class MyWebSocketServer extends WebSocketServer {

        public MyWebSocketServer(int port) {
            super(new InetSocketAddress(port));
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            Log.e("asd", "onOpen");
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            Log.e("asd", "onClose");
        }

        @Override
        public void onMessage(WebSocket conn, String message) {
            handleWebServerRequest(conn, message);
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
            Log.e("asd", "onError：" + ex.getMessage());
            port++;
            if (("" + ex.getMessage()).contains("Address already in use")) {
                stopServer();
                startServer();
            }
        }

        @Override
        public void onStart() {
            Log.e("asd", "onStart");
        }
    }

}
