package com.maptest.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.amap.api.maps.offlinemap.OfflineMapManager;
import com.maptest.R;

public class OfflineMap extends Activity implements View.OnClickListener,OfflineMapManager.OfflineMapDownloadListener{	//com.amap.api.maps.offlinemap.OfflineMapActivity

	OfflineMapManager offlineMapManager = new OfflineMapManager(this,this);
	private TextView downloadmap,updatemap,deletemap;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_offline_map);

		downloadmap = (TextView)findViewById(R.id.downloadmap);
		updatemap = (TextView)findViewById(R.id.updatemap);
		deletemap= (TextView)findViewById(R.id.deletemap);

		downloadmap.setOnClickListener(this);
		updatemap.setOnClickListener(this);
		deletemap.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {

	}

	@Override
	public void onDownload(int i, int i1, String s) {

	}

	@Override
	public void onCheckUpdate(boolean b, String s) {

	}

	@Override
	public void onRemove(boolean b, String s, String s1) {

	}

}
