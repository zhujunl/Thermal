package com.miaxis.thermal.data.entity;

public class PhotoFaceFeature {

    private byte[] faceFeature;
    private byte[] maskFaceFeature;
    private String message;

    public PhotoFaceFeature(String message) {
        this.message = message;
    }

    public PhotoFaceFeature(byte[] faceFeature, byte[] maskFaceFeature, String message) {
        this.faceFeature = faceFeature;
        this.maskFaceFeature = maskFaceFeature;
        this.message = message;
    }

    public byte[] getFaceFeature() {
        return faceFeature;
    }

    public void setFaceFeature(byte[] faceFeature) {
        this.faceFeature = faceFeature;
    }

    public byte[] getMaskFaceFeature() {
        return maskFaceFeature;
    }

    public void setMaskFaceFeature(byte[] maskFaceFeature) {
        this.maskFaceFeature = maskFaceFeature;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
