package com.miaxis.thermal.view.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import com.miaxis.thermal.BR;
import com.miaxis.thermal.R;
import com.miaxis.thermal.bridge.GlideApp;
import com.miaxis.thermal.data.entity.Person;
import com.miaxis.thermal.data.event.FaceRegisterEvent;
import com.miaxis.thermal.databinding.FragmentAddPersonBinding;
import com.miaxis.thermal.manager.ToastManager;
import com.miaxis.thermal.util.DateUtil;
import com.miaxis.thermal.util.ValueUtil;
import com.miaxis.thermal.view.auxiliary.OnLimitClickHelper;
import com.miaxis.thermal.view.auxiliary.OnLimitClickListener;
import com.miaxis.thermal.view.base.BaseViewModelFragment;
import com.miaxis.thermal.viewModel.AddPersonViewModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Calendar;

public class AddPersonFragment extends BaseViewModelFragment<FragmentAddPersonBinding, AddPersonViewModel> {

    private Person person;

    public static AddPersonFragment newInstance(Person person) {
        AddPersonFragment fragment = new AddPersonFragment();
        fragment.setPerson(person);
        return fragment;
    }

    public AddPersonFragment() {
        // Required empty public constructor
    }

    @Override
    protected int setContentView() {
        return R.layout.fragment_add_person;
    }

    @Override
    protected AddPersonViewModel initViewModel() {
        return new ViewModelProvider(this, getViewModelProviderFactory()).get(AddPersonViewModel.class);
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
        if (person != null) {
            viewModel.setPersonCache(person);
            if (!TextUtils.isEmpty(person.getName())) {
                viewModel.name.set(person.getName());
                binding.etName.setEnabled(false);
            }
            if (!TextUtils.isEmpty(person.getIdentifyNumber())) {
                viewModel.number.set(person.getIdentifyNumber());
                binding.etNumber.setEnabled(false);
            }
            if (!TextUtils.isEmpty(person.getPhone())) {
                viewModel.phone.set(person.getPhone());
                binding.etPhone.setEnabled(false);
            }
            if (person.getEffectiveTime() != null) {
                viewModel.effectTime.set(DateUtil.DATE_FORMAT.format(person.getEffectiveTime()));
                binding.tvEffectTime.setEnabled(false);
            }
            if (person.getInvalidTime() != null) {
                viewModel.invalidTime.set(DateUtil.DATE_FORMAT.format(person.getInvalidTime()));
                binding.tvInvalidTime.setEnabled(false);
            }
            if (!TextUtils.isEmpty(person.getType())) {
                viewModel.type.set(TextUtils.equals(person.getType(), ValueUtil.PERSON_TYPE_WORKER));
                binding.rgType.setEnabled(false);
                binding.rbWorker.setEnabled(false);
                binding.rbVisitor.setEnabled(false);
            }
            if (!TextUtils.isEmpty(person.getFacePicturePath())) {
                GlideApp.with(this).load(person.getFacePicturePath()).into(binding.ivHeader);
            }
        }
        binding.ivBack.setOnClickListener(v -> onBackPressed());
        binding.tvHeader.setOnClickListener(new OnLimitClickHelper(view -> mListener.replaceFragment(FaceRegisterFragment.newInstance())));
        binding.tvEffectTime.setOnClickListener(new OnLimitClickHelper(view -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(getContext(), (view1, year, month, dayOfMonth) -> {
                String monthStr = month + 1 > 9 ? "" + (month + 1) : "0" + (month + 1);
                String dayStr = dayOfMonth > 9 ? "" + dayOfMonth : "0" + dayOfMonth;
                String date = year + "-" + monthStr + "-" + dayStr + " 23:59:59";
                viewModel.effectTime.set(date);
            }, calendar.get(Calendar.YEAR)
                    , calendar.get(Calendar.MONTH)
                    , calendar.get(Calendar.DAY_OF_MONTH)).show();
        }));
        binding.tvInvalidTime.setOnClickListener(new OnLimitClickHelper(view -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(getContext(), (view1, year, month, dayOfMonth) -> {
                String monthStr = month + 1 > 9 ? "" + (month + 1) : "0" + (month + 1);
                String dayStr = dayOfMonth > 9 ? "" + dayOfMonth : "0" + dayOfMonth;
                String date = year + "-" + monthStr + "-" + dayStr + " 23:59:59";
                viewModel.invalidTime.set(date);
            }, calendar.get(Calendar.YEAR)
                    , calendar.get(Calendar.MONTH)
                    , calendar.get(Calendar.DAY_OF_MONTH)).show();
        }));
        binding.btnRegister.setOnClickListener(v -> {
            if (viewModel.checkInput()) {
                viewModel.savePerson();
            } else {
                ToastManager.toast("请修改信息", ToastManager.INFO);
            }
        });
        EventBus.getDefault().register(this);
    }

    @Override
    public void onBackPressed() {
        mListener.backToStack(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onFaceRegisterEvent(FaceRegisterEvent event) {
        viewModel.faceFeatureHint.set("已采集");
        binding.tvHeader.setOnClickListener(null);
        viewModel.setFeatureCache(event.getFeature());
        viewModel.setHeaderCache(event.getBitmap());
        GlideApp.with(this).load(event.getBitmap()).into(binding.ivHeader);
        EventBus.getDefault().removeStickyEvent(event);
    }

    public void setPerson(Person person) {
        this.person = person;
    }
}
