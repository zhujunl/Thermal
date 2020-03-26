package com.miaxis.thermal.data.entity;

public class PersonSearch {

    private int pageNum;
    private int pageSize;
    private String identifyNumber;
    private String phone;
    private String name;
    private Boolean upload;
    private Boolean face;
    private String status;
    private String type;

    public PersonSearch() {
    }

    private PersonSearch(Builder builder) {
        setPageNum(builder.pageNum);
        setPageSize(builder.pageSize);
        setIdentifyNumber(builder.identifyNumber);
        setPhone(builder.phone);
        setName(builder.name);
        setUpload(builder.upload);
        setFace(builder.face);
        setStatus(builder.status);
        setType(builder.type);
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

    public Boolean getUpload() {
        return upload;
    }

    public void setUpload(Boolean upload) {
        this.upload = upload;
    }

    public Boolean getFace() {
        return face;
    }

    public void setFace(Boolean face) {
        this.face = face;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public static final class Builder {
        private int pageNum;
        private int pageSize;
        private String identifyNumber;
        private String phone;
        private String name;
        private Boolean upload;
        private Boolean face;
        private String status;
        private String type;

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

        public Builder upload(Boolean val) {
            upload = val;
            return this;
        }

        public Builder face(Boolean val) {
            face = val;
            return this;
        }

        public Builder status(String val) {
            status = val;
            return this;
        }

        public Builder type(String val) {
            type = val;
            return this;
        }

        public PersonSearch build() {
            return new PersonSearch(this);
        }
    }
}
