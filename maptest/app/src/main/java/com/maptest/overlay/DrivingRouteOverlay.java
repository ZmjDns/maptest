package com.maptest.overlay;

import android.content.Context;
import android.graphics.Color;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveStep;
import com.amap.api.services.route.TMC;
import com.maptest.R;
import com.maptest.utils.AMapServicesUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZMJ on 2018/1/24.
 */
public class DrivingRouteOverlay extends RouteOverlay {

	private DrivePath drivePath;
	private List<LatLonPoint> throughPointList;
	private List<Marker> throughPointMarkList = new ArrayList<Marker>();
	private Boolean throughPointMarkVisible = true;
	private List<TMC> tmcs;
	private PolylineOptions mPolylineOptions;
	private PolylineOptions mPolylineOptionscolor;
	private Context mContext;
	private Boolean isColorfullline = true;
	private float mWidth = 25;
	private List<LatLng> mLatLongsOfPath;
	private BitmapDescriptor mDrivStationDescriptor;//驾车图标

	public void setIsColorfullline(Boolean iscolorfullline){
		this.isColorfullline = iscolorfullline;
	}

	public DrivingRouteOverlay(Context context) {
		super(context);
	}

	/**
	 * 	根据参数构造导航路线规划图层
	 * @param context
	 * @param aMap
	 * @param path		线路规划方案
	 * @param start
	 * @param end
	 * @param throughPointList
	 */
	public DrivingRouteOverlay(Context context, AMap aMap,DrivePath path,LatLonPoint start,LatLonPoint end,List<LatLonPoint> throughPointList){
		super(context);
		mContext = context;
		mAMap = aMap;
		this.drivePath = path;
		startPoint = AMapServicesUtil.convertToLatLng(start);
		endPoint = AMapServicesUtil.convertToLatLng(end);
		this.throughPointList = throughPointList;

		initBitmapDescriptor();
	}
	//设置线路的宽度
	public float getRouteWidth(float mWidth){
		return mWidth;
	}

	/**
	 * 添加驾车线路到地图
	 */
	public void addToMap(){
		initPolylineOptions();
		try {
			if (mAMap == null){
				return;
			}

			if (mWidth == 0 || drivePath == null){
				return;
			}

			mLatLongsOfPath = new ArrayList<LatLng>();
			tmcs = new ArrayList<TMC>();
			List<DriveStep> drivePaths = drivePath.getSteps();
			mPolylineOptions.add(startPoint);
			for (int i = 0;i < drivePaths.size();i++){
				DriveStep step = drivePaths.get(i);
				List<LatLonPoint> latLonPoints = step.getPolyline();
				List<TMC> tmclist = step.getTMCs();
				tmcs.addAll(tmclist);
				addDrivingStationMarkers(step,convertToLatLng(latLonPoints.get(0)));
				for (LatLonPoint latLonPoint : latLonPoints){
					mPolylineOptions.add(convertToLatLng(latLonPoint));
					mLatLongsOfPath.add(convertToLatLng(latLonPoint));
				}
			}
			mPolylineOptions.add(endPoint);
			if (startMarker != null){
				startMarker.remove();
				startMarker = null;
			}
			if (endMarker != null){
				endMarker.remove();
				endMarker = null;
			}
			addStartAndEndMarker();
			addThroughPointMark();
			if (isColorfullline && tmcs.size() > 0){
				colorWayUpdate(tmcs);
				showcolorPolyline();
			}else {
				showPolyline();
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	private BitmapDescriptor defaultRoute = null;
	private BitmapDescriptor unknownTraffic = null;
	private BitmapDescriptor smoothTraffic = null;
	private BitmapDescriptor slowTriffic = null;
	private BitmapDescriptor jamTraffic = null;
	private BitmapDescriptor veryjamTraffic = null;
	private void initBitmapDescriptor(){
		defaultRoute = BitmapDescriptorFactory.fromResource(R.drawable.amap_route_color_texture_6_arrow);
		unknownTraffic = BitmapDescriptorFactory.fromResource(R.drawable.amap_route_color_texture_0_arrow);
		smoothTraffic = BitmapDescriptorFactory.fromResource(R.drawable.amap_route_color_texture_4_arrow);
		slowTriffic = BitmapDescriptorFactory.fromResource(R.drawable.amap_route_color_texture_3_arrow);
		jamTraffic = BitmapDescriptorFactory.fromResource(R.drawable.amap_route_color_texture_2_arrow);
		veryjamTraffic = BitmapDescriptorFactory.fromResource(R.drawable.amap_route_color_texture_9_arrow);
	}
	//初始化路线属性
	private void initPolylineOptions(){
//		if (mDrivStationDescriptor == null){
//			mDrivStationDescriptor = getDriveBitmapDescriptor();//获取驾车图标
//		}
		mPolylineOptions = null;

		mPolylineOptions = new PolylineOptions();
		mPolylineOptions.color(getDriveColor()).width(getRouteWidth());
	}

	private void addDrivingStationMarkers(DriveStep driveStep,LatLng latLng){
		addStationMarker(new MarkerOptions()
			.position(latLng)
			.title("\u65B9\u5411:" + driveStep.getAction()
				+ "\n\u9053\u8DEF:" + driveStep.getRoad())
			.snippet(driveStep.getInstruction()).visible(nodeIconVisible)
			.anchor(0.5f,0.5f).icon(getDriveBitmapDescriptor())
		);
	}

	public LatLng convertToLatLng(LatLonPoint point){
		return new LatLng(point.getLatitude(),point.getLongitude());
	}

	private void addThroughPointMark(){
		if(this.throughPointList != null && this.throughPointList.size() > 0){
			LatLonPoint latLonPoint = null;
			for (int i = 0 ;i < this.throughPointList.size(); i++){
				latLonPoint = this.throughPointList.get(i);
				if (latLonPoint != null){
					throughPointMarkList.add(mAMap
					.addMarker((new MarkerOptions())
						.position(new LatLng(latLonPoint.getLatitude(),latLonPoint.getLongitude()))
						.visible(throughPointMarkVisible)
						.icon(BitmapDescriptorFactory.fromResource(R.drawable.amap_through))
						.title("\u9014\u7ECF\u70B9")));
				}
			}
		}
	}

	/**
	 * 根据不同的路段拥堵情况显示不同的颜色
	 * @param tmcSection
	 */
	private void colorWayUpdate(List<TMC> tmcSection){
		if (mAMap == null){
			return;
		}
		if (tmcSection == null || tmcSection.size() <= 0){
			return;
		}
		TMC segmentTrafficStatus;
		mPolylineOptionscolor = null;
		mPolylineOptionscolor = new PolylineOptions();
		mPolylineOptionscolor.width(getRouteWidth());
		List<Integer> colorList = new ArrayList<Integer>();
		List<BitmapDescriptor> bitmapDescriptors = new ArrayList<BitmapDescriptor>();
		List<LatLng> points = new ArrayList<>();
		List<Integer> texIndexList = new ArrayList<Integer>();

		points.add(startPoint);
		points.add(AMapServicesUtil.convertToLatLng(tmcSection.get(0).getPolyline().get(0)));
		colorList.add(getDriveColor());
		bitmapDescriptors.add(unknownTraffic);

		BitmapDescriptor bitmapDescriptor = null;
		int texIndex = 0;
		texIndexList.add(texIndex);
		texIndexList.add(++texIndex);
		for (int i = 0; i < tmcSection.size(); i++){
			segmentTrafficStatus = tmcSection.get(i);
			int color = getcolor(segmentTrafficStatus.getStatus());
			bitmapDescriptor = getTrafficBitmapDescriptor(segmentTrafficStatus.getStatus());
			List<LatLonPoint> mployline = segmentTrafficStatus.getPolyline();
			for ( int j = 1; j <mployline.size(); j++){
				points.add(AMapServicesUtil.convertToLatLng(mployline.get(j)));

				colorList.add(color);

				texIndexList.add(++texIndex);
				bitmapDescriptors.add(bitmapDescriptor);
			}
		}

		points.add(endPoint);
		colorList.add(getDriveColor());
		bitmapDescriptors.add(defaultRoute);
		texIndexList.add(++texIndex);
		mPolylineOptionscolor.addAll(points);
		mPolylineOptionscolor.colorValues(colorList);
	}

	private int getcolor(String status){
		if (status.equals("畅通")){
			return Color.GREEN;
		}else if (status.equals("缓行")){
			return Color.YELLOW;
		}else if (status.equals("拥堵")){
			return Color.RED;
		}else if (status.equals("严重拥堵")){
			return Color.parseColor("#990033");
		}else {
			return Color.parseColor("#537edc");
		}
	}

	private BitmapDescriptor getTrafficBitmapDescriptor(String status){
		if (status.equals("畅通")){
			return smoothTraffic;
		}else if (status.equals("缓行")){
			return slowTriffic;
		}else if (status.equals("拥堵")){
			return jamTraffic;
		}else if (status.equals("严重拥堵")){
			return veryjamTraffic;
		}else {
			return defaultRoute;
		}
	}

	/**
	 * 去掉DriverLineOverlay上的线段和标记
	 */
	@Override
	public void removeFromMap() {
		try{
			super.removeFromMap();
			if (this.throughPointMarkList != null && this.throughPointMarkList.size() > 0){
				for (int i = 0 ; i < this.throughPointMarkList.size(); i++){
					this.throughPointMarkList.get(i).remove();
				}
				this.throughPointMarkList.clear();
			}
		}catch (Exception e){
			e.printStackTrace();
		}

	}

	private void showcolorPolyline(){
		addPolyLine(mPolylineOptionscolor);
	}

	private void showPolyline(){
		addPolyLine(mPolylineOptions);
	}
}
