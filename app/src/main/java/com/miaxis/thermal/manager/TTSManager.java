package com.miaxis.thermal.manager;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.lang.ref.WeakReference;
import java.util.Locale;

/**
 * 文字转语音单例类
 * 中文转语音依赖于第三方TTS，推荐讯飞语记
 */
public class TTSManager {

    private TTSManager() {}

    public static TTSManager getInstance () {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static final TTSManager instance = new TTSManager();
    }

    /** ================================ 静态内部类单例写法 ================================ **/

    private WeakReference<TextToSpeech> ttsRef;

    /**
     * 初始化
     * @param context
     */
    public void init(Context context) {
        ttsRef = new WeakReference<>(new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                ttsRef.get().setLanguage(Locale.CHINESE);
            }
        }));
    }

    /**
     * 文字转语音，打断当前语音播放，并立即播放这段语音
     * @param message
     */
    public void playVoiceMessageFlush(String message) {
        ttsRef.get().speak(message, TextToSpeech.QUEUE_FLUSH, null);
    }

    /**
     * 文字转语音，不打断当前播放，并将这段语音排队播放
     * @param message
     */
    public void playVoiceMessageAdd(String message) {
        ttsRef.get().speak(message, TextToSpeech.QUEUE_ADD, null);
    }

    /**
     * 停止播放
     */
    public void stop() {
        ttsRef.get().stop();
    }

    /**
     * 关闭
     */
    public void close() {
        if (ttsRef != null) {
            ttsRef.get().shutdown();
        }
    }

}
