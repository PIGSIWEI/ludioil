package com.example.ludioil.ludihexiao.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ludioil.ludihexiao.R;
import com.example.ludioil.ludihexiao.bean.SearchBean;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
    private Context context;
    private List<SearchBean> datas;

    public SearchAdapter(Context context,List<SearchBean> datas){
        this.context=context;
        this.datas=datas;
    }

    @NonNull
    @Override
    public SearchAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.search_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchAdapter.ViewHolder holder, int position) {
        holder.tv_count.setText(datas.get(position).getTotal()+"张");
        holder.tv_oil_gun.setText(datas.get(position).getGun_id()+"号油枪");
        holder.tv_money.setText("￥"+datas.get(position).getTotal());
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView tv_oil_gun,tv_money,tv_count;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_count=itemView.findViewById(R.id.tv_count);
            tv_oil_gun=itemView.findViewById(R.id.tv_oil_gun);
            tv_money=itemView.findViewById(R.id.tv_money);
        }
    }
}
