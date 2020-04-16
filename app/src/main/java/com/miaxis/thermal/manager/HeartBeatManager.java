package com.miaxis.thermal.manager;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.miaxis.thermal.data.entity.Person;
import com.miaxis.thermal.data.exception.MyException;
import com.miaxis.thermal.data.exception.NetResultFailedException;
import com.miaxis.thermal.data.repository.PersonRepository;
import com.miaxis.thermal.util.ValueUtil;

import java.io.IOException;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

public class HeartBeatManager {

    private HeartBeatManager() {
    }

    public static HeartBeatManager getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static final HeartBeatManager instance = new HeartBeatManager();
    }

    /**
     * ================================ 静态内部类单例写法 ================================
     **/

    private static final int MSG_TIME_DELAY_BURST = 0x39;
    private static final int MSG_TIME_LIMIT = 0x40;

    private HandlerThread handlerThread;
    private Handler handler;

    private volatile boolean updating = false;
    private volatile boolean activeBurstLimit = false;

    public void startHeartBeat() {
        handlerThread = new HandlerThread("HeartBeat");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == MSG_TIME_DELAY_BURST) {
                    getPersonData();
                } else if (msg.what == MSG_TIME_LIMIT) {
                    activeBurstLimit = false;
                }
            }
        };
        handler.sendMessage(handler.obtainMessage(MSG_TIME_DELAY_BURST));
    }

    public void stopHeartBeat() {
        handler.removeMessages(MSG_TIME_DELAY_BURST);
        handlerThread.quitSafely();
    }

    public void heartBeatLimitBurst() {
        if (!activeBurstLimit && !updating) {
            activeBurstLimit = true;
            heartBeatNow();
            long cold = ConfigManager.getInstance().getConfig().getFailedQueryCold() * 1000;
            handler.sendMessageDelayed(handler.obtainMessage(MSG_TIME_LIMIT), cold);
        }
    }

    public void relieveLimit() {
        handler.removeMessages(MSG_TIME_LIMIT);
        activeBurstLimit = false;
    }

    private void getPersonData() {
        try {
            updating = true;
            handler.removeMessages(MSG_TIME_DELAY_BURST);
            List<Person> personList = PersonRepository.getInstance().downloadPerson();
            if (personList != null && !personList.isEmpty()) {
                for (Person person : personList) {
                    PersonManager.getInstance().handlePersonHeartBeat(person);
                }
                Log.e("asd", "人员同步成功：" + personList.size());
            } else {
                throw new MyException("服务端待更新人员为为空");
            }
            PersonManager.getInstance().loadPersonDataFromCache();
            if (personList.size() == ValueUtil.PAGE_SIZE) {
                handler.sendMessage(handler.obtainMessage(MSG_TIME_DELAY_BURST));
            } else {
                prepareForNextHeartBeat();
            }
        } catch (Exception e) {
            Log.e("asd", "" + e.getMessage());
            prepareForNextHeartBeat();
        } finally {
            updating = false;
        }
    }

    private void prepareForNextHeartBeat() {
        long delayTime = ConfigManager.getInstance().getConfig().getHeartBeatInterval() * 1000;
        Message message = handler.obtainMessage(MSG_TIME_DELAY_BURST);
        handler.sendMessageDelayed(message, delayTime);
        updating = false;
    }

    private void heartBeatNow() {
        if (updating) return;
        handler.removeMessages(MSG_TIME_DELAY_BURST);
        handler.sendMessage(handler.obtainMessage(MSG_TIME_DELAY_BURST));
    }

}
