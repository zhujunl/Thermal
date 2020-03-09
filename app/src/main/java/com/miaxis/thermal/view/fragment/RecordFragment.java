package com.miaxis.thermal.view.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.miaxis.thermal.BR;
import com.miaxis.thermal.R;
import com.miaxis.thermal.data.entity.Record;
import com.miaxis.thermal.databinding.FragmentRecordBinding;
import com.miaxis.thermal.view.adapter.RecordAdapter;
import com.miaxis.thermal.view.base.BaseViewModelFragment;
import com.miaxis.thermal.view.dialog.PhotoDialogFragment;
import com.miaxis.thermal.viewModel.RecordViewModel;

import java.util.List;

public class RecordFragment extends BaseViewModelFragment<FragmentRecordBinding, RecordViewModel> {

    private RecordAdapter adapter;
    private LinearLayoutManager layoutManager;
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
        binding.ivBack.setOnClickListener(v -> onBackPressed());
        binding.srlRecord.setOnRefreshListener(this::refresh);
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
        layoutManager = new LinearLayoutManager(getContext());
        binding.rvRecord.addOnScrollListener(onScrollListener);
        binding.rvRecord.setLayoutManager(layoutManager);
        binding.rvRecord.setAdapter(adapter);
        ((SimpleItemAnimator) binding.rvRecord.getItemAnimator()).setSupportsChangeAnimations(false);
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
        viewModel.updateRecordCount();
    };

    private Observer<Boolean> refreshingObserver = flag -> binding.srlRecord.setRefreshing(flag);

    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        private boolean loadingMore = true;
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                if (!loadingMore && layoutManager.findLastVisibleItemPosition() + 1 == adapter.getItemCount()) {
                    loadMore();
                }
            }
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (loadingMore && layoutManager.findLastVisibleItemPosition() + 1 == adapter.getItemCount()) {
                loadMore();
            } else if (loadingMore) {
                loadingMore = false;
            }
        }
    };

    private void refresh() {
        localCount = 0;
        viewModel.loadRecordByPage(currentPage = 1);
    }

    private void loadMore() {
        viewModel.loadRecordByPage(++currentPage);
    }


}
