package com.miaxis.thermal.bridge;

import android.content.Context;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

/**
 * Created by tang.yf on 2018/8/22.
 */

/**
 * 设置缓存路径
 */
@GlideModule
public class MyGlideAppModule extends AppGlideModule {

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
    }

    //    针对V4用户可以提升速度
    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }

}
