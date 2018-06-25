package com.maptest.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.MyLocationStyle;
import com.maptest.R;
import com.maptest.bean.MyMark;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZMJ on 2018/1/30.
 */
public abstract class BaseMapActivity_1 extends Activity implements
		LocationSource,AMapLocationListener,AMap.InfoWindowAdapter,AMap.OnMarkerClickListener{

	protected AMap aMap = null;
	protected MapView mapView = null;

	private OnLocationChangedListener onLocationChangedListener = null;
	protected AMapLocationClient aMapLocationClient;
	protected AMapLocationClientOption aMapLocationClientOption;
//	protected LocationManagerProxy

	private AMapLocation currentLication = null;//当前地体位置

	/**
	 * 是否对Camera的变化作出响应.（即是否执行onCameraChangeFinish方法中的代码）
	 */
	protected boolean responseForCameraChange = true;

	private List<MyMark> currentMarks = new ArrayList<MyMark>();

	@Override
	public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
		super.onCreate(savedInstanceState, persistentState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mapView != null){
			mapView.onResume();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mapView != null){
			mapView.onPause();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mapView != null){
			mapView.onDestroy();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mapView != null){
			mapView.onSaveInstanceState(outState);
		}
	}
	//http://blog.csdn.net/guchuanhang/article/details/51722207

	@Override
	public void activate(OnLocationChangedListener listener) {
		this.onLocationChangedListener = listener;
		if (aMapLocationClient == null){
			aMapLocationClient = new AMapLocationClient(this);
			aMapLocationClientOption = new AMapLocationClientOption();
			aMapLocationClient.setLocationListener(this);

			aMapLocationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
			aMapLocationClientOption.setOnceLocation(true);
			aMapLocationClient.setLocationOption(aMapLocationClientOption);
			aMapLocationClient.startLocation();
		}
	}

	@Override
	public void deactivate() {
		onLocationChangedListener = null;
		if (aMapLocationClient != null){
//			aMapLocationClient.startLocation();
			aMapLocationClient.stopLocation();
			aMapLocationClient.onDestroy();
		}
	}

	@Override
	public void onLocationChanged(AMapLocation aMapLocation) {
		if (isFreeScan()){
			onCurrentLocationChanged(currentLication);
			onLocationChangedListener.onLocationChanged(null);

			Log.e("定位模式","自动定位....");
			return;
		}else {
			Log.e("定位模式","自由查看");
		}
		if (onLocationChangedListener != null && aMapLocation != null){
			if (aMapLocation.getErrorCode() == 0){
				currentLication = aMapLocation;
				onCurrentLocationChanged(currentLication);
				onLocationChangedListener.onLocationChanged(aMapLocation);//显示系统小蓝点
				float bearing = aMapLocation.getBearing();
				aMap.setMyLocationRotateAngle(bearing);		//设置小蓝点的旋转角度
			}else {
				String errText = "定位失败，" + aMapLocation.getErrorCode() + aMapLocation.getErrorInfo();
				Log.e("AmapErr" , errText);
			}
		}
	}

	protected boolean isFreeScan(){
		return false;
	}
	protected AMapLocation getCurrentLocation(){return currentLication;}

	public abstract  void onCurrentLocationChanged(final AMapLocation currentLocation);

	/**
	 * 清除地图上所有的标记和在List中记录的Markers
	 */
	protected void clearAllMarkersOnMapAndSaveMarkList(){
		if (aMap != null){
			aMap.clear();
			setCustomCurrentLocationStyle();
		}
	}
	/**
	 * 自定义当前图层样式
	 */
	protected void setCustomCurrentLocationStyle(){
		MyLocationStyle myLocationStyle = new MyLocationStyle();
		//设置自定义的图标
		myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.location_marker));
		aMap.setMyLocationStyle(myLocationStyle);
//		if (aMap != null){
//			aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
//					new LatLng(aMap.getMyLocation().getLatitude(), aMap.getMyLocation().getLongitude()),
//					aMap.getCameraPosition().zoom));
//		}
	}


}
