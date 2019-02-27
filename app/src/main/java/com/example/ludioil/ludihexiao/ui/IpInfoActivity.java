package com.example.ludioil.ludihexiao.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ludioil.ludihexiao.R;

public class IpInfoActivity extends AppCompatActivity {

    private ImageView iv_back;
    private TextView tv_title,tv_ipinfo,tv_station_id;

    private String ip,station_id;

    private Button btn_updata;
    private EditText et_ip,et_station_id;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.ipinfo_layout);
        this.init();
    }

    private void init() {
        iv_back=findViewById(R.id.iv_back);
        tv_title=findViewById(R.id.tv_title);
        tv_ipinfo=findViewById(R.id.tv_ipinfo);
        tv_station_id=findViewById(R.id.tv_station_id);
        btn_updata=findViewById(R.id.btn_updata);
        et_station_id=findViewById(R.id.et_station_id);
        et_ip=findViewById(R.id.et_ip);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        tv_title.setText("修改局域网配置");

        //检查当前局域网有没有配置
        //拿token
        SharedPreferences sp = getSharedPreferences("LoginUser", MODE_PRIVATE);
        ip = sp.getString("ip", "");
        station_id = sp.getString("station_id", "");
        if (ip.equals("")){
            tv_ipinfo.setText("当前未配置局域网IP");
        }else {
            tv_ipinfo.setText(ip);
        }
        if (station_id.equals("")){
            tv_station_id.setText("当前未配置油站ID");
        }else {
            tv_station_id.setText(station_id);
        }
        btn_updata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String input_ip=et_ip.getText().toString().trim();
                String input_id=et_station_id.getText().toString().trim();
                if (input_ip.equals("")||input_id.equals("")){
                    Toast.makeText(IpInfoActivity.this,"请正确输入IP地址和油站ID",Toast.LENGTH_SHORT).show();
                }else {
                    SharedPreferences mSharedPreferences =getSharedPreferences("LoginUser",Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putString("ip",input_ip);
                    editor.putString("station_id",input_id);
                    editor.commit();
                    tv_ipinfo.setText(ip);
                    tv_station_id.setText(station_id);
                    Toast.makeText(IpInfoActivity.this,"更新成功！",Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });

    }
}
