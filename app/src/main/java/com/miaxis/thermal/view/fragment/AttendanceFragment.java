package com.miaxis.thermal.view.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.miaxis.thermal.BR;
import com.miaxis.thermal.R;
import com.miaxis.thermal.bridge.GlideApp;
import com.miaxis.thermal.data.entity.FaceDraw;
import com.miaxis.thermal.databinding.FragmentAttendanceBinding;
import com.miaxis.thermal.manager.CameraManager;
import com.miaxis.thermal.manager.FaceManager;
import com.miaxis.thermal.util.DateUtil;
import com.miaxis.thermal.view.base.BaseViewModelFragment;
import com.miaxis.thermal.view.custom.ComboCustom;
import com.miaxis.thermal.view.custom.RoundBorderView;
import com.miaxis.thermal.view.custom.RoundFrameLayout;
import com.miaxis.thermal.viewModel.AttendanceViewModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AttendanceFragment extends BaseViewModelFragment<FragmentAttendanceBinding, AttendanceViewModel> {

    private static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd  HH:mm", Locale.CHINA);

    private RoundBorderView roundBorderView;
    private RoundFrameLayout roundFrameLayout;

    public static AttendanceFragment newInstance() {
        return new AttendanceFragment();
    }

    public AttendanceFragment() {
        // Required empty public constructor
    }

    @Override
    protected int setContentView() {
        return R.layout.fragment_attendance;
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
    }

    @Override
    protected void initView() {
        binding.tvCamera.getViewTreeObserver().addOnGlobalLayoutListener(globalListener);
        binding.ccCombo.setNeedPassword(true);
        binding.ccCombo.setListener(() -> {
            mListener.backToStack(null);
        });
        initTimeReceiver();
    }

    @Override
    public void onBackPressed() {
        mListener.backToStack(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.stopFaceDetect();
        viewModel.faceDraw.removeObserver(faceDrawObserver);
    }

    private ViewTreeObserver.OnGlobalLayoutListener globalListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            binding.tvCamera.getViewTreeObserver().removeOnGlobalLayoutListener(globalListener);
//            ViewGroup.LayoutParams layoutParams = binding.tvCamera.getLayoutParams();
//            layoutParams.width = binding.flCameraRoot.getWidth();
//            layoutParams.height = binding.flCameraRoot.getHeight();
//            binding.tvCamera.setLayoutParams(layoutParams);
            CameraManager.getInstance().openCamera(binding.tvCamera, cameraListener);
        }
    };

    private CameraManager.OnCameraOpenListener cameraListener = previewSize -> {
        int rootHeight = binding.flCameraRoot.getHeight();
        int rootWidth = rootHeight * previewSize.height / previewSize.width;
        resetLayoutParams(binding.tvCamera, rootWidth, rootHeight);
        resetLayoutParams(binding.rsvRect, rootWidth, rootHeight);
        binding.rsvRect.setRootSize(rootWidth, rootHeight);
        binding.rsvRect.setZoomRate((float) rootWidth / FaceManager.ZOOM_WIDTH);
//        FrameLayout.LayoutParams textureViewLayoutParams = (FrameLayout.LayoutParams) binding.tvCamera.getLayoutParams();
//        int newHeight = textureViewLayoutParams.width * previewSize.width / previewSize.height;
//        int newWidth = textureViewLayoutParams.width;
//
//        roundFrameLayout = new RoundFrameLayout(getContext());
//        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(newWidth, newHeight);
//        roundFrameLayout.setLayoutParams(layoutParams);
//        roundFrameLayout.setBackgroundColor(Color.BLACK);
//        FrameLayout parentView = (FrameLayout) binding.tvCamera.getParent();
//        parentView.removeView(binding.tvCamera);
//        parentView.addView(roundFrameLayout);
//
//        roundFrameLayout.addView(binding.tvCamera);
//        FrameLayout.LayoutParams newTextureViewLayoutParams = new FrameLayout.LayoutParams(newWidth, newHeight);
//        newTextureViewLayoutParams.topMargin = -(newHeight - newWidth) / 2;
//        binding.tvCamera.setLayoutParams(newTextureViewLayoutParams);
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
        date = date + "  " + DateUtil.getWeekStr();
        viewModel.dateStr.set(date);
    }

}
