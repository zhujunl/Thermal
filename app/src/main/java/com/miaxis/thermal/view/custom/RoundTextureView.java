package com.miaxis.thermal.view.custom;

import android.content.Context;
import android.graphics.Outline;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.TextureView;
import android.view.View;
import android.view.ViewOutlineProvider;

public class RoundTextureView extends TextureView {

    private int radius = 0;

    public RoundTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                Rect rect = new Rect(4, 4, view.getMeasuredWidth() - 4, view.getMeasuredHeight() - 4);
                outline.setRoundRect(rect, radius);
            }
        });
        setClipToOutline(true);
    }

    public void turnRound() {
        invalidateOutline();
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getRadius() {
        return radius;
    }
}