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
import com.miaxis.thermal.data.entity.Person;
import com.miaxis.thermal.databinding.FragmentPersonBinding;
import com.miaxis.thermal.view.adapter.PersonAdapter;
import com.miaxis.thermal.view.base.BaseViewModelFragment;
import com.miaxis.thermal.viewModel.PersonViewModel;
import com.miaxis.thermal.viewModel.RecordViewModel;

import java.util.List;

public class PersonFragment extends BaseViewModelFragment<FragmentPersonBinding, PersonViewModel> {

    private PersonAdapter adapter;
    private LinearLayoutManager layoutManager;
    private int currentPage = 1;
    private int localCount = 0;

    public static PersonFragment newInstance() {
        return new PersonFragment();
    }

    public PersonFragment() {
        // Required empty public constructor
    }

    @Override
    protected int setContentView() {
        return R.layout.fragment_person;
    }

    @Override
    protected PersonViewModel initViewModel() {
        return new ViewModelProvider(this, getViewModelProviderFactory()).get(PersonViewModel.class);
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
        binding.srlPerson.setOnRefreshListener(this::refresh);
        viewModel.refreshing.observe(this, refreshingObserver);
        viewModel.personListLiveData.observe(this, personListObserver);
        refresh();
    }

    @Override
    public void onBackPressed() {
        mListener.backToStack(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.personListLiveData.removeObserver(personListObserver);
    }

    private void initRecycleView() {
        adapter = new PersonAdapter(getContext());
        adapter.setListener(adapterListener);
        layoutManager = new LinearLayoutManager(getContext());
        binding.rvPerson.addOnScrollListener(onScrollListener);
        binding.rvPerson.setLayoutManager(layoutManager);
        binding.rvPerson.setAdapter(adapter);
        ((SimpleItemAnimator) binding.rvPerson.getItemAnimator()).setSupportsChangeAnimations(false);
    }

    private PersonAdapter.OnItemClickListener adapterListener = (view, position) -> {
        mListener.replaceFragment(AddPersonFragment.newInstance(adapter.getData(position)));
    };

    private Observer<List<Person>> personListObserver = personList -> {
        if (currentPage == 1) {
            adapter.setDataList(personList);
            adapter.notifyDataSetChanged();
            if (localCount == 0) {
                binding.rvPerson.scrollToPosition(0);
            }
            localCount = personList.size();
        } else {
            adapter.setDataList(personList);
            adapter.notifyItemRangeChanged(localCount, personList.size() - localCount);
            if (localCount != 0) {
                binding.rvPerson.scrollToPosition(localCount);
            }
            localCount = personList.size();
        }
        viewModel.updatePersonCount();
    };

    private Observer<Boolean> refreshingObserver = flag -> binding.srlPerson.setRefreshing(flag);

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
        viewModel.loadPersonByPage(currentPage = 1);
    }

    private void loadMore() {
        viewModel.loadPersonByPage(++currentPage);
    }

}
