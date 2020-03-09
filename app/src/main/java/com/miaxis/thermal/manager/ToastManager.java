package com.miaxis.thermal.manager;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.miaxis.thermal.app.App;

import es.dmoral.toasty.Toasty;

public class ToastManager {

    public static final String SUCCESS = "SUCCESS";
    public static final String ERROR = "ERROR";
    public static final String INFO = "INFO";

    private static Toast toast;
    private static ToastBody toastBody = new ToastBody();
    private static Handler handler = new Handler(Looper.getMainLooper());

    public static void toast(String message, String toastMode) {
        handler.post(() -> {
            if (toast != null) {
                toast.cancel();
            }
            switch (toastMode) {
                case SUCCESS:
                    toast = Toasty.success(App.getInstance().getApplicationContext(), message, Toast.LENGTH_SHORT, true);
                    break;
                case ERROR:
                    toast = Toasty.error(App.getInstance().getApplicationContext(), message, Toast.LENGTH_SHORT, true);
                    break;
                case INFO:
                    toast = Toasty.info(App.getInstance().getApplicationContext(), message, Toast.LENGTH_SHORT, true);
                    break;
            }
            toast.show();
        });
    }

    public static ToastBody getToastBody(String message, String mode) {
        toastBody.setMessage(message);
        toastBody.setMode(mode);
        return toastBody;
    }

    public static class ToastBody {

        private String message = "";
        private String mode = "";

        private ToastBody() {
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }
    }

}
