package com.example.ludioil.ludihexiao.ui;

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
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.json.JSONException;
import org.json.JSONObject;

import static com.example.ludioil.ludihexiao.api.Constant.API_URL;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText et_old_password,et_new_password,et_confirm_password;
    private Button btn_confirm;

    private String login_token;

    private ImageView iv_back;
    private TextView tv_title;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.change_password_layout);
        this.init();
    }

    /**
     * 初始化
     */
    private void init() {

        //拿token
        SharedPreferences sp = getSharedPreferences("LoginUser", MODE_PRIVATE);
        login_token = sp.getString("user_token", null);


        et_old_password=findViewById(R.id.et_old_password);
        et_new_password=findViewById(R.id.et_new_password);
        iv_back=findViewById(R.id.iv_back);
        tv_title=findViewById(R.id.tv_title);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        tv_title.setText("修改密码  ");
        et_confirm_password=findViewById(R.id.et_confirm_password);
        btn_confirm=findViewById(R.id.btn_confirm);

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String oldPwd=et_old_password.getText().toString().trim();
                String newPwd=et_new_password.getText().toString().trim();
                String conPwd=et_confirm_password.getText().toString().trim();
                if (oldPwd.equals("")||newPwd.equals("")||conPwd.equals("")){
                    Toast.makeText(ChangePasswordActivity.this,"密码不能为空，请重新输入！",Toast.LENGTH_SHORT).show();
                }else if (!newPwd.equals(conPwd)){
                    Toast.makeText(ChangePasswordActivity.this,"两次输入的密码不正确，请重新输入！",Toast.LENGTH_SHORT).show();
                }else {
                    ChangePassword(oldPwd,newPwd);
                }
            }
        });
    }

    /**
     * 修改密码
     */
    private void ChangePassword(String oldpassword,String password){
        OkGo.<String>post(API_URL+"?request=private.admin.update_pwd_merchant_app&platform=merchant_user_app" +
                "&token="+login_token+"&password="+password+"&old_password="+oldpassword)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject jsonObject=new JSONObject(response.body());
                            int code =jsonObject.getInt("code");
                            String msg=jsonObject.getString("msg");
                            if (code == 0){
                                Toast.makeText(ChangePasswordActivity.this,msg,Toast.LENGTH_SHORT).show();
                                finish();
                            }else {
                                Toast.makeText(ChangePasswordActivity.this,msg,Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


}
