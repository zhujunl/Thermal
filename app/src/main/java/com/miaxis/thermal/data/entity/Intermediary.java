package com.miaxis.thermal.data.entity;

import org.zz.api.MXFaceInfoEx;

public class Intermediary {

    public int width;
    public int height;
    public MXFaceInfoEx mxFaceInfoEx;
    public byte[] data;
    public byte[] liveness;

    public Intermediary() {
    }

    public Intermediary(Intermediary intermediary) {
        this.width = intermediary.width;
        this.height = intermediary.height;
        this.mxFaceInfoEx = new MXFaceInfoEx(intermediary.mxFaceInfoEx);
        if (intermediary.data != null && intermediary.data.length != 0) {
            this.data = new byte[intermediary.data.length];
            System.arraycopy(intermediary.data, 0, data, 0, this.data.length);
        }
        if (intermediary.liveness != null && intermediary.liveness.length != 0) {
            this.liveness = new byte[intermediary.liveness.length];
            System.arraycopy(intermediary.liveness, 0, liveness, 0, this.liveness.length);
        }
    }

}
