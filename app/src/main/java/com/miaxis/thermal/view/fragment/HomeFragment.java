package com.miaxis.thermal.view.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.miaxis.thermal.BR;
import com.miaxis.thermal.R;
import com.miaxis.thermal.app.App;
import com.miaxis.thermal.databinding.FragmentHomeBinding;
import com.miaxis.thermal.manager.strategy.Sign;
import com.miaxis.thermal.util.ValueUtil;
import com.miaxis.thermal.view.auxiliary.OnLimitClickHelper;
import com.miaxis.thermal.view.auxiliary.OnLimitClickListener;
import com.miaxis.thermal.view.base.BaseViewModelFragment;
import com.miaxis.thermal.view.custom.ComboCustom;
import com.miaxis.thermal.viewModel.HomeViewModel;

public class HomeFragment extends BaseViewModelFragment<FragmentHomeBinding, HomeViewModel> {

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    protected int setContentView() {
        return R.layout.fragment_home;
    }

    @Override
    protected HomeViewModel initViewModel() {
        return new ViewModelProvider(this, getViewModelProviderFactory()).get(HomeViewModel.class);
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
        binding.clAttendance.setOnClickListener(new OnLimitClickHelper(view -> {
            if (ValueUtil.DEFAULT_SIGN == Sign.XH
                    || ValueUtil.DEFAULT_SIGN == Sign.XH_N) {
                mListener.replaceFragment(AttendanceLandFragment.newInstance());
            } else if (ValueUtil.DEFAULT_SIGN == Sign.MR870
                    || ValueUtil.DEFAULT_SIGN == Sign.ZH
                    || ValueUtil.DEFAULT_SIGN == Sign.TPS980P
                    || ValueUtil.DEFAULT_SIGN == Sign.MR890) {
                mListener.replaceFragment(AttendanceFragment.newInstance());
            }
        }));
        binding.clPerson.setOnClickListener(new OnLimitClickHelper(view -> mListener.replaceFragment(PersonFragment.newInstance())));
        binding.clAddPerson.setOnClickListener(new OnLimitClickHelper(view -> mListener.replaceFragment(AddPersonFragment.newInstance(null))));
        binding.clRecord.setOnClickListener(new OnLimitClickHelper(view -> mListener.replaceFragment(RecordFragment.newInstance())));
        binding.clConfig.setOnClickListener(new OnLimitClickHelper(view -> mListener.replaceFragment(ConfigFragment.newInstance())));
        binding.ccCombo.setListener(() -> mListener.exitApp());
        binding.ccCombo.setNeedPassword(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (App.getInstance().isFirstIn()) {
            binding.clAttendance.performClick();
        }
    }

    @Override
    public void onBackPressed() {
        mListener.exitApp();
    }
}
