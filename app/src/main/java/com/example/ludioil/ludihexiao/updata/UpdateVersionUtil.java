package com.example.ludioil.ludihexiao.updata;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.example.ludioil.ludihexiao.MainActivity;
import com.example.ludioil.ludihexiao.R;
import com.example.ludioil.ludihexiao.ui.LoginActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Method;

import static android.content.Context.DOWNLOAD_SERVICE;


/**
 * 
 * @author	wenjie
 *	版本更新的工具类
 */
public class UpdateVersionUtil {
	private DownloadManager mDownloadManager;
	private Long downloadId;
	/**
	 * 接口回调
	 * @author wenjie
	 *
	 */
	public interface UpdateListener{
		void onUpdateReturned(int updateStatus, VersionInfo versionInfo);
	}
	
	public UpdateListener updateListener;
	
	public void setUpdateListener(UpdateListener updateListener) {
		this.updateListener = updateListener;
	}
	
	/**
	 * 网络测试 检测版本
	 * @param context 上下文
	 */
	public static void checkVersion(final Context context, final UpdateListener updateListener){
		HttpRequest.get(ServerReqAddress.UPDATA_VERSION_REQ, new HttpRequest.RequestCallBackListener() {
			
			@Override
			public void onSuccess(String resultData) {
				try {
					JSONObject jsonObject = JsonUtil.stringToJson(resultData);
					Log.i("ppppppp",resultData);
					JSONArray array = jsonObject.getJSONArray("data");
					VersionInfo mVersionInfo = JsonUtil.jsonToBean(array.getJSONObject(0).toString(), VersionInfo.class);
					int clientVersionCode = ApkUtils.getVersionCode(context);
					int serverVersionCode = mVersionInfo.getVersionCode();
					//有新版本
					if(clientVersionCode < serverVersionCode){
						int i = NetworkUtil.checkedNetWorkType(context);

						if(i == NetworkUtil.NOWIFI){
							updateListener.onUpdateReturned(UpdateStatus.NOWIFI,mVersionInfo);
						}else if(i == NetworkUtil.WIFI){
							updateListener.onUpdateReturned(UpdateStatus.YES,mVersionInfo);
						}
					}else{
						//无新本
						updateListener.onUpdateReturned(UpdateStatus.NO,null);
					}
				} catch (Exception e) {
					e.printStackTrace();
					updateListener.onUpdateReturned(UpdateStatus.ERROR,null);
				}
			}
			
			@Override
			public void onFailure(String error) {
				updateListener.onUpdateReturned(UpdateStatus.TIMEOUT,null);
			}
		});
	}
	
	/**
	 * 弹出新版本提示
	 * @param context 上下文
	 * @param versionInfo 更新内容
	 */
	public static void showDialog(final Context context, final VersionInfo versionInfo){
		final Dialog dialog = new AlertDialog.Builder(context).create();
		final File file = new File(SDCardUtils.getRootDirectory()+"/updateVersion/landOil.apk");
		dialog.setCancelable(false);// 可以用“返回键”取消
		dialog.setCanceledOnTouchOutside(false);//
		dialog.show();
		final View view = LayoutInflater.from(context).inflate(R.layout.version_update_dialog, null);
		dialog.setContentView(view);
		
		final Button btnOk = (Button) view.findViewById(R.id.btn_update_id_ok);
		TextView tvContent = (TextView) view.findViewById(R.id.tv_update_content);
		TextView tvUpdateTile = (TextView) view.findViewById(R.id.tv_update_title);
		final TextView tvUpdateMsgSize = (TextView) view.findViewById(R.id.tv_update_msg_size);
		
		tvContent.setText(versionInfo.getVersionDesc());
		tvUpdateTile.setText("最新版本："+versionInfo.getVersionName());
		
		if(file.exists() && file.getName().equals("landOil.apk")){
			tvUpdateMsgSize.setText("新版本已经下载，是否安装？");
		}else{
			tvUpdateMsgSize.setText("新版本大小："+versionInfo.getVersionSize());
		}
		
		btnOk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(v.getId() == R.id.btn_update_id_ok){
						//新版本已经下载
						if(file.exists() && file.getName().equals("landOil.apk")){
							file.delete();
							Intent intent = new Intent(context,UpdateVersionService.class);
							intent.putExtra("downloadUrl", versionInfo.getDownloadUrl());
							context.startService(intent);
							Toast.makeText(context,"开始下载...",Toast.LENGTH_SHORT).show();
						}else{
							//没有下载，则开启服务下载新版本
							Intent intent = new Intent(context,UpdateVersionService.class);
							intent.putExtra("downloadUrl", versionInfo.getDownloadUrl());
							context.startService(intent);
							Toast.makeText(context,"开始下载...",Toast.LENGTH_SHORT).show();
						}
				}
			}
		});

	}
	
	/**
	 * 收起通知栏
	 * @param context
	 */
	public static void collapseStatusBar(Context context) {
		try{
			Object statusBarManager = context.getSystemService("statusbar");
			Method collapse;
			if (Build.VERSION.SDK_INT <= 16){
				collapse = statusBarManager.getClass().getMethod("collapse"); 
			}else{ 
				collapse = statusBarManager.getClass().getMethod("collapsePanels"); 
			} 
			collapse.invoke(statusBarManager);
		}catch (Exception localException){
			localException.printStackTrace();
		} 
	}


}
