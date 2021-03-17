package com.miaxis.thermal.view.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.miaxis.thermal.R;
import com.miaxis.thermal.app.App;
import com.miaxis.thermal.bridge.GlideApp;
import com.miaxis.thermal.data.entity.Config;
import com.miaxis.thermal.data.entity.FaceDraw;
import com.miaxis.thermal.databinding.FragmentAttendanceLandBinding;
import com.miaxis.thermal.manager.AmapManager;
import com.miaxis.thermal.manager.CameraManager;
import com.miaxis.thermal.manager.CardManager;
import com.miaxis.thermal.manager.ConfigManager;
import com.miaxis.thermal.manager.FaceManager;
import com.miaxis.thermal.manager.FingerManager;
import com.miaxis.thermal.manager.GpioManager;
import com.miaxis.thermal.manager.ICCardManager;
import com.miaxis.thermal.manager.TimingSwitchManager;
import com.miaxis.thermal.manager.strategy.Sign;
import com.miaxis.thermal.util.DateUtil;
import com.miaxis.thermal.util.ValueUtil;
import com.miaxis.thermal.view.auxiliary.OnLimitClickHelper;
import com.miaxis.thermal.view.auxiliary.OnLimitClickListener;
import com.miaxis.thermal.view.base.BaseViewModelFragment;
import com.miaxis.thermal.view.custom.ComboCustom;
import com.miaxis.thermal.view.dialog.AdvertisementDialogFragment;
import com.miaxis.thermal.viewModel.AttendanceViewModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantLock;

public class AttendanceLandFragment extends BaseViewModelFragment<FragmentAttendanceLandBinding, AttendanceViewModel> {

    private static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.CHINA);
    private static DateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);

    private AdvertisementDialogFragment advertisementDialogFragment;
    private Handler handler;

    private boolean feverCache = false;
    private boolean dormancyCache = false;

    private ReentrantLock cameraOpenReentrantLock = new ReentrantLock();

    public static AttendanceLandFragment newInstance() {
        return new AttendanceLandFragment();
    }

    public AttendanceLandFragment() {
        // Required empty public constructor
    }

    @Override
    protected int setContentView() {
        return R.layout.fragment_attendance_land;
    }

    @Override
    protected AttendanceViewModel initViewModel() {
        return new ViewModelProvider(this, getViewModelProviderFactory()).get(AttendanceViewModel.class);
    }

    @Override
    public int initVariableId() {
        return com.miaxis.thermal.BR.viewModel;
    }

    @Override
    protected void initData() {
        viewModel.faceDraw.observe(this, faceDrawObserver);
        viewModel.updateHeader.observe(this, headerObserver);
        viewModel.fever.observe(this, feverObserver);
        viewModel.faceDormancy.observe(this, dormancyObserver);
        viewModel.heatMapUpdate.observe(this, heatMapObserver);
        if (ConfigManager.isCardDevice()) {
            viewModel.initCard.observe(this, initCardObserver);
            viewModel.cardStatus.observe(this, cardStatusObserver);
        }
        if (ConfigManager.isFingerDevice()) {
            viewModel.initFinger.observe(this, initFingerObserver);
            viewModel.fingerStatus.observe(this, fingerStatusObserver);
        }
        if (ConfigManager.isICCardDevice()) {
            viewModel.initICCard.observe(this, initICCardObserver);
            viewModel.icCardStatus.observe(this, icCardStatusObserver);
        }
        if (ConfigManager.isHumanBodySensorDevice()) {
            viewModel.humanDetect.observe(this, humanDetectObserver);
            viewModel.startHumanDetect();
        }
        handler = new Handler(Looper.getMainLooper());
        binding.clPanel.setOnClickListener(new OnLimitClickHelper(view -> {
            GpioManager.getInstance().openWhiteLedInTime();
        }));
    }

    @Override
    protected void initView() {
        binding.tvCamera.getViewTreeObserver().addOnGlobalLayoutListener(globalListener);
        binding.ccCombo.setNeedPassword(true);
        binding.ccCombo.setListener(() -> {
            mListener.backToStack(null);
        });
        initTimeReceiver();
        initAdvertisementDialog();
        AmapManager.getInstance().startLocation(App.getInstance());
        AmapManager.getInstance().setListener(weather -> {
            viewModel.weather.set(weather);
        });
        Config config = ConfigManager.getInstance().getConfig();
        if (config.isTimingSwitch()) {
            viewModel.timingSwitch.observe(this, timingSwitchObserver);
            handler.postDelayed(() -> {
                viewModel.startTimingSwitch();
            }, 5000);
        }
        if (ConfigManager.isNeedBarcode()) {
            viewModel.hintLock.set(false);
            binding.fabBarcode.setOnClickListener(new OnLimitClickHelper(view -> {
                viewModel.barcodeScanMode();
            }));
        } else {
            binding.fabBarcode.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
//        mListener.backToStack(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.tvCamera.setDrawingCacheEnabled(false);
        CameraManager.getInstance().closeCamera();
        CameraManager.getInstance().release();
        Config config = ConfigManager.getInstance().getConfig();
        if (config.isTimingSwitch()) {
            viewModel.stopTimingSwitch();
        }
        viewModel.stopFaceDetect();
        viewModel.faceDraw.removeObserver(faceDrawObserver);
        if (ConfigManager.isCardDevice()) {
            CardManager.getInstance().release();
        }
        if (ConfigManager.isFingerDevice()) {
            FingerManager.getInstance().release();
        }
        if (ConfigManager.isICCardDevice()) {
            ICCardManager.getInstance().release();
        }
        if (ConfigManager.isHumanBodySensorDevice()) {
            viewModel.stopHumanDetect();
        }
        GpioManager.getInstance().resetGpio();
        GpioManager.getInstance().clearLedThread();
    }

    private ViewTreeObserver.OnGlobalLayoutListener globalListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            binding.tvCamera.getViewTreeObserver().removeOnGlobalLayoutListener(globalListener);
            CameraManager.getInstance().openCamera(binding.tvCamera, cameraListener);
        }
    };

    private CameraManager.OnCameraOpenListener cameraListener = (previewSize, message) -> {
        if (previewSize == null) {
            mListener.showResultDialog("摄像机多次打开失败：\n" + message);
        } else {
            int rootHeight = binding.flCameraRoot.getHeight();
            int rootWidth = binding.flCameraRoot.getWidth();
            resetLayoutParams(binding.tvCamera, rootWidth, rootHeight);
            resetLayoutParams(binding.rsvRect, rootWidth, rootHeight);
            binding.rsvRect.setRootSize(rootWidth, rootHeight);
            binding.rsvRect.setZoomRate((float) rootWidth / CameraManager.getInstance().getPreviewSize().getWidth());
            viewModel.startFaceDetect();
        }
    };

    private Observer<FaceDraw> faceDrawObserver = faceDraw -> {
        binding.rsvRect.drawRect(faceDraw.getMxFaceInfoExes(), faceDraw.getFaceNum());
    };

    private Observer<Boolean> headerObserver = update -> {
        Bitmap header = viewModel.headerCache;
        if (header == null) {
            GlideApp.with(this).clear(binding.ivHeader);
        } else {
            GlideApp.with(this).clear(binding.ivHeader);
            GlideApp.with(this)
                    .asDrawable()
                    .load(header)
                    .dontAnimate()
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(binding.ivHeader);
        }
    };

    private Observer<Boolean> feverObserver = fever -> {
        Context context = getContext();
        if (context != null) {
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (fever) {
                    binding.ivPanel.setImageResource(R.drawable.background_horizontal_line_board_red);
                    binding.clRoot.setBackgroundResource(R.drawable.background_horizontal_red);
                    binding.ivHeaderBackground.setImageResource(R.drawable.head_mask_red);
                    binding.ivFaceBox.setImageResource(R.drawable.face_box_red);
                    feverCache = true;
                } else if (feverCache) {
                    Log.e("asd", "No fever~~~~~~~~~~~~````");
                    binding.ivPanel.setImageResource(R.drawable.background_horizontal_line_board);
                    binding.clRoot.setBackgroundResource(R.drawable.background_horizontal);
                    binding.ivHeaderBackground.setImageResource(R.drawable.head_mask);
                    binding.ivFaceBox.setImageResource(R.drawable.face_box);
                    feverCache = false;
                }
            } else {

            }
        }
    };

    private Observer<Boolean> dormancyObserver = dormancy -> {
        if (dormancyCache == dormancy) return;
        if (dormancy) {
            binding.ivFaceSign.setVisibility(View.VISIBLE);
        } else {
            binding.ivFaceSign.setVisibility(View.INVISIBLE);
        }
        dormancyCache = dormancy;
    };

    private Observer<Boolean> heatMapObserver = update -> {
        if (viewModel.heatMapCache != null) {
            GlideApp.with(this)
                    .asDrawable()
                    .load(viewModel.heatMapCache)
                    .dontAnimate()
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(binding.ivHeatMap);
        } else {
            GlideApp.with(this).clear(binding.ivHeatMap);
        }
    };

    private Observer<Boolean> initCardObserver = result -> {
        App.getInstance().getThreadExecutor().execute(() -> {
            CardManager.getInstance().initDevice(App.getInstance(), viewModel.cardStatusListener);
        });
    };

    private Observer<Boolean> cardStatusObserver = status -> {};

    private Observer<Boolean> initFingerObserver = result -> {
        App.getInstance().getThreadExecutor().execute(() -> {
            FingerManager.getInstance().initDevice(viewModel.fingerStatusListener);
        });
    };

    private Observer<Boolean> fingerStatusObserver = status -> {};

    private Observer<Boolean> initICCardObserver = result -> {
        App.getInstance().getThreadExecutor().execute(() -> {
            ICCardManager.getInstance().initDevice(App.getInstance(), viewModel.icCardStatusListener);
        });
    };

    private Observer<Boolean> icCardStatusObserver = status -> {};

    private Observer<Boolean> timingSwitchObserver = this::controlCameraOpen;

    private Observer<Boolean> humanDetectObserver = status -> {
        Log.e("asd", "HumanDetectInMain!!!!!!:" + status);
        Config config = ConfigManager.getInstance().getConfig();
        if (config.isTimingSwitch()) {
            if (!TimingSwitchManager.getInstance().isInSwitchTime()) {
                return;
            }
        }
        controlCameraOpen(status);
    };

    private void resetLayoutParams(View view, int fixWidth, int fixHeight) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = fixWidth;
        layoutParams.height = fixHeight;
        view.setLayoutParams(layoutParams);
    }

    private void initTimeReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        getContext().registerReceiver(timeReceiver, filter);
        onTimeEvent();
    }

    private BroadcastReceiver timeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            App.getInstance().getThreadExecutor().execute(() -> {
                if (Intent.ACTION_TIME_TICK.equals(intent.getAction())) {
                    onTimeEvent();//每一分钟更新时间
                }
            });
        }
    };

    private void onTimeEvent() {
        String date = dateFormat.format(new Date());
        String time = timeFormat.format(new Date());
        String week = DateUtil.getWeekStr();
        viewModel.date.set(date);
        viewModel.time.set(time);
        viewModel.week.set(week);
    }

    private void initAdvertisementDialog() {
        advertisementDialogFragment = AdvertisementDialogFragment.newInstance();
        advertisementDialogFragment.setOnClickListener(v -> {
//            advertisementDialogFragment.dismiss();
//            FaceManager.getInstance().interruptDormancy();
        });
        advertisementDialogFragment.setComboListener(() -> {
            advertisementDialogFragment.dismiss();
            mListener.backToStack(null);
        });
    }

    private void controlCameraOpen(boolean status) {
        if (cameraOpenReentrantLock.isLocked()) return;
        try {
            cameraOpenReentrantLock.lock();
            if (status) {
                CameraManager.getInstance().openCamera(binding.tvCamera, cameraListener);
            } else {
                viewModel.stopFaceDetect();
                CameraManager.getInstance().closeCamera();
            }
            controlAdvertisementDialog(!status);
        } finally {
            if (handler != null) {
                handler.postDelayed(() -> {
                    if (cameraOpenReentrantLock.isLocked()) {
                        cameraOpenReentrantLock.unlock();
                    }
                }, 700);
            }
        }
    }

    private void controlAdvertisementDialog(boolean show) {
        try {
            if (show && !advertisementDialogFragment.isVisible()) {
                advertisementDialogFragment.show(getChildFragmentManager(), "AdvertisementDialogFragment");
            } else if (advertisementDialogFragment.isVisible()) {
                if (handler != null) {
                    handler.postDelayed(() -> {
                        advertisementDialogFragment.dismiss();
                    }, 500);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
