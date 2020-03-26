package com.miaxis.thermal.viewModel;

import android.text.TextUtils;
import android.util.Log;

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import com.miaxis.thermal.bridge.SingleLiveEvent;
import com.miaxis.thermal.data.entity.Person;
import com.miaxis.thermal.data.entity.PersonSearch;
import com.miaxis.thermal.data.exception.MyException;
import com.miaxis.thermal.data.repository.PersonRepository;
import com.miaxis.thermal.manager.PersonManager;
import com.miaxis.thermal.manager.ToastManager;
import com.miaxis.thermal.util.ValueUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class PersonViewModel extends BaseViewModel {

    public MutableLiveData<List<Person>> personListLiveData = new MutableLiveData<>(new ArrayList<>());
    public MutableLiveData<Integer> personCountLiveData = new MutableLiveData<>();

    public MutableLiveData<Boolean> refreshing = new SingleLiveEvent<>();
    public MutableLiveData<Boolean> updating = new SingleLiveEvent<>();

    public PersonViewModel() {
    }

    public List<Person> getPersonList() {
        List<Person> value = personListLiveData.getValue();
        if (value == null) {
            List<Person> newArrayList = new ArrayList<>();
            personListLiveData.setValue(newArrayList);
            return newArrayList;
        } else {
            return value;
        }
    }

    public void loadPersonByPage(PersonSearch personSearch) {
        Disposable disposable = Observable.create((ObservableOnSubscribe<List<Person>>) emitter -> {
            List<Person> personList = PersonRepository.getInstance().searchPerson(personSearch);
            if (personList != null) {
                emitter.onNext(personList);
            } else {
                emitter.onError(new MyException("查询结果为空"));
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(personList -> {
                    refreshing.setValue(Boolean.FALSE);
                    if (personSearch.getPageNum() == 1) {
                        personListLiveData.setValue(personList);
                    } else {
                        List<Person> LocalOrderList = getPersonList();
                        LocalOrderList.addAll(personList);
                        personListLiveData.setValue(LocalOrderList);
                    }
                    if (personList.isEmpty()) {
                        toast.setValue(ToastManager.getToastBody("没有更多了", ToastManager.SUCCESS));
                    }
                }, throwable -> {
                    refreshing.setValue(Boolean.FALSE);
                    resultMessage.setValue(handleError(throwable));
                });
    }

    public void updatePersonCount() {
        Disposable disposable = Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
            int count = PersonRepository.getInstance().loadPersonCount();
            emitter.onNext(count);
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(count -> {
                    personCountLiveData.setValue(count);
                }, throwable -> {
                    resultMessage.setValue(handleError(throwable));
                });
    }

    public void changePersonStatus(Person mPerson, boolean enable) {
        waitMessage.setValue("正在执行操作，请稍后...");
        Observable.just(mPerson)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnNext(person -> {
                    person.setUpload(false);
                    person.setUpdateTime(new Date());
                    person.setStatus(enable ? "1" : "2");
                    PersonRepository.getInstance().savePerson(person);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(person -> {
                    waitMessage.setValue("人员更新成功，正在尝试上传...");
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
                    updating.setValue(Boolean.TRUE);
                    waitMessage.setValue("");
                    resultMessage.setValue("人员操作已上传");
                    PersonManager.getInstance().startUploadPerson();
                }, throwable -> {
                    updating.setValue(Boolean.TRUE);
                    waitMessage.setValue("");
                    resultMessage.setValue("人员操作上传失败，已缓存至本地，将自动尝试续传");
                });
    }

}
