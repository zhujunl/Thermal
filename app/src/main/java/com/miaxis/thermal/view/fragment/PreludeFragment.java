package com.miaxis.thermal.view.fragment;

import androidx.lifecycle.ViewModelProvider;

import com.miaxis.thermal.R;
import com.miaxis.thermal.databinding.FragmentPreludeBinding;
import com.miaxis.thermal.view.auxiliary.OnLimitClickHelper;
import com.miaxis.thermal.view.base.BaseViewModelFragment;
import com.miaxis.thermal.viewModel.PreludeViewModel;

public class PreludeFragment extends BaseViewModelFragment<FragmentPreludeBinding, PreludeViewModel> {

    public static PreludeFragment newInstance() {
        return new PreludeFragment();
    }

    public PreludeFragment() {
        // Required empty public constructor
    }

    @Override
    protected int setContentView() {
        return R.layout.fragment_prelude;
    }

    @Override
    protected PreludeViewModel initViewModel() {
        return new ViewModelProvider(this, getViewModelProviderFactory()).get(PreludeViewModel.class);
    }

    @Override
    public int initVariableId() {
        return com.miaxis.thermal.BR.viewModel;
    }

    @Override
    protected void initData() {
        viewModel.getInitSuccess().observe(this, initResult -> {
            if (initResult) {
                mListener.setRoot(HomeFragment.newInstance());
            }
        });
    }

    @Override
    protected void initView() {
        binding.ivConfig.setOnClickListener(new OnLimitClickHelper(view -> mListener.replaceFragment(ConfigFragment.newInstance())));
        binding.btnQuit.setOnClickListener(v -> mListener.exitApp());
        binding.btnRetry.setOnClickListener(v -> viewModel.requirePermission(PreludeFragment.this));
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.requirePermission(this);
    }

}