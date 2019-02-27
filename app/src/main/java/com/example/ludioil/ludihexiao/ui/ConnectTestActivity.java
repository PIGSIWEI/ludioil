package com.example.ludioil.ludihexiao.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ludioil.ludihexiao.R;
import com.example.ludioil.ludihexiao.scan.ScanActivity;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.example.ludioil.ludihexiao.api.Constant.API_URL;

/**
 * Created by PIGROAD on 2018/11/27
 * Email:920015363@qq.com
 */
public class ConnectTestActivity extends AppCompatActivity {

    private String ip,station_id;

    private ImageView iv_back;
    private TextView tv_title;

    private Button btn_test;
    private TextView tv_test;

    private String login_token,base64;

    private List<String> datas=new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.test_layout);
        this.init();
    }

    private void init() {
        iv_back=findViewById(R.id.iv_back);
        tv_title=findViewById(R.id.tv_title);
        btn_test=findViewById(R.id.btn_test);
        tv_test=findViewById(R.id.tv_test);
        tv_title.setText("中控连接测试");
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        tv_test=findViewById(R.id.tv_test);

        tv_title.setMovementMethod(new ScrollingMovementMethod());

        btn_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scrollLog("正在进行连接···");
                getBase64();
            }
        });
    }

    private void scrollLog(String message) {
        Spannable colorMessage = new SpannableString(message + "\n");
        colorMessage.setSpan(new ForegroundColorSpan(0xff0000ff), 0, message.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_test.append(colorMessage);
        Layout layout = tv_test.getLayout();
        if (layout != null) {
            int scrollAmount = layout.getLineTop(tv_test.getLineCount()) - tv_test.getHeight();
            if (scrollAmount > 0) {
                tv_test.scrollTo(0, scrollAmount + tv_test.getCompoundPaddingBottom());
            } else {
                tv_test.scrollTo(0, 0);
            }
        }
    }

    private void print(Message msg) {
        String message = (String) msg.obj;
        if (message != null) {
            scrollLog(message);
        }
    }

    /**
     * 获取Base64
     */
    private void getBase64(){
        //拿token
        SharedPreferences sp = getSharedPreferences("LoginUser", MODE_PRIVATE);
        login_token = sp.getString("user_token", null);
        ip = sp.getString("ip", "");
        station_id = sp.getString("station_id", "");
        if (station_id.equals("")||ip.equals("")){
            //TODO 未填写IP和ID
            showInfo("系统检测未填写IP和ID，请填写后重试！");
            scrollLog("系统检测未填写IP和ID，请填写后重试！");
        }else {

            OkGo.<String>post(API_URL+"?request=private.cms.get.cms.flu.for.four&platform=merchant_user_app&token="+login_token+
                    "&gun_id=1&station_id="+station_id)
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            try {
                                JSONObject jsonObject=new JSONObject(response.body());
                                base64=jsonObject.getString("data");
                                scrollLog("获取base64成功！");
                                getData2();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(Response<String> response) {
                            super.onError(response);
                        }
                    });
        }
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

    private void getData2(){
        scrollLog("正在进行中控连接··");
        final Date dt = new Date();
        String url="http://"+ip+"/datasnap/rest/tservermethods1/WXUniformInterface/"+base64;
        OkGo.<String>get(url)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject jsonObject=new JSONObject(response.body());
                            JSONArray result=jsonObject.getJSONArray("result");
                            String json=new String(Base64.decode((result.getString(0)).getBytes(), Base64.DEFAULT));
                            JSONObject jsonObject1=new JSONObject(json);
                            Log.i("pppppp","arrow:"+jsonObject1.toString());
                            JSONObject data=jsonObject1.getJSONObject("data");
                            JSONArray OilRecord=data.getJSONArray("OilRecord");
                            //判断是否有流水
                            if (OilRecord.isNull(0)){
                               scrollLog("没有获取到1号枪流水，中控连接正常");
                            }else {
                                scrollLog("能获取到最新流水，中控连接正常");
                            }

                            //连接中控测试 返回后台接口
                            String title="中控连接测试 当前IP："+ip+"，门店ID："+station_id;
                            scrollLog(title);
                            String info =response.body();
                            scrollLog(info);
                            sendLog(title,info);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            showInfo("网络连接错误，访问不到中控流水");
                            scrollLog("中控连接不通，请联系中控管理员！"+"当前门店ID："+station_id+",当前IP："+ip);
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        scrollLog(response.getException().toString());
                        scrollLog("中控连接不通，请联系中控管理员！"+"当前门店ID："+station_id+",当前IP："+ip);
                    }
                });

    }


    /**
     * 发送日志 给后台
     */
    private void sendLog(String title,String info){
        OkGo.<String>post(API_URL+"?request=public.auth.alert_app_log&platform=merchant_user_app&title="+title+"&content="+info)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {

                    }
                });
    }
}
