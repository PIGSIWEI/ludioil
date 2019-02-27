package com.example.ludioil.ludihexiao.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ludioil.ludihexiao.MainActivity;
import com.example.ludioil.ludihexiao.R;
import com.example.ludioil.ludihexiao.adapter.OilMerchantAdapter;
import com.example.ludioil.ludihexiao.adapter.OnItemClickListener;
import com.example.ludioil.ludihexiao.api.Constant;
import com.example.ludioil.ludihexiao.bean.OilMerchantBean;
import com.example.ludioil.ludihexiao.scan.ScanActivity;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.example.ludioil.ludihexiao.api.Constant.API_URL;

public class OilMerchantActivity extends AppCompatActivity {

    private RecyclerView recycler_view;
    private List<OilMerchantBean> datas=new ArrayList<>();
    private OilMerchantAdapter adapter;
    private final int RESULT_REQUEST_CODE = 1;

    private String login_token;

    private ImageView iv_back;
    private TextView tv_title;

    private Button btn_hxcx,btn_errow,btn_coupon;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.oil_machine_layout);
        this.init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            // action with ID action_settings was selected
            case R.id.action_settings:
                startActivity(new Intent(OilMerchantActivity.this,ChangePasswordActivity.class));
                break;
            //action with info
            case R.id.action_info:
                changeDialog();
                break;
            case R.id.action_jiaoban:
                jiaobanDiaolog();
                break;
            case R.id.action_test:
                startActivity(new Intent(OilMerchantActivity.this,ConnectTestActivity.class));
                break;
            case R.id.action_hexiao:
                Intent intent = new Intent(OilMerchantActivity.this, ScanActivity.class);
                startActivityForResult(intent, RESULT_REQUEST_CODE);
                break;
            case R.id.action_exit:
                AlertDialog.Builder builder=new AlertDialog.Builder(this);
                builder.setTitle("确定要退出登录嘛？");
                builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SharedPreferences sp = getSharedPreferences("LoginUser", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("user_token", "");
                        editor.commit();
                        finish();
                        startActivity(new Intent(OilMerchantActivity.this,LoginActivity.class));
                    }
                });
                builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.create().show();
                break;

            default:
                break;
        }
        return true;
    }

    /**
     * 初始化
     */
    private void init() {

        //拿token
        SharedPreferences sp = getSharedPreferences("LoginUser", MODE_PRIVATE);
        login_token = sp.getString("user_token", null);

        recycler_view=findViewById(R.id.recycler_view);
        iv_back=findViewById(R.id.iv_back);
        iv_back.setVisibility(View.INVISIBLE);
        tv_title=findViewById(R.id.tv_title);
        btn_hxcx=findViewById(R.id.btn_hxcx);
        btn_errow=findViewById(R.id.btn_errow);
        btn_coupon=findViewById(R.id.btn_coupon);
        tv_title.setText("陆地石油油机列表");
        recycler_view=findViewById(R.id.recycler_view);
        adapter=new OilMerchantAdapter(this,datas);
        GridLayoutManager gridLayoutManager=new GridLayoutManager(this,3);
        recycler_view.setAdapter(adapter);
        recycler_view.setLayoutManager(gridLayoutManager);

        getData();

        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent=new Intent(OilMerchantActivity.this,OilGunActivity.class);
                intent.putExtra("oil_merchant_id",datas.get(position).getId());
                startActivity(intent);
            }
        });

        btn_hxcx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(OilMerchantActivity.this,SearchActivity.class);
                startActivity(intent);
            }
        });

        btn_errow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(OilMerchantActivity.this,ErrowActivity.class);
                startActivity(intent);
            }
        });

        btn_coupon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(OilMerchantActivity.this,CouponActivity.class);
                startActivity(intent);
            }
        });

    }

    /**
     * 获取数据
     */
    private void getData(){
        OkGo.<String>post(API_URL+"?request=private.admin.get_my_engine&platform=merchant_user_app&token="+login_token)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject jsonObject=new JSONObject(response.body());
                            int code =jsonObject.getInt("code");
                            if (code == 0){
                                JSONArray data=jsonObject.getJSONArray("data");
                                for (int i=0;i<data.length();i++){
                                    JSONObject temp=data.getJSONObject(i);
                                    OilMerchantBean bean=new OilMerchantBean();
                                    bean.setId(temp.getInt("oil_merchant_id"));
                                    datas.add(bean);
                                }
                                adapter.notifyDataSetChanged();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * 弹出修改 信息框
     */
    private void changeDialog(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        final View view = LayoutInflater.from(this).inflate(R.layout.change_info_layout,null);
        builder.setView(view);
        final Dialog dialog=builder.create();
        dialog.show();
        final EditText et_password=view.findViewById(R.id.et_password);
        Button btn_confirm=view.findViewById(R.id.btn_confirm);
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pwd=et_password.getText().toString().trim();
                if (pwd.equals("888888")){
                    startActivity(new Intent(OilMerchantActivity.this,IpInfoActivity.class));
                    dialog.dismiss();
                }else {
                    Toast.makeText(OilMerchantActivity.this,"请输入正确的密码！",Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        });
    }

    /**
     * 交班窗口
     */
    private void jiaobanDiaolog(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("是否要交班！");
        builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //TODO 交班操作
                JB();
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    /**
     * 交班操作
     */
    private void JB(){
        OkGo.<String>post(API_URL+"?request=private.gas_station.jiaoban_coupon_ok&platform=merchant_user_app&token="+login_token)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject jsonObject=new JSONObject(response.body());
                            Toast.makeText(OilMerchantActivity.this,jsonObject.getString("msg"),Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RESULT_REQUEST_CODE:
                    if (data == null) return;
                    String type = data.getStringExtra(Constant.EXTRA_RESULT_CODE_TYPE);
                    String content = data.getStringExtra(Constant.EXTRA_RESULT_CONTENT);
                    //TODO content就是扫描的值
                    hexiao(content);
                    break;
                default:
                    break;

            }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    /**
     * 核销大抽奖的接口
     */
    private void hexiao(String content){
        OkGo.<String>post(API_URL+"?request=private.games.games.gift.hexiao&platform=merchant_user_app&token="+login_token+"&hexiao_code="+content)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject jsonObject=new JSONObject(response.body());
                            showInfo(jsonObject.getString("msg"));
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
            }
        });
        builder.show();
    }

}
