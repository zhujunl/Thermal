package com.miaxis.thermal.view.base;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.miaxis.thermal.app.App;
import com.miaxis.thermal.manager.ToastManager;
import com.miaxis.thermal.viewModel.BaseViewModel;

public abstract class BaseViewModelFragment<V extends ViewDataBinding, VM extends BaseViewModel> extends Fragment {

    protected OnFragmentInteractionListener mListener;
    protected V binding;
    protected VM viewModel;
    protected int viewModelId;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, setContentView(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = initViewModel();
        viewModelId = initVariableId();
        binding.setLifecycleOwner(this);
        binding.setVariable(viewModelId, viewModel);
        viewModel.waitMessage.observe(this, s -> {
            if (TextUtils.isEmpty(s)) {
                mListener.dismissWaitDialog();
            } else {
                mListener.showWaitDialog(s);
            }
        });
        viewModel.resultMessage.observe(this, s -> {
            if (TextUtils.isEmpty(s)) {
                mListener.dismissResultDialog();
            } else {
                mListener.showResultDialog(s);
            }
        });
        viewModel.toast.observe(this, toastBody -> ToastManager.toast(toastBody.getMessage(), toastBody.getMode()));
        view.setOnTouchListener((v, motionEvent) -> getActivity().onTouchEvent(motionEvent));
        initData();
        initView();
    }

    protected abstract int setContentView();

    protected abstract VM initViewModel();

    public abstract int initVariableId();

    protected abstract void initData();

    protected abstract void initView();

    public abstract void onBackPressed();

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (binding != null) {
            binding.unbind();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    protected ViewModelProvider.AndroidViewModelFactory getViewModelProviderFactory() {
        return ViewModelProvider.AndroidViewModelFactory.getInstance(App.getInstance());
    }

}
