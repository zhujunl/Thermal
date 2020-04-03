package com.miaxis.thermal.viewModel;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import com.miaxis.thermal.bridge.SingleLiveEvent;
import com.miaxis.thermal.data.entity.IDCardMessage;
import com.miaxis.thermal.data.entity.Person;
import com.miaxis.thermal.data.exception.MyException;
import com.miaxis.thermal.data.repository.PersonRepository;
import com.miaxis.thermal.manager.CardManager;
import com.miaxis.thermal.manager.PersonManager;
import com.miaxis.thermal.manager.strategy.Sign;
import com.miaxis.thermal.util.DateUtil;
import com.miaxis.thermal.util.FileUtil;
import com.miaxis.thermal.util.ValueUtil;

import java.io.File;
import java.util.Date;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class AddPersonViewModel extends BaseViewModel {

    public ObservableField<String> name = new ObservableField<>();
    public ObservableField<String> number = new ObservableField<>();
    public ObservableField<String> phone = new ObservableField<>();
    public ObservableField<String> effectTime = new ObservableField<>(DateUtil.DATE_FORMAT.format(new Date()));
    public ObservableField<String> invalidTime = new ObservableField<>("请选择失效日期");
    public ObservableField<Boolean> type = new ObservableField<>(true);
    public ObservableField<String> faceFeatureHint = new ObservableField<>("点击采集");

    public MutableLiveData<Boolean> registerFlag = new SingleLiveEvent<>();
    public MutableLiveData<Boolean> initCard = new SingleLiveEvent<>();
    public MutableLiveData<Boolean> cardStatus = new SingleLiveEvent<>();

    private String featureCache;
    private String maskFeatureCache;
    private Bitmap headerCache;

    private Person personCache;

    public AddPersonViewModel() {
    }

    public boolean checkFaceInfo() {
        if (TextUtils.isEmpty(featureCache)
                || TextUtils.isEmpty(maskFeatureCache)
                || headerCache == null) {
            return false;
        }
        return true;
    }

    public void savePerson() {
        if (personCache == null) {
            addPerson();
        } else {
            updatePerson(personCache);
        }
    }

    private void addPerson() {
        waitMessage.setValue("正在保存，请稍后...");
        Observable.create((ObservableOnSubscribe<Person>) emitter -> {
            Person person = new Person.Builder()
                    .id(null)
                    .identifyNumber(number.get())
                    .phone(phone.get())
                    .name(name.get())
                    .effectiveTime(DateUtil.DATE_FORMAT.parse(effectTime.get()))
                    .invalidTime(DateUtil.DATE_FORMAT.parse(invalidTime.get()))
                    .faceFeature(featureCache)
                    .maskFaceFeature(maskFeatureCache)
                    .timeStamp(new Date().getTime())
                    .type(type.get() ? ValueUtil.PERSON_TYPE_WORKER : ValueUtil.PERSON_TYPE_VISITOR)
                    .upload(false)
                    .updateTime(new Date())
                    .status("1")
                    .build();
            emitter.onNext(person);
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnNext(person -> {
                    Person findPerson = PersonRepository.getInstance().findPerson(person.getIdentifyNumber());
                    if (findPerson == null) {
                        findPerson = PersonRepository.getInstance().findPerson(person.getPhone());
                        if (findPerson != null) {
                            throw new MyException("该手机号码已重复");
                        }
                    } else {
                        throw new MyException("该身份证号码已重复");
                    }
                })
                .doOnNext(person -> {
                    String filePath = FileUtil.FACE_STOREHOUSE_PATH + File.separator + person.getName() + "-" + person.getIdentifyNumber() + "-" + System.currentTimeMillis() + ".jpg";
                    FileUtil.saveBitmap(headerCache, filePath);
                    person.setFacePicturePath(filePath);
                    PersonRepository.getInstance().savePerson(person);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(person -> {
                    waitMessage.setValue("人员保存成功，正在尝试上传...");
                    uploadPerson(person);
                }, throwable -> {
                    waitMessage.setValue("");
                    Log.e("asd", "" + throwable.getMessage());
                    resultMessage.setValue(throwable.getMessage());
                });
    }

    private void updatePerson(Person mPerson) {
        waitMessage.setValue("正在更新，请稍后...");
        Observable.just(mPerson)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnNext(person -> {
                    String filePath = FileUtil.FACE_STOREHOUSE_PATH + File.separator + person.getName() + "-" + person.getIdentifyNumber() + "-" + System.currentTimeMillis() + ".jpg";
                    FileUtil.saveBitmap(headerCache, filePath);
                    FileUtil.deleteImg(person.getFacePicturePath());
                    person.setFacePicturePath(filePath);
                    person.setFaceFeature(featureCache);
                    person.setMaskFaceFeature(maskFeatureCache);
                    person.setUpload(false);
                    person.setUpdateTime(new Date());
                    PersonRepository.getInstance().savePerson(person);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(person -> {
                    waitMessage.setValue("人员保存成功，正在尝试上传...");
                    uploadPerson(person);
                }, throwable -> {
                    waitMessage.setValue("");
                    Log.e("asd", "" + throwable.getMessage());
                    resultMessage.setValue(throwable.getMessage());
                });
    }

    private void uploadPerson(Person person) {
        Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            PersonRepository.getInstance().updatePerson(person);
            emitter.onNext(Boolean.TRUE);
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnNext(result -> {
                    person.setUpload(true);
                    PersonRepository.getInstance().savePerson(person);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    waitMessage.setValue("");
                    resultMessage.setValue("人员信息已上传");
                    PersonManager.getInstance().startUploadPerson();
                }, throwable -> {
                    waitMessage.setValue("");
                    resultMessage.setValue("人员信息上传失败，已缓存至本地，将自动尝试续传");
                });
    }

    public void setFeatureCache(String featureCache) {
        this.featureCache = featureCache;
    }

    public void setHeaderCache(Bitmap headerCache) {
        this.headerCache = headerCache;
    }

    public void setPersonCache(Person personCache) {
        this.personCache = personCache;
    }

    public void setMaskFeatureCache(String maskFeatureCache) {
        this.maskFeatureCache = maskFeatureCache;
    }

    private CardManager.OnCardReadListener cardListener = idCardMessage -> {
        if (idCardMessage != null) {
            name.set(idCardMessage.getName());
            number.set(idCardMessage.getCardNumber());
//                CardManager.getInstance().setNeedReadCard(true);
        }
    };

    public CardManager.OnCardStatusListener statusListener = result -> {
        if (result) {
            cardStatus.postValue(Boolean.TRUE);
            CardManager.getInstance().startReadCard(cardListener);
        } else {
            cardStatus.postValue(Boolean.FALSE);
        }
    };

}
