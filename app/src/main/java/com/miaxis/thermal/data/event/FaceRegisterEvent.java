package com.miaxis.thermal.data.event;

import android.graphics.Bitmap;

public class FaceRegisterEvent {

    private String feature;
    private Bitmap bitmap;

    public FaceRegisterEvent(String feature, Bitmap bitmap) {
        this.feature = feature;
        this.bitmap = bitmap;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
