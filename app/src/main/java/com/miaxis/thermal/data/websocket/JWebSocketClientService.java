package com.miaxis.thermal.data.websocket;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.miaxis.thermal.manager.ConfigManager;
import com.miaxis.thermal.manager.GpioManager;
import com.miaxis.thermal.util.DeviceUtil;

import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;

public class JWebSocketClientService  extends Service {

    private final static String TAG = JWebSocketClientService.class.getSimpleName();

    public JWebSocketClient client;
    private final JWebSocketClientBinder mBinder = new JWebSocketClientBinder();
    private static final int GRAY_SERVICE_ID = 1001;
    private static final long CLOSE_RECON_TIME = 1000;//连接断开或者连接错误立即重连

    private String deviceId;
    private String sendDevice;
    private String leaveDevice;

    //用于Activity和service通讯
    public class JWebSocketClientBinder extends Binder {
        public JWebSocketClientService getService() {
            return JWebSocketClientService.this;
        }
    }

    //灰色保活
    public static class GrayInnerService extends Service {

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(GRAY_SERVICE_ID, new Notification());
            stopForeground(true);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "WebSocketService onBind");
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        deviceId = DeviceUtil.getMacFromHardware();
        sendDevice = "join-" + deviceId;
        leaveDevice = "leave-" + deviceId;
        //初始化WebSocket
        initSocketClient();
        mHandler.postDelayed(heartBeatRunnable, HEART_BEAT_RATE);//开启心跳检测
        return START_STICKY;
    }

    private void initSocketClient() {
        if (TextUtils.isEmpty(ConfigManager.getInstance().getConfig().getHost())) {
            return;
        }

        URI uri = URI.create("ws://" + URI.create(ConfigManager.getInstance().getConfig().getHost()).getHost() + ":6001");
        client = new JWebSocketClient(uri) {
            @Override
            public void onMessage(String message) {
                //message就是接收到的消息
                Log.i(TAG, "WebSocketService收到的消息：" + message);
                if (message.equals("pong")) {
                    //alive
                } else {
                    try {
                        JSONObject jsonObject1 = new JSONObject(message);
                        String order = (String) jsonObject1.get("action");
                        if (order.equals("1")) {
                            GpioManager.getInstance().openDoorForGate();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onOpen(ServerHandshake handShakeData) {//在webSocket连接开启时调用
                Log.i(TAG, "WebSocket 连接成功");
                sendMsg(sendDevice);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {//在连接断开时调用
                Log.e(TAG, "onClose() 连接断开_reason：" + reason);
                sendMsg(leaveDevice);
                mHandler.removeCallbacks(heartBeatRunnable);
                mHandler.postDelayed(heartBeatRunnable, CLOSE_RECON_TIME);//开启心跳检测
            }

            @Override
            public void onError(Exception ex) {//在连接出错时调用
                Log.e(TAG, "onError() 连接出错：" + ex.getMessage());
                mHandler.removeCallbacks(heartBeatRunnable);
                mHandler.postDelayed(heartBeatRunnable, CLOSE_RECON_TIME);//开启心跳检测
            }
        };
        connect();
    }

    /**
     * 连接WebSocket
     */
    private void connect() {
        new Thread() {
            @Override
            public void run() {
                try {
                    //connectBlocking多出一个等待操作，会先连接再发送，否则未连接发送会报错
                    client.connectBlocking();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 发送消息
     */
    public void sendMsg(String msg) {
        if (null != client) {
            Log.i(TAG, "发送的消息：" + msg);
            try {
                client.send(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "Service onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        closeConnect();
        sendMsg(leaveDevice);
        super.onDestroy();
    }

    /**
     * 断开连接
     */
    public void closeConnect() {
        mHandler.removeCallbacks(heartBeatRunnable);
        try {
            if (null != client) {
                client.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client = null;
        }
    }


    //    -------------------------------------WebSocket心跳检测------------------------------------------------
    private static final long HEART_BEAT_RATE = 60 * 1000;//每隔60秒进行一次对长连接的心跳检测
    private final Handler mHandler = new Handler();
    private final Runnable heartBeatRunnable = new Runnable() {
        @Override
        public void run() {
            if (client != null) {
                if (client.isClosed()) {
                    sendMsg(leaveDevice);
                    reconnectWs();
                    Log.e(TAG, "心跳包检测WebSocket连接状态：已关闭");
                } else if (client.isOpen()) {
                    sendMsg("ping");
                    Log.d(TAG, "心跳包检测WebSocket连接状态：已连接");
                } else {
                    sendMsg(leaveDevice);
                    Log.e(TAG, "心跳包检测WebSocket连接状态：已断开");
                }
            } else {
                //如果client已为空，重新初始化连接
                initSocketClient();
                Log.e(TAG, "心跳包检测WebSocket连接状态：client已为空，重新初始化连接");
            }
            //每隔一定的时间，对长连接进行一次心跳检测
            mHandler.postDelayed(this, HEART_BEAT_RATE);
        }
    };

    /**
     * 开启重连
     */
    private void reconnectWs() {
        mHandler.removeCallbacks(heartBeatRunnable);
        new Thread() {
            @Override
            public void run() {
                try {
                    Log.e(TAG, "开启重连");
                    client.reconnectBlocking();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
