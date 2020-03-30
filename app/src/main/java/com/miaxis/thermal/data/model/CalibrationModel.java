package com.miaxis.thermal.data.model;

import com.miaxis.thermal.data.dao.AppDatabase;
import com.miaxis.thermal.data.entity.Calibration;

public class CalibrationModel {

    public static void saveCalibration(Calibration calibration) {
        calibration.setId(1L);
        AppDatabase.getInstance().calibrationDao().deleteAll();
        AppDatabase.getInstance().calibrationDao().insert(calibration);
    }

    public static Calibration loadCalibration() {
        return AppDatabase.getInstance().calibrationDao().loadCalibration();
    }

}
