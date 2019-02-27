package com.example.ludioil.ludihexiao.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ludioil.ludihexiao.R;
import com.example.ludioil.ludihexiao.bean.OilGunBean;
import com.example.ludioil.ludihexiao.bean.OilMerchantBean;

import java.util.List;

public class OilGunAdapter extends RecyclerView.Adapter<OilGunAdapter.ViewHolder> {

    private List<OilGunBean> datas;
    private Context context;
    private OnItemClickListener mOnItemClickListener;


    public OilGunAdapter(Context context,List<OilGunBean> datas){
        this.context=context;
        this.datas=datas;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public OilGunAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.oil_gun_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final OilGunAdapter.ViewHolder holder, int position) {
        holder.tv_oil_gun.setText(datas.get(position).getGun_id()+"号油枪（"+datas.get(position).getOil_name()+"）");

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

        TextView tv_oil_gun;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_oil_gun=itemView.findViewById(R.id.tv_oil_gun);
        }
    }
}
