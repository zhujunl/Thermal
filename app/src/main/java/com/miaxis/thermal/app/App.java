package com.miaxis.thermal.app;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDex;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.liulishuo.filedownloader.FileDownloader;
import com.miaxis.thermal.data.dao.AppDatabase;
import com.miaxis.thermal.data.net.ThermalApi;
import com.miaxis.thermal.manager.CalibrationManager;
import com.miaxis.thermal.manager.CameraManager;
import com.miaxis.thermal.manager.CardManager;
import com.miaxis.thermal.manager.ConfigManager;
import com.miaxis.thermal.manager.CrashExceptionManager;
import com.miaxis.thermal.manager.FaceManager;
import com.miaxis.thermal.manager.FingerManager;
import com.miaxis.thermal.manager.GpioManager;
import com.miaxis.thermal.manager.HumanSensorManager;
import com.miaxis.thermal.manager.PersonManager;
import com.miaxis.thermal.manager.RecordManager;
import com.miaxis.thermal.manager.TTSManager;
import com.miaxis.thermal.manager.TemperatureManager;
import com.miaxis.thermal.manager.WatchDogManager;
import com.miaxis.thermal.util.FileUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class App extends Application {

    private static App instance;
    private boolean firstIn = true;

    private ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("ThermalApp-%d").build();
    private ExecutorService threadExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2, namedThreadFactory);

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        MultiDex.install(this);
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
            ThermalApi.rebuildRetrofit();
            CalibrationManager.getInstance().checkCalibration();
            WatchDogManager.getInstance().init(this);
            CrashExceptionManager.getInstance().init(this);
            FileDownloader.setup(this);
            TTSManager.getInstance().init(getApplicationContext());
            CameraManager.getInstance().init();
            TemperatureManager.getInstance().init();
            CardManager.getInstance().init();
            FingerManager.getInstance().init();
            GpioManager.getInstance().init(this);
            HumanSensorManager.getInstance().init(this);
            int result = FaceManager.getInstance().initFaceST(getApplicationContext(), FileUtil.LICENCE_PATH);
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

    public boolean isFirstIn() {
        if (firstIn) {
            firstIn = false;
            return true;
        } else {
            return false;
        }
    }

    public static boolean isZh() {
        Locale locale = instance.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.toLowerCase().endsWith("zh"))
            return true;
        else
            return false;
    }

    public ExecutorService getThreadExecutor() {
        return threadExecutor;
    }
}
