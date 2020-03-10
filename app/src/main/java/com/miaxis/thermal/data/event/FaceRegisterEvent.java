package com.miaxis.thermal.data.event;

import android.graphics.Bitmap;

public class FaceRegisterEvent {

    private String faceFeature;
    private String maskFaceFeature;
    private Bitmap bitmap;

    public FaceRegisterEvent(String faceFeature, String maskFaceFeature, Bitmap bitmap) {
        this.faceFeature = faceFeature;
        this.maskFaceFeature = maskFaceFeature;
        this.bitmap = bitmap;
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

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
