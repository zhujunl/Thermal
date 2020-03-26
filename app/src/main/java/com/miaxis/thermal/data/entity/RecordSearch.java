package com.miaxis.thermal.data.entity;

public class RecordSearch {

    private int pageNum;
    private int pageSize;
    private String name;
    private String identifyNumber;
    private String phone;
    private Boolean upload;
    private String startTime;
    private String endTime;
    private Boolean fever;

    public RecordSearch() {
    }

    private RecordSearch(Builder builder) {
        setPageNum(builder.pageNum);
        setPageSize(builder.pageSize);
        setName(builder.name);
        setIdentifyNumber(builder.identifyNumber);
        setPhone(builder.phone);
        setUpload(builder.upload);
        setStartTime(builder.startTime);
        setEndTime(builder.endTime);
        setFever(builder.fever);
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Boolean getUpload() {
        return upload;
    }

    public void setUpload(Boolean upload) {
        this.upload = upload;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Boolean getFever() {
        return fever;
    }

    public void setFever(Boolean fever) {
        this.fever = fever;
    }

    public static final class Builder {
        private int pageNum;
        private int pageSize;
        private String name;
        private String identifyNumber;
        private String phone;
        private Boolean upload;
        private String startTime;
        private String endTime;
        private Boolean fever;

        public Builder() {
        }

        public Builder pageNum(int val) {
            pageNum = val;
            return this;
        }

        public Builder pageSize(int val) {
            pageSize = val;
            return this;
        }

        public Builder name(String val) {
            name = val;
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

        public Builder upload(Boolean val) {
            upload = val;
            return this;
        }

        public Builder startTime(String val) {
            startTime = val;
            return this;
        }

        public Builder endTime(String val) {
            endTime = val;
            return this;
        }

        public Builder fever(Boolean val) {
            fever = val;
            return this;
        }

        public RecordSearch build() {
            return new RecordSearch(this);
        }
    }
}
