package com.miaxis.thermal.view.base;

import android.view.View;

import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

public class BaseViewHolder<DB extends ViewDataBinding> extends RecyclerView.ViewHolder {

    DB binding;

    public BaseViewHolder(View itemView) {
        super(itemView);
    }

    public void setBinding(DB binding) {
        this.binding = binding;
    }

    public DB getBinding() {
        return binding;
    }

}