package com.miaxis.thermal.data.dto;

public class RecordDto {

    private String identifyNumber;
    private String userName;
    private String userPhone;
    private String verifyTime;
    private float score;
    private float temperature;
    private String type;
    private String mac;
    private String verifyImage;

    public RecordDto() {
    }

    private RecordDto(Builder builder) {
        setIdentifyNumber(builder.identifyNumber);
        setUserName(builder.userName);
        setUserPhone(builder.userPhone);
        setVerifyTime(builder.verifyTime);
        setScore(builder.score);
        setTemperature(builder.temperature);
        setType(builder.type);
        setMac(builder.mac);
        setVerifyImage(builder.verifyImage);
    }

    public String getIdentifyNumber() {
        return identifyNumber;
    }

    public void setIdentifyNumber(String identifyNumber) {
        this.identifyNumber = identifyNumber;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getVerifyTime() {
        return verifyTime;
    }

    public void setVerifyTime(String verifyTime) {
        this.verifyTime = verifyTime;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getVerifyImage() {
        return verifyImage;
    }

    public void setVerifyImage(String verifyImage) {
        this.verifyImage = verifyImage;
    }

    public static final class Builder {
        private String identifyNumber;
        private String userName;
        private String userPhone;
        private String verifyTime;
        private float score;
        private float temperature;
        private String type;
        private String mac;
        private String verifyImage;

        public Builder() {
        }

        public Builder identifyNumber(String val) {
            identifyNumber = val;
            return this;
        }

        public Builder userName(String val) {
            userName = val;
            return this;
        }

        public Builder userPhone(String val) {
            userPhone = val;
            return this;
        }

        public Builder verifyTime(String val) {
            verifyTime = val;
            return this;
        }

        public Builder score(float val) {
            score = val;
            return this;
        }

        public Builder temperature(float val) {
            temperature = val;
            return this;
        }

        public Builder type(String val) {
            type = val;
            return this;
        }

        public Builder mac(String val) {
            mac = val;
            return this;
        }

        public Builder verifyImage(String val) {
            verifyImage = val;
            return this;
        }

        public RecordDto build() {
            return new RecordDto(this);
        }
    }
}
