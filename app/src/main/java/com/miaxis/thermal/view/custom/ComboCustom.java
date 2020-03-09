package com.miaxis.thermal.view.custom;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.MaterialDialog;
import com.miaxis.thermal.R;
import com.miaxis.thermal.manager.ConfigManager;
import com.miaxis.thermal.manager.ToastManager;

public class ComboCustom extends FrameLayout {

    private long[] mHits;
    private OnPwdConfirmListener listener;
    private Handler handler = new Handler(Looper.getMainLooper());
    private MaterialDialog passwordDialog;
    private int delay;
    private boolean needPassword = true;

    public ComboCustom(Context context) {
        super(context);
        init();
    }

    public ComboCustom(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ComboCustom(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        View view = inflate(getContext(), R.layout.custom_combo, this);
        bringToFront();
        FrameLayout flCombo = view.findViewById(R.id.fl_combo);
        flCombo.setOnClickListener(v -> onComboClickEvent());
        initDialog();
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (delay == 0) {
                passwordDialog.dismiss();
            } else {
                handler.postDelayed(runnable, 1000);
                passwordDialog.getTitleView().setText("请输入设备密码(" + delay + "S)");
            }
            delay--;
        }
    };

    private void showPasswordDialog() {
        delay = 15;
        handler.post(runnable);
        passwordDialog.getInputEditText().setText("");
        passwordDialog.show();
    }

    private void onComboClickEvent() {
        if (mHits == null) {
            mHits = new long[4];
        }
        System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
        mHits[mHits.length - 1] = SystemClock.uptimeMillis();
        if (SystemClock.uptimeMillis() - mHits[0] <= 2000) {
            mHits = null;
            if (needPassword) {
                showPasswordDialog();
            } else {
                if (listener != null) {
                    listener.pwdConfirm();
                }
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void setListener(OnPwdConfirmListener listener) {
        this.listener = listener;
    }

    public interface OnPwdConfirmListener {
        void pwdConfirm();
    }

    private void initDialog() {
        passwordDialog = new MaterialDialog.Builder(getContext())
                .title("请输入设备密码")
                .inputType(InputType.TYPE_CLASS_NUMBER)
                .input("", "", (dialog, input) -> {
                })
                .inputRange(6, 6)
                .positiveText("确认")
                .onPositive((dialog, which) -> {
                    handler.removeCallbacks(runnable);
                    if (TextUtils.equals(dialog.getInputEditText().getText().toString(), ConfigManager.getInstance().getConfig().getDevicePassword())) {
                        if (listener != null) {
                            listener.pwdConfirm();
                        }
                    } else {
                        ToastManager.toast("密码错误", ToastManager.INFO);
                    }
                })
                .negativeText("取消")
                .onNegative((dialog, which) -> handler.removeCallbacks(runnable))
                .dismissListener(dialog -> handler.removeCallbacks(runnable))
                .build();
        passwordDialog.getInputEditText().setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
    }

    public void setNeedPassword(boolean needPassword) {
        this.needPassword = needPassword;
    }
}
