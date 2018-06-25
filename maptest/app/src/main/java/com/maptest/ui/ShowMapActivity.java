package com.maptest.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RidePath;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.maptest.Const;
import com.maptest.R;
import com.maptest.adapter.BusResultListAdapter;
import com.maptest.overlay.BusRouteOverlay;
import com.maptest.overlay.DrivingRouteOverlay;
import com.maptest.overlay.PoiOverlay;
import com.maptest.overlay.RideRouteOverlay;
import com.maptest.overlay.WalkRouteOverlay;
import com.maptest.utils.AppUtils;
import com.maptest.utils.GetPermissionsutil;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ShowMapActivity extends AppCompatActivity implements View.OnClickListener,
		RouteSearch.OnRouteSearchListener,AMap.OnMarkerClickListener, AMap.InfoWindowAdapter,
		PoiSearch.OnPoiSearchListener{

	private MapView mapView;
	private AMap aMap;

	private RouteSearch routeSearch;
	private WalkRouteResult mWalkRouteResult;
	private WalkRouteOverlay walkRouteOverlay;
	private DriveRouteResult mDriveRouteResult;
	private BusRouteResult mBusRouteResult;
	private RideRouteResult mRideRouteResult;

	//声明AMapLocationClient对象
	public AMapLocationClient mLocationClient = null;



	//声明定位回调监听器
	public AMapLocationListener mLocationListener = new AMapLocationListener() {
		@Override
		public void onLocationChanged(AMapLocation amapLocation) {
			if (amapLocation != null){
				if (amapLocation.getErrorCode() == 0){
					//定位成功回调信息，设置相关消息
					amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
					amapLocation.getLatitude();//获取纬度
					amapLocation.getLongitude();//获取经度
					amapLocation.getAccuracy();//获取精度信息
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Date date = new Date(amapLocation.getTime());
					df.format(date);//定位时间
					amapLocation.getAddress();//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
					//逐步精确的位置信息
					amapLocation.getCountry();
					amapLocation.getProvince();
					amapLocation.getCity();
					amapLocation.getDistrict();
					amapLocation.getStreet();
					amapLocation.getStreetNum();
					amapLocation.getCityCode();
					amapLocation.getAdCode();
					amapLocation.getAoiName();
					lat = amapLocation.getLatitude();
					lon = amapLocation.getLongitude();
					Log.v("pcw","经度：" + lat + "纬度：" + lon );
					Log.v("pcw", "国家：" + amapLocation.getCountry() + "省市" + amapLocation.getProvince() + " 城市 : " + amapLocation.getCity() + " 街道 : " + amapLocation.getDistrict());

					//设置地图为当前位置
					aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lon),16));
					MarkerOptions markerOptions = new MarkerOptions();
					markerOptions.position(new LatLng(lat,lon));
					markerOptions.title("当前位置");
					markerOptions.visible(true);
					BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.location_marker));
					markerOptions.icon(bitmapDescriptor);
					aMap.clear();
					aMap.addMarker(markerOptions);//调用addMarkers方法可以添加多个标记点
//					aMap.addMarkers()
				}else {
					//显示错误信息ErrorCode是错误码，errorinfo是错误信息，详见错误码表
					Log.e("AmapError","LocationError,ErrorCode:" + amapLocation.getErrorCode() + ",errorInfo:" + amapLocation.getErrorInfo());
				}
			}
		}
	};

	//声明mLocationOption对象
	public AMapLocationClientOption mLocationOption = null;
	private double lat;//纬度
	private double lon;//经度

	private RouteSearch.FromAndTo fromAndTo;
	ProgressDialog dialog = null;

	private TextView freeSearchTV,advanceSearchTV,tongJiInfo,setting;
	private ListView transporentList;
	private BusResultListAdapter busResultListAdapter;
	private ProgressDialog progDialog = null;// 搜索时进度条
	private TextView mKeywordsTextView;		//收索kunag
	private String mKeyWords = "";// 要输入的poi搜索关键字
	private PoiSearch.Query query;// Poi查询条件类
	private PoiSearch poiSearch;// POI搜索
	private Marker mPoiMarker;
	private PoiResult poiResult; // poi返回的结果

	private TextView searchText;		//收索kunag
	public static final int REQUEST_CODE = 100;
	public static final int RESULT_CODE_INPUTTIPS = 101;
	public static final int RESULT_CODE_KEYWORDS = 102;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_map);

		mapView = (MapView) findViewById(R.id.markermap);
		mapView.onCreate(savedInstanceState);

		freeSearchTV = (TextView) findViewById(R.id.freeSearchTV);
		advanceSearchTV = (TextView) findViewById(R.id.advanceSearchTV);
		tongJiInfo = (TextView) findViewById(R.id.tongJiInfo);
		setting = (TextView) findViewById(R.id.setting);

		mKeywordsTextView = (TextView) findViewById(R.id.searchText);
		mKeywordsTextView.setOnClickListener(this);
//		searchText = (TextView) findViewById(R.id.searchText);
//		searchText.setOnClickListener(this);

		transporentList = (ListView) findViewById(R.id.transporentList);
		transporentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			}
		});

		freeSearchTV.setOnClickListener(this);
		advanceSearchTV.setOnClickListener(this);
		tongJiInfo.setOnClickListener(this);
		setting.setOnClickListener(this);

		GetPermissionsutil.verifyStoragePermissions(this);

		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
		}else{
			//do your job
		}

		if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
		} else {
			//
		}


		//初始化定位
		mLocationClient = new AMapLocationClient(getApplicationContext());
		mLocationClient.setLocationListener(mLocationListener);

		ToggleButton changeModleBt = (ToggleButton) findViewById(R.id.changeModleBt);
		changeModleBt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked){
					aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
				}else {
					aMap.setMapType(AMap.MAP_TYPE_NORMAL);
//					new CheckAppUpdateTask(ShowMapActivity.this);
				}
			}
		});

		init();
//		new CheckAppUpdate().execute();
		initRouteSearch();
		mKeyWords = "";
	}


	private void init(){
		if (aMap == null){
			aMap = mapView.getMap();
		}
		setUpMap();
	}

	//定位参数设置
	private void setUpMap(){
		//初始化定位参数
		mLocationOption = new AMapLocationClientOption();
		//设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
		mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
		//设置是否返回地址信息（默认返回地址信息）
		mLocationOption.setNeedAddress(true);
		//设置是否只定位一次,默认为false
		mLocationOption.setOnceLocation(true);//只定位一次
		//设置是否强制刷新WIFI，true为强制刷新
		mLocationOption.setWifiActiveScan(true);
		//设置是否允许模拟位置,默认为false，不允许模拟位置
		mLocationOption.setMockEnable(true);
		//设置定位间隔,单位毫秒,默认为2000ms
		//mLocationOption.setInterval(2000);
		//给定位客户端对象设置定位参数
		mLocationClient.setLocationOption(mLocationOption);
		//启动定位
		mLocationClient.startLocation();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.freeSearchTV:
				calculateWalkRoute();
				break;
			case R.id.advanceSearchTV:
				calculDriveRoute();
				break;
			case R.id.tongJiInfo:
				calculBusRoute();
				break;
			case R.id.setting:
//				new CheckAppUpdate().execute();
//				calculateRideRoute();
				startActivity(new Intent(this.getApplicationContext(),com.amap.api.maps.offlinemap.OfflineMapActivity.class));
				break;
			case R.id.searchText:
				Intent intent = new Intent(this,InputTipsActivity.class);
				startActivityForResult(intent,REQUEST_CODE);
				break;
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_CODE_INPUTTIPS && data != null){
			//获取data并标注到地图上
			aMap.clear();
			Tip tip = data.getParcelableExtra("ExtraTip");
			if (tip.getPoiID() == null || tip.getPoiID().equals("")){
				doSearchQuery(tip.getName());
			}else {
				addTipMarker(tip);
			}
			mKeywordsTextView.setText(tip.getName());
			if(!tip.getName().equals("")){

			}
		}else if (resultCode == RESULT_CODE_KEYWORDS && data != null){

		}
	}
	private void doSearchQuery(String keywords){
		showProgressDialog();// 显示进度框
		int currentPage = 1;
		// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
		query = new PoiSearch.Query(keywords, "", "北京");
		// 设置每页最多返回多少条poiitem
		query.setPageSize(10);
		// 设置查第一页
		query.setPageNum(currentPage);

		poiSearch = new PoiSearch(this, query);
		poiSearch.setOnPoiSearchListener(this);
		poiSearch.searchPOIAsyn();
	}

	/**
	 * 用Marker标记点击地点
	 * @param tip
	 */
	private void addTipMarker(Tip tip){
		if (tip == null){
			return;
		}
		mPoiMarker  = aMap.addMarker(new MarkerOptions());
		LatLonPoint point = tip.getPoint();
		if (point != null){
			LatLng markerPosition = new LatLng(point.getLatitude(),point.getLongitude());
			mPoiMarker.setPosition(markerPosition);
			aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerPosition,17));
		}
		mPoiMarker.setTitle(tip.getName());
		mPoiMarker.setSnippet(tip.getAddress());
	}
	/**
	 * 显示进度框
	 */
	private void showProgressDialog() {
		if (progDialog == null)
			progDialog = new ProgressDialog(this);
		progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progDialog.setIndeterminate(false);
		progDialog.setCancelable(false);
		progDialog.setMessage("正在搜索:\n" + mKeyWords);
		progDialog.show();
	}

	/*初始化 搜索路线所需的类*/
	private void initRouteSearch() {
		routeSearch = new RouteSearch(this);
		routeSearch.setRouteSearchListener(this);
		fromAndTo = new RouteSearch.FromAndTo(new LatLonPoint(39.942295, 116.335891),
				new LatLonPoint(39.995576, 116.481288));
	}

	/**
	 * 公交查询信息请求
	 */
	private void calculBusRoute(){
		dialog = ProgressDialog.show(this,"提示","正在计算公交路线");
		RouteSearch.BusRouteQuery busRouteQuery = new RouteSearch.BusRouteQuery(fromAndTo,RouteSearch.BUS_DEFAULT,"天津",0);
		routeSearch.calculateBusRouteAsyn(busRouteQuery);
	}

	/**
	 * 获取驾车出行信息
	 */
	private void calculDriveRoute(){
		dialog = ProgressDialog.show(this,"提示","正在计算驾车路线");
		RouteSearch.DriveRouteQuery driveRouteQuery = new RouteSearch.DriveRouteQuery(fromAndTo,RouteSearch.DRIVING_SINGLE_DEFAULT,null,null,"");
		routeSearch.calculateDriveRouteAsyn(driveRouteQuery);
	}

	/*计算步行路线*/
	private void calculateWalkRoute() {
		dialog = ProgressDialog.show(this,"提示","请稍等...");
//		//初始化query对象，fromAndTo是包含起终点信息，walkMode是步行路径规划的模式
//		RouteSearch.WalkRouteQuery walkRouteQuery = new RouteSearch.WalkRouteQuery(
//				fromAndTo, RouteSearch.WALK_DEFAULT);
//		routeSearch.calculateWalkRouteAsyn(walkRouteQuery);

		RouteSearch.WalkRouteQuery walkRouteQuery1 = new RouteSearch.WalkRouteQuery(fromAndTo);
		routeSearch.calculateWalkRouteAsyn(walkRouteQuery1);
	}

	/**
	 * 骑行路线规划请求
	 */
	private void calculateRideRoute(){
		dialog = ProgressDialog.show(this,"提示","请稍等...");
		RouteSearch.RideRouteQuery rideRouteQuery = new RouteSearch.RideRouteQuery(fromAndTo);
		routeSearch.calculateRideRouteAsyn(rideRouteQuery);
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
	protected void onStop() {
		super.onStop();
		mLocationClient.stopLocation();//停止定位
	}

	@Override
	public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
		mLocationClient.onDestroy();
	}



	private String appUpdateResp;//版本信息
	private int appVersionCode;//版本号
	private String newAppurl;	//新版本地址

	@Override
	public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {
		dialog.dismiss();

		if (i == 1000){
			if (busRouteResult != null && busRouteResult.getPaths() != null && busRouteResult.getPaths().size() > 0){
				mBusRouteResult = busRouteResult;
//				final BusPath busPath = mBusRouteResult.getPaths().get(0);
//				BusRouteOverlay busRouteOverlay = new BusRouteOverlay(ShowMapActivity.this,aMap,busPath,
//												mBusRouteResult.getStartPos(),mBusRouteResult.getTargetPos());
//				busRouteOverlay.removeFromMap();
//				busRouteOverlay.addToMap();
//				busRouteOverlay.zoomToSpan();
				transporentList.setVisibility(View.VISIBLE);
				busResultListAdapter = new BusResultListAdapter(ShowMapActivity.this,mBusRouteResult);
				transporentList.setAdapter(busResultListAdapter);
				busResultListAdapter.setOnItemClick(new BusResultListAdapter.OnItemClickListener() {
					@Override
					public void onItemClick(BusPath busPath) {
						aMap.clear();
						transporentList.setVisibility(View.GONE);
						BusRouteOverlay busRouteOverlay = new BusRouteOverlay(ShowMapActivity.this,aMap,busPath,
								mBusRouteResult.getStartPos(),mBusRouteResult.getTargetPos());
						busRouteOverlay.removeFromMap();
						busRouteOverlay.addToMap();
						busRouteOverlay.zoomToSpan();
					}
				});
			}else {
				Toast.makeText(this,"没有返回公交结果",Toast.LENGTH_SHORT).show();
			}
		}else {
			Toast.makeText(this,"网络错误",Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {
		dialog.dismiss();
		aMap.clear();
		if (i==1000){
			if(driveRouteResult != null && driveRouteResult.getPaths() !=  null&&driveRouteResult.getPaths().size() > 0){
				mDriveRouteResult = driveRouteResult;
				final DrivePath drivePath = mDriveRouteResult.getPaths().get(0);
				DrivingRouteOverlay drivingRouteOverlay = new DrivingRouteOverlay(ShowMapActivity.this,aMap,drivePath,
						mDriveRouteResult.getStartPos(),mDriveRouteResult.getTargetPos(),null);
				drivingRouteOverlay.setNodeIconVisibility(true);//设置节点marker是否显示
				drivingRouteOverlay.setIsColorfullline(true);//否用颜色显示交通拥堵情况。默认true
				drivingRouteOverlay.removeFromMap();
				drivingRouteOverlay.addToMap();
				drivingRouteOverlay.zoomToSpan();
			}else{
				Toast.makeText(this,"对不起，暂无规划数据",Toast.LENGTH_SHORT).show();
			}
		}else {
			Toast.makeText(ShowMapActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
		}

	}

	/**
	 * 步行回调结果
	 * @param walkRouteResult
	 * @param i
	 */
	@Override
	public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {
		dialog.dismiss();
		aMap.clear();
		if(i == 1000){
			if (walkRouteResult != null && walkRouteResult.getPaths() != null && walkRouteResult.getPaths().size() >0){
				mWalkRouteResult = walkRouteResult;
				final WalkPath walkPath = mWalkRouteResult.getPaths().get(0);
				if(walkRouteOverlay != null){
					walkRouteOverlay.removeFromMap();
				}
				walkRouteOverlay = new WalkRouteOverlay(this,aMap,walkPath,walkRouteResult.getStartPos(),walkRouteResult.getTargetPos());
				walkRouteOverlay.addToMap();//加载到地图
				walkRouteOverlay.zoomToSpan();//移到当前视角

			}else {
				Toast.makeText(this,"对不起，暂无规划数据",Toast.LENGTH_SHORT).show();
			}
		}else {
			Toast.makeText(this,"网络错误",Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {
		dialog.dismiss();
		aMap.clear();	//mRideRouteResult
		if (i == 1000){
			if (rideRouteResult.getPaths() != null && rideRouteResult.getPaths().size() > 0){
				mRideRouteResult = rideRouteResult;
				final RidePath ridePath = mRideRouteResult.getPaths()
						.get(0);
				RideRouteOverlay rideRouteOverlay = new RideRouteOverlay(
						this, aMap, ridePath,
						mRideRouteResult.getStartPos(),
						mRideRouteResult.getTargetPos());
				rideRouteOverlay.removeFromMap();
				rideRouteOverlay.addToMap();
				rideRouteOverlay.zoomToSpan();
			}
		}
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
	public void onPoiSearched(PoiResult result, int rCode) {
		dissmissProgressDialog();// 隐藏对话框
		if (rCode == 1000) {
			if (result != null && result.getQuery() != null) {// 搜索poi的结果
				if (result.getQuery().equals(query)) {// 是否是同一条
					poiResult = result;
					// 取得搜索到的poiitems有多少页
					List<PoiItem> poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
					List<SuggestionCity> suggestionCities = poiResult
							.getSearchSuggestionCitys();// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息

					if (poiItems != null && poiItems.size() > 0) {
						aMap.clear();// 清理之前的图标
						PoiOverlay poiOverlay = new PoiOverlay(aMap, poiItems);
						poiOverlay.removeFromMap();
						poiOverlay.addToMap();
						poiOverlay.zoomToSpan();
					} else if (suggestionCities != null
							&& suggestionCities.size() > 0) {
						showSuggestCity(suggestionCities);
					} else {
						Toast.makeText(this,"没有相关数据", Toast.LENGTH_LONG).show();
					}
				}
			} else {
				Toast.makeText(this,"没有相关数据", Toast.LENGTH_LONG).show();
			}
		} else {
			Toast.makeText(this,rCode, Toast.LENGTH_LONG).show();
		}
	}
	/**
	 * poi没有搜索到数据，返回一些推荐城市的信息
	 */
	private void showSuggestCity(List<SuggestionCity> cities) {
		String infomation = "推荐城市\n";
		for (int i = 0; i < cities.size(); i++) {
			infomation += "城市名称:" + cities.get(i).getCityName() + "城市区号:"
					+ cities.get(i).getCityCode() + "城市编码:"
					+ cities.get(i).getAdCode() + "\n";
		}
		Toast.makeText(this,infomation, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onPoiItemSearched(PoiItem poiItem, int i) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		return false;
	}
	/**
	 * 隐藏进度框
	 */
	private void dissmissProgressDialog() {
		if (progDialog != null) {
			progDialog.dismiss();
		}
	}
//按下确认键触发，本例为键盘回车或搜索键
//	@Override
//	public boolean onQueryTextSubmit(String query) {
////		Intent intent = new Intent();
////		intent.putExtra("KeyWord",query);
//
//		return false;
//	}
//	//输入字符变化时出发
//	@Override
//	public boolean onQueryTextChange(String newText) {
//		if (!SearchPOIUtil.IsEmptyOrNullString(newText )){
//			InputtipsQuery inputtipsQuery = new InputtipsQuery(newText,"北京");
//			Inputtips inputtips = new Inputtips(this.getApplicationContext(),inputtipsQuery);
//			inputtips.setInputtipsListener(this);
//			inputtips.requestInputtipsAsyn();
//		}
//		return false;
//	}

	private class CheckAppUpdate extends AsyncTask<Void,Void,Boolean> {

		private ProgressDialog dialog;

//		private Context context;



		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = ProgressDialog.show(ShowMapActivity.this,"提示","正在检查更新,请保持设备联网");
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
				new AlertDialog.Builder(ShowMapActivity.this)
						.setTitle("提示")
						.setMessage("有新版本发布，是否更新？\n请保证设备联网")
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								try{
									Intent intent = new Intent();
									intent.setAction("android.intent.action.VIEW");
									Uri content_uri = Uri.parse(newAppurl);
									intent.setData(content_uri);
									startActivity(intent);
								}catch (Exception e){
									e.printStackTrace();
								}
							}
						})
						.setNegativeButton("下次再说", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						})
						.show();
			}
		}
	}

	private Boolean checkAppUpdate(){
		HttpURLConnection connection = null;
		try {
			URL url = new URL(Const.SXWW_API_URL + "?method=check_app_update&client_version_code=" + AppUtils.getVersionCode(ShowMapActivity.this));
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
				if(appUpdateResp.equals("FLAG_NOT_HAVE_NEW") || appUpdateResp == null || ("").equals(appUpdateResp)){
					return false;
				}else {
					try{
						JSONObject object = new JSONObject(appUpdateResp);
						appVersionCode = Integer.parseInt(object.optString("version_code"));
						newAppurl = object.optString("version_url");
					}catch (Exception e){
						e.printStackTrace();
						return  false;
					}
				}
			}else {
				Toast.makeText(this,"检查更新失败",Toast.LENGTH_SHORT).show();
				return false;
			}
		}catch (Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
