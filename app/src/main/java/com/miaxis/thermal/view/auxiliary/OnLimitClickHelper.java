package com.miaxis.thermal.view.auxiliary;

import android.view.View;

import java.util.Calendar;

public class OnLimitClickHelper implements View.OnClickListener {

    public static final int LIMIT_TIME = 1000;
    private long lastClickTime = 0;
    private OnLimitClickListener onLimitClickListener;

    public OnLimitClickHelper(OnLimitClickListener onLimitClickListener){
        this.onLimitClickListener = onLimitClickListener;
    }

    @Override
    public void onClick(View v) {
        long curTime = Calendar.getInstance().getTimeInMillis();
        if (curTime - lastClickTime > LIMIT_TIME || curTime - lastClickTime < 0) {
            lastClickTime = curTime;
            if(onLimitClickListener != null){
                onLimitClickListener.onClick(v);
            }
        }
    }
}