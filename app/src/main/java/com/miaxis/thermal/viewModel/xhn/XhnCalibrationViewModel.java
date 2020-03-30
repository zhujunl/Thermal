package com.miaxis.thermal.viewModel.xhn;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;

import androidx.databinding.ObservableField;
import androidx.databinding.ObservableFloat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.OnLifecycleEvent;

import com.miaxis.thermal.app.App;
import com.miaxis.thermal.bridge.SingleLiveEvent;
import com.miaxis.thermal.data.entity.Calibration;
import com.miaxis.thermal.manager.CalibrationManager;
import com.miaxis.thermal.manager.TemperatureManager;
import com.miaxis.thermal.manager.ToastManager;
import com.miaxis.thermal.manager.strategy.xhn.XhnTemperatureStrategy;
import com.miaxis.thermal.view.activity.MainActivity;
import com.miaxis.thermal.viewModel.BaseViewModel;

public class XhnCalibrationViewModel extends BaseViewModel implements LifecycleObserver {

    public ObservableField<String> temperature = new ObservableField<>();
    public MutableLiveData<Boolean> heatMapUpdate = new SingleLiveEvent<>();

    public Bitmap heatMapCache;

    private XhnTemperatureStrategy strategy;

    public XhnCalibrationViewModel() {
        strategy = (XhnTemperatureStrategy) TemperatureManager.getInstance().getTemperatureStrategy();
        strategy.open();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void readTemperature() {
        strategy.setXhnListener(listener);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void stopRead() {
        strategy.setXhnListener(null);
    }

    public void saveCalibration(Calibration calibration) {
        waitMessage.setValue("请稍后");
        CalibrationManager.getInstance().saveCalibration(calibration, (result, message) -> {
            if (result) {
                waitMessage.setValue("");
                toast.setValue(ToastManager.getToastBody(message, ToastManager.SUCCESS));
                restartApp();
            } else {
                waitMessage.setValue("");
                toast.setValue(ToastManager.getToastBody(message, ToastManager.ERROR));
            }
        });
    }

    private TemperatureManager.TemperatureListener listener = new TemperatureManager.TemperatureListener() {
        @Override
        public void onTemperature(float temp) {
            temperature.set(temp + " °C");
        }

        @Override
        public void onHeatMap(Bitmap bitmap) {
            heatMapCache = bitmap;
            Matrix matrix = new Matrix();
            matrix.setScale(-1, -1);//垂直翻转
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            //生成的翻转后的bitmap
            heatMapCache = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
            heatMapUpdate.postValue(Boolean.TRUE);
        }
    };

    private void restartApp() {
        Context context = App.getInstance().getApplicationContext();
        Intent mStartActivity = new Intent(context, MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }

}
