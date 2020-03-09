package com.miaxis.thermal.data.entity;

public class WebServerRequest<T> {

    private String request;
    private T parameter;

    public WebServerRequest() {
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public T getParameter() {
        return parameter;
    }

    public void setParameter(T parameter) {
        this.parameter = parameter;
    }
}
