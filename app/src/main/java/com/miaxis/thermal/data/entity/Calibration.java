package com.miaxis.thermal.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Calibration {

    @PrimaryKey
    private Long id;
    private int xhnEmissivity;
    private int xhnModel;

    public Calibration() {
    }

    private Calibration(Builder builder) {
        setId(builder.id);
        setXhnEmissivity(builder.xhnEmissivity);
        setXhnModel(builder.xhnModel);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getXhnEmissivity() {
        return xhnEmissivity;
    }

    public void setXhnEmissivity(int xhnEmissivity) {
        this.xhnEmissivity = xhnEmissivity;
    }

    public int getXhnModel() {
        return xhnModel;
    }

    public void setXhnModel(int xhnModel) {
        this.xhnModel = xhnModel;
    }

    public static final class Builder {
        private Long id;
        private int xhnEmissivity;
        private int xhnModel;

        public Builder() {
        }

        public Builder id(Long val) {
            id = val;
            return this;
        }

        public Builder xhnEmissivity(int val) {
            xhnEmissivity = val;
            return this;
        }

        public Builder xhnModel(int val) {
            xhnModel = val;
            return this;
        }

        public Calibration build() {
            return new Calibration(this);
        }
    }
}
