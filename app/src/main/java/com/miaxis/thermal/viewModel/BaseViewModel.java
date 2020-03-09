package com.miaxis.thermal.viewModel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.miaxis.thermal.bridge.SingleLiveEvent;
import com.miaxis.thermal.data.exception.MyException;
import com.miaxis.thermal.data.exception.NetResultFailedException;
import com.miaxis.thermal.manager.ToastManager;
import com.miaxis.thermal.util.ValueUtil;

public class BaseViewModel extends ViewModel {

    public MutableLiveData<String> waitMessage = new SingleLiveEvent<>();
    public MutableLiveData<String> resultMessage = new SingleLiveEvent<>();
    public MutableLiveData<ToastManager.ToastBody> toast = new SingleLiveEvent<>();

    protected String handleError(Throwable throwable) {
        throwable.printStackTrace();
        Log.e("asd", "" + throwable.getMessage());
        if (ValueUtil.isNetException(throwable)) {
            return "联网错误";
        } else if (throwable instanceof NetResultFailedException) {
            return throwable.getMessage();
        } else if (throwable instanceof MyException) {
            return throwable.getMessage();
        } else {
            return "出现错误";
        }
    }

}
