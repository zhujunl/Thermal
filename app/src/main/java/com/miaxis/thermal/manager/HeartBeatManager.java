package com.miaxis.thermal.manager;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.miaxis.thermal.data.entity.Person;
import com.miaxis.thermal.data.exception.MyException;
import com.miaxis.thermal.data.repository.PersonRepository;
import com.miaxis.thermal.util.ValueUtil;

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
            getPersonData();
            long cold = ConfigManager.getInstance().getConfig().getFailedQueryCold() * 1000;
            handler.sendMessageDelayed(handler.obtainMessage(MSG_TIME_LIMIT), cold);
        }
    }

    public void relieveLimit() {
        handler.removeMessages(MSG_TIME_LIMIT);
        activeBurstLimit = false;
    }

    private void getPersonData() {
        handler.removeMessages(MSG_TIME_DELAY_BURST);
        if (updating) return;
        Observable.create((ObservableOnSubscribe<List<Person>>) emitter -> {
            updating = true;
            List<Person> personList = PersonRepository.getInstance().downloadPerson();
            if (personList != null) {
                emitter.onNext(personList);
            } else {
                emitter.onError(new MyException("接口结果为空"));
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnNext(personList -> {
                    for (Person person : personList) {
                        PersonManager.getInstance().handlePersonHeartBeat(person);
                    }
                })
                .subscribe(personList -> {
                    Log.e("asd", "更新人员：" + personList.size() + "个");
                    PersonManager.getInstance().loadPersonDataFromCache();
                    if (personList.size() == ValueUtil.PAGE_SIZE) {
                        getPersonData();
                    } else {
                        prepareForNextHeartBeat();
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                    Log.e("asd", "更新人员出错：" + throwable.getMessage());
                    prepareForNextHeartBeat();
                });
    }

    private void prepareForNextHeartBeat() {
        long delayTime = ConfigManager.getInstance().getConfig().getHeartBeatInterval() * 1000;
        Message message = handler.obtainMessage(MSG_TIME_DELAY_BURST);
        handler.sendMessageDelayed(message, delayTime);
        updating = false;
    }

}
