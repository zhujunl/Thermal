package com.miaxis.thermal.view.fragment.xhn;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.miaxis.thermal.BR;
import com.miaxis.thermal.R;
import com.miaxis.thermal.bridge.GlideApp;
import com.miaxis.thermal.data.entity.Calibration;
import com.miaxis.thermal.databinding.FragmentXhnCalibrationBinding;
import com.miaxis.thermal.manager.CalibrationManager;
import com.miaxis.thermal.manager.ToastManager;
import com.miaxis.thermal.view.auxiliary.OnLimitClickHelper;
import com.miaxis.thermal.view.base.BaseViewModelFragment;
import com.miaxis.thermal.view.dialog.DialogHelper;
import com.miaxis.thermal.viewModel.xhn.XhnCalibrationViewModel;

public class XhnCalibrationFragment extends BaseViewModelFragment<FragmentXhnCalibrationBinding, XhnCalibrationViewModel> {

    public static XhnCalibrationFragment newInstance() {
        return new XhnCalibrationFragment();
    }

    @Override
    protected int setContentView() {
        return R.layout.fragment_xhn_calibration;
    }

    @Override
    protected XhnCalibrationViewModel initViewModel() {
        return new ViewModelProvider(this, getViewModelProviderFactory()).get(XhnCalibrationViewModel.class);
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    protected void initData() {
        viewModel.heatMapUpdate.observe(this, heatMapUpdateObserver);
        getLifecycle().addObserver(viewModel);
    }

    @Override
    protected void initView() {
        Calibration calibration = CalibrationManager.getInstance().getCalibration();
        binding.etEmissivity.setText(String.valueOf(calibration.getXhnEmissivity()));
        binding.etModel.setText(String.valueOf(calibration.getXhnModel()));
        binding.ivCheck.setOnClickListener(new OnLimitClickHelper(view -> {
            DialogHelper.fullScreenMaterialDialogLink(new MaterialDialog.Builder(getContext())
                    .content("保存成功之后，应用将自动重启，是否确认？")
                    .positiveText("确认")
                    .onPositive((dialog, which) -> {
                        checkCalibration();
                    })
                    .negativeText("取消")
                    .build())
                    .show();
        }));
    }

    @Override
    public void onBackPressed() {
        mListener.backToStack(null);
    }


    private Observer<Boolean> heatMapUpdateObserver = update -> {
        if (viewModel.heatMapCache != null) {
            GlideApp.with(this)
                    .asDrawable()
                    .load(viewModel.heatMapCache)
                    .dontAnimate()
//                    .placeholder(binding.ivHeatMap.getDrawable())
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            binding.ivHeatMap.setImageDrawable(resource);
                        }
                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });
        }
    };

    private void checkCalibration() {
        Calibration calibration = CalibrationManager.getInstance().getCalibration();
        if (TextUtils.isEmpty(binding.etEmissivity.getText().toString())
                || Integer.parseInt(binding.etEmissivity.getText().toString()) < 850
                || Integer.parseInt(binding.etEmissivity.getText().toString()) > 1000) {
            ToastManager.toast("辐射率范围 850 - 1000", ToastManager.INFO);
            return;
        }
        if (TextUtils.isEmpty(binding.etModel.getText().toString())
                || Integer.parseInt(binding.etModel.getText().toString()) < 1
                || Integer.parseInt(binding.etModel.getText().toString()) > 3) {
            ToastManager.toast("温度模型范围 1 - 3", ToastManager.INFO);
            return;
        }
        calibration.setXhnEmissivity(Integer.parseInt(binding.etEmissivity.getText().toString()));
        calibration.setXhnModel(Integer.parseInt(binding.etModel.getText().toString()));
        viewModel.saveCalibration(calibration);
    }

}
