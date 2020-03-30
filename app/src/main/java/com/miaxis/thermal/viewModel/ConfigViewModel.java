package com.miaxis.thermal.viewModel;

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import com.miaxis.thermal.bridge.SingleLiveEvent;
import com.miaxis.thermal.data.entity.Config;
import com.miaxis.thermal.manager.ConfigManager;
import com.miaxis.thermal.manager.ToastManager;

public class ConfigViewModel extends BaseViewModel {

    public MutableLiveData<Boolean> clearTimeStampResult = new SingleLiveEvent<>();

    public ConfigViewModel() {
    }

    public void clearTimeStamp() {
        Config config = ConfigManager.getInstance().getConfig();
        config.setTimeStamp(0L);
        ConfigManager.getInstance().saveConfig(config, (result, message) -> {
            if (result) {
                clearTimeStampResult.setValue(Boolean.TRUE);
                toast.setValue(ToastManager.getToastBody("清空同步时间戳成功", ToastManager.SUCCESS));
            } else {
                toast.setValue(ToastManager.getToastBody(message, ToastManager.ERROR));
            }
        });
    }

    public void saveConfig(Config config) {
        ConfigManager.getInstance().saveConfig(config, (result, message) -> {
            if (result) {
                toast.setValue(ToastManager.getToastBody(message, ToastManager.SUCCESS));
            } else {
                toast.setValue(ToastManager.getToastBody(message, ToastManager.ERROR));
            }
        });
    }

}
