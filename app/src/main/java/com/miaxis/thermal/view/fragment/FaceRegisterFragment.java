package com.miaxis.thermal.view.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import com.miaxis.thermal.BR;
import com.miaxis.thermal.R;
import com.miaxis.thermal.databinding.FragmentFaceRegisterBinding;
import com.miaxis.thermal.manager.CameraManager;
import com.miaxis.thermal.view.auxiliary.OnLimitClickHelper;
import com.miaxis.thermal.view.base.BaseViewModelFragment;
import com.miaxis.thermal.view.custom.RoundBorderView;
import com.miaxis.thermal.view.custom.RoundFrameLayout;
import com.miaxis.thermal.viewModel.FaceRegisterViewModel;

public class FaceRegisterFragment extends BaseViewModelFragment<FragmentFaceRegisterBinding, FaceRegisterViewModel> {

    private RoundBorderView roundBorderView;
    private RoundFrameLayout roundFrameLayout;

    public static FaceRegisterFragment newInstance() {
        return new FaceRegisterFragment();
    }

    public FaceRegisterFragment() {
        // Required empty public constructor
    }

    @Override
    protected int setContentView() {
        return R.layout.fragment_face_register;
    }

    @Override
    protected FaceRegisterViewModel initViewModel() {
        return new ViewModelProvider(this, getViewModelProviderFactory()).get(FaceRegisterViewModel.class);
    }

    @Override
    public int initVariableId() {
        return com.miaxis.thermal.BR.viewModel;
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initView() {
        binding.ivBack.setOnClickListener(v -> onBackPressed());
        binding.rtvCamera.getViewTreeObserver().addOnGlobalLayoutListener(globalListener);
        binding.ivTakePhoto.setOnClickListener(new OnLimitClickHelper(view -> {
            viewModel.takePicture(binding.rtvCamera);
        }));
        binding.ivRetry.setOnClickListener(new OnLimitClickHelper(view -> viewModel.retry()));
        binding.ivConfirm.setOnClickListener(new OnLimitClickHelper(v -> viewModel.confirm()));
        viewModel.confirmFlag.observe(this, confirm -> mListener.backToStack(null));
    }

    @Override
    public void onBackPressed() {
        mListener.backToStack(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        CameraManager.getInstance().closeCamera();
    }

    private ViewTreeObserver.OnGlobalLayoutListener globalListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            binding.rtvCamera.getViewTreeObserver().removeOnGlobalLayoutListener(globalListener);
            ViewGroup.LayoutParams layoutParams = binding.rtvCamera.getLayoutParams();
            layoutParams.width = binding.flCamera.getWidth();
            layoutParams.height = binding.flCamera.getHeight();
            binding.rtvCamera.setLayoutParams(layoutParams);
            binding.rtvCamera.turnRound();
            CameraManager.getInstance().openCamera(binding.rtvCamera, cameraListener);
        }
    };

    private CameraManager.OnCameraOpenListener cameraListener = previewSize -> {
        FrameLayout.LayoutParams textureViewLayoutParams = (FrameLayout.LayoutParams) binding.rtvCamera.getLayoutParams();
        int newWidth = textureViewLayoutParams.width;
        int newHeight = textureViewLayoutParams.width * previewSize.height / previewSize.width;

        //当不是正方形预览的情况下，添加一层ViewGroup限制View的显示区域
        roundFrameLayout = new RoundFrameLayout(getContext());
        int sideLength = Math.min(newWidth, newHeight);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(sideLength, sideLength);
        int margin = (newWidth - newHeight) / 2;
        layoutParams.setMargins(margin, margin, margin, margin);
        roundFrameLayout.setLayoutParams(layoutParams);
        FrameLayout parentView = (FrameLayout) binding.rtvCamera.getParent();
        parentView.removeView(binding.rtvCamera);
        parentView.addView(roundFrameLayout);

        roundFrameLayout.addView(binding.rtvCamera);
        FrameLayout.LayoutParams newTextureViewLayoutParams = new FrameLayout.LayoutParams(newWidth, newHeight);
        newTextureViewLayoutParams.rightMargin = margin * 4;
        binding.rtvCamera.setLayoutParams(newTextureViewLayoutParams);

        View siblingView = roundFrameLayout != null ? roundFrameLayout : binding.rtvCamera;
        roundBorderView = new RoundBorderView(getContext());
        ((FrameLayout) siblingView.getParent()).addView(roundBorderView, siblingView.getLayoutParams());

//        new Handler(Looper.getMainLooper()).post(() -> {
//            roundFrameLayout.setRadius(Math.min(roundFrameLayout.getWidth(), roundFrameLayout.getHeight()) / 2);
//            roundFrameLayout.turnRound();
//            roundBorderView.setRadius(Math.min(roundBorderView.getWidth(), roundBorderView.getHeight()) / 2);
//            roundBorderView.turnRound();
//        });

    };

}
