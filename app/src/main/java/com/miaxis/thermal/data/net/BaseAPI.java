package com.miaxis.thermal.data.net;

import android.text.TextUtils;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.miaxis.thermal.data.exception.MyException;
import com.miaxis.thermal.manager.ConfigManager;
import com.miaxis.thermal.util.ValueUtil;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class BaseAPI {

    protected final static Retrofit.Builder RETROFIT_BUILDER = new Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create());

    protected static Retrofit retrofit;

    protected static Retrofit getRetrofit() {
        return retrofit;
    }

    protected static ThermalNet getThermalNetSync() {
        return getRetrofit().create(ThermalNet.class);
    }

    public static void rebuildRetrofit() {
        retrofit = RETROFIT_BUILDER
                .baseUrl(ConfigManager.getInstance().getConfig().getHost())
                .build();
    }


}
