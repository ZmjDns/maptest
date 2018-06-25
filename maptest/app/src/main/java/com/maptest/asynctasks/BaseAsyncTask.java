package com.maptest.asynctasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

/**
 * Created by ZMJ on 2018/1/3.
 */
public abstract class BaseAsyncTask<Params,Progress,Result> extends AsyncTask<Params,Progress,Boolean> {

	protected ProgressDialog dialog = null;
	protected Context context;

	public 	BaseAsyncTask(Context context){
		this.context = context;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		try{
			if(showDialog()){
				dialog = ProgressDialog.show(context,getTitle(),getMessage());
				dialog.setCancelable(canCancle());
				dialog.setCanceledOnTouchOutside(canCancle());
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		try {
			if(showDialog()){
				dialog.dismiss();
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	protected abstract Boolean showDialog();
	protected abstract Boolean canCancle();
	protected abstract String getTitle();
	protected abstract String getMessage();
}
