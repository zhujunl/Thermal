package com.miaxis.thermal.util;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.miaxis.thermal.R;
import com.miaxis.thermal.app.App;
import com.miaxis.thermal.manager.strategy.Sign;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.HttpException;

public class ValueUtil {

    public static final Sign DEFAULT_SIGN = Sign.TPS980P_T;

    public static final Gson GSON = new Gson();

    public static final String DEFAULT_SERVER_MODE = "0";//0-联网版，1-单机版
//    public static final String DEFAULT_HOST = "http://47.114.39.63:9008/saasbus/";
//    public static final String DEFAULT_HOST = "http://183.129.171.153:9221/saasbus/";
//    public static final String DEFAULT_HOST = "http://115.236.80.98:8085/saasbus/";
//    public static final String DEFAULT_HOST = "http://192.168.5.210:8086/attence/";
//    public static final String DEFAULT_HOST = "https://www.miaxisatd.com/";
    public static final String DEFAULT_HOST = "http://192.168.6.32:9000/";
    public static final String DEFAULT_DOWNLOAD_PERSON_PATH = "api/v1/attence/person/downPersonListByDeviceMac";
//    public static final String DEFAULT_DOWNLOAD_PERSON_PATH = "api/v1/attendance/person/downPersonListByDeviceMac";
    public static final String DEFAULT_UPDATE_PERSON_PATH = "api/v1/attence/person/updatePerson";
//    public static final String DEFAULT_UPDATE_PERSON_PATH = "api/v1/attendance/person/updatePerson";
    public static final String DEFAULT_UPLOAD_RECORD_PATH = "api/v1/attence/record/uploadRecordFromApp";
//    public static final String DEFAULT_UPLOAD_RECORD_PATH = "api/v1/attendance/record/uploadRecordFromApp";
//    public static final String DEFAULT_UPLOAD_RECORD_PATH = "api/v1/attence/personRecord/uploadRecordFromApp";
    public static final boolean DEFAULT_CAMERA_SHOW = false; //true:近红外，false:可见光
    public static final boolean DEFAULT_CAMERA_FACE = false; //true:近红外，false:可见光
//    public static final boolean DEFAULT_LIVENESS = false;
    public static final boolean DEFAULT_LIVENESS = true;
    public static final int DEFAULT_QUALITY_SCORE = 50;
    public static final int DEFAULT_REGISTER_QUALITY_SCORE = 80;
    public static final int DEFAULT_MASK_SCORE = 40;
    public static final boolean DEFAULT_FORCED_MASK = false;
    public static final boolean DEFAULT_STRANGER_RECORD = false;
//    public static final boolean DEFAULT_DEVICE_MODE = true; // t考勤/f闸机
    public static final boolean DEFAULT_DEVICE_MODE = false; // t考勤/f闸机
    public static final boolean DEFAULT_ACCESS_SIGN = true; // t进/f出
    public static final boolean DEFAULT_GATE_LIMIT = false;
    public static final boolean DEFAULT_ID_CARD_ENTRY = false;
    public static final boolean DEFAULT_ID_CARD_VERIFY = true;
//    public static final float DEFAULT_VERIFY_SCORE = 0.76f;
    public static final float DEFAULT_VERIFY_SCORE = 0.80f;
//    public static final float DEFAULT_MASK_VERIFY_SCORE = 0.73f;
    public static final float DEFAULT_MASK_VERIFY_SCORE = 0.76f;
    public static final int DEFAULT_LIVENESS_SCORE = 80;
    public static final int DEFAULT_PUPIL_DISTANCE_MIN_HORIZONTAL = 70;
    public static final int DEFAULT_PUPIL_DISTANCE_MAX_HORIZONTAL = 100;
    public static final int DEFAULT_PUPIL_DISTANCE_MIN_VERTICAL = 50;
    public static final int DEFAULT_PUPIL_DISTANCE_MAX_VERTICAL = 70;
    public static final int DEFAULT_PUPIL_DISTANCE_MIN_NO_LIMIT = 25;
    public static final int DEFAULT_PUPIL_DISTANCE_MAX_NO_LIMIT = 150;
    public static final int DEFAULT_DORMANCY_INTERVAL = 2;
    public static final int DEFAULT_DORMANCY_TIME = 180;
    public static final float DEFAULT_FEVER_SCORE = 37.2f;
    public static final float DEFAULT_TEMP_SCORE = 34.0f;
    public static final boolean DEFAULT_HEAT_MAP = false;
    public static final boolean DEFAULT_TEMP_REAL_TIME = false;
    public static final int DEFAULT_HEART_BEAT_INTERVAL = 300;
    public static final int DEFAULT_FAILED_QUERY_COLD = 10;
    public static final int DEFAULT_RECORD_CLEAR_THRESHOLD = 10000;
    public static final int DEFAULT_VERIFY_COLD = 3;
    public static final int DEFAULT_FAILED_VERIFY_COLD = 2;
    public static final int DEFAULT_FLASH_TIME = 5;
    public static final String DEFAULT_DEVICE_PASSWORD = "666666";
    public static final long DEFAULT_TIME_STAMP = 0;

    public static final boolean DEFAULT_TIMING_SWITCH = false;
    public static final String DEFAULT_SWITCH_START_TIME = "08:00";
    public static final String DEFAULT_SWITCH_END_TIME = "18:00";

    public static final String SUCCESS = "200";

    public static final int PAGE_SIZE = 10;

    public static final String WORK_MODE_NET = "0";
    public static final String WORK_MODE_LOCAL = "1";

    public static final String PERSON_TYPE_WORKER = "00601";
    public static final String PERSON_TYPE_VISITOR = "00602";
    public static final String PERSON_TYPE_BLACK = "00603";

    public static final String PERSON_STATUS_READY = "1";
    public static final String PERSON_STATUS_DELETE = "2";

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
                return App.getInstance().getString(R.string.person_type_staff);
            case PERSON_TYPE_VISITOR:
                return App.getInstance().getString(R.string.person_type_visitor);
            case PERSON_TYPE_BLACK:
                return App.getInstance().getString(R.string.person_type_black);
        }
        return "";
    }

    public static String getAccessSignName(String accessSign) {
        if (TextUtils.isEmpty(accessSign)) return "";
        switch (accessSign) {
            case "0":
                return "进";
            case "1":
                return "出";
        }
        return "";
    }

    private static final char[] encodeTable = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
            'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9'};

    public static String getRandomString(int len) {
        String returnStr = "";
        char[] ch = new char[len];
        Random rd = new Random();
        for (int i = 0; i < len; i++) {
            ch[i] = (char) (rd.nextInt(9) + 65);
            ch[i] = encodeTable[rd.nextInt(36)];
        }
        returnStr = new String(ch);
        return returnStr;
    }

}
