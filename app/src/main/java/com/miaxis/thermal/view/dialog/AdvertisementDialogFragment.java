package com.miaxis.thermal.view.dialog;

import android.app.Dialog;
import android.os.Bundle;

import androidx.lifecycle.ViewModelProvider;

import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.miaxis.thermal.BR;
import com.miaxis.thermal.R;
import com.miaxis.thermal.databinding.FragmentAdvertisementDialogBinding;
import com.miaxis.thermal.view.base.BaseViewModelDialogFragment;
import com.miaxis.thermal.view.custom.ComboCustom;
import com.miaxis.thermal.viewModel.AdvertisementDialogViewModel;

public class AdvertisementDialogFragment extends BaseViewModelDialogFragment<FragmentAdvertisementDialogBinding, AdvertisementDialogViewModel> {

    private View.OnClickListener clickListener;
    private ComboCustom.OnPwdConfirmListener comboListener;

    public static AdvertisementDialogFragment newInstance() {
        return new AdvertisementDialogFragment();
    }

    public AdvertisementDialogFragment() {
        // Required empty public constructor
    }

    @Override
    protected int setContentView() {
        return R.layout.fragment_advertisement_dialog;
    }

    @Override
    protected AdvertisementDialogViewModel initViewModel() {
        return new ViewModelProvider(this, getDefaultViewModelProviderFactory()).get(AdvertisementDialogViewModel.class);
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initView() {
//        binding.banner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE);
//        binding.banner.setImageLoader(new GlideImageLoader());
//        binding.banner.setImages(viewModel.getAdvertisementList());
//        binding.banner.setOnClickListener(clickListener);
        binding.ccCombo.setNeedPassword(true);
        binding.ccCombo.setListener(comboListener);
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        binding.banner.start();
//        binding.banner.startAutoPlay();
//    }

//    @Override
//    public void onStop() {
//        super.onStop();
//        binding.banner.stopAutoPlay();
//    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = new Dialog(getActivity(), R.style.AppTheme);
        Window window = dialog.getWindow();
        window.setGravity(Gravity.CENTER); //可设置dialog的位置
        window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);
        return dialog;
    }

    public void setOnClickListener(View.OnClickListener listener) {
        clickListener = listener;
    }

    public void setComboListener(ComboCustom.OnPwdConfirmListener comboListener) {
        this.comboListener = comboListener;
    }
}