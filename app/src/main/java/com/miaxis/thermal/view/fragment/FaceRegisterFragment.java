package com.miaxis.thermal.view.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.miaxis.thermal.R;
import com.miaxis.thermal.data.entity.MatchPerson;
import com.miaxis.thermal.databinding.FragmentFaceRegisterBinding;
import com.miaxis.thermal.manager.CameraManager;
import com.miaxis.thermal.manager.GpioManager;
import com.miaxis.thermal.view.auxiliary.OnLimitClickHelper;
import com.miaxis.thermal.view.base.BaseViewModelFragment;
import com.miaxis.thermal.view.custom.RoundBorderView;
import com.miaxis.thermal.view.custom.RoundFrameLayout;
import com.miaxis.thermal.view.dialog.DialogHelper;
import com.miaxis.thermal.viewModel.FaceRegisterViewModel;

public class FaceRegisterFragment extends BaseViewModelFragment<FragmentFaceRegisterBinding, FaceRegisterViewModel> {

    private RoundBorderView roundBorderView;
    private RoundFrameLayout roundFrameLayout;

    private boolean forUpdate = false;

    public static FaceRegisterFragment newInstance(boolean forUpdate) {
        FaceRegisterFragment fragment = new FaceRegisterFragment();
        fragment.setForUpdate(forUpdate);
        return fragment;
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
        viewModel.repeatFaceFlag.observe(this, repeatFaceFlag);
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
        GpioManager.getInstance().openWhiteLed();
    }

    @Override
    public void onBackPressed() {
        mListener.backToStack(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        CameraManager.getInstance().closeCamera();
        GpioManager.getInstance().closeWhiteLed();
    }

    private Observer<MatchPerson> repeatFaceFlag = matchPerson -> {
        if (matchPerson != null && !forUpdate) {
            DialogHelper.fullScreenMaterialDialogLink(new MaterialDialog.Builder(getContext())
                    .title("高相似度人脸")
                    .content("检测到就绪状态人员库中，存在高相似度人脸\n"
                            + "最高相似度人员：" + matchPerson.getPerson().getIdentifyNumber()
                            + "姓名：" + matchPerson.getPerson().getName()
                            + "相似度：" + matchPerson.getScore())
                    .cancelable(false)
                    .autoDismiss(false)
                    .positiveText("丢弃")
                    .onPositive((dialog, which) -> {
                        binding.ivRetry.performClick();
                        dialog.dismiss();

                    })
                    .negativeText("忽视")
                    .onNegative((dialog, which) -> {
                        dialog.dismiss();
                    })
                    .build())
                    .show();
        }
    };

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

    private CameraManager.OnCameraOpenListener cameraListener = (previewSize, message) -> {
        if (previewSize == null) {
            mListener.showResultDialog("摄像机多次打开失败：\n" + message);
        } else {
            FrameLayout.LayoutParams textureViewLayoutParams = (FrameLayout.LayoutParams) binding.rtvCamera.getLayoutParams();
            int newHeight = textureViewLayoutParams.width * previewSize.width / previewSize.height;
            int newWidth = textureViewLayoutParams.width;

            roundFrameLayout = new RoundFrameLayout(getContext());
            int sideLength = Math.min(newWidth, newHeight);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(sideLength, sideLength);
            roundFrameLayout.setLayoutParams(layoutParams);
            FrameLayout parentView = (FrameLayout) binding.rtvCamera.getParent();
            parentView.removeView(binding.rtvCamera);
            parentView.addView(roundFrameLayout);

            roundFrameLayout.addView(binding.rtvCamera);
            FrameLayout.LayoutParams newTextureViewLayoutParams = new FrameLayout.LayoutParams(newWidth, newHeight);
            newTextureViewLayoutParams.topMargin = -(newHeight - newWidth) / 2;
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
        }
    };

    public void setForUpdate(boolean forUpdate) {
        this.forUpdate = forUpdate;
    }
}