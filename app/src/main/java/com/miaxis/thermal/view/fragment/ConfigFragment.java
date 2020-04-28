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
import com.miaxis.thermal.manager.FaceManager;
import com.miaxis.thermal.manager.ToastManager;
import com.miaxis.thermal.manager.strategy.Sign;
import com.miaxis.thermal.util.DeviceUtil;
import com.miaxis.thermal.util.ValueUtil;
import com.miaxis.thermal.view.auxiliary.OnLimitClickHelper;
import com.miaxis.thermal.view.auxiliary.OnLimitClickListener;
import com.miaxis.thermal.view.base.BaseViewModelFragment;
import com.miaxis.thermal.view.dialog.DialogHelper;
import com.miaxis.thermal.view.fragment.xhn.XhnCalibrationFragment;
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
        binding.tvFaceVersion.setText(FaceManager.getInstance().faceVersion());
        String versionName = DeviceUtil.getCurVersion(getContext()) + "_" + Sign.getSignName(ValueUtil.DEFAULT_SIGN);
        binding.tvVersion.setText(versionName);
        if (TextUtils.equals(config.getServerMode(), ValueUtil.WORK_MODE_LOCAL)) {
            binding.rbLocalWork.setChecked(true);
            binding.rbNetWork.setChecked(false);
        } else {
            binding.rbLocalWork.setChecked(false);
            binding.rbNetWork.setChecked(true);
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
        binding.rbForcesMaskOpen.setChecked(config.isForcedMask());
        binding.rbForcesMaskClose.setChecked(!config.isForcedMask());
        binding.rbStrangerRecordOn.setChecked(config.isStrangerRecord());
        binding.rbStrangerRecordOff.setChecked(!config.isStrangerRecord());
        binding.rbDeviceModeAttendance.setChecked(config.isDeviceMode());
        binding.rbDeviceModeGate.setChecked(!config.isDeviceMode());
        binding.rbGateLimitOn.setChecked(config.isGateLimit());
        binding.rbGateLimitOff.setChecked(!config.isGateLimit());
        binding.etRegisterQualityScore.setText(String.valueOf(config.getRegisterQualityScore()));
        binding.etQualityScore.setText(String.valueOf(config.getQualityScore()));
        binding.etVerifyScore.setText(String.valueOf(config.getVerifyScore()));
        binding.etMaskVerifyScore.setText(String.valueOf(config.getMaskVerifyScore()));
        binding.etMaskScore.setText(String.valueOf(config.getMaskScore()));
        binding.etLivenessScore.setText(String.valueOf(config.getLivenessScore()));
        binding.etPupilDistanceMin.setText(String.valueOf(config.getPupilDistanceMin()));
        binding.etPupilDistanceMax.setText(String.valueOf(config.getPupilDistanceMax()));
        binding.etDormancyInterval.setText(String.valueOf(config.getDormancyInterval()));
        binding.etDormancyTime.setText(String.valueOf(config.getDormancyTime()));
        binding.etFeverScore.setText(String.valueOf(config.getFeverScore()));
        binding.etTempScore.setText(String.valueOf(config.getTempScore()));
        binding.rbTempRealTimeOn.setChecked(config.isTempRealTime());
        binding.rbTempRealTimeOff.setChecked(!config.isTempRealTime());
        binding.rbHeatMapShow.setChecked(config.isHeatMap());
        binding.rbHeatMapDismiss.setChecked(!config.isHeatMap());
        binding.etHeartBeatInterval.setText(String.valueOf(config.getHeartBeatInterval()));
        binding.etFailedQueryCold.setText(String.valueOf(config.getFailedQueryCold()));
        binding.etRecordClearThreshold.setText(String.valueOf(config.getRecordClearThreshold()));
        binding.etVerifyCold.setText(String.valueOf(config.getVerifyCold()));
        binding.etFailedVerifyCold.setText(String.valueOf(config.getFailedVerifyCold()));
        binding.etFlashTime.setText(String.valueOf(config.getFlashTime()));
        binding.etDevicePassword.setText(config.getDevicePassword());
        binding.ivBack.setOnClickListener(v -> mListener.backToStack(null));
        binding.tvCalibration.setOnClickListener(new OnLimitClickHelper(view -> {
            if (ValueUtil.DEFAULT_SIGN == Sign.XH_N) {
                mListener.replaceFragment(XhnCalibrationFragment.newInstance());
            } else {
                ToastManager.toast("该版本未开放校准功能", ToastManager.INFO);
            }
        }));
    }

    @Override
    protected void initView() {
        binding.tvClearTimeStamp.setOnClickListener(new OnLimitClickHelper(view -> {
            DialogHelper.fullScreenMaterialDialogLink(new MaterialDialog.Builder(getContext())
                    .title("确认清空同步时间戳？")
                    .onPositive((dialog, which) -> {
                        viewModel.clearTimeStamp();
                    })
                    .positiveText("确认")
                    .negativeText("取消")
                    .build())
                    .show();
        }));
        binding.ivBack.setOnClickListener(v -> onBackPressed());
        binding.ivSave.setOnClickListener(v -> {
            try {
                Config config = ConfigManager.getInstance().getConfig();
                if (config != null) {
                    if (TextUtils.isEmpty(binding.etHost.getText().toString())) {
                        ToastManager.toast("请输入平台地址", ToastManager.INFO);
                        return;
                    }
                    if (TextUtils.isEmpty(binding.etRegisterQualityScore.getText().toString())
                            || Integer.parseInt(binding.etRegisterQualityScore.getText().toString()) < 50
                            || Integer.parseInt(binding.etRegisterQualityScore.getText().toString()) > 100) {
                        ToastManager.toast("注册质量阈值范围 50 - 100", ToastManager.INFO);
                        return;
                    }
                    if (TextUtils.isEmpty(binding.etQualityScore.getText().toString())
                            || Integer.parseInt(binding.etQualityScore.getText().toString()) < 20
                            || Integer.parseInt(binding.etQualityScore.getText().toString()) > 100) {
                        ToastManager.toast("质量阈值范围 20 - 100", ToastManager.INFO);
                        return;
                    }
                    if (TextUtils.isEmpty(binding.etVerifyScore.getText().toString())
                            || Float.parseFloat(binding.etVerifyScore.getText().toString()) < 0.7f
                            || Float.parseFloat(binding.etVerifyScore.getText().toString()) > 1.0f) {
                        ToastManager.toast("人脸比对阈值范围 0.7 - 1.0", ToastManager.INFO);
                        return;
                    }
                    if (TextUtils.isEmpty(binding.etMaskVerifyScore.getText().toString())
                            || Float.parseFloat(binding.etMaskVerifyScore.getText().toString()) < 0.65f
                            || Float.parseFloat(binding.etMaskVerifyScore.getText().toString()) > 1.0f) {
                        ToastManager.toast("戴口罩人脸比对阈值范围 0.65 - 1.0", ToastManager.INFO);
                        return;
                    }
                    if (TextUtils.isEmpty(binding.etMaskScore.getText().toString())
                            || Integer.parseInt(binding.etRegisterQualityScore.getText().toString()) < 20
                            || Integer.parseInt(binding.etMaskScore.getText().toString()) > 100) {
                        ToastManager.toast("口罩检测阈值范围 20 - 100", ToastManager.INFO);
                        return;
                    }
                    if (TextUtils.isEmpty(binding.etLivenessScore.getText().toString())
                            || Integer.parseInt(binding.etLivenessScore.getText().toString()) < 60
                            || Integer.parseInt(binding.etLivenessScore.getText().toString()) > 99) {
                        ToastManager.toast("活检阈值范围 60 - 99", ToastManager.INFO);
                        return;
                    }
                    if (TextUtils.isEmpty(binding.etPupilDistanceMin.getText().toString())
                            || Integer.parseInt(binding.etPupilDistanceMin.getText().toString()) < 1
                            || Integer.parseInt(binding.etPupilDistanceMin.getText().toString()) > 200) {
                        ToastManager.toast("瞳距最小值阈值范围 1 - 200", ToastManager.INFO);
                        return;
                    }
                    if (TextUtils.isEmpty(binding.etPupilDistanceMax.getText().toString())
                            || Integer.parseInt(binding.etPupilDistanceMax.getText().toString()) < 1
                            || Integer.parseInt(binding.etPupilDistanceMax.getText().toString()) > 200) {
                        ToastManager.toast("瞳距最大值阈值范围 1 - 200", ToastManager.INFO);
                        return;
                    }
                    if (Integer.parseInt(binding.etPupilDistanceMin.getText().toString()) >=
                            Integer.parseInt(binding.etPupilDistanceMax.getText().toString())) {
                        ToastManager.toast("瞳距最小值不应小于瞳距最大值", ToastManager.INFO);
                        return;
                    }
                    if (TextUtils.isEmpty(binding.etDormancyInterval.getText().toString())
                            || Integer.parseInt(binding.etDormancyInterval.getText().toString()) < 1
                            || Integer.parseInt(binding.etDormancyInterval.getText().toString()) > 5) {
                        ToastManager.toast("休眠检测间隔范围 1 - 5 秒", ToastManager.INFO);
                        return;
                    }
                    if (TextUtils.isEmpty(binding.etDormancyTime.getText().toString())
                            || Integer.parseInt(binding.etDormancyTime.getText().toString()) < 10
                            || Integer.parseInt(binding.etDormancyTime.getText().toString()) > 600) {
                        ToastManager.toast("休眠延迟时间范围 10 - 600 秒", ToastManager.INFO);
                        return;
                    }
                    if (TextUtils.isEmpty(binding.etHeartBeatInterval.getText().toString())
                            || Integer.parseInt(binding.etHeartBeatInterval.getText().toString()) < 60
                            || Integer.parseInt(binding.etHeartBeatInterval.getText().toString()) > 6000) {
                        ToastManager.toast("心跳间隔时间 60 - 6000 秒", ToastManager.INFO);
                        return;
                    }
                    if (TextUtils.isEmpty(binding.etFailedQueryCold.getText().toString())
                            || Integer.parseInt(binding.etFailedQueryCold.getText().toString()) < 5
                            || Integer.parseInt(binding.etFailedQueryCold.getText().toString()) > 120) {
                        ToastManager.toast("失败重查冷却 5 - 120 秒", ToastManager.INFO);
                        return;
                    }
                    if (TextUtils.isEmpty(binding.etVerifyCold.getText().toString())
                            || Integer.parseInt(binding.etVerifyCold.getText().toString()) < 2
                            || Integer.parseInt(binding.etVerifyCold.getText().toString()) > 5) {
                        ToastManager.toast("成功验证冷却 2 - 5 秒", ToastManager.INFO);
                        return;
                    }
                    if (TextUtils.isEmpty(binding.etFailedVerifyCold.getText().toString())
                            || Integer.parseInt(binding.etFailedVerifyCold.getText().toString()) < 2
                            || Integer.parseInt(binding.etFailedVerifyCold.getText().toString()) > 5) {
                        ToastManager.toast("陌生人验证冷却 2 - 5 秒", ToastManager.INFO);
                        return;
                    }
                    if (TextUtils.isEmpty(binding.etRecordClearThreshold.getText().toString())
                            || Integer.parseInt(binding.etRecordClearThreshold.getText().toString()) < 2000
                            || Integer.parseInt(binding.etRecordClearThreshold.getText().toString()) > 20000) {
                        ToastManager.toast("最大日志保存数目 2000 - 20000 条", ToastManager.INFO);
                        return;
                    }
                    if (binding.rbDeviceModeGate.isChecked() && !ConfigManager.isGateDevice()) {
                        ToastManager.toast("该版本不支持闸机开门", ToastManager.INFO);
                        return;
                    }
                    if (!TextUtils.equals(config.getServerMode(), binding.rbNetWork.isChecked() ? ValueUtil.WORK_MODE_NET : ValueUtil.WORK_MODE_LOCAL)) {
                        mListener.showResultDialog("请注意，修改工作模式可能会造成数据不同步，单机版下的某些操作将是不可逆的");
                    }
                    config.setServerMode(binding.rbNetWork.isChecked() ? ValueUtil.WORK_MODE_NET : ValueUtil.WORK_MODE_LOCAL);
                    config.setHost(binding.etHost.getText().toString());
                    config.setDownloadPersonPath(binding.etDownloadPersonPath.getText().toString());
                    config.setUpdatePersonPath(binding.etUpdatePersonPath.getText().toString());
                    config.setUploadRecordPath(binding.etUploadRecordPath.getText().toString());
                    config.setShowCamera(binding.rbInfraredShow.isChecked());
                    config.setFaceCamera(binding.rbInfraredFace.isChecked());
                    config.setLiveness(binding.rbLivenessOpen.isChecked());
                    config.setForcedMask(binding.rbForcesMaskOpen.isChecked());
                    config.setStrangerRecord(binding.rbStrangerRecordOn.isChecked());
                    config.setDeviceMode(binding.rbDeviceModeAttendance.isChecked());
                    config.setGateLimit(binding.rbGateLimitOn.isChecked());
                    config.setRegisterQualityScore(Integer.parseInt(binding.etRegisterQualityScore.getText().toString()));
                    config.setQualityScore(Integer.parseInt(binding.etQualityScore.getText().toString()));
                    config.setVerifyScore(Float.parseFloat(binding.etVerifyScore.getText().toString()));
                    config.setMaskVerifyScore(Float.parseFloat(binding.etMaskVerifyScore.getText().toString()));
                    config.setMaskScore(Integer.parseInt(binding.etMaskScore.getText().toString()));
                    config.setLivenessScore(Integer.parseInt(binding.etLivenessScore.getText().toString()));
                    config.setPupilDistanceMin(Integer.parseInt(binding.etPupilDistanceMin.getText().toString()));
                    config.setPupilDistanceMax(Integer.parseInt(binding.etPupilDistanceMax.getText().toString()));
                    config.setDormancyInterval(Integer.parseInt(binding.etDormancyInterval.getText().toString()));
                    config.setDormancyTime(Integer.parseInt(binding.etDormancyTime.getText().toString()));
                    config.setFeverScore(Float.parseFloat(binding.etFeverScore.getText().toString()));
                    config.setTempScore(Float.parseFloat(binding.etTempScore.getText().toString()));
                    config.setHeatMap(binding.rbHeatMapShow.isChecked());
                    config.setTempRealTime(binding.rbTempRealTimeOn.isChecked());
                    config.setHeartBeatInterval(Integer.parseInt(binding.etHeartBeatInterval.getText().toString()));
                    config.setFailedQueryCold(Integer.parseInt(binding.etFailedQueryCold.getText().toString()));
                    config.setRecordClearThreshold(Integer.parseInt(binding.etRecordClearThreshold.getText().toString()));
                    config.setVerifyCold(Integer.parseInt(binding.etVerifyCold.getText().toString()));
                    config.setFailedVerifyCold(Integer.parseInt(binding.etFailedVerifyCold.getText().toString()));
                    config.setFlashTime(Integer.parseInt(binding.etFlashTime.getText().toString()));
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
