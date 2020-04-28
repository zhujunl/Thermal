package com.miaxis.thermal.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Config {

    @PrimaryKey
    private Long id;
    private String serverMode;
    private String host;
    private String downloadPersonPath;
    private String updatePersonPath;
    private String uploadRecordPath;
    private String mac;
    private boolean showCamera;
    private boolean faceCamera;
    private boolean liveness;
    private int registerQualityScore;
    private int qualityScore;
    private float verifyScore;
    private float maskVerifyScore;
    private int maskScore;
    private boolean forcedMask;
    private boolean strangerRecord;
    private boolean deviceMode;
    private boolean gateLimit;
    private int livenessScore;
    private int pupilDistanceMin;
    private int pupilDistanceMax;
    private int dormancyInterval;
    private int dormancyTime;
    private float feverScore;
    private float tempScore;
    private boolean heatMap;
    private boolean tempRealTime;
    private int heartBeatInterval;
    private int failedQueryCold;
    private int recordClearThreshold;
    private int verifyCold;
    private int failedVerifyCold;
    private int flashTime;
    private String devicePassword;
    private long timeStamp;

    public Config() {
    }

    private Config(Builder builder) {
        setId(builder.id);
        setServerMode(builder.serverMode);
        setHost(builder.host);
        setDownloadPersonPath(builder.downloadPersonPath);
        setUpdatePersonPath(builder.updatePersonPath);
        setUploadRecordPath(builder.uploadRecordPath);
        setMac(builder.mac);
        setShowCamera(builder.showCamera);
        setFaceCamera(builder.faceCamera);
        setLiveness(builder.liveness);
        setRegisterQualityScore(builder.registerQualityScore);
        setQualityScore(builder.qualityScore);
        setVerifyScore(builder.verifyScore);
        setMaskVerifyScore(builder.maskVerifyScore);
        setMaskScore(builder.maskScore);
        setForcedMask(builder.forcedMask);
        setStrangerRecord(builder.strangerRecord);
        setDeviceMode(builder.deviceMode);
        setGateLimit(builder.gateLimit);
        setLivenessScore(builder.livenessScore);
        setPupilDistanceMin(builder.pupilDistanceMin);
        setPupilDistanceMax(builder.pupilDistanceMax);
        setDormancyInterval(builder.dormancyInterval);
        setDormancyTime(builder.dormancyTime);
        setFeverScore(builder.feverScore);
        setTempScore(builder.tempScore);
        setHeatMap(builder.heatMap);
        setTempRealTime(builder.tempRealTime);
        setHeartBeatInterval(builder.heartBeatInterval);
        setFailedQueryCold(builder.failedQueryCold);
        setRecordClearThreshold(builder.recordClearThreshold);
        setVerifyCold(builder.verifyCold);
        setFailedVerifyCold(builder.failedVerifyCold);
        setFlashTime(builder.flashTime);
        setDevicePassword(builder.devicePassword);
        setTimeStamp(builder.timeStamp);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getServerMode() {
        return serverMode;
    }

    public void setServerMode(String serverMode) {
        this.serverMode = serverMode;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getDownloadPersonPath() {
        return downloadPersonPath;
    }

    public void setDownloadPersonPath(String downloadPersonPath) {
        this.downloadPersonPath = downloadPersonPath;
    }

    public String getUpdatePersonPath() {
        return updatePersonPath;
    }

    public void setUpdatePersonPath(String updatePersonPath) {
        this.updatePersonPath = updatePersonPath;
    }

    public String getUploadRecordPath() {
        return uploadRecordPath;
    }

    public void setUploadRecordPath(String uploadRecordPath) {
        this.uploadRecordPath = uploadRecordPath;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public boolean isShowCamera() {
        return showCamera;
    }

    public void setShowCamera(boolean showCamera) {
        this.showCamera = showCamera;
    }

    public boolean isFaceCamera() {
        return faceCamera;
    }

    public void setFaceCamera(boolean faceCamera) {
        this.faceCamera = faceCamera;
    }

    public boolean isLiveness() {
        return liveness;
    }

    public void setLiveness(boolean liveness) {
        this.liveness = liveness;
    }

    public int getRegisterQualityScore() {
        return registerQualityScore;
    }

    public void setRegisterQualityScore(int registerQualityScore) {
        this.registerQualityScore = registerQualityScore;
    }

    public int getQualityScore() {
        return qualityScore;
    }

    public void setQualityScore(int qualityScore) {
        this.qualityScore = qualityScore;
    }

    public float getVerifyScore() {
        return verifyScore;
    }

    public void setVerifyScore(float verifyScore) {
        this.verifyScore = verifyScore;
    }

    public float getMaskVerifyScore() {
        return maskVerifyScore;
    }

    public void setMaskVerifyScore(float maskVerifyScore) {
        this.maskVerifyScore = maskVerifyScore;
    }

    public int getMaskScore() {
        return maskScore;
    }

    public void setMaskScore(int maskScore) {
        this.maskScore = maskScore;
    }

    public boolean isForcedMask() {
        return forcedMask;
    }

    public void setForcedMask(boolean forcedMask) {
        this.forcedMask = forcedMask;
    }

    public boolean isStrangerRecord() {
        return strangerRecord;
    }

    public void setStrangerRecord(boolean strangerRecord) {
        this.strangerRecord = strangerRecord;
    }

    public boolean isDeviceMode() {
        return deviceMode;
    }

    public void setDeviceMode(boolean deviceMode) {
        this.deviceMode = deviceMode;
    }

    public boolean isGateLimit() {
        return gateLimit;
    }

    public void setGateLimit(boolean gateLimit) {
        this.gateLimit = gateLimit;
    }

    public int getLivenessScore() {
        return livenessScore;
    }

    public void setLivenessScore(int livenessScore) {
        this.livenessScore = livenessScore;
    }

    public int getPupilDistanceMin() {
        return pupilDistanceMin;
    }

    public void setPupilDistanceMin(int pupilDistanceMin) {
        this.pupilDistanceMin = pupilDistanceMin;
    }

    public int getPupilDistanceMax() {
        return pupilDistanceMax;
    }

    public void setPupilDistanceMax(int pupilDistanceMax) {
        this.pupilDistanceMax = pupilDistanceMax;
    }

    public int getDormancyInterval() {
        return dormancyInterval;
    }

    public void setDormancyInterval(int dormancyInterval) {
        this.dormancyInterval = dormancyInterval;
    }

    public int getDormancyTime() {
        return dormancyTime;
    }

    public void setDormancyTime(int dormancyTime) {
        this.dormancyTime = dormancyTime;
    }

    public float getFeverScore() {
        return feverScore;
    }

    public void setFeverScore(float feverScore) {
        this.feverScore = feverScore;
    }

    public float getTempScore() {
        return tempScore;
    }

    public void setTempScore(float tempScore) {
        this.tempScore = tempScore;
    }

    public boolean isHeatMap() {
        return heatMap;
    }

    public void setHeatMap(boolean heatMap) {
        this.heatMap = heatMap;
    }

    public boolean isTempRealTime() {
        return tempRealTime;
    }

    public void setTempRealTime(boolean tempRealTime) {
        this.tempRealTime = tempRealTime;
    }

    public int getHeartBeatInterval() {
        return heartBeatInterval;
    }

    public void setHeartBeatInterval(int heartBeatInterval) {
        this.heartBeatInterval = heartBeatInterval;
    }

    public int getFailedQueryCold() {
        return failedQueryCold;
    }

    public void setFailedQueryCold(int failedQueryCold) {
        this.failedQueryCold = failedQueryCold;
    }

    public int getRecordClearThreshold() {
        return recordClearThreshold;
    }

    public void setRecordClearThreshold(int recordClearThreshold) {
        this.recordClearThreshold = recordClearThreshold;
    }

    public int getVerifyCold() {
        return verifyCold;
    }

    public void setVerifyCold(int verifyCold) {
        this.verifyCold = verifyCold;
    }

    public int getFailedVerifyCold() {
        return failedVerifyCold;
    }

    public void setFailedVerifyCold(int failedVerifyCold) {
        this.failedVerifyCold = failedVerifyCold;
    }

    public int getFlashTime() {
        return flashTime;
    }

    public void setFlashTime(int flashTime) {
        this.flashTime = flashTime;
    }

    public String getDevicePassword() {
        return devicePassword;
    }

    public void setDevicePassword(String devicePassword) {
        this.devicePassword = devicePassword;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public static final class Builder {
        private Long id;
        private String serverMode;
        private String host;
        private String downloadPersonPath;
        private String updatePersonPath;
        private String uploadRecordPath;
        private String mac;
        private boolean showCamera;
        private boolean faceCamera;
        private boolean liveness;
        private int registerQualityScore;
        private int qualityScore;
        private float verifyScore;
        private float maskVerifyScore;
        private int maskScore;
        private boolean forcedMask;
        private boolean strangerRecord;
        private boolean deviceMode;
        private boolean gateLimit;
        private int livenessScore;
        private int pupilDistanceMin;
        private int pupilDistanceMax;
        private int dormancyInterval;
        private int dormancyTime;
        private float feverScore;
        private float tempScore;
        private boolean heatMap;
        private boolean tempRealTime;
        private int heartBeatInterval;
        private int failedQueryCold;
        private int recordClearThreshold;
        private int verifyCold;
        private int failedVerifyCold;
        private int flashTime;
        private String devicePassword;
        private long timeStamp;

        public Builder() {
        }

        public Builder id(Long val) {
            id = val;
            return this;
        }

        public Builder serverMode(String val) {
            serverMode = val;
            return this;
        }

        public Builder host(String val) {
            host = val;
            return this;
        }

        public Builder downloadPersonPath(String val) {
            downloadPersonPath = val;
            return this;
        }

        public Builder updatePersonPath(String val) {
            updatePersonPath = val;
            return this;
        }

        public Builder uploadRecordPath(String val) {
            uploadRecordPath = val;
            return this;
        }

        public Builder mac(String val) {
            mac = val;
            return this;
        }

        public Builder showCamera(boolean val) {
            showCamera = val;
            return this;
        }

        public Builder faceCamera(boolean val) {
            faceCamera = val;
            return this;
        }

        public Builder liveness(boolean val) {
            liveness = val;
            return this;
        }

        public Builder registerQualityScore(int val) {
            registerQualityScore = val;
            return this;
        }

        public Builder qualityScore(int val) {
            qualityScore = val;
            return this;
        }

        public Builder verifyScore(float val) {
            verifyScore = val;
            return this;
        }

        public Builder maskVerifyScore(float val) {
            maskVerifyScore = val;
            return this;
        }

        public Builder maskScore(int val) {
            maskScore = val;
            return this;
        }

        public Builder forcedMask(boolean val) {
            forcedMask = val;
            return this;
        }

        public Builder strangerRecord(boolean val) {
            strangerRecord = val;
            return this;
        }

        public Builder deviceMode(boolean val) {
            deviceMode = val;
            return this;
        }

        public Builder gateLimit(boolean val) {
            gateLimit = val;
            return this;
        }

        public Builder livenessScore(int val) {
            livenessScore = val;
            return this;
        }

        public Builder pupilDistanceMin(int val) {
            pupilDistanceMin = val;
            return this;
        }

        public Builder pupilDistanceMax(int val) {
            pupilDistanceMax = val;
            return this;
        }

        public Builder dormancyInterval(int val) {
            dormancyInterval = val;
            return this;
        }

        public Builder dormancyTime(int val) {
            dormancyTime = val;
            return this;
        }

        public Builder feverScore(float val) {
            feverScore = val;
            return this;
        }

        public Builder tempScore(float val) {
            tempScore = val;
            return this;
        }

        public Builder heatMap(boolean val) {
            heatMap = val;
            return this;
        }

        public Builder tempRealTime(boolean val) {
            tempRealTime = val;
            return this;
        }

        public Builder heartBeatInterval(int val) {
            heartBeatInterval = val;
            return this;
        }

        public Builder failedQueryCold(int val) {
            failedQueryCold = val;
            return this;
        }

        public Builder recordClearThreshold(int val) {
            recordClearThreshold = val;
            return this;
        }

        public Builder verifyCold(int val) {
            verifyCold = val;
            return this;
        }

        public Builder failedVerifyCold(int val) {
            failedVerifyCold = val;
            return this;
        }

        public Builder flashTime(int val) {
            flashTime = val;
            return this;
        }

        public Builder devicePassword(String val) {
            devicePassword = val;
            return this;
        }

        public Builder timeStamp(long val) {
            timeStamp = val;
            return this;
        }

        public Config build() {
            return new Config(this);
        }
    }
}
