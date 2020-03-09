package com.miaxis.thermal.view.adapter;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import com.miaxis.thermal.R;
import com.miaxis.thermal.data.entity.Record;
import com.miaxis.thermal.databinding.ItemRecordBinding;
import com.miaxis.thermal.view.base.BaseViewHolder;
import com.miaxis.thermal.view.base.BaseViewModelAdapter;

public class RecordAdapter extends BaseViewModelAdapter<Record, ItemRecordBinding, RecordAdapter.MyViewHolder> {

    private OnItemClickListener listener;

    public RecordAdapter(@NonNull Context context) {
        super(context);
    }

    @Override
    protected int setContentView() {
        return R.layout.item_record;
    }

    @Override
    protected MyViewHolder createViewHolder(View view, ItemRecordBinding binding) {
        MyViewHolder viewHolder = new MyViewHolder(view);
        viewHolder.setBinding(binding);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecordAdapter.MyViewHolder holder, int position) {
        Record item = dataList.get(position);
        holder.getBinding().setItem(item);
        holder.getBinding().tvRecordUpload.setText(item.isUpload() ? "已上传" : "未上传");
        holder.getBinding().tvRecordUpload.setTextColor(item.isUpload()
                ? context.getResources().getColor(R.color.darkgreen)
                : context.getResources().getColor(R.color.darkred));
        holder.getBinding().llItem.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(holder.getBinding().llItem, holder.getLayoutPosition());
            }
        });
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    class MyViewHolder extends BaseViewHolder<ItemRecordBinding> {
        MyViewHolder(View itemView) {
            super(itemView);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

}
