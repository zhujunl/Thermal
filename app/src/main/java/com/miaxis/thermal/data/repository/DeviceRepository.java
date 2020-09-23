package com.miaxis.thermal.data.repository;

import android.text.TextUtils;

import com.miaxis.thermal.data.entity.Update;
import com.miaxis.thermal.data.dto.UpdateDto;
import com.miaxis.thermal.data.exception.MyException;
import com.miaxis.thermal.data.exception.NetResultFailedException;
import com.miaxis.thermal.data.net.ResponseEntity;
import com.miaxis.thermal.data.net.ThermalApi;
import com.miaxis.thermal.util.ValueUtil;

import java.io.IOException;

import retrofit2.Response;

public class DeviceRepository extends BaseRepository {

    private DeviceRepository() {
    }

    public static DeviceRepository getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static final DeviceRepository instance = new DeviceRepository();
    }

    /**
     * ================================ 静态内部类单例写法 ================================
     **/

    public Update updateApp(String versionName) throws IOException, MyException, NetResultFailedException {
        Response<ResponseEntity<UpdateDto>> execute = ThermalApi.updateApp(versionName).execute();
        try {
            ResponseEntity<UpdateDto> body = execute.body();
            if (body != null) {
                if (TextUtils.equals(body.getCode(), ValueUtil.SUCCESS) && body.getData() != null) {
                    return body.getData().transform();
                } else {
                    throw new NetResultFailedException("服务端返回，" + body.getMessage());
                }
            }
        }  catch (NetResultFailedException e) {
            throw e;
        }  catch (Exception e) {
            e.printStackTrace();
            throw new MyException(e.getMessage());
        }
        throw new MyException("服务端返回数据解析失败，或为空");
    }

}
