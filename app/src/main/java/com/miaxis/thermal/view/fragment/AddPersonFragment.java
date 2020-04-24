package com.miaxis.thermal.view.fragment;

import android.app.DatePickerDialog;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.miaxis.thermal.R;
import com.miaxis.thermal.app.App;
import com.miaxis.thermal.bridge.GlideApp;
import com.miaxis.thermal.data.entity.Person;
import com.miaxis.thermal.data.event.FaceRegisterEvent;
import com.miaxis.thermal.databinding.FragmentAddPersonBinding;
import com.miaxis.thermal.manager.CardManager;
import com.miaxis.thermal.manager.ConfigManager;
import com.miaxis.thermal.manager.ToastManager;
import com.miaxis.thermal.manager.strategy.Sign;
import com.miaxis.thermal.util.DateUtil;
import com.miaxis.thermal.util.PatternUtil;
import com.miaxis.thermal.util.ValueUtil;
import com.miaxis.thermal.view.auxiliary.OnLimitClickHelper;
import com.miaxis.thermal.view.base.BaseViewModelFragment;
import com.miaxis.thermal.view.dialog.DialogHelper;
import com.miaxis.thermal.viewModel.AddPersonViewModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Calendar;
import java.util.Date;

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
        viewModel.registerFlag.observe(this, registerObserver);
    }

    @Override
    protected void initView() {
        binding.ivBack.setOnClickListener(v -> onBackPressed());
        binding.tvFaceFeature.setOnClickListener(new OnLimitClickHelper(view -> {
            if (ConfigManager.isLandCameraDevice()) {
                mListener.replaceFragment(FaceRegisterLandFragment.newInstance(person != null));
            } else {
                mListener.replaceFragment(FaceRegisterFragment.newInstance(person != null));
            }
        }));
        initPerson();
        initTextListener();
        initDateSelector();
        binding.btnRegister.setOnClickListener(v -> savePerson());
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
        if (ConfigManager.isCardDevice()) {
            CardManager.getInstance().release();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onFaceRegisterEvent(FaceRegisterEvent event) {
        viewModel.faceFeatureHint.set(getString(R.string.fragment_add_person_face_collect_over));
        binding.tvFaceFeature.setOnClickListener(null);
        viewModel.setFeatureCache(event.getFaceFeature());
        viewModel.setMaskFeatureCache(event.getMaskFaceFeature());
        viewModel.setHeaderCache(event.getBitmap());
        GlideApp.with(this)
                .load(event.getBitmap())
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(binding.ivHeader);
        EventBus.getDefault().removeStickyEvent(event);
    }

    private void initPerson() {
        if (person != null) {
            binding.tvTitle.setText("修改人员");
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
                GlideApp.with(this)
                        .load(person.getFacePicturePath())
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(binding.ivHeader);
            }
            binding.btnRegister.setText("修改");
        } else {
            binding.tvTitle.setText("新增人员");
            if (ConfigManager.isCardDevice()) {
                viewModel.initCard.observe(this, initCardObserver);
                viewModel.cardStatus.observe(this, cardStatusObserver);
                viewModel.initCard.setValue(Boolean.TRUE);
            } else {
                initTextListener();
            }
        }
    }

    private void initTextListener() {
        if (person == null && ConfigManager.isNeedPatternMatcherDevice()) {
            binding.etNumber.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    String inputText = s.toString();
                    if (TextUtils.isEmpty(inputText) || PatternUtil.isIDNumber(inputText)) {
                        binding.tvNumberWarn.setVisibility(View.INVISIBLE);
                    } else {
                        binding.tvNumberWarn.setVisibility(View.VISIBLE);
                    }
                }
            });
            binding.etPhone.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    String inputText = s.toString();
                    if (TextUtils.isEmpty(inputText) || PatternUtil.checkMobilePhone(inputText)) {
                        binding.tvPhoneWarn.setVisibility(View.INVISIBLE);
                    } else {
                        binding.tvPhoneWarn.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    private void initDateSelector() {
        binding.tvEffectTime.setOnClickListener(new OnLimitClickHelper(view -> {
            Calendar calendar = Calendar.getInstance();
            DialogHelper.fullScreenAlertDialogLink(new DatePickerDialog(getContext(), (view1, year, month, dayOfMonth) -> {
                String monthStr = month + 1 > 9 ? "" + (month + 1) : "0" + (month + 1);
                String dayStr = dayOfMonth > 9 ? "" + dayOfMonth : "0" + dayOfMonth;
                String date = year + "-" + monthStr + "-" + dayStr + " 23:59:59";
                viewModel.effectTime.set(date);
            }, calendar.get(Calendar.YEAR)
                    , calendar.get(Calendar.MONTH)
                    , calendar.get(Calendar.DAY_OF_MONTH))).show();
        }));
        binding.tvInvalidTime.setOnClickListener(new OnLimitClickHelper(view -> {
            Calendar calendar = Calendar.getInstance();
            DialogHelper.fullScreenAlertDialogLink(new DatePickerDialog(getContext(), (view1, year, month, dayOfMonth) -> {
                String monthStr = month + 1 > 9 ? "" + (month + 1) : "0" + (month + 1);
                String dayStr = dayOfMonth > 9 ? "" + dayOfMonth : "0" + dayOfMonth;
                String date = year + "-" + monthStr + "-" + dayStr + " 23:59:59";
                viewModel.invalidTime.set(date);
            }, calendar.get(Calendar.YEAR)
                    , calendar.get(Calendar.MONTH)
                    , calendar.get(Calendar.DAY_OF_MONTH))).show();
        }));
    }

    private void savePerson() {
        try {
            if (TextUtils.isEmpty(viewModel.name.get())) {
                ToastManager.toast("请输入姓名", ToastManager.INFO);
                return;
            }
            if (TextUtils.isEmpty(viewModel.number.get())) {
                ToastManager.toast("请输入证件号码", ToastManager.INFO);
                return;
            }
            if (TextUtils.isEmpty(viewModel.phone.get())) {
                ToastManager.toast("请输入手机号码", ToastManager.INFO);
                return;
            }
            if (TextUtils.isEmpty(viewModel.effectTime.get()) || TextUtils.equals(viewModel.effectTime.get(), "请选择生效日期")) {
                ToastManager.toast("请输入生效日期", ToastManager.INFO);
                return;
            }
            if (TextUtils.equals(viewModel.invalidTime.get(), getString(R.string.fragment_add_person_invalid_time_hint))) {
                ToastManager.toast("请输入失效日期", ToastManager.INFO);
                return;
            }
            if (viewModel.type.get() == null) {
                ToastManager.toast("请选择人员类型", ToastManager.INFO);
                return;
            }
            if (!viewModel.checkFaceInfo()) {
                if (person == null) {
                    ToastManager.toast("请采集人脸信息", ToastManager.INFO);
                } else {
                    ToastManager.toast("请修改人脸信息", ToastManager.INFO);
                }
                return;
            }
            if (person == null && !PatternUtil.checkMobilePhone(viewModel.phone.get())) {
                ToastManager.toast("手机号码格式验证不通过", ToastManager.INFO);
                return;
            }
            if (person == null && !PatternUtil.isIDNumber(viewModel.number.get())) {
                ToastManager.toast("证件号码格式验证不通过", ToastManager.INFO);
                return;
            }
            Date effectTime = DateUtil.DATE_FORMAT.parse(viewModel.effectTime.get());
            Date invalidTime = DateUtil.DATE_FORMAT.parse(viewModel.invalidTime.get());
            if (invalidTime.getTime() <= effectTime.getTime()) {
                ToastManager.toast("失效时间必须大于生效时间", ToastManager.INFO);
                return;
            }
            viewModel.savePerson();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Observer<Boolean> initCardObserver = aBoolean -> {
        App.getInstance().getThreadExecutor().execute(() -> CardManager.getInstance().initDevice(App.getInstance(), viewModel.statusListener));
    };

    private Observer<Boolean> cardStatusObserver = result -> {
        if (result) {
            binding.etName.setEnabled(false);
            binding.etNumber.setEnabled(false);
        } else {
            binding.etName.setEnabled(true);
            binding.etNumber.setEnabled(true);
            initTextListener();
        }
    };

    private Observer<Boolean> registerObserver = flag -> {
        onBackPressed();
    };

    public void setPerson(Person person) {
        this.person = person;
    }
}
