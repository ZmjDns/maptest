package com.maptest.asynctasks;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by ZMJ on 2018/1/3.
 */
public class CheckAppUpdate extends AsyncTask<Void,Void,Boolean> {

	private ProgressDialog dialog;

	private Context context;

	private String appUpdateResp;

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		dialog = ProgressDialog.show(context,"提示","正在检查更新,请保持设备联网");
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		return checkAppUpdate();
	}

	@Override
	protected void onPostExecute(Boolean aBoolean) {
		super.onPostExecute(aBoolean);
		if(aBoolean){
			dialog.dismiss();
			if (appUpdateResp.equals("HAVE_NEW")){
				new AlertDialog.Builder(context)
						.setTitle("提示")
						.setMessage("有新版本发布，是否更新？\n请保证设备联网")
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								try{
									Intent intent = new Intent();
									intent.setAction("android.intent.action.VIEW");
//									Uri content_uri = Uri.parse();
//									intent.setData(content_uri);
//									startActivity(intent);
								}catch (Exception e){
									e.printStackTrace();
								}
							}
						})
						.setPositiveButton("下次再说", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						});
			}

		}
	}

	public Boolean checkAppUpdate(){
		HttpURLConnection connection = null;
		try {
			URL url = new URL("http://192.168.18.11:8080/SXWWServer/api2.jsp?method=check_app_update&client_version_code=1");
			connection = (HttpURLConnection)url.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			connection.setRequestMethod("POST");

			int code = connection.getResponseCode();
			if (code == 200){
				InputStream inputStream = connection.getInputStream();
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				byte[] tmpbuffer = new byte[1024];
				int len = -1;
				while ((len = inputStream.read(tmpbuffer)) != -1){
					outputStream.write(tmpbuffer,0,len);
				}
				outputStream.close();
				inputStream.close();
				appUpdateResp = new String(outputStream.toByteArray()).trim();
				if(appUpdateResp != null || !("").equals(appUpdateResp)){
					return true;
				}else {
					return  false;
				}
			}else {
				//Toast.makeText(this,"检查更新失败",Toast.LENGTH_SHORT).show();
				return false;
			}
		}catch (Exception e){
			e.printStackTrace();
			return false;
		}
	}
}
