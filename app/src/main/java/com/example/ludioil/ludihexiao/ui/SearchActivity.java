package com.example.ludioil.ludihexiao.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ludioil.ludihexiao.R;
import com.example.ludioil.ludihexiao.adapter.SearchAdapter;
import com.example.ludioil.ludihexiao.bean.SearchBean;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.example.ludioil.ludihexiao.api.Constant.API_URL;

public class SearchActivity extends AppCompatActivity {

    private List<SearchBean> datas = new ArrayList<>();
    private SearchAdapter adapter;

    private RecyclerView recycler_view;

    private ImageView iv_back;
    private TextView tv_title;

    private String login_token;

    public int oil_merchant_id;

    private TextView tv_all_oil_count,tv_all_oil_money,tv_all_oil_gun;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.search_layout);
        this.init();
    }

    /**
     * 初始化
     */
    private void init() {
        oil_merchant_id = getIntent().getIntExtra("oil_merchant_id", 0);
        iv_back = findViewById(R.id.iv_back);
        tv_title = findViewById(R.id.tv_title);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        tv_title.setText("核销查询");

        tv_all_oil_gun=findViewById(R.id.tv_all_oil_gun);
        tv_all_oil_money=findViewById(R.id.tv_all_oil_money);
        tv_all_oil_count=findViewById(R.id.tv_all_oil_count);

        //拿token
        SharedPreferences sp = getSharedPreferences("LoginUser", MODE_PRIVATE);
        login_token = sp.getString("user_token", null);

        recycler_view = findViewById(R.id.recycler_view);
        adapter = new SearchAdapter(this, datas);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recycler_view.setAdapter(adapter);
        recycler_view.setLayoutManager(linearLayoutManager);

        getData();
    }

    /**
     * 获取数据
     */
    private void getData() {
        if (oil_merchant_id == 0) {
            OkGo.<String>post(API_URL + "?request=private.gas_station.get_couponed&platform=merchant_user_app&token=" + login_token)
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response.body());
                                JSONArray data = jsonObject.getJSONArray("data");
                                for (int i = 0; i < data.length(); i++) {
                                    JSONObject temp = data.getJSONObject(i);
                                    SearchBean bean = new SearchBean();
                                    bean.setGun_id(temp.getInt("gun_id"));
                                    bean.setTotal(temp.getInt("total"));
                                    bean.setTotal_money(temp.getInt("total_money"));
                                    datas.add(bean);
                                }
                                adapter.notifyDataSetChanged();
                                //总计金额和张数
                                JSONObject total=jsonObject.getJSONObject("total");
                                tv_all_oil_money.setText("￥"+total.getString("total_money"));
                                tv_all_oil_count.setText(total.getString("count")+"张");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } else {
            OkGo.<String>post(API_URL + "?request=private.gas_station.get_couponed&platform=merchant_user_app&token=" + login_token+"&engine_id="+oil_merchant_id)
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response.body());
                                JSONArray data = jsonObject.getJSONArray("data");
                                for (int i = 0; i < data.length(); i++) {
                                    JSONObject temp = data.getJSONObject(i);
                                    SearchBean bean = new SearchBean();
                                    bean.setGun_id(temp.getInt("gun_id"));
                                    bean.setTotal(temp.getInt("total"));
                                    bean.setTotal_money(temp.getInt("total_money"));
                                    datas.add(bean);
                                }
                                adapter.notifyDataSetChanged();
                                //总计金额和张数
                                JSONObject total=jsonObject.getJSONObject("total");
                                tv_all_oil_money.setText("￥"+total.getString("total_money"));
                                tv_all_oil_count.setText(total.getString("count")+"张");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        }

    }
}