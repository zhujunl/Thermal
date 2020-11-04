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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.miaxis.thermal.BR;
import com.miaxis.thermal.R;
import com.miaxis.thermal.data.entity.Person;
import com.miaxis.thermal.data.entity.PersonSearch;
import com.miaxis.thermal.databinding.FragmentPersonBinding;
import com.miaxis.thermal.util.ValueUtil;
import com.miaxis.thermal.view.adapter.PersonAdapter;
import com.miaxis.thermal.view.auxiliary.OnLimitClickHelper;
import com.miaxis.thermal.view.auxiliary.OnLimitClickListener;
import com.miaxis.thermal.view.base.BaseViewModelFragment;
import com.miaxis.thermal.view.dialog.DialogHelper;
import com.miaxis.thermal.viewModel.PersonViewModel;
import com.miaxis.thermal.viewModel.RecordViewModel;

import java.util.List;

public class PersonFragment extends BaseViewModelFragment<FragmentPersonBinding, PersonViewModel> {

    private PersonAdapter adapter;
    private LinearLayoutManager layoutManager;

    private boolean loadingMore = false;
    private boolean search = false;
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
        binding.btnReset.setOnClickListener(new OnLimitClickHelper(view -> {
            binding.etName.setText("");
            binding.etNumber.setText("");
            binding.etPhone.setText("");
            binding.spFaceStatus.setSelection(0);
            binding.spUploadStatus.setSelection(0);
            binding.spPersonStatus.setSelection(0);
            binding.spPersonType.setSelection(0);
            hideInputMethod();
            search = false;
            refresh();
        }));
        viewModel.refreshing.observe(this, refreshingObserver);
        viewModel.updating.observe(this, updatingObserver);
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
//        adapter.setHasStableIds(true);
        layoutManager = new LinearLayoutManager(getContext());
        binding.rvPerson.addOnScrollListener(onScrollListener);
        binding.rvPerson.setLayoutManager(layoutManager);
        binding.rvPerson.setAdapter(adapter);
        ((SimpleItemAnimator) binding.rvPerson.getItemAnimator()).setSupportsChangeAnimations(false);
    }

    private PersonAdapter.OnItemClickListener adapterListener = (view, position) -> {
        switch (view.getId()) {
            case R.id.iv_edit:
                mListener.replaceFragment(AddPersonFragment.newInstance(adapter.getData(position)));
                break;
            case R.id.iv_delete:
                DialogHelper.fullScreenMaterialDialogLink(new MaterialDialog.Builder(getContext())
                        .title("确认删除？")
                        .positiveText("确认")
                        .onPositive((dialog, which) -> {
                            viewModel.changePersonStatus(adapter.getData(position), false);
                        })
                        .negativeText("取消")
                        .build())
                        .show();
                break;
            case R.id.iv_recover:
                DialogHelper.fullScreenMaterialDialogLink(new MaterialDialog.Builder(getContext())
                        .title("确认启用？")
                        .positiveText("确认")
                        .onPositive((dialog, which) -> {
                            viewModel.changePersonStatus(adapter.getData(position), true);
                        })
                        .negativeText("取消")
                        .build())
                        .show();
                break;
        }
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
        viewModel.updatePersonCount(search, search ? makePersonSearch() : null);
        loadingMore = false;
    };

    private Observer<Boolean> refreshingObserver = flag -> binding.srlPerson.setRefreshing(flag);

    private Observer<Boolean> updatingObserver = flag -> {
        if (flag) {
            refresh();
        }
    };

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
        viewModel.loadPersonByPage(makePersonSearch());
    }

    private void loadMore() {
        if (!viewModel.isLoadAllOver()) {
            loadingMore = true;
            currentPage++;
            viewModel.loadPersonByPage(makePersonSearch());
        }
    }

    private PersonSearch makePersonSearch() {
        if (!search) {
            return new PersonSearch.Builder()
                    .pageNum(currentPage)
                    .pageSize(ValueUtil.PAGE_SIZE)
                    .build();
        } else {
            return new PersonSearch.Builder()
                    .pageNum(currentPage)
                    .pageSize(ValueUtil.PAGE_SIZE)
                    .identifyNumber(binding.etNumber.getText().toString())
                    .phone(binding.etPhone.getText().toString())
                    .name(binding.etName.getText().toString())
                    .upload(binding.spUploadStatus.getSelectedItemPosition() == 0 ? null : (binding.spUploadStatus.getSelectedItemPosition() == 1 ? Boolean.TRUE : Boolean.FALSE))
                    .face(binding.spFaceStatus.getSelectedItemPosition() == 0 ? null : (binding.spFaceStatus.getSelectedItemPosition() == 1 ? Boolean.TRUE : Boolean.FALSE))
                    .status(binding.spPersonStatus.getSelectedItemPosition() == 0 ? null : (binding.spPersonStatus.getSelectedItemPosition() == 1 ? "1" : "2"))
                    .type(binding.spPersonType.getSelectedItemPosition() == 0 ? null : (binding.spPersonType.getSelectedItemPosition() == 1 ? ValueUtil.PERSON_TYPE_WORKER : ValueUtil.PERSON_TYPE_VISITOR))
                    .build();
        }
    }

}
