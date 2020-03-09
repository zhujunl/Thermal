package com.miaxis.thermal.viewModel;

import androidx.lifecycle.MutableLiveData;

import com.miaxis.thermal.bridge.SingleLiveEvent;
import com.miaxis.thermal.data.entity.Person;
import com.miaxis.thermal.data.exception.MyException;
import com.miaxis.thermal.data.repository.PersonRepository;
import com.miaxis.thermal.manager.ToastManager;
import com.miaxis.thermal.util.ValueUtil;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class PersonViewModel extends BaseViewModel {

    public MutableLiveData<List<Person>> personListLiveData = new MutableLiveData<>(new ArrayList<>());
    public MutableLiveData<Integer> personCountLiveData = new MutableLiveData<>(0);

    public MutableLiveData<Boolean> refreshing = new SingleLiveEvent<>();

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

    public void loadPersonByPage(int pageNum) {
        Disposable disposable = Observable.create((ObservableOnSubscribe<List<Person>>) emitter -> {
            List<Person> personList = PersonRepository.getInstance().loadPersonByPage(pageNum, ValueUtil.PAGE_SIZE);
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
                    if (pageNum == 1) {
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

}
