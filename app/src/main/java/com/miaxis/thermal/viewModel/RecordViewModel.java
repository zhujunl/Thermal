package com.miaxis.thermal.viewModel;

import androidx.lifecycle.MutableLiveData;

import com.miaxis.thermal.app.App;
import com.miaxis.thermal.bridge.SingleLiveEvent;
import com.miaxis.thermal.data.entity.Record;
import com.miaxis.thermal.data.entity.RecordSearch;
import com.miaxis.thermal.data.exception.MyException;
import com.miaxis.thermal.data.repository.RecordRepository;
import com.miaxis.thermal.manager.ToastManager;
import com.miaxis.thermal.util.ValueUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class RecordViewModel extends BaseViewModel {

    public MutableLiveData<List<Record>> recordListLiveData = new MutableLiveData<>(new ArrayList<>());
    public MutableLiveData<Integer> recordCountLiveData = new MutableLiveData<>(0);

    public MutableLiveData<Boolean> refreshing = new SingleLiveEvent<>();

    public RecordViewModel() {
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }

    public List<Record> getRecordList() {
        List<Record> value = recordListLiveData.getValue();
        if (value == null) {
            List<Record> newArrayList = new ArrayList<>();
            recordListLiveData.setValue(newArrayList);
            return newArrayList;
        } else {
            return value;
        }
    }

    public void loadRecordByPage(RecordSearch recordSearch) {
        Disposable disposable = Observable.create((ObservableOnSubscribe<List<Record>>) emitter -> {
            List<Record> recordList = RecordRepository.getInstance().searchRecord(recordSearch);
//            List<Record> recordList = RecordRepository.getInstance().loadRecordByPage(pageNum, 30);
            if (recordList != null) {
                emitter.onNext(recordList);
                emitter.onComplete();
            } else {
                emitter.onError(new MyException("查询结果为空"));
            }
        })
                .subscribeOn(Schedulers.from(App.getInstance().getThreadExecutor()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(recordList -> {
                    refreshing.setValue(Boolean.FALSE);
                    if (recordSearch.getPageNum() == 1) {
                        recordListLiveData.setValue(recordList);
                    } else {
                        List<Record> LocalOrderList = getRecordList();
                        LocalOrderList.addAll(recordList);
                        recordListLiveData.setValue(LocalOrderList);
                    }
                    if (recordList.isEmpty()) {
                        toast.setValue(ToastManager.getToastBody("没有更多了", ToastManager.SUCCESS));
                    }
                }, throwable -> {
                    refreshing.setValue(Boolean.FALSE);
                    resultMessage.setValue(handleError(throwable));
                });
    }

    public void updateRecordCount(boolean search, RecordSearch recordSearch) {
        Disposable disposable = Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
            int count;
            if (search) {
                count = RecordRepository.getInstance().searchRecordCount(recordSearch);
            } else {
                count = RecordRepository.getInstance().loadRecordCount();
            }
            emitter.onNext(count);
            emitter.onComplete();
        })
                .subscribeOn(Schedulers.from(App.getInstance().getThreadExecutor()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(count -> {
                    recordCountLiveData.setValue(count);
                }, throwable -> {
                    resultMessage.setValue(handleError(throwable));
                });
    }
}
