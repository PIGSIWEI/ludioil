package com.example.ludioil.ludihexiao.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ludioil.ludihexiao.R;
import com.example.ludioil.ludihexiao.bean.OilRecordBean;

import java.util.List;

public class OilRecordAdapter extends RecyclerView.Adapter<OilRecordAdapter.ViewHolder> {

    private Context context;
    private List<OilRecordBean> datas;
    private OnItemClickListener mOnItemClickListener;

    public OilRecordAdapter(Context context,List<OilRecordBean> datas){
        this.context=context;
        this.datas=datas;
    }


    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public OilRecordAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.get_oil_record_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final OilRecordAdapter.ViewHolder holder, int position) {
        holder.tv_money.setText(datas.get(position).getMoney());
        holder.tv_time.setText(datas.get(position).getTime());

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

        TextView tv_money,tv_time;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_money=itemView.findViewById(R.id.tv_money);
            tv_time=itemView.findViewById(R.id.tv_time);
        }
    }
}
