package com.example.ludioil.ludihexiao.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ludioil.ludihexiao.R;
import com.example.ludioil.ludihexiao.SUNMI.util.AidlUtil;
import com.example.ludioil.ludihexiao.adapter.ErrowAdapter;
import com.example.ludioil.ludihexiao.adapter.OnItemClickListener;
import com.example.ludioil.ludihexiao.bean.ErrowBean;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.example.ludioil.ludihexiao.api.Constant.API_URL;

public class ErrowActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<ErrowBean> datas=new ArrayList<>();
    private ErrowAdapter adapter;
    private ImageView iv_back;
    private TextView tv_title;
    private String station_id,login_token;
    private TextView tv_null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.errrow_layout);
        this.init();
    }

    /**
     * 初始化
     */
    private void init() {
        final SharedPreferences sp = getSharedPreferences("LoginUser", MODE_PRIVATE);
        login_token = sp.getString("user_token", null);
        station_id = sp.getString("station_id", "");
        if (station_id.equals("")){
            showInfo("系统检测未填写IP和ID，请填写后重试！");
        }
        recyclerView=findViewById(R.id.recycler_view);
        tv_null=findViewById(R.id.tv_null);
        iv_back=findViewById(R.id.iv_back);
        tv_title=findViewById(R.id.tv_title);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        tv_title.setText("核销异常列表");
        adapter=new ErrowAdapter(this,datas);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(linearLayoutManager);
        getData();

        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                showDetail(position);
            }
        });
    }

    /**
     * 获取数据
     */
    private void getData() {
        datas.clear();
        OkGo.<String>post(API_URL+"?request=private.coupon.get.check.fluid.list&station_id="+station_id+"&token="+login_token+"&platform=merchant_user_app")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject jsonObject=new JSONObject(response.body());
                            int code =jsonObject.getInt("code");
                            if (code == 0){
                                JSONArray data=jsonObject.getJSONArray("data");
                                //判断有没有数据
                                if (data.isNull(0)){
                                    tv_null.setVisibility(View.VISIBLE);
                                    recyclerView.setVisibility(View.GONE);
                                }else {
                                    tv_null.setVisibility(View.GONE);
                                    recyclerView.setVisibility(View.VISIBLE);
                                    for (int i=0;i<data.length();i++){
                                        JSONObject temp=data.getJSONObject(i);
                                        ErrowBean bean=new ErrowBean();
                                        bean.setAdd_time(temp.getString("add_time"));
                                        bean.setCoupon_id(temp.getString("coupon_id"));
                                        bean.setCoupon_money(temp.getString("coupon_money"));
                                        bean.setFluid(temp.getString("fluid"));
                                        bean.setGun_id(temp.getString("gun_id"));
                                        bean.setId(temp.getString("id"));
                                        bean.setMerchant_admin_id(temp.getString("merchant_admin_id"));
                                        bean.setOrder_money(temp.getString("order_money"));
                                        bean.setPay_money(temp.getString("pay_money"));
                                        bean.setStore_id(temp.getString("store_id"));
                                        bean.setTransaction_id(temp.getString("transaction_id"));
                                        datas.add(bean);
                                    }
                                    adapter.notifyDataSetChanged();
                                }

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * 弹出未填写信息
     */
    private void showInfo(String msg){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(msg);
        builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                finish();
            }
        });
        builder.show();
    }

    /**
     * 弹出订单详情
     */
    private void showDetail(final int position){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final LayoutInflater inflater = LayoutInflater.from(this);
        View view =inflater.inflate(R.layout.errow_dialog,null);
        builder.setView(view);
        final Dialog dialog = builder.create();
        dialog.getWindow().setGravity(Gravity.BOTTOM);//可以设置显示的位置setContentView(view);//自定义布局应该在这里添加，要在dialog.show()的后面
        TextView tv_translation_id,tv_fluid,tv_order_money,tv_coupon_money,
                tv_pay_money,tv_add_time,tv_station_id,tv_oil_gun;
        Button btn_fix;
        tv_translation_id=view.findViewById(R.id.tv_translation_id);
        tv_fluid=view.findViewById(R.id.tv_fluid);
        tv_order_money=view.findViewById(R.id.tv_order_money);
        tv_coupon_money=view.findViewById(R.id.tv_coupon_money);
        tv_pay_money=view.findViewById(R.id.tv_pay_money);
        tv_add_time=view.findViewById(R.id.tv_add_time);
        tv_station_id=view.findViewById(R.id.tv_station_id);
        tv_oil_gun=view.findViewById(R.id.tv_oil_gun);
        btn_fix=view.findViewById(R.id.btn_fix);
        tv_translation_id.setText(datas.get(position).getTransaction_id());
        tv_fluid.setText(datas.get(position).getFluid());
        tv_order_money.setText(datas.get(position).getOrder_money());
        tv_coupon_money.setText(datas.get(position).getCoupon_money());
        tv_pay_money.setText(datas.get(position).getPay_money());
        tv_add_time.setText(datas.get(position).getAdd_time());
        tv_station_id.setText(datas.get(position).getStore_id());
        tv_oil_gun.setText(datas.get(position).getGun_id()+"号油枪");
        btn_fix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fixOrder(datas.get(position).getStore_id(),datas.get(position).getFluid(),dialog);
            }
        });
        dialog.show();
    }


    /**
     * 修复订单接口
     */
    private void fixOrder(String station_id, String fluid, final Dialog dialog){
        OkGo.<String>post(API_URL+"?request=private.coupon.check.fluid.status&token="+login_token+"&platform=merchant_user_app&station_id="+station_id+"&fluid="+fluid)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject jsonObject=new JSONObject(response.body());
                            Toast.makeText(ErrowActivity.this,jsonObject.getString("msg"),Toast.LENGTH_SHORT).show();
                            int code = jsonObject.getInt("code");
                            if (code == 0) {
                                //打印小票操作
                                JSONArray ticket = jsonObject.getJSONArray("data");
                                for (int i = 0; i < ticket.length(); i++) {
                                    JSONObject temp = ticket.getJSONObject(i);
                                    JSONObject style = temp.getJSONObject("style");
                                    printServerText(temp.getString("value"), style.getInt("font_size"), style.getInt("is_bold"), style.getInt("is_underline"));
                                }
                                AidlUtil.getInstance().printText("----------------", 48, true, false);
                                AidlUtil.getInstance().print3Line();
                                dialog.dismiss();
                                getData();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * 从服务器获取打印信息
     */
    private void printServerText(String value, int font_size, int isBold, int is_underline) {
        Boolean b_isBolde;
        Boolean b_is_underline;
        if (isBold == 0) {
            b_isBolde = false;
        } else {
            b_isBolde = true;
        }
        if (is_underline == 0) {
            b_is_underline = false;
        } else {
            b_is_underline = true;
        }
        AidlUtil.getInstance().printText(value, font_size, b_isBolde, b_is_underline);
    }

}
