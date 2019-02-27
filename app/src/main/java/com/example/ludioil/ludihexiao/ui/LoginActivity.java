package com.example.ludioil.ludihexiao.ui;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ludioil.ludihexiao.MainActivity;
import com.example.ludioil.ludihexiao.R;
import com.example.ludioil.ludihexiao.SUNMI.util.AidlUtil;
import com.example.ludioil.ludihexiao.updata.SDCardUtils;
import com.example.ludioil.ludihexiao.updata.ToastUtils;
import com.example.ludioil.ludihexiao.updata.UpdateStatus;
import com.example.ludioil.ludihexiao.updata.UpdateVersionService;
import com.example.ludioil.ludihexiao.updata.UpdateVersionUtil;
import com.example.ludioil.ludihexiao.updata.VersionInfo;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import static com.example.ludioil.ludihexiao.api.Constant.API_URL;


/**
 * Created by PIGROAD on 2018/11/10
 * Email:920015363@qq.com
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText et_username, et_password;
    private Button btn_login;
    private TextView get_msg_btn_tv;
    private String login_token;

    private DownloadManager mDownloadManager;
    private Long downloadId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        this.init();
        SharedPreferences sp = getSharedPreferences("LoginUser", MODE_PRIVATE);
        login_token = sp.getString("user_token", null);
        //checkToken();
    }

    private void init() {

        //拿token
        SharedPreferences sp = getSharedPreferences("LoginUser", MODE_PRIVATE);
        login_token = sp.getString("user_token", null);

        et_username = findViewById(R.id.et_username);
        et_password = findViewById(R.id.et_password);
        btn_login = findViewById(R.id.btn_login);

        btn_login.setOnClickListener(this);

        checkUpdata();

        Grant();
        AidlUtil.getInstance().connectPrinterService(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                String username = et_username.getText().toString().trim();
                if (!username.equals("")) {
                    posLogin();
                } else {
                    Toast.makeText(getApplication(), "用户名不能为空", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void posLogin() {
        final String username = et_username.getText().toString().trim();
        String smsnumber = et_password.getText().toString().trim();
        long time = System.currentTimeMillis() / 1000;
        String logintime = String.valueOf(time);
        OkGo.<String>post(API_URL + "?request=public.auth.app_admin_login&platform=merchant_user_app&admin_id=" + username + "&password=" + smsnumber)
                .tag(this)
                .headers("header1", "headerValue1")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        final String responseStr = response.body();
                        try {
                            JSONObject jsonObject = new JSONObject(responseStr);
                            int code = (int) jsonObject.get("code");
                            Log.i("pigpigpigpigpigpig", responseStr);
                            if (code == 0) {
                                Toast.makeText(getApplicationContext(), "登录成功!", Toast.LENGTH_SHORT).show();
                                String token = (String) jsonObject.get("token");
                                SharedPreferences mSharedPreferences = getSharedPreferences("LoginUser", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = mSharedPreferences.edit();
                                editor.putString("user_token", token);
                                editor.commit();
                                Intent intent = new Intent(getApplicationContext(), OilMerchantActivity.class);
                                startActivityForResult(intent, 1);
                                finish();
                                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                            } else if (code == 1) {
                                String msg = (String) jsonObject.get("msg");
                                Toast.makeText(getApplicationContext(), "登录失败," + msg, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        Log.i("ppppppppppppppppp", "连接失败!!!!!!!!!!!!!!!");
                        Toast.makeText(LoginActivity.this, "请检查你的网络设置", Toast.LENGTH_SHORT).show();
                    }
                });

    }


    /**
     * 判断token会不会过期
     */
    private void checkToken() {
        OkGo.<String>post(API_URL + "?request=private.admin.check_token&platform=merchant_user_app&token=" + login_token)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body());
                            int code = jsonObject.getInt("code");
                            if (code == 0) {
                                startActivity(new Intent(LoginActivity.this, OilMerchantActivity.class));
                                finish();
                            } else if (code == 999) {
                                Toast.makeText(LoginActivity.this, "你的身份已过期，请重新登录", Toast.LENGTH_SHORT).show();
                                SharedPreferences sp = getSharedPreferences("LoginUser", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString("user_token", "");
                                editor.commit();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * 检查更新
     */
    private void checkUpdata() {
        //本地测试检测是否有新版本发布
        UpdateVersionUtil.checkVersion(LoginActivity.this, new UpdateVersionUtil.UpdateListener() {

            @Override
            public void onUpdateReturned(int updateStatus, VersionInfo versionInfo) {
                //判断回调过来的版本检测状态
                switch (updateStatus) {
                    case UpdateStatus.YES:
                        //弹出更新提示
                        if (Build.VERSION.SDK_INT >= 26){
                            showUpdata();
                        }else {
                            UpdateVersionUtil.showDialog(getBaseContext(), versionInfo);
                        }
                        break;
                    case UpdateStatus.NO:
                        //没有新版本
                        //ToastUtils.showToast(getApplicationContext(), "已经是最新版本了!");
                        checkToken();
                        break;
                    case UpdateStatus.NOWIFI:
                        //当前是非wifi网络
                        ToastUtils.showToast(getApplicationContext(), "温馨提示,当前非wifi网络,下载会消耗手机流量！");
                        if (Build.VERSION.SDK_INT >= 26){
                            showUpdata();
                        }else {
                            UpdateVersionUtil.showDialog(getBaseContext(), versionInfo);
                        }
                        break;
                    case UpdateStatus.ERROR:
                        //检测失败
                        ToastUtils.showToast(getApplicationContext(), "检测失败，请稍后重试！");
                        break;
                    case UpdateStatus.TIMEOUT:
                        //链接超时
                        ToastUtils.showToast(getApplicationContext(), "链接超时，请检查网络设置!");
                        break;
                }
            }

        });
    }

    /**
     * 权限申请
     */
    private void Grant() {

        int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        int permission = ActivityCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    LoginActivity.this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
        if (ContextCompat.checkSelfPermission(LoginActivity.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) LoginActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    1);
        }

    }

    private void showUpdata(){
        final Dialog dialog = new android.app.AlertDialog.Builder(this).create();
        final File file = new File(SDCardUtils.getRootDirectory()+"/updateVersion/landOil.apk");
        dialog.setCancelable(false);// 可以用“返回键”取消
        dialog.setCanceledOnTouchOutside(false);//
        dialog.show();
        final View view = LayoutInflater.from(this).inflate(R.layout.version_update_dialog, null);
        dialog.setContentView(view);
        final Button btnOk = (Button) view.findViewById(R.id.btn_update_id_ok);
        TextView tvContent = (TextView) view.findViewById(R.id.tv_update_content);
        TextView tvUpdateTile = (TextView) view.findViewById(R.id.tv_update_title);
        final TextView tvUpdateMsgSize = (TextView) view.findViewById(R.id.tv_update_msg_size);

        tvContent.setVisibility(View.GONE);
        tvUpdateTile.setVisibility(View.GONE);
        tvUpdateMsgSize.setVisibility(View.GONE);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.btn_update_id_ok){
                    showDownloadDialog();
                }
            }
        });

    }

    /**
     * 显示软件下载对话框
     */
    private void showDownloadDialog() {
        Toast.makeText(this, "正在下载···", Toast.LENGTH_SHORT).show();
        String downPath = "http://landoil.nd1688.com/source/upload/app/LandOil.apk";//下载路径 根据服务器返回的apk存放路径
        //使用系统下载类
        mDownloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(downPath);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedOverRoaming(false);
        File apkFile =
                new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "陆地石油.apk");
        if (apkFile.isFile()){
            apkFile.delete();
        }
        //创建目录下载
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "陆地石油.apk");
        // 把id保存好，在接收者里面要用
        downloadId = mDownloadManager.enqueue(request);
        //设置允许使用的网络类型，这里是移动网络和wifi都可以
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        //机型适配
        request.setMimeType("application/vnd.android.package-archive");
        //通知栏显示
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setTitle("下载");
        request.setDescription("正在下载中...");
        request.setVisibleInDownloadsUi(true);
        registerReceiver(mReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkStatus();
        }
    };

    /**
     * 检查下载状态
     */
    private void checkStatus() {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);
        Cursor cursor = mDownloadManager.query(query);
        if (cursor.moveToFirst()) {
            int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            switch (status) {
                //下载暂停
                case DownloadManager.STATUS_PAUSED:
                    break;
                //下载延迟
                case DownloadManager.STATUS_PENDING:
                    break;
                //正在下载
                case DownloadManager.STATUS_RUNNING:
                    break;
                //下载完成
                case DownloadManager.STATUS_SUCCESSFUL:
                    Toast.makeText(this, "下载完成", Toast.LENGTH_SHORT).show();
                    installAPK();
                    break;
                //下载失败
                case DownloadManager.STATUS_FAILED:
                    Toast.makeText(this, "下载失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
        cursor.close();
    }
    /**
     * 7.0兼容
     */
    private void installAPK() {
        File apkFile =
                new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "陆地石油.apk");
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //7.0以上需要
        if (Build.VERSION.SDK_INT >= 24) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri uri = FileProvider.getUriForFile(getApplication(), "com.example.ludioil.ludihexiao.updata.UpdateVersionService", apkFile);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        }
        startActivity(intent);
    }
}
