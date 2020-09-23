package com.miaxis.thermal.data.entity;

public class Update {

    private String versionCode;
    private String versionName;
    private String content;
    private String url;
    private String updateTime;

    public Update() {
    }

    private Update(Builder builder) {
        setVersionCode(builder.versionCode);
        setVersionName(builder.versionName);
        setContent(builder.content);
        setUrl(builder.url);
        setUpdateTime(builder.updateTime);
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public static final class Builder {
        private String versionCode;
        private String versionName;
        private String content;
        private String url;
        private String updateTime;

        public Builder() {
        }

        public Builder versionCode(String val) {
            versionCode = val;
            return this;
        }

        public Builder versionName(String val) {
            versionName = val;
            return this;
        }

        public Builder content(String val) {
            content = val;
            return this;
        }

        public Builder url(String val) {
            url = val;
            return this;
        }

        public Builder updateTime(String val) {
            updateTime = val;
            return this;
        }

        public Update build() {
            return new Update(this);
        }
    }
}
