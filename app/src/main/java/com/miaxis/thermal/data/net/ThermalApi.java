package com.miaxis.thermal.data.net;

import com.miaxis.thermal.data.dto.PersonDto;

import java.io.File;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

public class ThermalApi extends BaseAPI {

    public static Call<ResponseEntity<List<PersonDto>>> downloadPerson(String url,
                                                                       long timeStamp,
                                                                       String mac,
                                                                       int size) {
        return getThermalNetSync().downloadPerson(url,
                timeStamp,
                mac,
                size);
    }

    public static Call<ResponseEntity> updatePerson(String url,
                                                    String mac,
                                                    String userName,
                                                    String IdentifyNumber,
                                                    String userPhone,
                                                    String userType,
                                                    String startTime,
                                                    String invalidTime,
                                                    String faceFeature,
                                                    File file) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part fileBody = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
        return getThermalNetSync().updatePerson(url,
                mac,
                userName,
                IdentifyNumber,
                userPhone,
                userType,
                startTime,
                invalidTime,
                faceFeature,
                fileBody);
    }

    public static Call<ResponseEntity> uploadRecord(String url,
                                                    String identifyNumber,
                                                    String userName,
                                                    String userPhone,
                                                    String verifyTime,
                                                    float score,
                                                    float temperature,
                                                    String type,
                                                    String mac,
                                                    File file) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part fileBody = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
        return getThermalNetSync().uploadRecord(url,
                identifyNumber,
                userName,
                userPhone,
                verifyTime,
                score,
                temperature,
                type,
                mac,
                fileBody);
    }

}
