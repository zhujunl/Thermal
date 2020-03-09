package com.miaxis.thermal.manager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.miaxis.thermal.app.App;
import com.miaxis.thermal.data.entity.IDCardMessage;

import java.io.IOException;
import java.io.InputStream;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class CardManager {

    private CardManager() {
    }

    public static CardManager getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static final CardManager instance = new CardManager();
    }

    /**
     * ================================ 静态内部类单例 ================================
     **/

    private OnCardReadListener listener;

    public void test() {
        if (listener != null) {
            Observable.create((ObservableOnSubscribe<Bitmap>) emitter -> {
                Thread.sleep(1000);
                Bitmap bitmap=null;
                try {
                    InputStream is = App.getInstance().getApplicationContext().getAssets().open("zp.bmp");
                    bitmap = BitmapFactory.decodeStream(is);
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                emitter.onNext(bitmap);
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bitmap -> {
                        if (listener != null) {
                            listener.onCardRead(new IDCardMessage.Builder()
                                    .cardNumber("340823199601021913")
                                    .name("唐一非")
                                    .cardBitmap(bitmap)
                                    .build());
                        }
                    }, throwable -> {
                        throwable.printStackTrace();
                    });
        }
    }

    public void setListener(OnCardReadListener listener) {
        this.listener = listener;
    }

    public interface OnCardReadListener {
        void onCardRead(IDCardMessage idCardMessage);
    }

}
