package com.miaxis.thermal.data.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(indices = {@Index(value = {"identifyNumber"}, unique = true)})
public class Person {

    @PrimaryKey(autoGenerate = true)
    private Long id;
    private String identifyNumber;
    private String phone;
    private String name;
    private String type;
    private Date effectiveTime;
    private Date invalidTime;
    private Date updateTime;
    private String faceFeature;
    private String facePicturePath;
    private long timeStamp;
    private String remarks;
    private boolean upload;

    public Person() {
    }

    private Person(Builder builder) {
        setId(builder.id);
        setIdentifyNumber(builder.identifyNumber);
        setPhone(builder.phone);
        setName(builder.name);
        setType(builder.type);
        setEffectiveTime(builder.effectiveTime);
        setInvalidTime(builder.invalidTime);
        setUpdateTime(builder.updateTime);
        setFaceFeature(builder.faceFeature);
        setFacePicturePath(builder.facePicturePath);
        setTimeStamp(builder.timeStamp);
        setRemarks(builder.remarks);
        setUpload(builder.upload);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Date getEffectiveTime() {
        return effectiveTime;
    }

    public void setEffectiveTime(Date effectiveTime) {
        this.effectiveTime = effectiveTime;
    }

    public Date getInvalidTime() {
        return invalidTime;
    }

    public void setInvalidTime(Date invalidTime) {
        this.invalidTime = invalidTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getFaceFeature() {
        return faceFeature;
    }

    public void setFaceFeature(String faceFeature) {
        this.faceFeature = faceFeature;
    }

    public String getFacePicturePath() {
        return facePicturePath;
    }

    public void setFacePicturePath(String facePicturePath) {
        this.facePicturePath = facePicturePath;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public boolean isUpload() {
        return upload;
    }

    public void setUpload(boolean upload) {
        this.upload = upload;
    }

    public static final class Builder {
        private Long id;
        private String identifyNumber;
        private String phone;
        private String name;
        private String type;
        private Date effectiveTime;
        private Date invalidTime;
        private Date updateTime;
        private String faceFeature;
        private String facePicturePath;
        private long timeStamp;
        private String remarks;
        private boolean upload;

        public Builder() {
        }

        public Builder id(Long val) {
            id = val;
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

        public Builder effectiveTime(Date val) {
            effectiveTime = val;
            return this;
        }

        public Builder invalidTime(Date val) {
            invalidTime = val;
            return this;
        }

        public Builder updateTime(Date val) {
            updateTime = val;
            return this;
        }

        public Builder faceFeature(String val) {
            faceFeature = val;
            return this;
        }

        public Builder facePicturePath(String val) {
            facePicturePath = val;
            return this;
        }

        public Builder timeStamp(long val) {
            timeStamp = val;
            return this;
        }

        public Builder remarks(String val) {
            remarks = val;
            return this;
        }

        public Builder upload(boolean val) {
            upload = val;
            return this;
        }

        public Person build() {
            return new Person(this);
        }
    }
}
