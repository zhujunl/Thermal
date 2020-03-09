package com.miaxis.thermal.viewModel;

import androidx.databinding.ObservableField;

import com.miaxis.thermal.data.entity.Config;
import com.miaxis.thermal.manager.ConfigManager;
import com.miaxis.thermal.manager.ToastManager;

public class ConfigViewModel extends BaseViewModel {

    public ObservableField<Config> config = new ObservableField<>(ConfigManager.getInstance().getConfig());

    public ConfigViewModel() {
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
