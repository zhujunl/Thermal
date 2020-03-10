package com.miaxis.thermal.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity
public class Record {

    @PrimaryKey(autoGenerate = true)
    private Long id;
    private Long personId;
    private String identifyNumber;
    private String phone;
    private String name;
    private String type;
    private String faceType;
    private Date verifyTime;
    private String verifyPicturePath;
    private float score;
    private boolean upload;
    private float temperature;

    public Record() {
    }

    private Record(Builder builder) {
        setId(builder.id);
        setPersonId(builder.personId);
        setIdentifyNumber(builder.identifyNumber);
        setPhone(builder.phone);
        setName(builder.name);
        setType(builder.type);
        setFaceType(builder.faceType);
        setVerifyTime(builder.verifyTime);
        setVerifyPicturePath(builder.verifyPicturePath);
        setScore(builder.score);
        setUpload(builder.upload);
        setTemperature(builder.temperature);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public String getIdentifyNumber() {
        return identifyNumber;
    }

    public void setIdentifyNumber(String identifyNumber) {
        this.identifyNumber = identifyNumber;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFaceType() {
        return faceType;
    }

    public void setFaceType(String faceType) {
        this.faceType = faceType;
    }

    public Date getVerifyTime() {
        return verifyTime;
    }

    public void setVerifyTime(Date verifyTime) {
        this.verifyTime = verifyTime;
    }

    public String getVerifyPicturePath() {
        return verifyPicturePath;
    }

    public void setVerifyPicturePath(String verifyPicturePath) {
        this.verifyPicturePath = verifyPicturePath;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public boolean isUpload() {
        return upload;
    }

    public void setUpload(boolean upload) {
        this.upload = upload;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public static final class Builder {
        private Long id;
        private Long personId;
        private String identifyNumber;
        private String phone;
        private String name;
        private String type;
        private String faceType;
        private Date verifyTime;
        private String verifyPicturePath;
        private float score;
        private boolean upload;
        private float temperature;

        public Builder() {
        }

        public Builder id(Long val) {
            id = val;
            return this;
        }

        public Builder personId(Long val) {
            personId = val;
            return this;
        }

        public Builder identifyNumber(String val) {
            identifyNumber = val;
            return this;
        }

        public Builder phone(String val) {
            phone = val;
            return this;
        }

        public Builder name(String val) {
            name = val;
            return this;
        }

        public Builder type(String val) {
            type = val;
            return this;
        }

        public Builder faceType(String val) {
            faceType = val;
            return this;
        }

        public Builder verifyTime(Date val) {
            verifyTime = val;
            return this;
        }

        public Builder verifyPicturePath(String val) {
            verifyPicturePath = val;
            return this;
        }

        public Builder score(float val) {
            score = val;
            return this;
        }

        public Builder upload(boolean val) {
            upload = val;
            return this;
        }

        public Builder temperature(float val) {
            temperature = val;
            return this;
        }

        public Record build() {
            return new Record(this);
        }
    }
}
