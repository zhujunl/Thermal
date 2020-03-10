package com.miaxis.thermal.app;

import android.app.Application;

import androidx.annotation.NonNull;

import com.miaxis.thermal.data.dao.AppDatabase;
import com.miaxis.thermal.manager.ConfigManager;
import com.miaxis.thermal.manager.CrashExceptionManager;
import com.miaxis.thermal.manager.FaceManager;
import com.miaxis.thermal.manager.GpioManager;
import com.miaxis.thermal.manager.PersonManager;
import com.miaxis.thermal.manager.RecordManager;
import com.miaxis.thermal.manager.TTSManager;
import com.miaxis.thermal.util.FileUtil;

import org.greenrobot.eventbus.EventBus;

public class App extends Application {

    private static App instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
//        EventBus.builder().addIndex(new MyEventBusIndex()).installDefaultEventBus();
    }

    public static App getInstance() {
        return instance;
    }

    public void initApplication(@NonNull OnAppInitListener listener) {
        try {
            FileUtil.initDirectory();
            AppDatabase.initDB(this);
            ConfigManager.getInstance().checkConfig();
            CrashExceptionManager.getInstance().init(this);
            TTSManager.getInstance().init(getApplicationContext());
            GpioManager.getInstance().init(this);
            int result = FaceManager.getInstance().initFaceST(getApplicationContext(), FileUtil.MODEL_PATH, FileUtil.LICENCE_PATH);
            listener.onInit(result == FaceManager.INIT_SUCCESS, FaceManager.getFaceInitResultDetail(result));
//            listener.onInit(true, "");
        } catch (Exception e) {
            e.printStackTrace();
            listener.onInit(false, e.getMessage());
        }
    }

    public interface OnAppInitListener {
        void onInit(boolean result, String message);
    }

}
