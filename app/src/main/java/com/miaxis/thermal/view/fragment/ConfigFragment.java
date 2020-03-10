package com.miaxis.thermal.view.fragment;

import android.text.TextUtils;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.miaxis.thermal.BR;
import com.miaxis.thermal.R;
import com.miaxis.thermal.data.entity.Config;
import com.miaxis.thermal.databinding.FragmentConfigBinding;
import com.miaxis.thermal.manager.ConfigManager;
import com.miaxis.thermal.manager.ToastManager;
import com.miaxis.thermal.util.DeviceUtil;
import com.miaxis.thermal.util.ValueUtil;
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
        binding.tvMac.setText(config.getMac());
        binding.rbInfraredShow.setChecked(config.isShowCamera());
        binding.rbVisibleShow.setChecked(!config.isShowCamera());
        binding.rbInfraredFace.setChecked(config.isFaceCamera());
        binding.rbVisibleFace.setChecked(!config.isFaceCamera());
        binding.rbLivenessOpen.setChecked(config.isLiveness());
        binding.rbLivenessClose.setChecked(!config.isLiveness());
        binding.etVerifyScore.setText(String.valueOf(config.getVerifyScore()));
        binding.etQualityScore.setText(String.valueOf(config.getQualityScore()));
        binding.etLivenessScore.setText(String.valueOf(config.getLivenessScore()));
        binding.etPupilDistance.setText(String.valueOf(config.getPupilDistance()));
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
        binding.ivBack.setOnClickListener(v -> onBackPressed());
        binding.ivSave.setOnClickListener(v -> {
            Config config = viewModel.config.get();
            if (config != null) {
                if (TextUtils.isEmpty(binding.etHost.getText().toString())) {
                    ToastManager.toast("请输入平台地址", ToastManager.INFO);
                    return;
                }
                if (TextUtils.isEmpty(binding.etVerifyScore.getText().toString())
                        || Float.valueOf(binding.etVerifyScore.getText().toString()) < 0.7f
                        || Float.valueOf(binding.etVerifyScore.getText().toString()) > 1.0f) {
                    ToastManager.toast("比对阈值范围 0.7 - 1.0", ToastManager.INFO);
                    return;
                }
                if (TextUtils.isEmpty(binding.etQualityScore.getText().toString())
                        || Integer.valueOf(binding.etQualityScore.getText().toString()) < 20
                        || Integer.valueOf(binding.etQualityScore.getText().toString()) > 100) {
                    ToastManager.toast("质量阈值范围 20 - 100", ToastManager.INFO);
                    return;
                }
                if (TextUtils.isEmpty(binding.etHeartBeatInterval.getText().toString())
                        || Integer.valueOf(binding.etHeartBeatInterval.getText().toString()) < 60
                        || Integer.valueOf(binding.etHeartBeatInterval.getText().toString()) > 600) {
                    ToastManager.toast("心跳间隔时间 60 - 600 秒", ToastManager.INFO);
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
                    ToastManager.toast("成功验证冷却 1 - 5 秒", ToastManager.INFO);
                    return;
                }
                if (TextUtils.isEmpty(binding.etRecordClearThreshold.getText().toString())
                        || Integer.valueOf(binding.etRecordClearThreshold.getText().toString()) < 2000
                        || Integer.valueOf(binding.etRecordClearThreshold.getText().toString()) > 10000) {
                    ToastManager.toast("最大日志保存数目 2000 - 10000 条", ToastManager.INFO);
                    return;
                }
                if (TextUtils.isEmpty(binding.etPupilDistance.getText().toString())
                        || Integer.valueOf(binding.etPupilDistance.getText().toString()) < 1
                        || Integer.valueOf(binding.etPupilDistance.getText().toString()) > 99) {
                    ToastManager.toast("瞳距阈值范围 1 - 99 ", ToastManager.INFO);
                    return;
                }
                if (TextUtils.isEmpty(binding.etLivenessScore.getText().toString())
                        || Integer.valueOf(binding.etLivenessScore.getText().toString()) < 60
                        || Integer.valueOf(binding.etLivenessScore.getText().toString()) > 99) {
                    ToastManager.toast("活检阈值范围 60 - 99", ToastManager.INFO);
                    return;
                }
                config.setHost(binding.etHost.getText().toString());
                config.setVerifyScore(Float.valueOf(binding.etVerifyScore.getText().toString()));
                config.setQualityScore(Integer.valueOf(binding.etQualityScore.getText().toString()));
                config.setDevicePassword(binding.etDevicePassword.getText().toString());
                config.setHeartBeatInterval(Integer.valueOf(binding.etHeartBeatInterval.getText().toString()));
                config.setFailedQueryCold(Integer.valueOf(binding.etFailedQueryCold.getText().toString()));
                config.setRecordClearThreshold(Integer.valueOf(binding.etRecordClearThreshold.getText().toString()));
                config.setVerifyCold(Integer.valueOf(binding.etVerifyCold.getText().toString()));
                config.setShowCamera(binding.rbInfraredShow.isChecked());
                config.setFaceCamera(binding.rbInfraredFace.isChecked());
                config.setLiveness(binding.rbLivenessOpen.isChecked());
                config.setFlashTime(Integer.valueOf(binding.etFlashTime.getText().toString()));
                config.setPupilDistance(Integer.valueOf(binding.etPupilDistance.getText().toString()));
                config.setLivenessScore(Integer.valueOf(binding.etLivenessScore.getText().toString()));
                viewModel.saveConfig(config);
            }
        });
    }

    @Override
    public void onBackPressed() {
        mListener.backToStack(null);
    }
}
