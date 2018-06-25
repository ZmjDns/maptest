package com.maptest.asynctasks;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Creted by ZMJ on 2018/1/3.
 */
public class CheckAppUpdateTask extends BaseAsyncTask<Void,Void,Boolean> {

	private String appUpdateResp;

	public CheckAppUpdateTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean showDialog() {
		return true;
	}

	@Override
	protected Boolean canCancle() {
		return false;
	}

	@Override
	protected String getTitle() {
		return "请稍后";
	}

	@Override
	protected String getMessage() {
		return "正在检查更新.......";
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		return checkAppUpdate();
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);

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
