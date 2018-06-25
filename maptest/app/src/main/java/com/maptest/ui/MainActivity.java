package com.maptest.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.maptest.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener{
//	//声明AMapLocationClient类对象
//	public AMapLocationClient mLocationClient = null;
//	//声明定位回调监听器
//	public AMapLocationListener mLocationListener ;
	private MapView mapView;
	private AMap amap;

	private String[] permissions = new String[]{
			Manifest.permission.ACCESS_COARSE_LOCATION,
			Manifest.permission.ACCESS_FINE_LOCATION,
			Manifest.permission.ACCESS_NETWORK_STATE,
			Manifest.permission.ACCESS_WIFI_STATE,
			Manifest.permission.CHANGE_WIFI_STATE,
			Manifest.permission.INTERNET,
			Manifest.permission.READ_PHONE_STATE
	};

	List<String> mPermissions = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mapView = (MapView)findViewById(R.id.map);
		mapView.onCreate(savedInstanceState);
		init();

		ToggleButton bt = (ToggleButton)findViewById(R.id.tb);
		bt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked){
					amap.setMapType(AMap.MAP_TYPE_SATELLITE);
				}else {
					amap.setMapType(AMap.MAP_TYPE_NORMAL);
				}
			}
		});

		getPermission();



	}

	private void getPermission(){//判断未授予的权限
		mPermissions.clear();
		for (int i = 0; i < permissions.length; i++) {
			if (ContextCompat.checkSelfPermission(MainActivity.this,permissions[i]) != PackageManager.PERMISSION_GRANTED){
				mPermissions.add(permissions[i]);
			}
		}
	}

	private void getEmptyPremission(){//判断是否为空
//		delayEntryPage();
	}

	private void init(){
		if(amap == null){
			amap = mapView.getMap();
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

	}

	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}


//	//声明mlocationClient对象
//	public AMapLocationClient mlocationClient;
//	//声明mLocationOption对象
//	public AMapLocationClientOption mLocationOption = null;
//	mlocationClient = new AMapLocationClient(this);
////初始化定位参数
//	mLocationOption = new AMapLocationClientOption();
////设置定位监听
//	mlocationClient.setLocationListener(this);
////设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
//	mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
////设置定位间隔,单位毫秒,默认为2000ms
//	mLocationOption.setInterval(2000);
////设置定位参数
//	mlocationClient.setLocationOption(mLocationOption);
//// 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
//// 注意设置合适的定位时间的间隔（最小间隔支持为1000ms），并且在合适时间调用stopLocation()方法来取消定位请求
//// 在定位结束后，在合适的生命周期调用onDestroy()方法
//// 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
////启动定位
//	mlocationClient.startLocation();
//	@Override
//	public void onLocationChanged(AMapLocation amapLocation) {
//		if (amapLocation != null) {
//			if (amapLocation.getErrorCode() == 0) {
//				//定位成功回调信息，设置相关消息
//				amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
//				amapLocation.getLatitude();//获取纬度
//				amapLocation.getLongitude();//获取经度
//				amapLocation.getAccuracy();//获取精度信息
//				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//				Date date = new Date(amapLocation.getTime());
//				df.format(date);//定位时间
//			} else {
//				//显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
//				Log.e("AmapError","location Error, ErrCode:"
//						+ amapLocation.getErrorCode() + ", errInfo:"
//						+ amapLocation.getErrorInfo());
//			}
//		}
//	}
}
