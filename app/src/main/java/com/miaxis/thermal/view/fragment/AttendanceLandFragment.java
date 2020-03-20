package com.miaxis.thermal.view.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.miaxis.thermal.R;
import com.miaxis.thermal.app.App;
import com.miaxis.thermal.bridge.GlideApp;
import com.miaxis.thermal.data.entity.FaceDraw;
import com.miaxis.thermal.databinding.FragmentAttendanceLandBinding;
import com.miaxis.thermal.manager.AmapManager;
import com.miaxis.thermal.manager.CameraManager;
import com.miaxis.thermal.manager.FaceManager;
import com.miaxis.thermal.manager.GpioManager;
import com.miaxis.thermal.util.DateUtil;
import com.miaxis.thermal.view.base.BaseViewModelFragment;
import com.miaxis.thermal.viewModel.AttendanceViewModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AttendanceLandFragment extends BaseViewModelFragment<FragmentAttendanceLandBinding, AttendanceViewModel> {

    private static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.CHINA);
    private static DateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);

    private boolean feverCache = false;

    public  static AttendanceLandFragment newInstance() {
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
    }

    @Override
    protected void initView() {
        binding.tvCamera.getViewTreeObserver().addOnGlobalLayoutListener(globalListener);
        binding.ccCombo.setNeedPassword(true);
        binding.ccCombo.setListener(() -> {
            mListener.backToStack(null);
        });
        initTimeReceiver();
        AmapManager.getInstance().startLocation(App.getInstance());
        AmapManager.getInstance().setListener(weather -> {
            viewModel.weather.set(weather);
        });
    }

    @Override
    public void onBackPressed() {
        mListener.backToStack(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.tvCamera.setDrawingCacheEnabled(false);
        CameraManager.getInstance().closeCamera();
        viewModel.stopFaceDetect();
        viewModel.faceDraw.removeObserver(faceDrawObserver);
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

    private CameraManager.OnCameraOpenListener cameraListener = previewSize -> {
        int rootHeight = binding.flCameraRoot.getHeight();
        int rootWidth = binding.flCameraRoot.getWidth();
        resetLayoutParams(binding.tvCamera, rootWidth, rootHeight);
        resetLayoutParams(binding.rsvRect, rootWidth, rootHeight);
        binding.rsvRect.setRootSize(rootWidth, rootHeight);
        binding.rsvRect.setZoomRate((float) rootWidth / CameraManager.getInstance().getPreviewSize().getWidth());
        viewModel.startFaceDetect();
    };

    private Observer<FaceDraw> faceDrawObserver = faceDraw -> {
        binding.rsvRect.drawRect(faceDraw.getMxFaceInfoExes(), faceDraw.getFaceNum());
    };

    private Observer<Boolean> headerObserver = update -> {
        Bitmap header = viewModel.headerCache;
        if (header == null) {
            GlideApp.with(this).clear(binding.ivHeader);
        } else {
            GlideApp.with(this)
                    .load(header)
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(binding.ivHeader);
        }
    };

    private Observer<Boolean> feverObserver = fever -> {
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
            if (Intent.ACTION_TIME_TICK.equals(intent.getAction())) {
                onTimeEvent();//每一分钟更新时间
            }
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

}
