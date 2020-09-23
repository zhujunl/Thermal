package com.miaxis.thermal.data.dto;

import com.miaxis.thermal.data.entity.Update;
import com.miaxis.thermal.data.exception.MyException;

public class UpdateDto implements Mapper<Update> {

    private int id;
    private String url;
    private String version;
    private String name;
    private String content;
    private String uploadTime;

    public UpdateDto() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(String uploadTime) {
        this.uploadTime = uploadTime;
    }

    @Override
    public Update transform() throws MyException {
        try {
            return new Update.Builder()
                    .versionCode(version)
                    .versionName(name)
                    .content(content)
                    .url(url)
                    .updateTime(uploadTime)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            throw new MyException("应用更新信息解码失败，原因：" + e.getMessage());
        }
    }
}
