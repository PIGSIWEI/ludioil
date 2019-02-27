package com.example.ludioil.ludihexiao.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.ludioil.ludihexiao.R;
import com.example.ludioil.ludihexiao.bean.OilMerchantBean;

import java.util.List;

public class OilMerchantAdapter extends RecyclerView.Adapter<OilMerchantAdapter.ViewHolder> {

    private List<OilMerchantBean> datas;
    private Context context;
    private OnItemClickListener mOnItemClickListener;

    public OilMerchantAdapter(Context context,List<OilMerchantBean> datas){
        this.context=context;
        this.datas=datas;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }


    @NonNull
    @Override
    public OilMerchantAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.oil_merchant_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final OilMerchantAdapter.ViewHolder holder, int position) {
        holder.tv_id.setText(datas.get(position).getId()+"号油机");

        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getLayoutPosition();
                    mOnItemClickListener.onItemClick(holder.itemView, position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView tv_id;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_id=itemView.findViewById(R.id.tv_id);
        }
    }
}
