package com.miaxis.thermal.view.dialog;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.miaxis.thermal.R;
import com.miaxis.thermal.bridge.GlideApp;
import com.miaxis.thermal.databinding.FragmentPhotoDialogBinding;
import com.miaxis.thermal.view.base.BaseDialogFragment;
import com.miaxis.thermal.view.base.BaseViewModelDialogFragment;

public class PhotoDialogFragment extends BaseDialogFragment<FragmentPhotoDialogBinding> {

    private Object image;

    public static PhotoDialogFragment newInstance(Object image) {
        PhotoDialogFragment dialogFragment = new PhotoDialogFragment();
        dialogFragment.setImage(image);
        return dialogFragment;
    }

    public PhotoDialogFragment() {
        // Required empty public constructor
    }

    @Override
    protected int setContentView() {
        return R.layout.fragment_photo_dialog;
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initView() {
        GlideApp.with(this).load(image).into(binding.ivPhoto);
    }

    @Override
    public void onStart() {
        super.onStart();
        Window win = getDialog().getWindow();
        win.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        WindowManager.LayoutParams params = win.getAttributes();
        params.gravity = Gravity.CENTER;
        params.width = (int) (dm.widthPixels * 0.7);
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        win.setAttributes(params);
    }

    public void setImage(Object image) {
        this.image = image;
    }
}
