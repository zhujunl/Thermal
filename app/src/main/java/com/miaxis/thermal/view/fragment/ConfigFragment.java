package com.miaxis.thermal.view.fragment;

import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.miaxis.thermal.BR;
import com.miaxis.thermal.R;
import com.miaxis.thermal.data.entity.Config;
import com.miaxis.thermal.databinding.FragmentConfigBinding;
import com.miaxis.thermal.manager.ConfigManager;
import com.miaxis.thermal.manager.ToastManager;
import com.miaxis.thermal.util.DeviceUtil;
import com.miaxis.thermal.util.ValueUtil;
import com.miaxis.thermal.view.auxiliary.OnLimitClickHelper;
import com.miaxis.thermal.view.auxiliary.OnLimitClickListener;
import com.miaxis.thermal.view.base.BaseViewModelFragment;
import com.miaxis.thermal.viewModel.ConfigViewModel;

public class ConfigFragment extends BaseViewModelFragment<FragmentConfigBinding, ConfigViewModel> {

    public static ConfigFragment newInstance() {
        return new ConfigFragment();
    }

    public ConfigFragment() {
        // Required empty public constructor
    }

    @Override
    protected int setContentView() {
        return R.layout.fragment_config;
    }

    @Override
    protected ConfigViewModel initViewModel() {
        return new ViewModelProvider(this, getViewModelProviderFactory()).get(ConfigViewModel.class);
    }

    @Override
    public int initVariableId() {
        return com.miaxis.thermal.BR.viewModel;
    }

    @Override
    protected void initData() {
        viewModel.clearTimeStampResult.observe(this, clearTimeStamp);
        Config config = ConfigManager.getInstance().getConfig();
        binding.tvVersion.setText(DeviceUtil.getCurVersion(getContext()));
        if (TextUtils.equals(config.getServerMode(), "0")) {
            binding.rbMix.setChecked(true);
            binding.rbServer.setChecked(false);
            binding.rbClient.setChecked(false);
        } else if (TextUtils.equals(config.getServerMode(), "1")) {
            binding.rbMix.setChecked(false);
            binding.rbServer.setChecked(true);
            binding.rbClient.setChecked(false);
        } else if (TextUtils.equals(config.getServerMode(), "2")) {
            binding.rbMix.setChecked(false);
            binding.rbServer.setChecked(false);
            binding.rbClient.setChecked(true);
        }
        binding.etHost.setText(config.getHost());
        binding.etDownloadPersonPath.setText(config.getDownloadPersonPath());
        binding.etUpdatePersonPath.setText(config.getUpdatePersonPath());
        binding.etUploadRecordPath.setText(config.getUploadRecordPath());
        binding.tvMac.setText(ConfigManager.getInstance().getMacAddress());
        binding.tvTimeStamp.setText(String.valueOf(config.getTimeStamp()));
        binding.rbInfraredShow.setChecked(config.isShowCamera());
        binding.rbVisibleShow.setChecked(!config.isShowCamera());
        binding.rbInfraredFace.setChecked(config.isFaceCamera());
        binding.rbVisibleFace.setChecked(!config.isFaceCamera());
        binding.rbLivenessOpen.setChecked(config.isLiveness());
        binding.rbLivenessClose.setChecked(!config.isLiveness());
        binding.etRegisterQualityScore.setText(String.valueOf(config.getRegisterQualityScore()));
        binding.etQualityScore.setText(String.valueOf(config.getQualityScore()));
        binding.etVerifyScore.setText(String.valueOf(config.getVerifyScore()));
        binding.etMaskVerifyScore.setText(String.valueOf(config.getMaskVerifyScore()));
        binding.etMaskScore.setText(String.valueOf(config.getMaskScore()));
        binding.etLivenessScore.setText(String.valueOf(config.getLivenessScore()));
        binding.etPupilDistance.setText(String.valueOf(config.getPupilDistance()));
        binding.etFeverScore.setText(String.valueOf(config.getFeverScore()));
        binding.etHeartBeatInterval.setText(String.valueOf(config.getHeartBeatInterval()));
        binding.etFailedQueryCold.setText(String.valueOf(config.getFailedQueryCold()));
        binding.etRecordClearThreshold.setText(String.valueOf(config.getRecordClearThreshold()));
        binding.etVerifyCold.setText(String.valueOf(config.getVerifyCold()));
        binding.etFlashTime.setText(String.valueOf(config.getFlashTime()));
        binding.etDevicePassword.setText(config.getDevicePassword());
        binding.ivBack.setOnClickListener(v -> mListener.backToStack(null));
    }

    @Override
    protected void initView() {
        binding.tvVersion.setText(DeviceUtil.getCurVersion(getContext()));
        binding.tvClearTimeStamp.setOnClickListener(new OnLimitClickHelper(view -> {
            new MaterialDialog.Builder(getContext())
                    .title("确认清空同步时间戳？")
                    .onPositive((dialog, which) -> {
                        viewModel.clearTimeStamp();
                    })
                    .positiveText("确认")
                    .negativeText("取消")
                    .show();
        }));
        binding.ivBack.setOnClickListener(v -> onBackPressed());
        binding.ivSave.setOnClickListener(v -> {
            try {
                Config config = viewModel.config.get();
                if (config != null) {
                    if (TextUtils.isEmpty(binding.etHost.getText().toString())) {
                        ToastManager.toast("请输入平台地址", ToastManager.INFO);
                        return;
                    }
                    if (TextUtils.isEmpty(binding.etRegisterQualityScore.getText().toString())
                            || Integer.valueOf(binding.etRegisterQualityScore.getText().toString()) < 50
                            || Integer.valueOf(binding.etRegisterQualityScore.getText().toString()) > 100) {
                        ToastManager.toast("注册质量阈值范围 50 - 100", ToastManager.INFO);
                        return;
                    }
                    if (TextUtils.isEmpty(binding.etQualityScore.getText().toString())
                            || Integer.valueOf(binding.etQualityScore.getText().toString()) < 20
                            || Integer.valueOf(binding.etQualityScore.getText().toString()) > 100) {
                        ToastManager.toast("质量阈值范围 20 - 100", ToastManager.INFO);
                        return;
                    }
                    if (TextUtils.isEmpty(binding.etVerifyScore.getText().toString())
                            || Float.valueOf(binding.etVerifyScore.getText().toString()) < 0.7f
                            || Float.valueOf(binding.etVerifyScore.getText().toString()) > 1.0f) {
                        ToastManager.toast("人脸比对阈值范围 0.7 - 1.0", ToastManager.INFO);
                        return;
                    }
                    if (TextUtils.isEmpty(binding.etMaskVerifyScore.getText().toString())
                            || Float.valueOf(binding.etMaskVerifyScore.getText().toString()) < 0.65f
                            || Float.valueOf(binding.etMaskVerifyScore.getText().toString()) > 1.0f) {
                        ToastManager.toast("戴口罩人脸比对阈值范围 0.65 - 1.0", ToastManager.INFO);
                        return;
                    }
                    if (TextUtils.isEmpty(binding.etMaskScore.getText().toString())
                            || Integer.valueOf(binding.etRegisterQualityScore.getText().toString()) < 20
                            || Integer.valueOf(binding.etMaskScore.getText().toString()) > 100) {
                        ToastManager.toast("口罩检测阈值范围 20 - 100", ToastManager.INFO);
                        return;
                    }
                    if (TextUtils.isEmpty(binding.etHeartBeatInterval.getText().toString())
                            || Integer.valueOf(binding.etHeartBeatInterval.getText().toString()) < 60
                            || Integer.valueOf(binding.etHeartBeatInterval.getText().toString()) > 6000) {
                        ToastManager.toast("心跳间隔时间 60 - 6000 秒", ToastManager.INFO);
                        return;
                    }
                    if (TextUtils.isEmpty(binding.etFailedQueryCold.getText().toString())
                            || Integer.valueOf(binding.etFailedQueryCold.getText().toString()) < 5
                            || Integer.valueOf(binding.etFailedQueryCold.getText().toString()) > 120) {
                        ToastManager.toast("失败重查冷却 5 - 120 秒", ToastManager.INFO);
                        return;
                    }
                    if (TextUtils.isEmpty(binding.etVerifyCold.getText().toString())
                            || Integer.valueOf(binding.etVerifyCold.getText().toString()) < 1
                            || Integer.valueOf(binding.etVerifyCold.getText().toString()) > 5) {
                        ToastManager.toast("成功验证冷却 2 - 5 秒", ToastManager.INFO);
                        return;
                    }
                    if (TextUtils.isEmpty(binding.etRecordClearThreshold.getText().toString())
                            || Integer.valueOf(binding.etRecordClearThreshold.getText().toString()) < 2000
                            || Integer.valueOf(binding.etRecordClearThreshold.getText().toString()) > 20000) {
                        ToastManager.toast("最大日志保存数目 2000 - 20000 条", ToastManager.INFO);
                        return;
                    }
                    if (TextUtils.isEmpty(binding.etPupilDistance.getText().toString())
                            || Integer.valueOf(binding.etPupilDistance.getText().toString()) < 1
                            || Integer.valueOf(binding.etPupilDistance.getText().toString()) > 200) {
                        ToastManager.toast("瞳距阈值范围 1 - 200", ToastManager.INFO);
                        return;
                    }
                    if (TextUtils.isEmpty(binding.etLivenessScore.getText().toString())
                            || Integer.valueOf(binding.etLivenessScore.getText().toString()) < 60
                            || Integer.valueOf(binding.etLivenessScore.getText().toString()) > 99) {
                        ToastManager.toast("活检阈值范围 60 - 99", ToastManager.INFO);
                        return;
                    }
                    if (binding.rbMix.isChecked()) {
                        config.setServerMode("0");
                    } else if (binding.rbServer.isChecked()) {
                        config.setServerMode("1");
                    } else if (binding.rbClient.isChecked()) {
                        config.setServerMode("2");
                    }
                    config.setHost(binding.etHost.getText().toString());
                    config.setDownloadPersonPath(binding.etDownloadPersonPath.getText().toString());
                    config.setUpdatePersonPath(binding.etUpdatePersonPath.getText().toString());
                    config.setUploadRecordPath(binding.etUploadRecordPath.getText().toString());
                    config.setShowCamera(binding.rbInfraredShow.isChecked());
                    config.setFaceCamera(binding.rbInfraredFace.isChecked());
                    config.setLiveness(binding.rbLivenessOpen.isChecked());
                    config.setRegisterQualityScore(Integer.valueOf(binding.etRegisterQualityScore.getText().toString()));
                    config.setQualityScore(Integer.valueOf(binding.etQualityScore.getText().toString()));
                    config.setVerifyScore(Float.valueOf(binding.etVerifyScore.getText().toString()));
                    config.setMaskVerifyScore(Float.valueOf(binding.etMaskVerifyScore.getText().toString()));
                    config.setMaskScore(Integer.valueOf(binding.etMaskScore.getText().toString()));
                    config.setLivenessScore(Integer.valueOf(binding.etLivenessScore.getText().toString()));
                    config.setPupilDistance(Integer.valueOf(binding.etPupilDistance.getText().toString()));
                    config.setFeverScore(Float.valueOf(binding.etFeverScore.getText().toString()));
                    config.setHeartBeatInterval(Integer.valueOf(binding.etHeartBeatInterval.getText().toString()));
                    config.setFailedQueryCold(Integer.valueOf(binding.etFailedQueryCold.getText().toString()));
                    config.setRecordClearThreshold(Integer.valueOf(binding.etRecordClearThreshold.getText().toString()));
                    config.setVerifyCold(Integer.valueOf(binding.etVerifyCold.getText().toString()));
                    config.setFlashTime(Integer.valueOf(binding.etFlashTime.getText().toString()));
                    config.setDevicePassword(binding.etDevicePassword.getText().toString());
                    viewModel.saveConfig(config);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onBackPressed() {
        mListener.backToStack(null);
    }

    private Observer<Boolean> clearTimeStamp = result -> {
        binding.tvTimeStamp.setText("0");
    };

}
