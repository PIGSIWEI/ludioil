package com.example.ludioil.ludihexiao.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ludioil.ludihexiao.R;
import com.example.ludioil.ludihexiao.bean.CouponBean;

import java.util.List;

public class CouponAdapter extends RecyclerView.Adapter<CouponAdapter.ViewHolder> {

    private Context context;
    private List<CouponBean> datas;
    private OnItemClickListener mOnItemClickListener;

    public CouponAdapter(Context context,List<CouponBean> datas){
        this.context=context;
        this.datas=datas;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public CouponAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(context).inflate(R.layout.errow_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CouponAdapter.ViewHolder holder, int position) {

        if(holder instanceof ViewHolder) {
            final ViewHolder viewHolder = holder;
            if (mOnItemClickListener != null)
            {
                viewHolder.itemView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        mOnItemClickListener.onItemClick(viewHolder.itemView, viewHolder.getAdapterPosition());
                    }
                });
            }
        }

        holder.tv_oil_gun.setText(datas.get(position).getGun_id()+"号油枪");
        holder.tv_order_money.setText("￥"+datas.get(position).getOrder_money());
        holder.tv_order_time.setText(datas.get(position).getAdd_time());
        holder.tv_translation_id.setText("流水号："+datas.get(position).getFluid());

    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView tv_oil_gun,tv_translation_id,tv_order_time,tv_order_money;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_oil_gun=itemView.findViewById(R.id.tv_oil_gun);
            tv_translation_id=itemView.findViewById(R.id.tv_translation_id);
            tv_order_time=itemView.findViewById(R.id.tv_order_time);
            tv_order_money=itemView.findViewById(R.id.tv_order_money);
        }
    }
}
