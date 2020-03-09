package com.miaxis.thermal.view.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.miaxis.thermal.BR;
import com.miaxis.thermal.R;
import com.miaxis.thermal.databinding.FragmentHomeBinding;
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
        binding.clAttendance.setOnClickListener(new OnLimitClickHelper(view -> mListener.replaceFragment(AttendanceLandFragment.newInstance())));
        binding.clPerson.setOnClickListener(new OnLimitClickHelper(view -> mListener.replaceFragment(PersonFragment.newInstance())));
        binding.clAddPerson.setOnClickListener(new OnLimitClickHelper(view -> mListener.replaceFragment(AddPersonFragment.newInstance(null))));
        binding.clRecord.setOnClickListener(new OnLimitClickHelper(view -> mListener.replaceFragment(RecordFragment.newInstance())));
        binding.clConfig.setOnClickListener(new OnLimitClickHelper(view -> mListener.replaceFragment(ConfigFragment.newInstance())));
        binding.ccCombo.setListener(() -> mListener.exitApp());
        binding.ccCombo.setNeedPassword(false);
    }

    @Override
    public void onBackPressed() {

    }
}
