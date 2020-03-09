package com.miaxis.thermal.manager;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.miaxis.thermal.app.App;
import com.miaxis.thermal.data.dto.PersonDto;
import com.miaxis.thermal.data.entity.Person;
import com.miaxis.thermal.data.entity.PersonSearch;
import com.miaxis.thermal.data.entity.Record;
import com.miaxis.thermal.data.entity.RecordSearch;
import com.miaxis.thermal.data.entity.WebServerRequest;
import com.miaxis.thermal.data.exception.MyException;
import com.miaxis.thermal.data.net.ResponseEntity;
import com.miaxis.thermal.data.repository.PersonRepository;
import com.miaxis.thermal.data.repository.RecordRepository;
import com.miaxis.thermal.util.DeviceUtil;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
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

    private OnServerStatusListener listener;

    public interface OnServerStatusListener {
        void onServerStatus(boolean status, String message);
    }

    public boolean startServer(@NonNull OnServerStatusListener listener) {
        try {
            socketServer = new MyWebSocketServer(port);
            socketServer.start();
            address = DeviceUtil.getIP(App.getInstance());
            Log.e("asd", "Start ServerSocket Success...");
            listener.onServerStatus(true, "开启成功");
            return true;
        } catch (Exception e) {
            Log.e("asd", "Start Failed...");
            listener.onServerStatus(false, "开启失败");
            e.printStackTrace();
            return false;
        }
    }

    public boolean stopServer() {
        try {
            socketServer.stop();
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
                case "person/addPerson":
                    break;
                case "person/updatePerson":
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
            }
        } catch (Exception e) {
            e.printStackTrace();
            conn.send(GSON.toJson(new ResponseEntity("400", "服务器错误:" + e.getMessage())));
        }
    }

    public void addPerson(WebSocket conn, String message) {
        try {
            WebServerRequest<PersonDto> webServerRequest;
            try {
                webServerRequest = GSON.fromJson(message, new TypeToken<WebServerRequest<PersonDto>>() {}.getType());
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
                findPerson = PersonRepository.getInstance().findPerson(transform.getIdentifyNumber());
                if (findPerson != null) {
                    throw new MyException("该手机号码已重复");
                }
            } else {
                throw new MyException("该身份证号码已重复");
            }
//            if (TextUtils.isEmpty(parameter)) {
//                conn.send(GSON.toJson(new ResponseEntity("400", "删除人员时，参数不应为空")));
//                return;
//            }
//            Person person = PersonRepository.getInstance().findPerson(parameter);
//            if (person == null) {
//                conn.send(GSON.toJson(new ResponseEntity("400", "未找到该人员")));
//                return;
//            }
//            PersonRepository.getInstance().deletePerson(person);
            PersonManager.getInstance().loadPersonDataFromCache();
            conn.send(GSON.toJson(new ResponseEntity("200", "删除成功")));
        } catch (Exception e) {
            e.printStackTrace();
            conn.send(GSON.toJson(new ResponseEntity("400", "新增人员时遇到错误:" + e.getMessage())));
        }
    }

    private void deletePerson(WebSocket conn, String message) {
        try {
            WebServerRequest<String> webServerRequest;
            try {
                webServerRequest = GSON.fromJson(message, new TypeToken<WebServerRequest<String>>() {}.getType());
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                conn.send(GSON.toJson(new ResponseEntity("400", "无法解析请求Json")));
                return;
            }
            String parameter = webServerRequest.getParameter();
            if (TextUtils.isEmpty(parameter)) {
                conn.send(GSON.toJson(new ResponseEntity("400", "删除人员时，参数不应为空")));
                return;
            }
            Person person = PersonRepository.getInstance().findPerson(parameter);
            if (person == null) {
                conn.send(GSON.toJson(new ResponseEntity("400", "未找到该人员")));
                return;
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
                webServerRequest = GSON.fromJson(message, new TypeToken<WebServerRequest<PersonSearch>>() {}.getType());
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                conn.send(GSON.toJson(new ResponseEntity("400", "无法解析请求Json")));
                return;
            }
            PersonSearch parameter = webServerRequest.getParameter();
            if (parameter.getPageNum() == 0 || parameter.getPageSize() == 0) {
                conn.send(GSON.toJson(new ResponseEntity("400", "获取人员：页码和容量不应为0")));
                return;
            }
            List<Person> personList = PersonRepository.getInstance().loadPersonByPage(parameter.getPageNum(),
                    parameter.getPageSize());
            conn.send(GSON.toJson(new ResponseEntity<>("200", "获取人员成功", personList)));
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
                webServerRequest = GSON.fromJson(message, new TypeToken<WebServerRequest<RecordSearch>>() {}.getType());
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                conn.send(GSON.toJson(new ResponseEntity("400", "无法解析请求Json")));
                return;
            }
            RecordSearch parameter = webServerRequest.getParameter();
            if (parameter.getPageNum() == 0 || parameter.getPageSize() == 0) {
                conn.send(GSON.toJson(new ResponseEntity("400", "获取日志：页码和容量不应为0")));
                return;
            }
            List<Record> recordList = RecordRepository.getInstance().searchRecord(parameter);
            conn.send(GSON.toJson(new ResponseEntity<>("200", "获取日志成功", recordList)));
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

    public void setListener(OnServerStatusListener listener) {
        this.listener = listener;
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
        }

        @Override
        public void onStart() {
            Log.e("asd", "onStart");
        }
    }

}
