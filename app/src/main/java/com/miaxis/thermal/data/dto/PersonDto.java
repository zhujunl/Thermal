package com.miaxis.thermal.data.dto;

import com.miaxis.thermal.data.entity.Person;
import com.miaxis.thermal.data.exception.MyException;
import com.miaxis.thermal.util.DateUtil;

import java.text.ParseException;
import java.util.Date;

public class PersonDto implements Mapper<Person> {

    private long id;
    private String userName;
    private String userType;
    private String userPhone;
    private String identifyNumber;
    private String startTime;
    private String invalidTime;
    private long updateTime;
    private String facePicture;
    private String faceFeature;
    private String maskFaceFeature;
    private String status;
    private String memberId;

    public PersonDto() {
    }

    private PersonDto(Builder builder) {
        setId(builder.id);
        setUserName(builder.userName);
        setUserType(builder.userType);
        setUserPhone(builder.userPhone);
        setIdentifyNumber(builder.identifyNumber);
        setStartTime(builder.startTime);
        setInvalidTime(builder.invalidTime);
        setUpdateTime(builder.updateTime);
        setFacePicture(builder.facePicture);
        setFaceFeature(builder.faceFeature);
        setMaskFaceFeature(builder.maskFaceFeature);
        setStatus(builder.status);
        setMemberId(builder.memberId);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getIdentifyNumber() {
        return identifyNumber;
    }

    public void setIdentifyNumber(String identifyNumber) {
        this.identifyNumber = identifyNumber;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getInvalidTime() {
        return invalidTime;
    }

    public void setInvalidTime(String invalidTime) {
        this.invalidTime = invalidTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public String getFacePicture() {
        return facePicture;
    }

    public void setFacePicture(String facePicture) {
        this.facePicture = facePicture;
    }

    public String getFaceFeature() {
        return faceFeature;
    }

    public void setFaceFeature(String faceFeature) {
        this.faceFeature = faceFeature;
    }

    public String getMaskFaceFeature() {
        return maskFaceFeature;
    }

    public void setMaskFaceFeature(String maskFaceFeature) {
        this.maskFaceFeature = maskFaceFeature;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    @Override
    public Person transform() throws MyException {
        try {
            Date effectiveDate = null;
            try {
                effectiveDate = DateUtil.DATE_FORMAT.parse(startTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Date invalidDate = null;
            try {
                invalidDate = DateUtil.DATE_FORMAT.parse(invalidTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return new Person.Builder()
                    .id(null)
                    .identifyNumber(identifyNumber)
                    .phone(userPhone)
                    .name(userName)
                    .cardCode(memberId)
                    .type(userType)
                    .effectiveTime(effectiveDate)
                    .invalidTime(invalidDate)
                    .faceFeature(faceFeature)
                    .facePicturePath(facePicture)
                    .maskFaceFeature(maskFaceFeature)
                    .timeStamp(updateTime)
                    .upload(true)
                    .status(status)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            throw new MyException("解析人员详细信息失败，原因：" + e.getMessage());
        }
    }

    public static final class Builder {
        private long id;
        private String userName;
        private String userType;
        private String userPhone;
        private String identifyNumber;
        private String startTime;
        private String invalidTime;
        private long updateTime;
        private String facePicture;
        private String faceFeature;
        private String maskFaceFeature;
        private String status;
        private String memberId;

        public Builder() {
        }

        public Builder id(long val) {
            id = val;
            return this;
        }

        public Builder userName(String val) {
            userName = val;
            return this;
        }

        public Builder userType(String val) {
            userType = val;
            return this;
        }

        public Builder userPhone(String val) {
            userPhone = val;
            return this;
        }

        public Builder identifyNumber(String val) {
            identifyNumber = val;
            return this;
        }

        public Builder startTime(String val) {
            startTime = val;
            return this;
        }

        public Builder invalidTime(String val) {
            invalidTime = val;
            return this;
        }

        public Builder updateTime(long val) {
            updateTime = val;
            return this;
        }

        public Builder facePicture(String val) {
            facePicture = val;
            return this;
        }

        public Builder faceFeature(String val) {
            faceFeature = val;
            return this;
        }

        public Builder maskFaceFeature(String val) {
            maskFaceFeature = val;
            return this;
        }

        public Builder status(String val) {
            status = val;
            return this;
        }

        public Builder memberId(String val) {
            memberId = val;
            return this;
        }

        public PersonDto build() {
            return new PersonDto(this);
        }
    }
}
