package com.miaxis.thermal.view.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

public abstract class BaseViewModelAdapter<T, DB extends ViewDataBinding, VH extends RecyclerView.ViewHolder> extends BaseAdapter<T, VH> {

    public BaseViewModelAdapter(@NonNull Context context) {
        super(context);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        DB binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), setContentView(), parent, false);
        return createViewHolder(binding.getRoot(), binding);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    protected abstract int setContentView();

    protected abstract VH createViewHolder(View view, DB binding);

}
