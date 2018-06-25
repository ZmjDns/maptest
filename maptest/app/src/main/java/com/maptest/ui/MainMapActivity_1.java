package com.maptest.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.Marker;
import com.maptest.R;

public class MainMapActivity_1 extends BaseMapActivity_1 implements AMap.OnInfoWindowClickListener,
					AMap.OnCameraChangeListener,
					AMap.OnMapLoadedListener,
					View.OnClickListener{
	private UiSettings uiSettings = null;
	private TextView freeSan;

//	private boolean freeScanMode = true;		//自由浏览模式
//	private boolean freeScanMode2 = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_map_1);

		mapView = (MapView)findViewById(R.id.aMap);
		mapView.onCreate(savedInstanceState);
		if (aMap == null) aMap = mapView.getMap();
		setCustomCurrentLocationStyle();		//自定义小蓝点
		aMap.setLocationSource(this);
		aMap.setMyLocationEnabled(true);
		aMap.setMyLocationRotateAngle(180);
		uiSettings = aMap.getUiSettings();
		uiSettings.setCompassEnabled(true);	//指南针
		uiSettings.setMyLocationButtonEnabled(true);	//我的位置
		uiSettings.setScaleControlsEnabled(true);		//比例尺

		aMap.setInfoWindowAdapter(this);
		aMap.setOnMarkerClickListener(this);
		aMap.setOnInfoWindowClickListener(this);
		aMap.setOnCameraChangeListener(this);

		freeSan = (TextView)findViewById(R.id.freeSan);
		freeSan.setOnClickListener(this);


	}

	@Override
	public void onCurrentLocationChanged(AMapLocation currentLocation) {

	}

	@Override
	public View getInfoWindow(Marker marker) {
		return null;
	}

	@Override
	public View getInfoContents(Marker marker) {
		return null;
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		return false;
	}

	@Override
	public void onCameraChange(CameraPosition cameraPosition) {

	}

	@Override
	public void onCameraChangeFinish(CameraPosition cameraPosition) {

	}

	@Override
	public void onInfoWindowClick(Marker marker) {

	}

	@Override
	public void onMapLoaded() {

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.freeSan:
//				freeScanMode = true;
//				clearAllMarkersOnMapAndSaveMarkList();
//				freeScanMode2 = !freeScanMode2;
//				if (freeScanMode2){
//					aMap.setLocationSource(null);
//					freeSan.setText("跟踪查看");
//				}else {
//					aMap.setLocationSource(this);
//					freeSan.setText("自由查看");
//				}
		}
	}
}
