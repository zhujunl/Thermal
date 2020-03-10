package com.miaxis.thermal.data.repository;

import android.text.TextUtils;

import com.miaxis.thermal.data.dto.PersonDto;
import com.miaxis.thermal.data.entity.Config;
import com.miaxis.thermal.data.entity.Person;
import com.miaxis.thermal.data.entity.PersonSearch;
import com.miaxis.thermal.data.exception.MyException;
import com.miaxis.thermal.data.exception.NetResultFailedException;
import com.miaxis.thermal.data.model.PersonModel;
import com.miaxis.thermal.data.net.ResponseEntity;
import com.miaxis.thermal.data.net.ThermalApi;
import com.miaxis.thermal.manager.ConfigManager;
import com.miaxis.thermal.util.DateUtil;
import com.miaxis.thermal.util.FileUtil;
import com.miaxis.thermal.util.ValueUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

public class PersonRepository {

    private PersonRepository() {
    }

    public static PersonRepository getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static final PersonRepository instance = new PersonRepository();
    }

    /**
     * ================================ 静态内部类单例写法 ================================
     **/

    public List<Person> downloadPerson() throws IOException, MyException, NetResultFailedException {
        Config config = ConfigManager.getInstance().getConfig();
        String url = config.getHost() + config.getDownloadPersonPath();
        String mac = config.getMac();
        long timeStamp = config.getTimeStamp();
        Response<ResponseEntity<List<PersonDto>>> execute = ThermalApi.downloadPerson(url,
                timeStamp,
                mac,
                ValueUtil.PAGE_SIZE)
                .execute();
        try {
            ResponseEntity<List<PersonDto>> body = execute.body();
            if (body != null) {
                if (TextUtils.equals(body.getCode(), ValueUtil.SUCCESS) && body.getData() != null) {
                    return transformPersonDtoList(body.getData());
                } else if (!TextUtils.equals(body.getCode(), ValueUtil.SUCCESS)) {
                    throw new NetResultFailedException("服务端返回，" + body.getMessage());
                }
            }
        } catch (NetResultFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new MyException(e.getMessage());
        }
        throw new MyException("服务端返回数据解析失败，或为空");
    }

    public void updatePerson(Person person) throws IOException, MyException, NetResultFailedException {
        Config config = ConfigManager.getInstance().getConfig();
        String url = config.getHost() + config.getUpdatePersonPath();
        String mac = config.getMac();
        File faceFile = new File(person.getFacePicturePath());
        Response<ResponseEntity> execute = ThermalApi.updatePerson(url,
                mac,
                person.getName(),
                person.getIdentifyNumber(),
                person.getPhone(),
                person.getType(),
                DateUtil.DATE_FORMAT.format(person.getEffectiveTime()),
                DateUtil.DATE_FORMAT.format(person.getInvalidTime()),
                person.getFaceFeature(),
                person.getMaskFaceFeature(),
                faceFile)
                .execute();
        try {
            ResponseEntity body = execute.body();
            if (body != null) {
                if (TextUtils.equals(body.getCode(), ValueUtil.SUCCESS)) {
                    return;
                } else if (!TextUtils.equals(body.getCode(), ValueUtil.SUCCESS)) {
                    throw new NetResultFailedException("服务端返回，" + body.getMessage());
                }
            }
        } catch (NetResultFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new MyException(e.getMessage());
        }
        throw new MyException("服务端返回数据解析失败，或为空");
    }

    public void savePerson(Person person) {
        PersonModel.savePerson(person);
    }

    public List<Person> loadAll() {
        return PersonModel.loadAll();
    }

    public List<Person> loadPersonByPage(int pageNum, int pageSize) {
        return PersonModel.loadPersonByPage(pageNum, pageSize);
    }

    public Person findPerson(String field) {
        return PersonModel.findPerson(field);
    }

    public int loadPersonCount() {
        return PersonModel.loadPersonCount();
    }

    public Person findOldestRecord() {
        return PersonModel.findOldestPerson();
    }

    public List<Person> searchPerson(PersonSearch personSearch) {
        return PersonModel.searchPerson(personSearch);
    }

    public void clearAll() {
        PersonModel.deleteAll();
        FileUtil.deleteDirectory(new File(FileUtil.FACE_STOREHOUSE_PATH));
    }

    public void deletePerson(Person person) {
        PersonModel.deletePerson(person);
        FileUtil.deleteImg(person.getFacePicturePath());
    }

    private List<Person> transformPersonDtoList(List<PersonDto> dtoList) throws MyException {
        List<Person> personList = new ArrayList<>();
        for (PersonDto personDto : dtoList) {
            personList.add(personDto.transform());
        }
        return personList;
    }

}
