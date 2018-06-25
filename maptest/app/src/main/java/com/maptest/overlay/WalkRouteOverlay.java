package com.maptest.overlay;

import android.content.Context;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkStep;
import com.maptest.utils.AMapServicesUtil;

import java.util.List;

/**
 * Created by ZMJ on 2018/1/23.
 */
public class WalkRouteOverlay extends RouteOverlay {
	private PolylineOptions mPolylineOptions;
	private BitmapDescriptor walkStationDescriptor = null;
	private WalkPath walkPath;

	/**
	 * 通过此构造方法来创建步行图层
	 * @param context
	 * @param aMap
	 * @param walkPath
	 * @param start
	 * @param end
	 */
	public WalkRouteOverlay(Context context, AMap aMap, WalkPath walkPath, LatLonPoint start,LatLonPoint end){
		super(context);
		this.mAMap = aMap;
		this.walkPath = walkPath;
		startPoint = AMapServicesUtil.convertToLatLng(start);
		endPoint = AMapServicesUtil.convertToLatLng(end);
	}

	/**
	 *将路线放到地图上
	 */
	public void addToMap(){
		initPolylineOptions();
		try{
			List<WalkStep> walkPaths = walkPath.getSteps();
			mPolylineOptions.add(startPoint);
			addStartAndEndMarker();			//添加起始点和终点
			for (int i = 0 ;i < walkPaths.size();i++){
				WalkStep walkStep = walkPaths.get(i);
				LatLng latLng = AMapServicesUtil.convertToLatLng(walkStep.getPolyline().get(0));
				addWalkStationMarkers(walkStep,latLng);
				addWalkPolyLines(walkStep);
			}
			showPolyline();
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * 初始化线段属性
	 */
	private void initPolylineOptions(){
		if (walkStationDescriptor == null){
			walkStationDescriptor = getWalkBitmapDescriptor();//获取步行图标
		}
		mPolylineOptions = null;
		mPolylineOptions = new PolylineOptions();
		mPolylineOptions.color(getWalkColor()).width(getRouteWidth());

	}

	private void addWalkStationMarkers(WalkStep walkStep,LatLng position){
		addStationMarker(new MarkerOptions()
		.position(position)
		.title("\u65B9\u5411:" + walkStep.getAction()
		+ "\n\u9053\u8DEF:" + walkStep.getRoad())
		.snippet(walkStep.getInstruction()).visible(nodeIconVisible)
		.anchor(0.5f,0.5f).icon(walkStationDescriptor));
	}

	private void addWalkPolyLines(WalkStep walkStep){
		mPolylineOptions.addAll(AMapServicesUtil.convertArrList(walkStep.getPolyline()));
	}

	private void showPolyline(){
		addPolyLine(mPolylineOptions);
	}

	private void checkDistanceToNextStep(WalkStep walkStep,WalkStep walkStep1){
		LatLonPoint lastPoint = getLastWalkPoint(walkStep);
		LatLonPoint nextFirstPoint = getLastWalkPoint(walkStep1);
		if(!(lastPoint.equals(nextFirstPoint))){
			addWalkPolyLine(lastPoint,nextFirstPoint);
		}
	}

	private LatLonPoint getLastWalkPoint(WalkStep walkStep){
		return walkStep.getPolyline().get(walkStep.getPolyline().size() - 1);
	}

	private void addWalkPolyLine(LatLonPoint pointFrom,LatLonPoint pointTo){
		addWalkPolyLine(AMapServicesUtil.convertToLatLng(pointFrom),AMapServicesUtil.convertToLatLng(pointTo));
	}
	private void addWalkPolyLine(LatLng pointFrom,LatLng poinTo){
		mPolylineOptions.add(pointFrom,poinTo);
	}
}
