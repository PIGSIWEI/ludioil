package com.example.ludioil.ludihexiao.ui;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ludioil.ludihexiao.R;
import com.example.ludioil.ludihexiao.SUNMI.util.AidlUtil;
import com.example.ludioil.ludihexiao.adapter.OilRecordAdapter;
import com.example.ludioil.ludihexiao.adapter.OnItemClickListener;
import com.example.ludioil.ludihexiao.api.Constant;
import com.example.ludioil.ludihexiao.bean.OilRecordBean;
import com.example.ludioil.ludihexiao.scan.ScanActivity;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static com.example.ludioil.ludihexiao.api.Constant.API_URL;

public class GetOilRecordActivity extends AppCompatActivity {
    
    private RecyclerView recycler_view;
    private List<OilRecordBean> datas=new ArrayList<>();
    private OilRecordAdapter adapter;

    private String base64,ip,login_token;

    private ImageView iv_back;
    private TextView tv_title;

    private int gun_id;

    private String station_id;

    private OilRecordBean bean=new OilRecordBean();
    private final int RESULT_REQUEST_CODE = 1;

    private MediaPlayer mPlayer_true,mPlayer_false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.get_oil_record_layout);
        this.init();
        getBase64();
    }

    private void init() {

        mPlayer_true = MediaPlayer.create(this, R.raw.mp3_succes);
        mPlayer_false = MediaPlayer.create(this, R.raw.mp3_error);
        mPlayer_true.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer_false.setAudioStreamType(AudioManager.STREAM_MUSIC);

        gun_id=getIntent().getIntExtra("gun_id",0);

        iv_back=findViewById(R.id.iv_back);

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        tv_title=findViewById(R.id.tv_title);
        tv_title.setText("枪号流水");

        recycler_view=findViewById(R.id.recycler_view);
        adapter=new OilRecordAdapter(this,datas);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        recycler_view.setLayoutManager(linearLayoutManager);
        recycler_view.setAdapter(adapter);

        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                getB64(position);
            }
        });

    }

    private void getData(){
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
                                showInfo("此枪号暂无流水！");
                            }else {
                                int ss=OilRecord.length();
                                if (ss>4){
                                    for (int i=0;i<4;i++){
                                        JSONObject temp=OilRecord.getJSONObject(i);
                                        OilRecordBean bean=new OilRecordBean();
                                        bean.setMoney("￥"+temp.getString("OilMount"));
                                        bean.setFluid(temp.getString("Fluid"));
                                        bean.setPayMount(temp.getString("PayMount"));
                                        bean.setTime(temp.getString("Finishtime"));
                                        datas.add(bean);
                                    }
                                }else {
                                    for (int i=0;i<OilRecord.length();i++){
                                        JSONObject temp=OilRecord.getJSONObject(i);
                                        OilRecordBean bean=new OilRecordBean();
                                        bean.setMoney("￥"+temp.getString("OilMount"));
                                        bean.setTime(temp.getString("Finishtime"));
                                        bean.setFluid(temp.getString("Fluid"));
                                        bean.setPayMount(temp.getString("PayMount"));
                                        datas.add(bean);
                                    }
                                }
                            }
                            adapter.notifyDataSetChanged();

                            //获取到中控 流水返回后台
                            String title="获取到中控流水：门店ID："+station_id+"油枪ID："+gun_id;
                            String info=response.body();
                            sendLog(title,info);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            showInfo("网络连接错误，访问不到中控流水");
                        }
                    }
                });
    }


    /**
     * 获取Base64
     */
    private void getBase64(){
        //拿token
        SharedPreferences sp = getSharedPreferences("LoginUser", MODE_PRIVATE);
        login_token = sp.getString("user_token", null);
        ip = sp.getString("ip", "");
        String station_id = sp.getString("station_id", "");
        if (station_id.equals("")||ip.equals("")){
            //TODO 未填写IP和ID
            showInfo("系统检测未填写IP和ID，请填写后重试！");
        }else {

            OkGo.<String>post(API_URL+"?request=private.cms.get.cms.flu.for.four&platform=merchant_user_app&token="+login_token+
                    "&gun_id="+gun_id+"&station_id="+station_id)
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            try {
                                JSONObject jsonObject=new JSONObject(response.body());
                                base64=jsonObject.getString("data");
                                getData();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
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
                finish();
            }
        });
        builder.show();
    }

    /**
     * 获取 中控流水
     */
    /**
     * 获取Base64
     */
    private void getB64(final int position){
        //拿token
        SharedPreferences sp = getSharedPreferences("LoginUser", MODE_PRIVATE);
        login_token = sp.getString("user_token", null);
        ip = sp.getString("ip", "");
        station_id = sp.getString("station_id", "");
        if (station_id.equals("")||ip.equals("")){
            //TODO 未填写IP和ID
            showInfo("系统检测未填写IP和ID，请填写后重试！");
        }else {

            OkGo.<String>post(API_URL+"?request=private.cms.get.cms.flu.for.four&platform=merchant_user_app&token="+login_token+
                    "&gun_id="+gun_id+"&station_id="+station_id)
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            try {
                                JSONObject jsonObject=new JSONObject(response.body());
                                base64=jsonObject.getString("data");
                                getData2(position);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        }
    }

    private void getData2(final int position){
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
                                showInfo2("此枪号暂无流水！");
                            }else {
                                bean.setFluid(datas.get(position).getFluid());
                                bean.setPayMount(datas.get(position).getPayMount());
                                if (ContextCompat.checkSelfPermission(GetOilRecordActivity.this,
                                        Manifest.permission.CAMERA)
                                        != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions((Activity) GetOilRecordActivity.this,
                                            new String[]{Manifest.permission.CAMERA},
                                            1);
                                }else {
                                    //TODO 执行扫一扫
                                    Intent intent = new Intent(GetOilRecordActivity.this, ScanActivity.class);
                                    startActivityForResult(intent, RESULT_REQUEST_CODE);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            showInfo("网络连接错误，访问不到中控流水");
                        }
                    }
                });
    }

    /**
     * 弹出未填写信息
     */
    private void showInfo2(String msg){
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

    /**
     * 核销操作
     */
    private void hx(final String tt){
        OkGo.<String>post(API_URL+"?request=private.coupon.coupon.hexiao.action&token="+login_token
                +"&platform=merchant_user_app&coupon_code="+tt+"&fluid="+bean.getFluid()+"&money="+bean.getPayMount()+"&store_id="
                +getIntent().getIntExtra("oil_merchant_id",0)+"&gun_id="+gun_id+"&station_id="+station_id
                +"&pay_type=1"
        ).execute(new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                try {
                    JSONObject jsonObject=new JSONObject(response.body());
                    int code =jsonObject.getInt("code");
                    if (code == 0) {
                        String b64=jsonObject.getString("data");
                        String url="http://"+ip+"/datasnap/rest/tservermethods1/WXUniformInterface/"+b64;
                        setPay(url,tt,bean.getFluid());
                    }else {
                        showInfo2(jsonObject.getString("msg"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 调取中控 设置收款
     */
    private void setPay(final String url, final String tt, final String flu){
        OkGo.<String>get(url)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject jsonObject=new JSONObject(response.body());
                            JSONArray result=jsonObject.getJSONArray("result");
                            String json=new String(Base64.decode((result.getString(0)).getBytes(), Base64.DEFAULT));
                            JSONObject jsonObject1=new JSONObject(json);
                            JSONObject data=jsonObject1.getJSONObject("data");
                            int DataResult=data.getInt("DataResult");
                            if (DataResult == 0){
                                checkHx(tt);
                            }else {
                                showInfo2(data.getString("msg"));
                                mPlayer_false.start();
                            }

                            //设置收款 返回后台
                            String title="设置收款 门店ID："+station_id+"枪号："+gun_id+"流水号："+flu;
                            String info=response.body();
                            sendLog(title,info);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * 核销确认
     */
    private void checkHx(final String content){
        OkGo.<String>post(API_URL+"?token="+login_token+"&request=private.coupon.coupon.hexiao.confirm&platform=merchant_user_app&coupon_code="+content)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject jsonObject=new JSONObject(response.body());
                            int code =jsonObject.getInt("code");
                            if (code == 0){
                                mPlayer_true.start();
                                datas.clear();
                                getBase64();
                                //核销成功后 打印操作
                                confirmCheck(content);
                            }else {
                                mPlayer_false.start();
                            }
                            showInfo2(jsonObject.getString("msg"));
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
                    getCouponInfo(content,getIntent().getIntExtra("oil_merchant_id",0),gun_id);
                    break;
                default:
                    break;

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    /**
     * 获取优惠券信息
     */
    private void getCouponInfo(final String coupon_code, int store_id, final int gun_id){
        OkGo.<String>post(API_URL+"?request=private.coupon.check.valid.coupon&platform=merchant_user_app&token="+login_token
                +"&coupon_code="+coupon_code+"&fluid="+bean.getFluid()+"&money="+bean.getPayMount()+"&store_id="+getIntent().getIntExtra("oil_merchant_id",0)+"&gun_id="+gun_id+"&station_id="+station_id
        ).execute(new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                try {
                    JSONObject jsonObject=new JSONObject(response.body());
                    int code =jsonObject.getInt("code");
                    if (code == 0){
                        JSONObject data=jsonObject.getJSONObject("data");
                        Float yuan=Float.parseFloat(bean.getPayMount());
                        Float pay=yuan-data.getInt("coupon_money");
                        DecimalFormat decimalFormat=new DecimalFormat(".00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
                        String p=decimalFormat.format(pay);//format 返回的是字符串
                        showCouponDialog(coupon_code,data.getString("coupon_name"),bean.getPayMount(),data.getString("coupon_money"),p);
                    }else {
                        showInfo2(jsonObject.getString("msg"));
                        mPlayer_false.start();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 弹出优惠券 窗口
     */
    private void showCouponDialog(final String contont, String title, String coupon_money, String you_hui, String paymoney){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        final View view = LayoutInflater.from(this).inflate(R.layout.coupon_info_dialog,null);
        TextView tv_coupon_title,tv_coupon_money,tv_youhui,tv_pay_money;
        Button btn_cancel,btn_confirm;
        tv_coupon_title=view.findViewById(R.id.tv_coupon_title);
        tv_coupon_money=view.findViewById(R.id.tv_coupon_money);
        tv_youhui=view.findViewById(R.id.tv_youhui);
        tv_pay_money=view.findViewById(R.id.tv_pay_money);
        btn_confirm=view.findViewById(R.id.btn_confirm);
        btn_cancel=view.findViewById(R.id.btn_cancel);
        tv_coupon_title.setText(title);
        tv_coupon_money.setText("原始金额："+coupon_money);
        tv_youhui.setText("优惠金额："+you_hui);
        tv_pay_money.setText(paymoney+"");
        builder.setView(view);
        final Dialog dialog=builder.create();
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hx(contont);
                dialog.dismiss();
            }
        });
        dialog.show();
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

    /**
     * 核销确认操作
     */
    private void confirmCheck(String coupon_code){
        OkGo.<String>post(API_URL+"?request=private.coupon.coupon.hexiao.confirm&platform=merchant_user_app&token="+login_token+"&coupon_code="+coupon_code)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject jsonObject=new JSONObject(response.body());
                            int code =jsonObject.getInt("code");
                            if (code == 0){
                                printTicket(jsonObject.getString("id"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void printTicket(String id){
        OkGo.<String>post(API_URL+"?request=private.coupon.coupon.print.pos&token="+login_token+"&platform=merchant_user_app&id="+id)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject jsonObject=new JSONObject(response.body());
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
