package com.miaxis.thermal.manager;

import android.app.Application;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.services.weather.LocalWeatherForecastResult;
import com.amap.api.services.weather.LocalWeatherLive;
import com.amap.api.services.weather.LocalWeatherLiveResult;
import com.amap.api.services.weather.WeatherSearch;
import com.amap.api.services.weather.WeatherSearchQuery;

import org.greenrobot.eventbus.EventBus;

public class AmapManager implements AMapLocationListener, WeatherSearch.OnWeatherSearchListener {

    private AmapManager() {}

    public static AmapManager getInstance () {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static final AmapManager instance = new AmapManager();
    }

    /** ================================ 静态内部类单例写法 ================================ **/

    private Application application;
    private AMapLocationClient aMapLocationClient;
    private AMapLocation aMapLocation;
    private LocalWeatherLive weatherLive;
    private String weather;

    private OnWeatherListener listener;

    /**
     * 开始定位
     */
    public void startLocation(Application application) {
        this.application = application;
        aMapLocationClient = new AMapLocationClient(application);
        aMapLocationClient.setLocationListener(this);
        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        mLocationOption.setInterval(1000 * 60 * 60);
        aMapLocationClient.setLocationOption(mLocationOption);
        aMapLocationClient.startLocation();
    }

    /**
     * 定位回调，回调后查询天气信息
     * @param aMapLocation
     */
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
            this.aMapLocation = aMapLocation;
            queryWeather(aMapLocation.getCity());
        }
    }

    /**
     * 天气查询回调
     * @param localWeatherLiveResult
     * @param rCode
     */
    @Override
    public void onWeatherLiveSearched(LocalWeatherLiveResult localWeatherLiveResult, int rCode ) {
        if (rCode  == 1000) {
            if (localWeatherLiveResult != null && localWeatherLiveResult.getLiveResult() != null) {
                this.weatherLive = localWeatherLiveResult.getLiveResult();
                this.weather = String.format("%s %s℃", weatherLive.getWeather(), weatherLive.getTemperature());
                if (listener != null) {
                    listener.onWeather(weather);
                }
            }
        }
    }

    @Override
    public void onWeatherForecastSearched(LocalWeatherForecastResult localWeatherForecastResult, int i) {
        // 忽略天气预报信息
    }

    private void queryWeather(String city) {
        WeatherSearchQuery mQuery = new WeatherSearchQuery(city, WeatherSearchQuery.WEATHER_TYPE_LIVE);
        WeatherSearch mWeatherSearch = new WeatherSearch(application);
        mWeatherSearch.setOnWeatherSearchListener(this);
        mWeatherSearch.setQuery(mQuery);
        mWeatherSearch.searchWeatherAsyn(); //异步搜索
    }

    public void setListener(OnWeatherListener listener) {
        this.listener = listener;
    }

    public AMapLocation getaMapLocation() {
        return aMapLocation;
    }

    public LocalWeatherLive getWeatherLive() {
        return weatherLive;
    }

    public String getWeather() {
        return weather;
    }

    public interface OnWeatherListener {
        void onWeather(String weather);
    }

}
