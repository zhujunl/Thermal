package com.miaxis.thermal.view.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.miaxis.thermal.BR;
import com.miaxis.thermal.R;
import com.miaxis.thermal.app.App;
import com.miaxis.thermal.data.entity.Record;
import com.miaxis.thermal.data.entity.RecordSearch;
import com.miaxis.thermal.databinding.FragmentRecordBinding;
import com.miaxis.thermal.manager.ExcelManager;
import com.miaxis.thermal.manager.ToastManager;
import com.miaxis.thermal.util.DateUtil;
import com.miaxis.thermal.util.FileUtil;
import com.miaxis.thermal.util.ValueUtil;
import com.miaxis.thermal.view.adapter.RecordAdapter;
import com.miaxis.thermal.view.auxiliary.OnLimitClickHelper;
import com.miaxis.thermal.view.auxiliary.OnLimitClickListener;
import com.miaxis.thermal.view.base.BaseViewModelFragment;
import com.miaxis.thermal.view.dialog.DialogHelper;
import com.miaxis.thermal.view.dialog.PhotoDialogFragment;
import com.miaxis.thermal.viewModel.RecordViewModel;

import java.io.File;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RecordFragment extends BaseViewModelFragment<FragmentRecordBinding, RecordViewModel> {

    private RecordAdapter adapter;
    private LinearLayoutManager layoutManager;

    private boolean loadingMore = false;
    private boolean search = false;
    private int currentPage = 1;
    private int localCount = 0;
    
    public static RecordFragment newInstance() {
        return new RecordFragment();
    }

    public RecordFragment() {
        // Required empty public constructor
    }

    @Override
    protected int setContentView() {
        return R.layout.fragment_record;
    }

    @Override
    protected RecordViewModel initViewModel() {
        return new ViewModelProvider(this, getViewModelProviderFactory()).get(RecordViewModel.class);
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
        initRecycleView();
        initSearchView();
        binding.ivBack.setOnClickListener(v -> onBackPressed());
        binding.srlRecord.setOnRefreshListener(this::refresh);
        binding.ivSearch.setOnClickListener(v -> {
            if (binding.clSearch.getVisibility() == View.VISIBLE) {
                binding.clSearch.setVisibility(View.GONE);
            } else {
                binding.clSearch.setVisibility(View.VISIBLE);
            }
        });
        binding.btnSearch.setOnClickListener(new OnLimitClickHelper(view -> {
            hideInputMethod();
            search = true;
            refresh();
        }));
        binding.btnExport.setOnClickListener(new OnLimitClickHelper(view -> {
            DialogHelper.fullScreenMaterialDialogLink(new MaterialDialog.Builder(getContext())
                    .title("确认导出查询结果？")
                    .positiveText("确认")
                    .onPositive((dialog, which) -> {
                        viewModel.exportRecord(makeRecordSearchView());
                    })
                    .negativeText("取消")
                    .build())
                    .show();
        }));
        binding.btnReset.setOnClickListener(new OnLimitClickHelper(view -> {
            binding.etName.setText("");
            binding.etNumber.setText("");
            binding.etPhone.setText("");
            binding.tvStartTime.setText("请选择开始日期");
            binding.tvEndTime.setText("请选择结束日期");
            binding.spFeverStatus.setSelection(0);
            binding.spUploadStatus.setSelection(0);
            hideInputMethod();
            search = false;
            refresh();
        }));
        viewModel.refreshing.observe(this, refreshingObserver);
        viewModel.recordListLiveData.observe(this, recordListObserver);
        refresh();
    }

    @Override
    public void onBackPressed() {
        mListener.backToStack(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.recordListLiveData.removeObserver(recordListObserver);
    }

    private void initRecycleView() {
        adapter = new RecordAdapter(getContext());
        adapter.setListener(adapterListener);
//        adapter.setHasStableIds(true);
        layoutManager = new LinearLayoutManager(getContext());
        binding.rvRecord.addOnScrollListener(onScrollListener);
        binding.rvRecord.setLayoutManager(layoutManager);
        binding.rvRecord.setAdapter(adapter);
        ((SimpleItemAnimator) binding.rvRecord.getItemAnimator()).setSupportsChangeAnimations(false);
    }

    private void initSearchView() {
        binding.tvStartTime.setOnClickListener(new OnLimitClickHelper(view -> {
            Calendar calendar = Calendar.getInstance();
            DialogHelper.fullScreenAlertDialogLink(new DatePickerDialog(getContext(), (view1, year, month, dayOfMonth) -> {
                String monthStr = month + 1 > 9 ? "" + (month + 1) : "0" + (month + 1);
                String dayStr = dayOfMonth > 9 ? "" + dayOfMonth : "0" + dayOfMonth;
                String date = year + "-" + monthStr + "-" + dayStr + " 00:00:00";
                if (!TextUtils.equals(binding.tvEndTime.getText().toString(), getString(R.string.fragment_record_search_end_time_value_hint))) {
                    if (!DateUtil.compareDate(date, binding.tvEndTime.getText().toString())) {
                        ToastManager.toast("开始时间必须小于结束时间", ToastManager.INFO);
                        binding.tvStartTime.setText(getString(R.string.fragment_record_search_start_time_value_hint));
                        return;
                    }
                }
                binding.tvStartTime.setText(date);
            }, calendar.get(Calendar.YEAR)
                    , calendar.get(Calendar.MONTH)
                    , calendar.get(Calendar.DAY_OF_MONTH))).show();
        }));
        binding.tvEndTime.setOnClickListener(new OnLimitClickHelper(view -> {
            Calendar calendar = Calendar.getInstance();
            DialogHelper.fullScreenAlertDialogLink(new DatePickerDialog(getContext(), (view1, year, month, dayOfMonth) -> {
                String monthStr = month + 1 > 9 ? "" + (month + 1) : "0" + (month + 1);
                String dayStr = dayOfMonth > 9 ? "" + dayOfMonth : "0" + dayOfMonth;
                String date = year + "-" + monthStr + "-" + dayStr + " 23:59:59";
                if (!TextUtils.equals(binding.tvStartTime.getText().toString(), getString(R.string.fragment_record_search_start_time_value_hint))) {
                    if (!DateUtil.compareDate(binding.tvStartTime.getText().toString(), date)) {
                        ToastManager.toast("开始时间必须小于结束时间", ToastManager.INFO);
                        binding.tvEndTime.setText(getString(R.string.fragment_record_search_end_time_value_hint));
                        return;
                    }
                }
                binding.tvEndTime.setText(date);
            }, calendar.get(Calendar.YEAR)
                    , calendar.get(Calendar.MONTH)
                    , calendar.get(Calendar.DAY_OF_MONTH))).show();
        }));
    }

    private RecordAdapter.OnItemClickListener adapterListener = (view, position) -> {
        PhotoDialogFragment.newInstance(adapter.getData(position).getVerifyPicturePath())
                .show(getChildFragmentManager(), "PhotoDialogFragment");
    };

    private Observer<List<Record>> recordListObserver = recordList -> {
        if (currentPage == 1) {
            adapter.setDataList(recordList);
            adapter.notifyDataSetChanged();
            if (localCount == 0) {
                binding.rvRecord.scrollToPosition(0);
            }
            localCount = recordList.size();
        } else {
            adapter.setDataList(recordList);
            adapter.notifyItemRangeChanged(localCount, recordList.size() - localCount);
            if (localCount != 0) {
                binding.rvRecord.scrollToPosition(localCount);
            }
            localCount = recordList.size();
        }
        viewModel.updateRecordCount(search, search ? makeRecordSearchView() : null);
        loadingMore = false;
    };

    private Observer<Boolean> refreshingObserver = flag -> binding.srlRecord.setRefreshing(flag);

    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (!loadingMore && layoutManager.findLastVisibleItemPosition() + 1 == adapter.getItemCount()) {
                loadMore();
            }
        }
    };

    private void refresh() {
        loadingMore = true;
        localCount = 0;
        currentPage = 1;
        viewModel.loadRecordByPage(makeRecordSearchView());
    }

    private void loadMore() {
        if (!viewModel.isLoadAllOver()) {
            loadingMore = true;
            currentPage++;
            viewModel.loadRecordByPage(makeRecordSearchView());
        }
    }

    private RecordSearch makeRecordSearchView() {
        if (!search) {
            return new RecordSearch.Builder()
                    .pageNum(currentPage)
                    .pageSize(30)
                    .build();
        } else {
            return new RecordSearch.Builder()
                    .pageNum(currentPage)
                    .pageSize(30)
                    .identifyNumber(binding.etNumber.getText().toString())
                    .phone(binding.etPhone.getText().toString())
                    .name(binding.etName.getText().toString())
                    .upload(binding.spUploadStatus.getSelectedItemPosition() == 0 ? null : (binding.spUploadStatus.getSelectedItemPosition() == 1 ? Boolean.TRUE : Boolean.FALSE))
                    .fever(binding.spFeverStatus.getSelectedItemPosition() == 0 ? null : (binding.spFeverStatus.getSelectedItemPosition() == 1 ? Boolean.FALSE : Boolean.TRUE))
                    .startTime(TextUtils.equals(binding.tvStartTime.getText().toString(), "请选择开始日期") ? null : binding.tvStartTime.getText().toString())
                    .endTime(TextUtils.equals(binding.tvEndTime.getText().toString(), "请选择结束日期") ? null : binding.tvEndTime.getText().toString())
                    .build();
        }
    }

}
