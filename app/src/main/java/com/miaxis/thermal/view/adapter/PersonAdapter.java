package com.miaxis.thermal.view.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;

import com.miaxis.thermal.R;
import com.miaxis.thermal.bridge.GlideApp;
import com.miaxis.thermal.data.entity.Person;
import com.miaxis.thermal.databinding.ItemPersonBinding;
import com.miaxis.thermal.util.ValueUtil;
import com.miaxis.thermal.view.base.BaseViewHolder;
import com.miaxis.thermal.view.base.BaseViewModelAdapter;

public class PersonAdapter extends BaseViewModelAdapter<Person, ItemPersonBinding, PersonAdapter.MyViewHolder> {

    private OnItemClickListener listener;

    public PersonAdapter(@NonNull Context context) {
        super(context);
    }

    @Override
    protected int setContentView() {
        return R.layout.item_person;
    }

    @Override
    protected MyViewHolder createViewHolder(View view, ItemPersonBinding binding) {
        MyViewHolder viewHolder = new MyViewHolder(view);
        viewHolder.setBinding(binding);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PersonAdapter.MyViewHolder holder, int position) {
        Person item = dataList.get(position);
        holder.getBinding().setItem(item);
        holder.getBinding().tvStatus.setText(item.isUpload() ? "已同步" : "未同步");
        holder.getBinding().tvFeatureStatus.setText(TextUtils.isEmpty(item.getFaceFeature()) ? "出错" : "就绪");
        holder.getBinding().tvRemarks.setText(TextUtils.isEmpty(item.getFaceFeature()) ? item.getRemarks() : "");
        holder.getBinding().tvType.setText(ValueUtil.getPersonTypeName(item.getType()));
        if (TextUtils.isEmpty(item.getFacePicturePath())) {
            GlideApp.with(context).load(R.drawable.default_header).into(holder.getBinding().ivHeader);
        } else {
            GlideApp.with(context).load(item.getFacePicturePath()).into(holder.getBinding().ivHeader);
        }
        holder.getBinding().ivEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(holder.getBinding().ivEdit, holder.getLayoutPosition());
            }
        });
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    class MyViewHolder extends BaseViewHolder<ItemPersonBinding> {
        MyViewHolder(View itemView) {
            super(itemView);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

}
