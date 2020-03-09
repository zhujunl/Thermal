package com.miaxis.thermal.data.entity;

import org.zz.api.MXFaceInfoEx;

public class FaceDraw {

    private int faceNum;
    private MXFaceInfoEx[] mxFaceInfoExes;

    public FaceDraw() {
    }

    public FaceDraw(int faceNum, MXFaceInfoEx[] mxFaceInfoExes) {
        this.faceNum = faceNum;
        this.mxFaceInfoExes = mxFaceInfoExes;
    }

    public int getFaceNum() {
        return faceNum;
    }

    public void setFaceNum(int faceNum) {
        this.faceNum = faceNum;
    }

    public MXFaceInfoEx[] getMxFaceInfoExes() {
        return mxFaceInfoExes;
    }

    public void setMxFaceInfoExes(MXFaceInfoEx[] mxFaceInfoExes) {
        this.mxFaceInfoExes = mxFaceInfoExes;
    }
}
