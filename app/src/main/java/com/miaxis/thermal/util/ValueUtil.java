package com.miaxis.thermal.util;

import android.text.TextUtils;

import com.google.gson.Gson;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.HttpException;

public class ValueUtil {

    public static final Gson GSON = new Gson();

    public static final String DEFAULT_SERVER_MODE = "0";//0-主机+客户端，1-仅主机，2-仅客户端
    public static final String DEFAULT_HOST = "http://47.114.39.63:9008/saasbus/";
    public static final String DEFAULT_DOWNLOAD_PERSON_PATH = "api/v1/attence/person/downPersonListByDeviceMac";
    public static final String DEFAULT_UPDATE_PERSON_PATH = "api/v1/attence/person/updatePerson";
    public static final String DEFAULT_UPLOAD_RECORD_PATH = "api/v1/attence/record/uploadRecordFromApp";
    public static final boolean DEFAULT_CAMERA_SHOW = false; //true:近红外，false:可见光
    public static final boolean DEFAULT_CAMERA_FACE = false; //true:近红外，false:可见光
    public static final boolean DEFAULT_LIVENESS = false;
    public static final int DEFAULT_QUALITY_SCORE = 50;
    public static final float DEFAULT_VERIFY_SCORE = 0.76f;
    public static final int DEFAULT_LIVENESS_SCORE = 80;
    public static final int DEFAULT_PUPIL_DISTANCE = 20;
    public static final int DEFAULT_HEART_BEAT_INTERVAL = 300;
    public static final int DEFAULT_FAILED_QUERY_COLD = 5;
    public static final int DEFAULT_RECORD_CLEAR_THRESHOLD = 10000;
    public static final int DEFAULT_VERIFY_COLD = 3;
    public static final int DEFAULT_FLASH_TIME = 10;
    public static final String DEFAULT_DEVICE_PASSWORD = "666666";
    public static final long DEFAULT_TIME_STAMP = 0;

    public static final String SUCCESS = "200";

    public static final int PAGE_SIZE = 10;

    public static final String PERSON_TYPE_WORKER = "00601";
    public static final String PERSON_TYPE_VISITOR = "00602";
    public static final String PERSON_TYPE_BLACK = "00603";

    public static final String PERSON_TYPE_WORKER_NAME = "员工";
    public static final String PERSON_TYPE_VISITOR_NAME = "访客";
    public static final String PERSON_TYPE_BLACK_NAME = "黑名单";

    public static boolean isNetException(Throwable throwable) {
        return throwable instanceof SocketTimeoutException
                || throwable instanceof ConnectException
                || throwable instanceof HttpException
                || throwable instanceof com.jakewharton.retrofit2.adapter.rxjava2.HttpException;
    }

    public static boolean isHttpFormat(String str) {
        Pattern pattern = Pattern.compile("^(http://)([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}(:)(\\d{1,5})(/)");
        Matcher match = pattern.matcher(str);
        return match.matches();
    }

    public static String unicode2String(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length / 2; i++) {
            int a = bytes[2 * i + 1];
            if (a < 0) {
                a = a + 256;
            }
            int b = bytes[2 * i];
            if (b < 0) {
                b = b + 256;
            }
            int c = (a << 8) | b;
            sb.append((char) c);
        }
        return sb.toString();
    }

    public static String fingerPositionCovert(byte finger) {
        switch ((int) finger) {
            case 11:
                return "右手拇指";
            case 12:
                return "右手食指";
            case 13:
                return "右手中指";
            case 14:
                return "右手环指";
            case 15:
                return "右手小指";
            case 16:
                return "左手拇指";
            case 17:
                return "左手食指";
            case 18:
                return "左手中指";
            case 19:
                return "左手环指";
            case 20:
                return "左手小指";
            case 97:
                return "右手不确定指位";
            case 98:
                return "左手不确定指位";
            case 99:
                return "其他不确定指位";
            default:
                return "其他不确定指位";
        }
    }

    public static String getPersonTypeName(String personType) {
        if (TextUtils.isEmpty(personType)) return "";
        switch (personType) {
            case PERSON_TYPE_WORKER:
                return PERSON_TYPE_WORKER_NAME;
            case PERSON_TYPE_VISITOR:
                return PERSON_TYPE_VISITOR_NAME;
            case PERSON_TYPE_BLACK:
                return PERSON_TYPE_BLACK_NAME;
        }
        return "";
    }

}
