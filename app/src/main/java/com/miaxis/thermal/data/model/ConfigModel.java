package com.miaxis.thermal.data.model;

import com.miaxis.thermal.data.dao.AppDatabase;
import com.miaxis.thermal.data.entity.Config;

public class ConfigModel {

    public static void saveConfig(Config config) {
        config.setId(1L);
        AppDatabase.getInstance().configDao().deleteAll();
        AppDatabase.getInstance().configDao().insert(config);
    }

    public static Config loadConfig() {
        return AppDatabase.getInstance().configDao().loadConfig();
    }

}
