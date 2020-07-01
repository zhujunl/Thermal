package com.miaxis.thermal.data.net;

import com.miaxis.thermal.data.dto.PersonDto;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Url;

public interface ThermalNet {

    @FormUrlEncoded
    @POST
    Call<ResponseEntity<List<PersonDto>>> downloadPerson(@Url String url,
                                                         @Field("timeStamp") long timeStamp,
                                                         @Field("mac") String mac,
                                                         @Field("size") int size);

    @Multipart
    @POST
    Call<ResponseEntity> updatePerson(@Url String url,
                                      @Part("mac") String mac,
                                      @Part("userName") String userName,
                                      @Part("IdentifyNumber") String IdentifyNumber,
                                      @Part("userPhone") String userPhone,
                                      @Part("userType") String userType,
                                      @Part("startTime") String startTime,
                                      @Part("invalidTime") String invalidTime,
                                      @Part("faceFeature") String faceFeature,
                                      @Part("maskFaceFeature") String maskFaceFeature,
                                      @Part("status") String status,
                                      @Part MultipartBody.Part faceFile);

    @Multipart
    @POST
    Call<ResponseEntity> uploadRecord(@Url String url,
                                      @Part("identifyNumber") String identifyNumber,
                                      @Part("userName") String userName,
                                      @Part("userPhone") String userPhone,
                                      @Part("verifyTime") String verifyTime,
                                      @Part("score") float score,
                                      @Part("temperature") float temperature,
                                      @Part("type") String type,
                                      @Part("faceType") String faceType,
                                      @Part("mac") String mac,
                                      @Part("access") String access,
                                      @Part MultipartBody.Part file);

}
